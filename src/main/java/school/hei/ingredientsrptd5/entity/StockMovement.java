package school.hei.ingredientsrptd5.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import school.hei.ingredientsrptd5.entity.enums.MovementTypeEnum;
import school.hei.ingredientsrptd5.entity.enums.UnitEnum;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockMovement {

    private Integer id;
    private Double quantity;
    private MovementTypeEnum type;
    private UnitEnum unit;
    private Instant creationDatetime;

}
