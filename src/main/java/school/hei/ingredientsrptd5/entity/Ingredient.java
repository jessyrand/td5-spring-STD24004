package school.hei.ingredientsrptd5.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import school.hei.ingredientsrptd5.entity.enums.CategoryEnum;
import school.hei.ingredientsrptd5.entity.enums.MovementTypeEnum;
import school.hei.ingredientsrptd5.entity.enums.UnitEnum;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ingredient {
    private Integer id;
    private String name;
    private Double price;
    private CategoryEnum category;

    private List<StockMovement> stockMovementList;

    public StockValue getStockValueAt(Instant t) {
        double total = 0;

        for (StockMovement sm : stockMovementList) {
            if (sm.getCreationDatetime().isBefore(t) || sm.getCreationDatetime().equals(t)) {
                if (sm.getType() == MovementTypeEnum.IN) {
                    total += sm.getQuantity();
                } else {
                    total -= sm.getQuantity();
                }
            }
        }

        return new StockValue(total, UnitEnum.KG);
    }
}
