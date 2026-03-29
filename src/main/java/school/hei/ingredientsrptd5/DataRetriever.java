import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    private DBConnection dbConnection = new DBConnection();


    public List<Dish> findDishsByIngredientName(String ingredientName) {

        String sql = """
        SELECT DISTINCT d.id, d.name, d.dish_type, d.selling_price
        FROM dish d
        JOIN dish_ingredient di ON d.id = di.id_dish
        JOIN ingredient i ON di.id_ingredient = i.id
        WHERE i.name ILIKE ?
    """;

        List<Dish> dishes = new ArrayList<>();

        try (Connection conn = dbConnection.getDBConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + ingredientName + "%");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Dish dish = new Dish();
                dish.setId(rs.getInt("id"));
                dish.setName(rs.getString("name"));
                dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
                dish.setSellingPrice((Double) rs.getObject("selling_price"));

                dishes.add(dish);
            }

            return dishes;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> findIngredientsByCriteria(
            String ingredientName,
            CategoryEnum category,
            String dishName,
            int page,
            int size
    ) {

        StringBuilder sql = new StringBuilder("""
        SELECT i.id, i.name, i.price, i.category
        FROM ingredient i
        LEFT JOIN dish_ingredient di ON i.id = di.id_ingredient
        LEFT JOIN dish d ON di.id_dish = d.id
        WHERE 1=1
    """);

        List<Object> params = new ArrayList<>();

        if (ingredientName != null) {
            sql.append(" AND i.name ILIKE ?");
            params.add("%" + ingredientName + "%");
        }

        if (category != null) {
            sql.append(" AND i.category = ?::category_enum");
            params.add(category.name());
        }

        if (dishName != null) {
            sql.append(" AND d.name ILIKE ?");
            params.add("%" + dishName + "%");
        }

        sql.append(" ORDER BY i.id LIMIT ? OFFSET ?");

        int offset = (page - 1) * size;
        params.add(size);
        params.add(offset);

        List<Ingredient> ingredients = new ArrayList<>();

        try (Connection conn = dbConnection.getDBConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt("id"));
                ingredient.setName(rs.getString("name"));
                ingredient.setPrice(rs.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));

                ingredients.add(ingredient);
            }

            return ingredients;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

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