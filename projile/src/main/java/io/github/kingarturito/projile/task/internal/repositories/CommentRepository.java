package io.github.kingarturito.projile.task.internal.repositories;

import io.github.kingarturito.projile.task.CommentId;
import io.github.kingarturito.projile.task.TaskId;
import io.github.kingarturito.projile.task.internal.entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, CommentId> {

    List<Comment> findAllByTaskIdOrderByCreatedAtAsc(TaskId taskId);
}
