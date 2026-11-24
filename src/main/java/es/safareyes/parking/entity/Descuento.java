package es.safareyes.parking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "descuento", uniqueConstraints = @UniqueConstraint(
        name = "uq_descuento_ticket_promocion", columnNames = {"id_ticket", "id_promocion"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Descuento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_descuento")
    private Integer idDescuento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_ticket", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_promocion", nullable = false)
    private Promocion promocion;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal minutos;

    @Column(name = "aplicado_en", nullable = false)
    private LocalDateTime aplicadoEn;

    public Descuento(Ticket ticket, Promocion promocion, BigDecimal minutos, LocalDateTime aplicadoEn) {
        this.ticket = ticket;
        this.promocion = promocion;
        this.minutos = minutos;
        this.aplicadoEn = aplicadoEn;
    }
}
