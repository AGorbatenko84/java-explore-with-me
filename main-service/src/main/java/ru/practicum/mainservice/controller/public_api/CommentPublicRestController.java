package ru.practicum.mainservice.controller.public_api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.dto.comment.CommentDto;
import ru.practicum.mainservice.service.CommentService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
public class CommentPublicRestController {

    private final CommentService commentService;

    @GetMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> getEventComments(
            @PathVariable Long eventId,
            @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        return commentService.getEventComments(eventId, from, size);
    }

    @GetMapping("/comments/search")
    @ResponseStatus(HttpStatus.OK)
    public List<CommentDto> search(
            @RequestParam String text) {
        return commentService.search(text);
    }
}