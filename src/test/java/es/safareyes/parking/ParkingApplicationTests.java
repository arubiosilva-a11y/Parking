package es.safareyes.parking;

import es.safareyes.parking.service.InformeService;
import es.safareyes.parking.service.PlazaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
class ParkingApplicationTests {

    @Autowired
    private PlazaService plazaService;

    @Autowired
    private InformeService informeService;

    @Test
    void servicesConsultanPlazasYReportes() {
        var plazas = plazaService.listar();
        var disponibilidad = plazaService.consultarDisponibilidad();
        var ocupacion = informeService.consultarOcupacion();

        assertFalse(plazas.isEmpty());
        assertFalse(disponibilidad.isEmpty());
        assertFalse(ocupacion.isEmpty());
    }

}
