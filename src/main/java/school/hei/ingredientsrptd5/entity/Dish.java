package school.hei.ingredientsrptd5.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import school.hei.ingredientsrptd5.entity.enums.DishTypeEnum;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dish {

    private Integer id;
    private String name;
    private DishTypeEnum dishType;
    private Double sellingPrice;

    private List<DishIngredient> dishIngredients;

    @JsonIgnore
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

    @JsonIgnore
    public Double getGrossMargin() {

        if (sellingPrice == null) {
            throw new RuntimeException("Selling price is null");
        }

        return sellingPrice - getDishCost();
    }
}
