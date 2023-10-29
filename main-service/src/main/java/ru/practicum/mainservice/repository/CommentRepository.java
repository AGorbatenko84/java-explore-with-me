package ru.practicum.mainservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.mainservice.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(value = "comment-entity-graph")
    List<Comment> findAllByAuthorId(Long userId);

    @EntityGraph(value = "comment-entity-graph")
    Page<Comment> findAllByEventId(Long eventId, Pageable pageable);

    @EntityGraph(value = "comment-entity-graph")
    @Query("select c from Comment c " +
            "where upper(c.text) like upper(concat('%', ?1, '%'))")
    List<Comment> search(String text);
}
