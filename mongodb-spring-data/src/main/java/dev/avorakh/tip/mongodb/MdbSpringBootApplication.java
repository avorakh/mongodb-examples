package dev.avorakh.tip.mongodb;

import dev.avorakh.tip.mongodb.repository.ItemRepository;
import dev.avorakh.tip.mongodb.svc.GroceryItemDemoService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MdbSpringBootApplication implements CommandLineRunner {

    GroceryItemDemoService groceryItemDemoService;
    ItemRepository itemRepository;

    public static void main(String[] args) {
        SpringApplication.run(MdbSpringBootApplication.class, args);
    }

    @Override
    public void run(String... args) {
        itemRepository.deleteAll();

        groceryItemDemoService.createSampleItems();
        groceryItemDemoService.printAllItems();
        groceryItemDemoService.printItemByName("Whole Wheat Biscuit");
        groceryItemDemoService.printItemsByCategory("millets");
        groceryItemDemoService.updateCategory("snacks", "munchies");
        groceryItemDemoService.deleteItemById("Kodo Millet");
        groceryItemDemoService.printItemCount();
    }
}
