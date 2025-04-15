package learn.foraging.data;

import learn.foraging.models.Category;
import learn.foraging.models.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class ItemFileRepository implements ItemRepository {

    private static final String HEADER = "id,name,category,dollars/kilogram";
    private final String filePath;
    private static final String DELIMITER = ",";
    private static final String DELIMITER_REPLACEMENT = "@@@";

    public ItemFileRepository(@Value("${itemRepository}") String filePath) {
        this.filePath = filePath;
    }

    @Override
    public List<Item> findAll() {
        ArrayList<Item> result = new ArrayList<>();
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
    public Item findById(int id) {
        return findAll().stream()
                .filter(i -> i.getId() == id)
                .findFirst()
                .orElse(null);
    }

    public Item add(Item item) throws DataException {

        if (item == null) {
            return null;
        }

        List<Item> all = findAll();

        int nextId = all.stream()
                .filter(Objects::nonNull)
                .mapToInt(Item::getId)
                .max()
                .orElse(0) + 1;

        item.setId(nextId);

        all.add(item);
        writeAll(all);

        return item;
    }

    public boolean update(Item item) throws DataException {

        if (item == null) {
            return false;
        }

        List<Item> all = findAll();
        for (int i = 0; i < all.size(); i++) {
            if (item.getId() == all.get(i).getId()) {
                all.set(i, item);
                writeAll(all);
                return true;
            }
        }

        return false;
    }

    private String serialize(Item item) {
        StringBuffer buffer = new StringBuffer(100);
        buffer.append(item.getId()).append(DELIMITER);
        buffer.append(clean(item.getName())).append(DELIMITER);
        buffer.append(item.getCategory()).append(DELIMITER);
        buffer.append(item.getDollarPerKilogram()).append(DELIMITER);
        return buffer.toString();
    }

    private Item deserialize(String line) {
        String[] fields = line.split(DELIMITER);

        if (fields.length != 4) {
            return null;
        }

        Item result = new Item();
        result.setId(Integer.parseInt(fields[0]));
        result.setName(restore(fields[1]));
        result.setCategory(Category.valueOf(fields[2]));
        result.setDollarPerKilogram(new BigDecimal(fields[3]));
        return result;
    }

    private String restore(String value) {
        return value.replace(DELIMITER_REPLACEMENT, DELIMITER);
    }

    private String clean(String value) {
        return value.replace(DELIMITER, DELIMITER_REPLACEMENT);
    }

    protected void writeAll(List<Item> items) throws DataException {
        try (PrintWriter writer = new PrintWriter(filePath)) {

            writer.println(HEADER);

            for (Item item : items) {
                if (item != null) {
                    writer.println(serialize(item));
                }
            }

        } catch (FileNotFoundException ex) {
            throw new DataException(ex);
        }
    }
}
