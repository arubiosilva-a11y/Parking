package es.safareyes.parking.repository;

import es.safareyes.parking.entity.*;
import es.safareyes.parking.enums.Rol;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:parking_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE;CASE_INSENSITIVE_IDENTIFIERS=TRUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.default_schema=public"
})
class ParkingRepositoryTest {

    @Autowired
    private IAbonadoRepository abonadoRepository;

    @Autowired
    private IPlazaRepository plazaRepository;

    @Autowired
    private IPromocionRepository promocionRepository;

    @Autowired
    private ITarifaRepository tarifaRepository;

    @Autowired
    private ITicketRepository ticketRepository;

    @Autowired
    private IUsuarioRepository usuarioRepository;

    @Test
    void abonadoRepository_shouldFindByMatriculaAndActiveDateRange() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 16, 12, 0);
        Abonado abonado = new Abonado(null, "Ale", "ale@test.com", "1234ABC", now.plusDays(1), now.minusDays(1));

        abonadoRepository.save(abonado);

        Abonado encontrado = abonadoRepository.findFirstByMatriculaAndFechaIniLessThanEqualAndFechaFinGreaterThanEqual(
                "1234ABC", now, now
        );

        assertThat(encontrado).isNotNull();
        assertThat(encontrado.getEmail()).isEqualTo("ale@test.com");
    }

    @Test
    void tarifaRepository_shouldReturnNewestTarifaByDate() {
        LocalDateTime fechaBase = LocalDateTime.of(2026, 10, 1, 8, 0);
        Tarifa antigua = new Tarifa(null, new BigDecimal("2.00"), 5, 8, fechaBase.minusDays(10));
        Tarifa actual = new Tarifa(null, new BigDecimal("2.50"), 10, 12, fechaBase);
        Tarifa futura = new Tarifa(null, new BigDecimal("3.00"), 15, 24, fechaBase.plusDays(10));

        tarifaRepository.saveAll(List.of(antigua, actual, futura));

        Tarifa tarifa = tarifaRepository.findFirstByFechaAplicacionLessThanEqualOrderByFechaAplicacionDesc(fechaBase.plusDays(20));

        assertThat(tarifa).isNotNull();
        assertThat(tarifa.getPrecioMin()).isEqualByComparingTo(new BigDecimal("3.00"));
    }

    @Test
    void plazaRepository_shouldFindByPlantaAndTipo() {
        Plaza coche1 = new Plaza(null, 1, 1, "Coche", null);
        Plaza coche2 = new Plaza(null, 2, 1, "Coche", null);
        Plaza moto = new Plaza(null, 3, 1, "Moto", null);

        plazaRepository.saveAll(List.of(coche1, coche2, moto));

        List<Plaza> porPlanta = plazaRepository.findAllByPlanta(1);
        List<Plaza> porTipo = plazaRepository.findAllByTipo("Coche");

        assertThat(porPlanta).hasSize(3);
        assertThat(porTipo).hasSize(2);
        assertThat(porTipo).extracting(Plaza::getNumero).containsExactlyInAnyOrder(1, 2);
    }

    @Test
    void promocionRepository_shouldSaveAndFindById() {
        Promocion promocion = new Promocion(null, "Verano", "Porcentaje", new BigDecimal("10.00"));

        Promocion guardada = promocionRepository.save(promocion);

        assertThat(promocionRepository.findById(guardada.getIdPromocion())).isPresent();
    }

    @Test
    void ticketRepository_shouldFindTicketByMatriculaAndLoadRelations() {
        Abonado abonado = abonadoRepository.save(new Abonado(null, "Luis", "luis@test.com", "9999ZZZ", LocalDateTime.now().plusDays(10), LocalDateTime.now().minusDays(10)));
        Plaza plaza = plazaRepository.save(new Plaza(null, 10, 2, "Coche", null));
        Tarifa tarifa = tarifaRepository.save(new Tarifa(null, new BigDecimal("1.80"), 15, 24, LocalDateTime.now().minusDays(1)));
        Promocion promocion = promocionRepository.save(new Promocion(null, "Navidad", "Descuento", new BigDecimal("5.00")));

        Ticket ticket = new Ticket(
                null,
                "9999ZZZ",
                LocalDateTime.of(2026, 11, 15, 9, 0),
                LocalDateTime.of(2026, 11, 15, 11, 30),
                150,
                new BigDecimal("10.00"),
                new BigDecimal("20.00"),
                new BigDecimal("18.00"),
                "Pagado",
                plaza,
                abonado,
                tarifa,
                promocion
        );

        ticketRepository.save(ticket);

        Ticket buscado = ticketRepository.findFirstByMatriculaAndEstadoIgnoreCase("9999ZZZ", "PAGADO");
        Ticket conRelaciones = ticketRepository.buscarPorIdConDescuentos(ticket.getIdTicket());

        assertThat(buscado).isNotNull();
        assertThat(buscado.getImporte()).isEqualByComparingTo(new BigDecimal("20.00"));
        assertThat(conRelaciones.getPlaza()).isNotNull();
        assertThat(conRelaciones.getAbonado()).isNotNull();
        assertThat(conRelaciones.getTarifa()).isNotNull();
        assertThat(conRelaciones.getPromocion()).isNotNull();
    }

    @Test
    void usuarioRepository_shouldPersistUsuarioWithAbonadoRelation() {
        Abonado abonado = abonadoRepository.save(new Abonado(null, "Marta", "marta@test.com", "5555BBB", LocalDateTime.now().plusDays(5), LocalDateTime.now().minusDays(5)));
        Usuario usuario = new Usuario(null, "Marta", "marta@test.com", "hash-de-ejemplo", Rol.ABONADO, abonado);

        Usuario guardado = usuarioRepository.save(usuario);

        assertThat(usuarioRepository.findById(guardado.getIdUsuario())).isPresent();
        assertThat(guardado.getAbonado()).isNotNull();
        assertThat(guardado.getAbonado().getMatricula()).isEqualTo("5555BBB");
    }
}
