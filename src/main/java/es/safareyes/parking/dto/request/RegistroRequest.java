package es.safareyes.parking.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 12, max = 72) String password,
        @NotBlank @Size(max = 72) String confirmPassword,
        @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{43}") String codigo
) {
    @Override
    public String toString() {
        return "RegistroRequest";
    }
}
