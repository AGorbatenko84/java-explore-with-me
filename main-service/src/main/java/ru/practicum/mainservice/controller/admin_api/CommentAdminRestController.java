package ru.practicum.mainservice.controller.admin_api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.mainservice.dto.comment.CommentDto;
import ru.practicum.mainservice.dto.comment.UpdateCommentDto;
import ru.practicum.mainservice.service.CommentService;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/admin/comments")
public class CommentAdminRestController {

    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long commentId) {
        commentService.deleteById(commentId);
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.OK)
    public CommentDto patchByAdmin(@Valid @RequestBody UpdateCommentDto dto) {
        return commentService.patchByAdmin(dto);
    }
}