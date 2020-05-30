package wooteco.subway.admin.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import wooteco.subway.admin.dto.response.ApiError;
import wooteco.subway.admin.exception.DuplicateNameException;
import wooteco.subway.admin.exception.EntityNotFoundException;
import wooteco.subway.admin.exception.LineStationException;

@RestControllerAdvice
public class ControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ControllerAdvice.class);

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError notFoundException(EntityNotFoundException e) {
        logger.error("error : {}, message : {}", e, e.getMessage());
        return new ApiError(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler({DuplicateNameException.class, LineStationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError duplicateNameException(IllegalArgumentException e) {
        logger.error("error : {}, message : {}", e, e.getMessage());
        return new ApiError(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError exception(Exception e) {
        logger.error("error : {}, message : {}", e, e.getMessage());
        return new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, "예기치 못한 에러가 발생했습니다.");
    }
}