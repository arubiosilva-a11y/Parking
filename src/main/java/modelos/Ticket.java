package modelos;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name="ticket")
@Getter @Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ticket")
    private Long idTicket;

    @Column(name = "matricula")
    private String matricula;

    @Column(name = "hora_entrada")
    private LocalDateTime horaEntrada;

    @Column(name = "hora_salida")
    private LocalDateTime horaSalida;

    @Column(name = "minutos")
    private Integer minutos;

    @Column(name = "descuento")
    private Integer descuento;

    @Column(name = "importe")
    private BigDecimal importe;

    @Column(name = "importe_descuento")
    private BigDecimal importeDescuento;

    @Column(name = "estado")
    private String estado;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plaza")
    private Plaza plaza;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "id_abonado")
    private Abonado abonado;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tarifa")
    private Tarifa tarifa;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "id_promocion")
    private Promocion promocion;


}
