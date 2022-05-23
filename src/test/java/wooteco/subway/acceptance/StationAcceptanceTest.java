package wooteco.subway.acceptance;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static wooteco.subway.acceptance.RestUtil.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.ori.acceptancetest.SpringBootAcceptanceTest;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import wooteco.subway.controller.dto.LineRequest;
import wooteco.subway.controller.dto.StationRequest;
import wooteco.subway.service.dto.StationResponse;

@DisplayName("지하철역 관련 기능 인수 테스트")
@SpringBootAcceptanceTest
class StationAcceptanceTest {

    private final StationRequest stationRequest = new StationRequest("강남역");
    private final String DEFAULT_STATION_URL = "/stations";

    @DisplayName("지하철역을 생성한다.")
    @Test
    void createStation() {
        // when
        ExtractableResponse<Response> response = post(DEFAULT_STATION_URL, stationRequest);

        // then
        StationResponse stationResponse = RestUtil.toResponseDto(response, StationResponse.class);

        assertAll(
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value()),
            () -> assertThat(response.header("Location")).isNotBlank(),
            () -> assertThat(RestUtil.getIdFromStation(response)).isNotNull(),
            () -> assertThat(stationResponse.getName()).isEqualTo("강남역")
        );
    }

    @DisplayName("기존에 존재하는 지하철역 이름으로 지하철역을 생성하면 400 응답을 받는다.")
    @Test
    void createStationWithDuplicateName() {
        // given
        RestUtil.post(stationRequest);

        // when
        ExtractableResponse<Response> response = post(DEFAULT_STATION_URL, stationRequest);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("지하철역을 조회한다.")
    @Test
    void getStations() {
        /// given
        ExtractableResponse<Response> createResponse1 = RestUtil.post(stationRequest);
        ExtractableResponse<Response> createResponse2 = RestUtil.post(new StationRequest("역삼역"));

        // when
        ExtractableResponse<Response> response = get(DEFAULT_STATION_URL);

        // then
        List<Long> expectedLineIds = extractExpectedIds(createResponse1, createResponse2);
        List<Long> resultLineIds = RestUtil.getIdsFromStation(response);
        assertAll(
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
            () -> assertThat(resultLineIds).containsAll(expectedLineIds)
        );
    }

    private List<Long> extractExpectedIds(ExtractableResponse<Response> createResponse1,
        ExtractableResponse<Response> createResponse2) {
        return Stream.of(createResponse1, createResponse2)
            .map(it -> Long.parseLong(it.header("Location").split("/")[2]))
            .collect(Collectors.toList());
    }

    @DisplayName("지하철역을 제거한다.")
    @Test
    void deleteStation() {
        // given
        ExtractableResponse<Response> createResponse = RestUtil.post(stationRequest);

        // when
        String uri = createResponse.header("Location");
        ExtractableResponse<Response> response = delete(uri);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    @DisplayName("없는 지하철역을 제거하면 404 응답을 받는다.")
    @Test
    void deleteStationBadRequest() {
        // when
        ExtractableResponse<Response> response = delete(DEFAULT_STATION_URL + "/" + 1);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @DisplayName("구간에 등록되어 있는 역을 지우면 400 응답을 받는다.")
    @Test
    void deleteStationBadRequestBySection() {
        // given
        Long upStationId = RestUtil.getIdFromStation(RestUtil.post(new StationRequest("강남역")));
        Long downStationId = RestUtil.getIdFromStation(RestUtil.post(new StationRequest("역삼역")));
        RestUtil.post(new LineRequest("2호선", "red", upStationId, downStationId, 10, 200));

        // when
        ExtractableResponse<Response> response = delete(DEFAULT_STATION_URL + "/" + upStationId);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
}
