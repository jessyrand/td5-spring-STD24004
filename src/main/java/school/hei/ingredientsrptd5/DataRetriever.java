import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    private DBConnection dbConnection = new DBConnection();


    public Ingredient saveIngredient(Ingredient ingredient) {

        String upsertIngredientSql = """
            INSERT INTO ingredient (id, name, price, category)
            VALUES (?, ?, ?, ?::category_enum)
            ON CONFLICT (id) DO UPDATE
            SET name = EXCLUDED.name,
                price = EXCLUDED.price,
                category = EXCLUDED.category
        """;

        String insertStockSql = """
        INSERT INTO stock_movement (id, id_ingredient, quantity, type, unit, creation_datetime)
        VALUES (?, ?, ?, ?::movement_type, ?::unit_enum, ?)
        ON CONFLICT (id) DO NOTHING
    """;

        try (Connection conn = dbConnection.getDBConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement ingredientStmt = conn.prepareStatement(upsertIngredientSql);
                 PreparedStatement stockStmt = conn.prepareStatement(insertStockSql)) {

                ingredientStmt.setObject(1, ingredient.getId());
                ingredientStmt.setString(2, ingredient.getName());
                ingredientStmt.setDouble(3, ingredient.getPrice());
                ingredientStmt.setString(4, ingredient.getCategory().name());
                ingredientStmt.executeUpdate();

                if (ingredient.getStockMovementList() != null) {
                    for (StockMovement sm : ingredient.getStockMovementList()) {

                        stockStmt.setObject(1, sm.getId());
                        stockStmt.setInt(2, ingredient.getId());
                        stockStmt.setDouble(3, sm.getQuantity());
                        stockStmt.setString(4, sm.getType().name());
                        stockStmt.setString(5, sm.getUnit().name());
                        stockStmt.setTimestamp(6, Timestamp.from(sm.getCreationDatetime()));

                        stockStmt.executeUpdate();
                    }
                }

                conn.commit();
                return ingredient;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}