package io.github.kingarturito.projile.attachment.internal.repositories;

import io.github.kingarturito.projile.attachment.AttachmentId;
import io.github.kingarturito.projile.attachment.internal.entities.Attachment;
import io.github.kingarturito.projile.task.TaskId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, AttachmentId> {

    List<Attachment> findAllByTaskIdOrderByCreatedAtDesc(TaskId taskId);
}
