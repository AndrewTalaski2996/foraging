package learn.foraging.data;

import learn.foraging.models.Forager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ForagerFileRepositoryTest {

    static final String SEED_PATH = "./data/foragers-seed.csv";
    static final String TEST_PATH = "./data/foragers-test.csv";

    ForagerFileRepository repository = new ForagerFileRepository(TEST_PATH);

    @BeforeEach
    void setUp() throws IOException {
        Path seedPath = Paths.get(SEED_PATH);
        Path testPath = Paths.get(TEST_PATH);
        Files.copy(seedPath, testPath, StandardCopyOption.REPLACE_EXISTING);
    }

    @Test
    void shouldFindAll() {
        List<Forager> all = repository.findAll();
        assertEquals(57, all.size());
    }

    @Test
    void shouldAdd() throws DataException {
        Forager forager = new Forager();
        forager.setFirstName("Andrew");
        forager.setLastName("Talaski");
        forager.setState("NC");

        Forager actual = repository.addForager(forager);
        assertEquals("Andrew", actual.getFirstName());
        assertEquals("Talaski", actual.getLastName());
        assertEquals("NC", actual.getState());
        assertNotNull(actual.getId());
    }
}