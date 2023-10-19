package ru.practicum.mainservice.service;

import ru.practicum.mainservice.dto.comment.CommentDto;
import ru.practicum.mainservice.dto.comment.NewCommentDto;
import ru.practicum.mainservice.dto.comment.UpdateCommentDto;

import java.util.List;

public interface CommentService {

    CommentDto create(NewCommentDto dto, Long userId, Long eventId);

    CommentDto patch(UpdateCommentDto dto, Long userId);

    List<CommentDto> getOwn(Long userId);

    List<CommentDto> getEventComments(Long eventId, Integer from, Integer size);

    void deleteById(Long commentId);

    List<CommentDto> search(String text);

    CommentDto patchByAdmin(UpdateCommentDto dto);
}
