import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Dish {

    private Integer id;
    private String name;
    private DishTypeEnum dishType;
    private Double sellingPrice;

    private List<DishIngredient> dishIngredients;

    public Double getDishCost() {

        double total = 0;

        if (dishIngredients == null) {
            return total;
        }

        for (DishIngredient di : dishIngredients) {
            total += di.getIngredient().getPrice() * di.getQuantityRequired();
        }

        return total;
    }

    public Double getGrossMargin() {

        if (sellingPrice == null) {
            throw new RuntimeException("Selling price is null");
        }

        return sellingPrice - getDishCost();
    }
}