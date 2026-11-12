package es.safareyes.parking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name="abonado")
@Getter @Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class Abonado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_abonado")
    private Integer idAbonado;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "email")
    private String email;

    @Column(name = "matricula")
    private String matricula;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "fecha_ini")
    private LocalDateTime fechaIni;

}
