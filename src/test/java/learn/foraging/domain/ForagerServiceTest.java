package learn.foraging.domain;

import learn.foraging.data.DataException;
import learn.foraging.data.ForagerRepositoryDouble;
import learn.foraging.models.Forager;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ForagerServiceTest {

    ForagerService service = new ForagerService(new ForagerRepositoryDouble());

    @Test
    void findByState() {
        List<Forager> foragers = service.findByState("GA");
        assertEquals(1, foragers.size());
    }

    @Test
    void findByLastName() {
        List<Forager> foragers = service.findByLastName("S");
        assertEquals(1, foragers.size());
        assertEquals("Sisse", foragers.get(0).getLastName());
    }

    @Test
    void addForager() throws DataException {
        Forager forager = new Forager();
        forager.setFirstName("Andrew");
        forager.setLastName("Talaski");
        forager.setState("NC");

        Result<Forager> result = service.addForager(forager);

        assertNotNull(result.getPayload());
        assertNotNull(result.getPayload().getId());
        assertEquals("Andrew", result.getPayload().getFirstName());
        assertEquals("Talaski", result.getPayload().getLastName());
        assertEquals("NC", result.getPayload().getState());
    }

    @Test
    void shouldNotAddNullForager() throws DataException {
        Result<Forager> result = service.addForager(null);
        assertFalse(result.isSuccess());
    }

    @Test
    void shouldNotAddNullFirstName() throws DataException {
        Forager forager = new Forager();
        forager.setFirstName(null);
        forager.setLastName("Talaski");
        forager.setState("NC");

        Result<Forager> result = service.addForager(forager);
        assertFalse(result.isSuccess());
    }

    @Test
    void shouldNotAddBlankFirstName() throws DataException {
        Forager forager = new Forager();
        forager.setFirstName("");
        forager.setLastName("Talaski");
        forager.setState("NC");

        Result<Forager> result = service.addForager(forager);
        assertFalse(result.isSuccess());
    }

    @Test
    void shouldNotAddNullLastName() throws DataException {
        Forager forager = new Forager();
        forager.setFirstName("Andrew");
        forager.setLastName(null);
        forager.setState("NC");

        Result<Forager> result = service.addForager(forager);
        assertFalse(result.isSuccess());
    }

    @Test
    void shouldNotAddBlankLastName() throws DataException {
        Forager forager = new Forager();
        forager.setFirstName("Andrew");
        forager.setLastName("");
        forager.setState("NC");

        Result<Forager> result = service.addForager(forager);
        assertFalse(result.isSuccess());
    }

    @Test
    void shouldNotAddNullStateName() throws DataException {
        Forager forager = new Forager();
        forager.setFirstName("Andrew");
        forager.setLastName("Talaski");
        forager.setState(null);

        Result<Forager> result = service.addForager(forager);
        assertFalse(result.isSuccess());
    }

    @Test
    void shouldNotAddBlankStateName() throws DataException {
        Forager forager = new Forager();
        forager.setFirstName("Andrew");
        forager.setLastName("Talaski");
        forager.setState("");

        Result<Forager> result = service.addForager(forager);
        assertFalse(result.isSuccess());
    }

    @Test
    void shouldNotAddDuplicate() throws DataException {
        Forager forager = new Forager();
        forager.setFirstName("Jilly");
        forager.setLastName("Sisse");
        forager.setState("GA");

        Result<Forager> result = service.addForager(forager);
        assertFalse(result.isSuccess());
    }
}