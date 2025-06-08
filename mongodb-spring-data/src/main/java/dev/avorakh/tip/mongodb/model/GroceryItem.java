package dev.avorakh.tip.mongodb.model;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Document("grocery_items")
public class GroceryItem {
    @Id
    String id;

    String name;
    int quantity;
    String category;
}
