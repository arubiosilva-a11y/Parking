package es.safareyes.parking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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
    @Column(name = "id_tarifa")
    private Integer idTarifa;

    @Column(name = "precio_min")
    private BigDecimal precioMin;

    @Column(name = "min_cortesia")
    private Integer minCortesia;

    @Column(name = "horas_maximas")
    private Integer horasMaximas;

    @Column(name = "fecha_aplicacion")
    private LocalDateTime fechaAplicacion;
}
