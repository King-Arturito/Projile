package io.github.kingarturito.projile.task.internal.repositories;

import io.github.kingarturito.projile.board.BoardColumnId;
import io.github.kingarturito.projile.board.BoardId;
import io.github.kingarturito.projile.task.TaskId;
import io.github.kingarturito.projile.task.internal.entities.Task;
import io.github.kingarturito.projile.tenant.TenantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, TaskId> {

    Optional<Task> findByTaskIdAndTenantId(TaskId taskId, TenantId tenantId);

    List<Task> findAllByColumnIdOrderByPositionAsc(BoardColumnId columnId);

    List<Task> findAllByBoardIdAndTenantId(BoardId boardId, TenantId tenantId);
}
