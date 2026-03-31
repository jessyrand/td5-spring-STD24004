package school.hei.ingredientsrptd5.repository;

import org.springframework.stereotype.Repository;
import school.hei.ingredientsrptd5.entity.StockMovement;
import school.hei.ingredientsrptd5.entity.enums.MovementTypeEnum;
import school.hei.ingredientsrptd5.entity.enums.UnitEnum;

import javax.sql.DataSource;
import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
public class StockMovementRepository {

    private final DataSource dataSource;

    public StockMovementRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<StockMovement> findByIngredientId(int ingredientId) {

        String sql = """
            SELECT id, quantity, type, unit, creation_datetime
            FROM stock_movement
            WHERE id_ingredient = ?
            ORDER BY creation_datetime
        """;

        List<StockMovement> movements = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ingredientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                StockMovement sm = new StockMovement();
                sm.setId(rs.getInt("id"));
                sm.setQuantity(rs.getDouble("quantity"));
                sm.setType(
                        MovementTypeEnum.valueOf(rs.getString("type"))
                );
                sm.setUnit(
                        UnitEnum.valueOf(rs.getString("unit"))
                );
                sm.setCreationDatetime(
                        rs.getTimestamp("creation_datetime").toInstant()
                );

                movements.add(sm);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return movements;
    }

    public List<StockMovement> findByIngredientIdAndDateRange(
            int ingredientId,
            Instant from,
            Instant to
    ) {

        String sql = """
            SELECT id, quantity, type, unit, creation_datetime
            FROM stock_movement
            WHERE id_ingredient = ?
            AND creation_datetime BETWEEN ? AND ?
            ORDER BY creation_datetime
        """;

        List<StockMovement> movements = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ingredientId);
            stmt.setTimestamp(2, Timestamp.from(from));
            stmt.setTimestamp(3, Timestamp.from(to));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                StockMovement sm = new StockMovement();
                sm.setId(rs.getInt("id"));
                sm.setQuantity(rs.getDouble("quantity"));
                sm.setType(
                        MovementTypeEnum.valueOf(rs.getString("type").toUpperCase())
                );
                sm.setUnit(
                        UnitEnum.valueOf(rs.getString("unit").toUpperCase())
                );
                sm.setCreationDatetime(
                        rs.getTimestamp("creation_datetime").toInstant()
                );

                movements.add(sm);
            }

            return movements;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
