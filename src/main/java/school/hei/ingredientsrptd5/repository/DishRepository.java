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
import java.util.List;

@Repository
public class DishRepository {

    private final DataSource dataSource;

    public DishRepository(DataSource dataSource) {
        this.dataSource = dataSource;
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
                    dish.setDishType(DishTypeEnum.valueOf(rs.getString("dish_type")));

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
}