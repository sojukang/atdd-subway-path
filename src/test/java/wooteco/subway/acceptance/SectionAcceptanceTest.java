package wooteco.subway.acceptance;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static wooteco.subway.acceptance.RestUtil.*;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.ori.acceptancetest.SpringBootAcceptanceTest;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import wooteco.subway.controller.dto.LineRequest;
import wooteco.subway.controller.dto.LineResponse;
import wooteco.subway.controller.dto.SectionRequest;
import wooteco.subway.controller.dto.StationRequest;
import wooteco.subway.service.dto.StationResponse;

@DisplayName("지하철 구간 관련 인수 테스트")
@SpringBootAcceptanceTest
class SectionAcceptanceTest {

    private Long upStationId;
    private Long downStationId;
    private Long lineId;
    private final String DEFAULT_LINE_URL = "/lines/";

    @BeforeEach
    void init() {
        ExtractableResponse<Response> stationResponse1 = RestUtil.post(new StationRequest("강남역"));
        ExtractableResponse<Response> stationResponse2 = RestUtil.post(new StationRequest("역삼역"));
        LineRequest lineRequest = new LineRequest(
            "신분당선", "bg-red-600",
            RestUtil.getIdFromStation(stationResponse1),
            RestUtil.getIdFromStation(stationResponse2),
            10, 1000);
        ExtractableResponse<Response> lineResponse = RestUtil.post(lineRequest);

        upStationId = RestUtil.getIdFromStation(stationResponse1);
        downStationId = RestUtil.getIdFromStation(stationResponse2);
        lineId = RestUtil.getIdFromLine(lineResponse);
    }

    @DisplayName("하행 종점 이후에 지하철 구간을 등록한다.")
    @Test
    void addSectionDown() {
        // given
        ExtractableResponse<Response> stationResponse = RestUtil.post(new StationRequest("선릉역"));
        Long newStationId = RestUtil.getIdFromStation(stationResponse);
        SectionRequest sectionRequest = new SectionRequest(downStationId, newStationId, 10);

        // when
        ExtractableResponse<Response> response = post(DEFAULT_LINE_URL + lineId + "/sections", sectionRequest);

        // then
        List<String> stationNames = extractStationNames();

        assertAll(
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
            () -> assertThat(stationNames).containsExactly("강남역", "역삼역", "선릉역")
        );
    }

    @DisplayName("상행 종점 이전에 지하철 구간을 등록한다.")
    @Test
    void addSectionUp() {
        // given
        ExtractableResponse<Response> stationResponse = RestUtil.post(new StationRequest("선릉역"));
        Long newStationId = RestUtil.getIdFromStation(stationResponse);
        SectionRequest sectionRequest = new SectionRequest(newStationId, upStationId, 10);

        // when
        ExtractableResponse<Response> response = post(DEFAULT_LINE_URL + lineId + "/sections", sectionRequest);

        // then
        List<String> stationNames = extractStationNames();

        assertAll(
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
            () -> assertThat(stationNames).containsExactly("선릉역", "강남역", "역삼역")
        );
    }

    @DisplayName("지하철 구간 사이에  지하철 구간을 등록한다.")
    @Test
    void addSectionMiddle() {
        // given
        ExtractableResponse<Response> stationResponse1 = RestUtil.post(new StationRequest("선릉역"));
        ExtractableResponse<Response> stationResponse2 = RestUtil.post(new StationRequest("선정릉역"));
        Long newStationId1 = RestUtil.getIdFromStation(stationResponse1);
        Long newStationId2 = RestUtil.getIdFromStation(stationResponse2);
        SectionRequest sectionRequest1 = new SectionRequest(upStationId, newStationId1, 3);
        SectionRequest sectionRequest2 = new SectionRequest(newStationId2, downStationId, 3);

        // when
        ExtractableResponse<Response> response1 = post(DEFAULT_LINE_URL + lineId + "/sections", sectionRequest1);
        ExtractableResponse<Response> response2 = post(DEFAULT_LINE_URL + lineId + "/sections", sectionRequest2);

        // then
        List<String> stationNames = extractStationNames();

        assertAll(
            () -> assertThat(response1.statusCode()).isEqualTo(HttpStatus.OK.value()),
            () -> assertThat(response2.statusCode()).isEqualTo(HttpStatus.OK.value()),
            () -> assertThat(stationNames).containsExactly("강남역", "선릉역", "선정릉역", "역삼역")
        );
    }

    private List<String> extractStationNames() {
        return RestUtil.toResponseDto(
                RestUtil.get(DEFAULT_LINE_URL + lineId), LineResponse.class
            ).getStations()
            .stream()
            .map(StationResponse::getName)
            .collect(Collectors.toList());
    }

    @DisplayName("추가할 구간의 거리가 기존 구간보다 같거나 크면 400 응답을 받는다.")
    @Test
    void addSectionBadRequest() {
        // given
        ExtractableResponse<Response> stationResponse1 = RestUtil.post(new StationRequest("선릉역"));
        ExtractableResponse<Response> stationResponse2 = RestUtil.post(new StationRequest("선정릉역"));
        Long newStationId1 = RestUtil.getIdFromStation(stationResponse1);
        Long newStationId2 = RestUtil.getIdFromStation(stationResponse2);
        SectionRequest sectionRequest = new SectionRequest(newStationId1, newStationId2, 10);

        // when
        ExtractableResponse<Response> response = post(DEFAULT_LINE_URL + lineId + "/sections", sectionRequest);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("추가할 구간의 상행역, 하행역이 기존 노선에 없으면 400 응답을 받는다.")
    @Test
    void addSectionExceptionByCannotConnect() {
        // given
        ExtractableResponse<Response> stationResponse = RestUtil.post(new StationRequest("선릉역"));
        Long newStationId = RestUtil.getIdFromStation(stationResponse);
        SectionRequest sectionRequest = new SectionRequest(upStationId, newStationId, 10);

        // when
        ExtractableResponse<Response> response = post(DEFAULT_LINE_URL + lineId + "/sections", sectionRequest);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
}
