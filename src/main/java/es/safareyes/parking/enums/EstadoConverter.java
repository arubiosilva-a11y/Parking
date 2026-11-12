package es.safareyes.parking.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EstadoConverter implements AttributeConverter<Estado, String> {

    @Override
    public String convertToDatabaseColumn(Estado estado) {
        return estado == null ? null : switch (estado) {
            case LIBRE -> "Libre";
            case OCUPADO -> "Ocupada";
            case BLOQUEADA -> "Mantenimiento";
        };
    }

    @Override
    public Estado convertToEntityAttribute(String estado) {
        if (estado == null) {
            return null;
        }
        return switch (estado) {
            case "Libre" -> Estado.LIBRE;
            case "Ocupada" -> Estado.OCUPADO;
            case "Mantenimiento" -> Estado.BLOQUEADA;
            default -> throw new IllegalArgumentException("Estado de plaza desconocido: " + estado);
        };
    }
}
