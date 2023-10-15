package ru.practicum.mainservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class ExceptionApiHandler {
    private static final String CONDITIONS_NOT_MET = "Для запрошенной операции условия не выполнены.";

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFoundException(NotFoundException e) {
        log.warn(e.getMessage());
        ApiError error = new ApiError();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.NOT_FOUND);
        error.setMessage(e.getMessage());
        error.setReason("Запрашиваемый объект не найден");
        return error;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleEventDateNotValidException(EventDateNotValidException e) {
        log.warn(e.getMessage());
        ApiError error = new ApiError();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.BAD_REQUEST);
        error.setMessage(e.getMessage());
        error.setReason(CONDITIONS_NOT_MET);
        return error;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConflictException(ConflictException e) {
        log.warn(e.getMessage());
        ApiError error = new ApiError();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.CONFLICT);
        error.setMessage(e.getMessage());
        error.setReason(CONDITIONS_NOT_MET);
        return error;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleWrongStateException(WrongStateException e) {
        log.warn(e.getMessage());
        ApiError error = new ApiError();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.CONFLICT);
        error.setMessage(e.getMessage());
        error.setReason(CONDITIONS_NOT_MET);
        return error;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.warn(e.getMessage());
        ApiError error = new ApiError();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.CONFLICT);
        error.setMessage(e.getMessage());
        error.setReason(CONDITIONS_NOT_MET);
        return error;
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBadRequestException(BadRequestException e) {
        log.warn(e.getMessage());
        ApiError error = new ApiError();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.BAD_REQUEST);
        error.setMessage(e.getMessage());
        error.setReason("Не корректный запрос.");
        return error;
    }
}
