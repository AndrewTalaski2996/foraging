package learn.foraging.data;

import learn.foraging.models.Forager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class ForagerFileRepository implements ForagerRepository {

    private final String filePath;
    private static final String HEADER = "id,first_name,last_name,state";
    private static final String DELIMITER = ",";
    private static final String DELIMITER_REPLACEMENT = "@@@";

    public ForagerFileRepository(@Value("${foragerRepository}") String filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<Forager> findAll() {
        ArrayList<Forager> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

            reader.readLine(); // read header

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                result.add(deserialize(line));
            }
        } catch (IOException ex) {
            // don't throw on read
        }
        return result;
    }

    @Override
    public Forager findById(String id) {
        return findAll().stream()
                .filter(i -> i.getId().equalsIgnoreCase(id))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Forager> findByState(String stateAbbr) {
        return findAll().stream()
                .filter(i -> i.getState().equalsIgnoreCase(stateAbbr))
                .collect(Collectors.toList());
    }

    @Override
    public Forager addForager(Forager forager) throws DataException {
        if (forager == null) {
            return null;
        }

        List<Forager> all = findAll();

        forager.setId(String.valueOf(UUID.randomUUID()));
        all.add(forager);
        writeAll(all);

        return forager;
    }

    private String serialize(Forager forager) {
        StringBuffer buffer = new StringBuffer(100);
        buffer.append(clean(forager.getId())).append(DELIMITER);
        buffer.append(clean(forager.getFirstName())).append(DELIMITER);
        buffer.append(clean(forager.getLastName())).append(DELIMITER);
        buffer.append(clean(forager.getState())).append(DELIMITER);
        return buffer.toString();
    }
    
    private Forager deserialize(String line) {
        String[] fields = line.split(DELIMITER);

        if (fields.length != 4) {
            return null;
        }

        Forager result = new Forager();
        result.setId(restore(fields[0]));
        result.setFirstName(restore(fields[1]));
        result.setLastName(restore(fields[2]));
        result.setState(restore(fields[3]));
        return result;
    }

    private String restore(String value) {
        return value.replace(DELIMITER_REPLACEMENT, DELIMITER);
    }

    private String clean(String value) {
        return value.replace(DELIMITER, DELIMITER_REPLACEMENT);
    }

    private void writeAll(List<Forager> foragers) throws DataException {
        try (PrintWriter writer = new PrintWriter(filePath)) {
            writer.println(HEADER);

            for (Forager forager : foragers) {
                writer.println(serialize(forager));
            }
        } catch (FileNotFoundException ex) {
            throw new DataException(ex);
        }
    }
}
