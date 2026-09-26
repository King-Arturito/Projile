package io.github.kingarturito.projile.board.internal.repositories;

import io.github.kingarturito.projile.board.BoardColumnId;
import io.github.kingarturito.projile.board.BoardId;
import io.github.kingarturito.projile.board.internal.entities.BoardColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BoardColumnRepository extends JpaRepository<BoardColumn, BoardColumnId> {

    List<BoardColumn> findAllByBoardIdOrderByPositionAsc(BoardId boardId);

    Optional<BoardColumn> findByColumnIdAndBoardId(BoardColumnId columnId, BoardId boardId);
}
