package modelos;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name="tarifa")
@Getter @Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class Tarifa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tarifa")
    private Long idTarifa;

    @Column(name = "precio_min")
    private BigDecimal precioMin;

    @Column(name = "min_cortesía")
    private Integer minCortesia;

    @Column(name = "horas_maximas")
    private Integer horasMaximas;

    @Column(name = "fecha_aplicacion")
    private LocalDate fechaAplicacion;
}
