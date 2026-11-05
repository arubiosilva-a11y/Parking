package modelos;

import es.safareyes.parking.enums.Estado;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name="plaza")
@Getter @Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class Plaza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_plaza")
    private Long idPlaza;

    @Column(name = "num")
    private Integer numero;

    @Column(name = "planta")
    private Integer planta;

    @Column(name = "tipo")
    private String tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private Estado estado;



}
