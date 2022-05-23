package wooteco.subway.acceptance;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static wooteco.subway.acceptance.RestUtil.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;

import com.ori.acceptancetest.SpringBootAcceptanceTest;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import wooteco.subway.controller.dto.LineRequest;
import wooteco.subway.controller.dto.SectionRequest;
import wooteco.subway.controller.dto.StationRequest;
import wooteco.subway.domain.Station;
import wooteco.subway.service.dto.PathResponse;
import wooteco.subway.service.dto.StationResponse;

@DisplayName("경로 조회 인수 테스트")
@SpringBootAcceptanceTest
class PathAcceptanceTest {

    private final Map<String, Station> stations = new HashMap<>();

    //           (2호선)
    //            교대역
    //              11
    // 강남역 >11> 역삼역 >10> 선릉역 (신분당선)
    //              10
    //            잠실역
    @BeforeEach
    void init() {
        stations.putAll(postStations("강남역", "역삼역", "선릉역", "교대역", "잠실역"));
        LineRequest lineRequest1 = new LineRequest(
            "신분당선", "bg-red-600", stations.get("강남역").getId(), stations.get("역삼역").getId(), 11, 1000);
        ExtractableResponse<Response> lineResponse = RestUtil.post(lineRequest1);
        Long lineId = RestUtil.getIdFromLine(lineResponse);
        RestUtil.post(lineId, new SectionRequest(stations.get("역삼역").getId(), stations.get("선릉역").getId(), 10));

        LineRequest lineRequest2 = new LineRequest(
            "2호선", "bg-red-600", stations.get("교대역").getId(), stations.get("역삼역").getId(), 11, 1000);
        ExtractableResponse<Response> lineResponse2 = RestUtil.post(lineRequest2);
        Long lineId2 = RestUtil.getIdFromLine(lineResponse2);
        RestUtil.post(lineId2, new SectionRequest(stations.get("역삼역").getId(), stations.get("잠실역").getId(), 10));
    }

    private Map<String, Station> postStations(String... names) {
        Map<String, Station> stations = new HashMap<>();
        List<Long> ids = RestUtil.postStations(names);
        int index = 0;
        for (String name : names) {
            stations.put(name, new Station(ids.get(index), name));
            index++;
        }
        return stations;
    }

    @DisplayName("경로를 조회한다(신분당선 -> 2호선, 추가 요금 1000) .")
    @Test
    void findPath() {
        //given
        Long source = stations.get("강남역").getId();
        Long target = stations.get("잠실역").getId();

        // when
        ExtractableResponse<Response> response = get("/paths?source=" + source + "&target=" + target + "&age=19");

        // then
        PathResponse pathResponse = RestUtil.toResponseDto(response, PathResponse.class);
        assertAll(
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
            () -> assertThat(pathResponse.getStations())
                .map(StationResponse::getName)
                .containsExactly("강남역", "역삼역", "잠실역"),
            () -> assertThat(pathResponse.getDistance()).isEqualTo(21),
            () -> assertThat(pathResponse.getFare()).isEqualTo(2550)
        );
    }

    @DisplayName("연령별 할인 요금이 적용된 경로를 조회한다(신분당선 -> 2호선, 추가 요금 1000).")
    @ParameterizedTest
    @CsvSource(value = {"5:0", "6:1100", "12:1100", "13:1760", "18:1760", "19:2550"}, delimiter = ':')
    void findPathWithAgeDiscount(int age, int expected) {
        //given
        Long source = stations.get("강남역").getId();
        Long target = stations.get("잠실역").getId();

        // when
        ExtractableResponse<Response> response = get("/paths?source=" + source + "&target=" + target + "&age=" + age);

        // then
        PathResponse pathResponse = RestUtil.toResponseDto(response, PathResponse.class);
        assertAll(
            () -> assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value()),
            () -> assertThat(pathResponse.getStations())
                .map(StationResponse::getName)
                .containsExactly("강남역", "역삼역", "잠실역"),
            () -> assertThat(pathResponse.getDistance()).isEqualTo(21),
            () -> assertThat(pathResponse.getFare()).isEqualTo(expected)
        );
    }

    @DisplayName("구간에 없는 역으로 경로를 찾으면 400 에러가 발생한다.")
    @Test
    void pathBadRequest() {
        // given
        ExtractableResponse<Response> stationResponse = RestUtil.post(new StationRequest("잠실새내역"));

        // when
        Long stationId = RestUtil.getIdFromStation(stationResponse);
        ExtractableResponse<Response> response = get(
            "/paths?source=" + stations.get("강남역").getId() + "&target=" + stationId + "&age=15");

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
}
