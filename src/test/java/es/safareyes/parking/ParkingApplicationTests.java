package es.safareyes.parking;

import modelos.Plaza;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import repositorios.IPlazaRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
class ParkingApplicationTests {

    @Autowired
    private IPlazaRepository plazaRepository;

    @Test
    void testFindAllByPlanta() {

        List<Plaza> plazas = plazaRepository.findAllByPlanta(1);

        for (Plaza plaza : plazas) {
            System.out.println(plaza);
        }
    }

}
