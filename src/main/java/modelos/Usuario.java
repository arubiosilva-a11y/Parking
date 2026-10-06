package modelos;

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
    private Short idUsuario;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "email")
    private String email;

    @Column(name = "contraseña")
    private String contraseña;

    @Column(name = "rol")
    private String rol;

    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn(name = "id_abonado")
    private Abonado abonado;
}
