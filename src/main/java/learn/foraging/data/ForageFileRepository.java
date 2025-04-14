package learn.foraging.data;

import learn.foraging.models.Forage;
import learn.foraging.models.Forager;
import learn.foraging.models.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ForageFileRepository implements ForageRepository {

    private static final String HEADER = "id,forager_id,item_id,kg";
    private static final String DELIMITER = ",";
    private static final String DELIMITER_REPLACEMENT = "@@@";
    private final String directory;

    public ForageFileRepository(@Value("${forageRepository}") String directory) {
        this.directory = directory;
    }

    @Override
    public List<Forage> findByDate(LocalDate date) {
        ArrayList<Forage> result = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(getFilePath(date)))) {

            reader.readLine(); // read header

            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    result.add(deserialize(line, date));
            }
        } catch (IOException ex) {
            // don't throw on read
        }
        return result;
    }

    @Override
    public Forage add(Forage forage) throws DataException {

        if (forage == null) {
            return null;
        }

        List<Forage> all = findByDate(forage.getDate());

        forage.setId(java.util.UUID.randomUUID().toString());

        all.add(forage);
        writeAll(all, forage.getDate());

        return forage;
    }

    @Override
    public boolean update(Forage forage) throws DataException {
        List<Forage> all = findByDate(forage.getDate());
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId().equals(forage.getId())) {
                all.set(i, forage);
                writeAll(all, forage.getDate());
                return true;
            }
        }
        return false;
    }

    private String getFilePath(LocalDate date) {
        return Paths.get(directory, date + ".csv").toString();
    }

    private void writeAll(List<Forage> forages, LocalDate date) throws DataException {
        try (PrintWriter writer = new PrintWriter(getFilePath(date))) {

            writer.println(HEADER);

            for (Forage item : forages) {
                writer.println(serialize(item));
            }
        } catch (FileNotFoundException ex) {
            throw new DataException(ex);
        }
    }

    private String serialize(Forage item) {
        StringBuffer buffer = new StringBuffer(100);
        buffer.append(clean(item.getId())).append(DELIMITER);
        buffer.append(clean(item.getForager().getId())).append(DELIMITER);
        buffer.append(item.getItem().getId()).append(DELIMITER);
        buffer.append(item.getKilograms()).append(DELIMITER);
        return buffer.toString();
    }

    private Forage deserialize(String line, LocalDate date) {

        String[] fields = line.split(DELIMITER);
        if (fields.length != 4) {
            return null;
        }
        Forage forage = new Forage();
        forage.setId(restore(fields[0]));
        forage.setDate(date);
        Forager forager = new Forager();
        forager.setId(restore(fields[1]));
        forage.setForager(forager);
        Item item = new Item();
        item.setId(Integer.parseInt(restore(fields[2])));
        forage.setItem(item);
        forage.setKilograms(Double.parseDouble(restore(fields[3])));
        return forage;
    }

    private String restore(String value) {
        return value.replace(DELIMITER_REPLACEMENT, DELIMITER);
    }

    private String clean(String value) {
        return value.replace(DELIMITER, DELIMITER_REPLACEMENT);
    }
}
