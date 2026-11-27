package es.safareyes.parking.entity;

import es.safareyes.parking.enums.Rol;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name="usuario")
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString


public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "email", nullable = false)
    private String email;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Column(name = "contraseña", nullable = false)
    private String contraseña;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol", nullable = false)
    private Rol rol;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_abonado", unique = true)
    private Abonado abonado;
}
