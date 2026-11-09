package DTOS.request;

import es.safareyes.parking.enums.Estado;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PlazaRequest(

        @NotNull
        @Positive
        Integer numero,

        @NotNull
        Integer planta,

        @NotBlank
        String tipo,

        @NotNull
        Estado estado

) {}