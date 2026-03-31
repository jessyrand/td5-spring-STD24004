package school.hei.ingredientsrptd5.entity;

import lombok.Data;
import school.hei.ingredientsrptd5.entity.enums.MovementTypeEnum;
import school.hei.ingredientsrptd5.entity.enums.UnitEnum;

@Data
public class CreateStockMovement {

    private Double quantity;
    private MovementTypeEnum type;
    private UnitEnum unit;

}
