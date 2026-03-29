import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
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