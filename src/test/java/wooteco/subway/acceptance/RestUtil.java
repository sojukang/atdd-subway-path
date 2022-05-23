package wooteco.subway.acceptance;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.MediaType;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import wooteco.subway.controller.dto.LineRequest;
import wooteco.subway.controller.dto.LineResponse;
import wooteco.subway.controller.dto.SectionRequest;
import wooteco.subway.controller.dto.StationRequest;
import wooteco.subway.service.dto.StationResponse;

public class RestUtil {

    static List<Long> postStations(String... names) {
        List<Long> ids = new ArrayList<>();
        for (String name : names) {
            ids.add(getIdFromStation(post(new StationRequest(name))));
        }
        return ids;
    }

    public static ExtractableResponse<Response> get(String url) {
        return RestAssured.given()
            .when()
            .get(url)
            .then()
            .extract();
    }

    public static ExtractableResponse<Response> post(StationRequest stationRequest) {
        return RestAssured.given()
            .body(stationRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/stations")
            .then()
            .extract();
    }

    public static ExtractableResponse<Response> post(String url, Object request) {
        return RestAssured.given()
            .body(request)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post(url)
            .then()
            .extract();
    }

    static ExtractableResponse<Response> post(LineRequest lineRequest) {
        return RestAssured.given()
            .body(lineRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then()
            .extract();
    }

    static ExtractableResponse<Response> post(Long lineId, SectionRequest sectionRequest) {
        return RestAssured.given()
            .body(sectionRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines/" + lineId + "/sections")
            .then()
            .extract();
    }

    public static ExtractableResponse<Response> put(String url, Object request) {
        return RestAssured.given()
            .body(request)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .put(url)
            .then()
            .extract();
    }

    public static ExtractableResponse<Response> delete(String url) {
        return RestAssured.given()
            .when()
            .delete(url)
            .then()
            .extract();
    }

    public static Long getIdFromStation(ExtractableResponse<Response> response) {
        return response.jsonPath()
            .getObject(".", StationResponse.class)
            .getId();
    }

    static Long getIdFromLine(ExtractableResponse<Response> response) {
        return response.jsonPath()
            .getObject(".", LineResponse.class)
            .getId();
    }

    static List<Long> getIdsFromStation(ExtractableResponse<Response> response) {
        return response.jsonPath().getList(".", StationResponse.class).stream()
            .map(StationResponse::getId)
            .collect(Collectors.toList());
    }

    static List<Long> getIdsFromLine(ExtractableResponse<Response> response) {
        return response.jsonPath().getList(".", LineResponse.class).stream()
            .map(LineResponse::getId)
            .collect(Collectors.toList());
    }

    static <T> T toResponseDto(ExtractableResponse<Response> response, Class<T> responseClass) {
        return response.body()
            .jsonPath()
            .getObject(".", responseClass);
    }
}
