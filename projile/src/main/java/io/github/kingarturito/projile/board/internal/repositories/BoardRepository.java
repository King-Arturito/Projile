package io.github.kingarturito.projile.board.internal.repositories;

import io.github.kingarturito.projile.board.BoardId;
import io.github.kingarturito.projile.board.internal.entities.Board;
import io.github.kingarturito.projile.project.ProjectId;
import io.github.kingarturito.projile.tenant.TenantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, BoardId> {

    Optional<Board> findByProjectIdAndTenantId(ProjectId projectId, TenantId tenantId);

    List<Board> findAllByProjectIdAndTenantId(ProjectId projectId, TenantId tenantId);
}
