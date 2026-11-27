package es.safareyes.parking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "activacion_abonado")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivacionAbonado {

    @Id
    @Column(name = "id_abonado")
    private Integer idAbonado;

    @Column(name = "codigo_hash", nullable = false, unique = true, length = 64)
    private String codigoHash;

    @Column(name = "expira_en", nullable = false)
    private Instant expiraEn;

    public ActivacionAbonado(Integer idAbonado, String codigoHash, Instant expiraEn) {
        this.idAbonado = idAbonado;
        renovar(codigoHash, expiraEn);
    }

    public void renovar(String codigoHash, Instant expiraEn) {
        this.codigoHash = codigoHash;
        this.expiraEn = expiraEn;
    }
}
