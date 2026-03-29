package school.hei.ingredientsrptd5.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import school.hei.ingredientsrptd5.entity.enums.UnitEnum;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockValue {

    private Double quantity;
    private UnitEnum unit;

}
