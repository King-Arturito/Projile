package io.github.kingarturito.projile.shared.exceptions;

import io.github.kingarturito.projile.project.exceptions.ProjectNotFoundException;
import io.github.kingarturito.projile.user.internal.exceptions.EmailAlreadyRegisteredException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void reportsErrorCodeAndStatusOfBadRequest() {
        ResponseEntity<ErrorResponse> response =
                handler.handleApiException(new BadRequestException(ErrorCode.INVALID_BOARD_NAME, "Board name is required."));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("INVALID_BOARD_NAME");
        assertThat(response.getBody().message()).isEqualTo("Board name is required.");
        assertThat(response.getBody().status()).isEqualTo(400);
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    void reportsErrorCodeOfSubclass() {
        ResponseEntity<ErrorResponse> response = handler.handleApiException(new ProjectNotFoundException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("PROJECT_NOT_FOUND");
        assertThat(response.getBody().message()).isEqualTo(ErrorCode.PROJECT_NOT_FOUND.defaultMessage());
    }

    @Test
    void reportsConflictForDuplicateEmail() {
        ResponseEntity<ErrorResponse> response = handler.handleApiException(new EmailAlreadyRegisteredException());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().code()).isEqualTo("EMAIL_ALREADY_REGISTERED");
    }

    @Test
    void fallsBackToDefaultMessageWhenNoneIsGiven() {
        BadRequestException exception = new BadRequestException(ErrorCode.INVALID_POSITION);

        assertThat(exception.getMessage()).isEqualTo(ErrorCode.INVALID_POSITION.defaultMessage());
        assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void rejectsCodeThatDoesNotMatchTheExceptionCategory() {
        assertThatThrownBy(() -> new BadRequestException(ErrorCode.PROJECT_NOT_FOUND))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void keepsMessageOfSubclass() {
        assertThat(new ProjectNotFoundException("Project not found").getMessage()).isEqualTo("Project not found");
    }
}
