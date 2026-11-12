package es.safareyes.parking.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record AbonadoRequest(
        @NotBlank
        String nombre,

        @NotBlank
        @Email
        String email,

        @NotBlank
        String matricula,

        @NotNull
        LocalDateTime fechaFin,

        @NotNull
        LocalDateTime fechaIni
) {}