package school.hei.ingredientsrptd5.repository;

import org.springframework.stereotype.Repository;
import school.hei.ingredientsrptd5.entity.Dish;
import school.hei.ingredientsrptd5.entity.DishIngredient;
import school.hei.ingredientsrptd5.entity.Ingredient;
import school.hei.ingredientsrptd5.entity.enums.CategoryEnum;
import school.hei.ingredientsrptd5.entity.enums.DishTypeEnum;
import school.hei.ingredientsrptd5.entity.enums.UnitEnum;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DishRepository {

    private final DataSource dataSource;

    public DishRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Dish> findAll() {

        String sql = """
            SELECT d.id as d_id, d.name as d_name, d.dish_type, d.selling_price,
                di.quantity_required, di.unit,
                i.id as i_id, i.name as i_name, i.price, i.category
            FROM dish d
            LEFT JOIN dish_ingredient di ON d.id = di.id_dish
            LEFT JOIN ingredient i ON di.id_ingredient = i.id
            ORDER BY d.id
        """;

        List<Dish> dishes = new ArrayList<>();
        Map<Integer, Dish> dishMap = new HashMap<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {

                int dishId = rs.getInt("d_id");

                Dish dish = dishMap.get(dishId);

                if (dish == null) {
                    dish = new Dish();
                    dish.setId(dishId);
                    dish.setName(rs.getString("d_name"));
                    dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type").toUpperCase()));

                    Object price = rs.getObject("selling_price");
                    dish.setSellingPrice(
                            price != null ? ((java.math.BigDecimal) price).doubleValue() : null
                    );

                    dish.setDishIngredients(new ArrayList<>());
                    dishMap.put(dishId, dish);
                    dishes.add(dish);
                }

                Integer ingredientId = (Integer) rs.getObject("i_id");

                if (ingredientId != null) {
                    Ingredient ingredient = new Ingredient();
                    ingredient.setId(ingredientId);
                    ingredient.setName(rs.getString("i_name"));
                    ingredient.setPrice(rs.getDouble("price"));
                    ingredient.setCategory(CategoryEnum.valueOf(rs.getString("category").toUpperCase()));

                    DishIngredient di = new DishIngredient();
                    di.setIngredient(ingredient);

                    Object qtyObj = rs.getObject("quantity_required");
                    di.setQuantityRequired(
                            qtyObj != null ? ((Number) qtyObj).doubleValue() : 0.0
                    );

                    di.setUnit(
                            rs.getString("unit") != null
                                    ? UnitEnum.valueOf(rs.getString("unit").toUpperCase())
                                    : null
                    );

                    dish.getDishIngredients().add(di);
                }
            }

            return dishes;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Dish findById(Integer id) {

        String sql = """
            SELECT d.id as d_id, d.name as d_name, d.dish_type, d.selling_price,
            di.id as di_id, di.quantity_required, di.unit,
            i.id as i_id, i.name as i_name, i.price, i.category
            FROM dish d
            LEFT JOIN dish_ingredient di ON d.id = di.id_dish
            LEFT JOIN ingredient i ON di.id_ingredient = i.id
            WHERE d.id = ?;
        """;

        try (Connection conn = dataSource.getConnection();
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
                    dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type").toUpperCase()));

                    Object price = rs.getObject("selling_price");
                    dish.setSellingPrice(
                            price != null
                                    ? ((java.math.BigDecimal) price).doubleValue()
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

            if (dish != null) {
                dish.setDishIngredients(dishIngredients);
            }

            return dish;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

        try (Connection conn = dataSource.getConnection()) {

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

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + ingredientName + "%");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Dish dish = new Dish();
                dish.setId(rs.getInt("id"));
                dish.setName(rs.getString("name"));
                dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));
                dish.setSellingPrice(
                        rs.getObject("selling_price") != null
                                ? ((java.math.BigDecimal) rs.getObject("selling_price")).doubleValue()
                                : null
                );

                dishes.add(dish);
            }

            return dishes;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public void updateDishIngredients(int dishId, List<Integer> ingredientIds) {

        String checkDishSql = "SELECT COUNT(id) FROM dish WHERE id = ?";
        String checkIngredientSql = "SELECT id FROM ingredient WHERE id = ?";
        String deleteSql = "DELETE FROM dish_ingredient WHERE id_dish = ?";
        String insertSql = """
            INSERT INTO dish_ingredient (id_dish, id_ingredient, quantity_required, unit)
            VALUES (?, ?, 1, 'KG')
        """;

        try (Connection conn = dataSource.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement checkDishStmt = conn.prepareStatement(checkDishSql);
                 PreparedStatement checkIngredientStmt = conn.prepareStatement(checkIngredientSql);
                 PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                 PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

                checkDishStmt.setInt(1, dishId);
                ResultSet rs = checkDishStmt.executeQuery();

                if (rs.next() && rs.getInt(1) == 0) {
                    throw new RuntimeException("Dish.id=" + dishId + " is not found");
                }

                deleteStmt.setInt(1, dishId);
                deleteStmt.executeUpdate();

                for (Integer ingredientId : ingredientIds) {

                    checkIngredientStmt.setInt(1, ingredientId);
                    ResultSet rsIng = checkIngredientStmt.executeQuery();

                    if (rsIng.next()) {
                        insertStmt.setInt(1, dishId);
                        insertStmt.setInt(2, ingredientId);
                        insertStmt.executeUpdate();
                    }
                }

                conn.commit();

            } catch (Exception e) {
                conn.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}