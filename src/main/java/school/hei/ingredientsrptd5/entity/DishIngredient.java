package school.hei.ingredientsrptd5.entity;

import lombok.*;
import school.hei.ingredientsrptd5.entity.enums.UnitEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DishIngredient {

    private Integer id;
    private Dish dish;
    private Ingredient ingredient;
    private Double quantityRequired;
    private UnitEnum unit;

}
