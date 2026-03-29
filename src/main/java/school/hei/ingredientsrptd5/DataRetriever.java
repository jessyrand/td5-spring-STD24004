import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataRetriever {

    private DBConnection dbConnection = new DBConnection();

    public Dish findDishById(Integer id) {
        String sql = """
            SELECT d.id as d_id, d.name as d_name, d.dish_type, d.selling_price,
            di.id as di_id, di.quantity_required, di.unit,
            i.id as i_id, i.name as i_name, i.price, i.category
            FROM dish d
            LEFT JOIN dish_ingredient di ON d.id = di.id_dish
            LEFT JOIN ingredient i ON di.id_ingredient = i.id
            WHERE d.id = ?;
        """;

        try (Connection conn = DBConnection.getDBConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            Dish dish = null;
            List<DishIngredient> dishIngredients = new ArrayList<>();

            while (rs.next()) {

                if (dish == null) {
                    dish = new Dish();
                    dish.setId(rs.getInt("d_id"));
                    dish.setName(rs.getString("d_name"));
                    dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
                    dish.setSellingPrice(
                            rs.getObject("selling_price") != null
                                    ? ((java.math.BigDecimal) rs.getObject("selling_price")).doubleValue()
                                    : null
                    );
                }

                int ingredientId = rs.getInt("i_id");

                if (!rs.wasNull()) {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setId(ingredientId);
                    ingredient.setName(rs.getString("i_name"));
                    ingredient.setPrice(rs.getDouble("price"));
                    ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));

                    DishIngredient di = new DishIngredient();
                    di.setId(rs.getInt("di_id"));
                    di.setIngredient(ingredient);
                    di.setQuantityRequired(rs.getDouble("quantity_required"));
                    di.setUnit(UnitEnum.valueOf(rs.getString("unit")));

                    dishIngredients.add(di);
                }
            }

            if (dish == null) {
                throw new RuntimeException("Dish not found");
            }

            dish.setDishIngredients(dishIngredients);
            return dish;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> findIngredients(int page, int size) {

        String sql = """
        SELECT id, name, price, category
        FROM ingredient
        ORDER BY id
        LIMIT ? OFFSET ?
    """;

        List<Ingredient> ingredients = new ArrayList<>();

        int offset = (page - 1) * size;

        try (Connection conn = dbConnection.getDBConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, size);
            stmt.setInt(2, offset);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Ingredient ingredient = new Ingredient();
                ingredient.setId(rs.getInt("id"));
                ingredient.setName(rs.getString("name"));
                ingredient.setPrice(rs.getDouble("price"));
                ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category")));

                ingredient.setStockMovementList(
                        getStockMovementsByIngredientId(ingredient.getId())
                );

                ingredients.add(ingredient);
            }

            return ingredients;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {

        String checkSql = "SELECT COUNT(*) FROM ingredient WHERE LOWER(name) = LOWER(?)";

        String insertSql = """
        INSERT INTO ingredient (name, price, category)
        VALUES (?, ?, ?::category_enum)
    """;

        List<Ingredient> createdIngredients = new ArrayList<>();

        try (Connection conn = dbConnection.getDBConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                 PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

                for (Ingredient ingredient : newIngredients) {

                    checkStmt.setString(1, ingredient.getName());
                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new RuntimeException("Ingredient already exists: " + ingredient.getName());
                    }

                    insertStmt.setString(1, ingredient.getName());
                    insertStmt.setDouble(2, ingredient.getPrice());
                    insertStmt.setString(3, ingredient.getCategory().name());

                    insertStmt.executeUpdate();

                    ResultSet generatedKeys = insertStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        ingredient.setId(generatedKeys.getInt(1));
                    }

                    createdIngredients.add(ingredient);
                }

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return createdIngredients;
    }

    public Dish saveDish(Dish dishToSave) {

        String insertDishSql = """
        INSERT INTO dish (name, dish_type, selling_price)
        VALUES (?, ?::dish_type_enum, ?)
        RETURNING id
    """;

        String updateDishSql = """
        UPDATE dish
        SET name = ?, dish_type = ?::dish_type_enum, selling_price = ?
        WHERE id = ?
    """;

        String deleteRelationsSql = "DELETE FROM dish_ingredient WHERE id_dish = ?";

        String insertRelationSql = """
        INSERT INTO dish_ingredient (id_dish, id_ingredient, quantity_required, unit)
        VALUES (?, ?, ?, ?)
    """;

        try (Connection conn = dbConnection.getDBConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement insertStmt = conn.prepareStatement(insertDishSql);
                 PreparedStatement updateStmt = conn.prepareStatement(updateDishSql);
                 PreparedStatement deleteStmt = conn.prepareStatement(deleteRelationsSql);
                 PreparedStatement insertRelStmt = conn.prepareStatement(insertRelationSql)) {

                if (dishToSave.getId() == null) {

                    insertStmt.setString(1, dishToSave.getName());
                    insertStmt.setString(2, dishToSave.getDishType().name());
                    insertStmt.setObject(3, dishToSave.getSellingPrice());

                    ResultSet rs = insertStmt.executeQuery();
                    if (rs.next()) {
                        dishToSave.setId(rs.getInt(1));
                    }

                } else {

                    updateStmt.setString(1, dishToSave.getName());
                    updateStmt.setString(2, dishToSave.getDishType().name());
                    updateStmt.setObject(3, dishToSave.getSellingPrice());
                    updateStmt.setInt(4, dishToSave.getId());

                    updateStmt.executeUpdate();

                    deleteStmt.setInt(1, dishToSave.getId());
                    deleteStmt.executeUpdate();
                }

                if (dishToSave.getDishIngredients() != null) {
                    for (DishIngredient di : dishToSave.getDishIngredients()) {

                        insertRelStmt.setInt(1, dishToSave.getId());
                        insertRelStmt.setInt(2, di.getIngredient().getId());
                        insertRelStmt.setDouble(3, di.getQuantityRequired());
                        insertRelStmt.setString(4, di.getUnit().name());

                        insertRelStmt.executeUpdate();
                    }
                }

                conn.commit();
                return dishToSave;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

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

    private List<StockMovement> getStockMovementsByIngredientId(int ingredientId) {

        String sql = """
            SELECT id, quantity, type, unit, creation_datetime
            FROM stock_movement
            WHERE id_ingredient = ?
            ORDER BY creation_datetime
        """;

        List<StockMovement> movements = new ArrayList<>();

        try (Connection conn = dbConnection.getDBConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, ingredientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                StockMovement sm = new StockMovement();
                sm.setId(rs.getInt("id"));
                sm.setQuantity(rs.getDouble("quantity"));
                sm.setType(MovementTypeEnum.valueOf(rs.getString("type")));
                sm.setUnit(UnitEnum.valueOf(rs.getString("unit")));
                sm.setCreationDatetime(rs.getTimestamp("creation_datetime").toInstant());

                movements.add(sm);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return movements;
    }
}