package io.github.kingarturito.projile.attachment;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public record AttachmentId(UUID attachmentId) {
    public static AttachmentId newId() {
        return new AttachmentId(UuidCreator.getTimeOrderedEpoch());
    }
}
