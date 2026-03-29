import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class StockMovement {

    private Integer id;
    private Double quantity;
    private MovementTypeEnum type;
    private UnitEnum unit;
    private Instant creationDatetime;

}