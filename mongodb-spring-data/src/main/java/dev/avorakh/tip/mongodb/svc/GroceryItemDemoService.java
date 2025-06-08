package dev.avorakh.tip.mongodb.svc;

import dev.avorakh.tip.mongodb.model.GroceryItem;
import dev.avorakh.tip.mongodb.repository.ItemRepository;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroceryItemDemoService {

    ItemRepository repository;

    public void createSampleItems() {
        log.info("Creating sample grocery items...");
        List<GroceryItem> items = List.of(
                GroceryItem.builder()
                        .name("Whole Wheat Biscuit")
                        .quantity(5)
                        .category("snacks")
                        .build(),
                GroceryItem.builder()
                        .name("XYZ Kodo Millet healthy")
                        .quantity(2)
                        .category("millets")
                        .build(),
                GroceryItem.builder()
                        .name("Dried Whole Red Chilli")
                        .quantity(2)
                        .category("spices")
                        .build(),
                GroceryItem.builder()
                        .name("Healthy Pearl Millet")
                        .quantity(1)
                        .category("millets")
                        .build(),
                GroceryItem.builder()
                        .name("Bonny Cheese Crackers Plain")
                        .quantity(6)
                        .category("snacks")
                        .build());
        repository.saveAll(items);
        log.info("Sample items created.");
    }

    public void printAllItems() {
        repository.findAll().forEach(this::printItemDetails);
    }

    public void printItemByName(String name) {
        GroceryItem item = repository.findItemByName(name);
        if (item != null) {
            printItemDetails(item);
        } else {
            log.warn("Item with name '{}' not found.", name);
        }
    }

    public void printItemsByCategory(String category) {
        List<GroceryItem> items = repository.findAll(category);
        items.forEach(item -> log.info("Name: {}, Quantity: {}", item.getName(), item.getQuantity()));
    }

    public void updateCategory(String oldCategory, String newCategory) {
        List<GroceryItem> items = repository.findAll(oldCategory);
        items.forEach(item -> item.setCategory(newCategory));
        repository.saveAll(items);
        log.info("Updated category from '{}' to '{}' for {} items.", oldCategory, newCategory, items.size());
    }

    public void deleteItemById(String id) {
        repository.deleteById(id);
        log.info("Deleted item with id: {}", id);
    }

    public void printItemCount() {
        long count = repository.count();
        log.info("Total items in the database: {}", count);
    }

    private void printItemDetails(GroceryItem item) {
        log.info("Item Name: {}\nQuantity: {}\nCategory: {}", item.getName(), item.getQuantity(), item.getCategory());
    }
}
