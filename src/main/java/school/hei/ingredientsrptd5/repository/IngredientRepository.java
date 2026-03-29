package school.hei.ingredientsrptd5.repository;

import org.springframework.stereotype.Repository;
import school.hei.ingredientsrptd5.entity.Ingredient;
import school.hei.ingredientsrptd5.entity.enums.CategoryEnum;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class IngredientRepository {

    private final DataSource dataSource;

    public IngredientRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Ingredient> findAll(int page, int size) {

        String sql = """
            SELECT id, name, price, category
            FROM ingredient
            ORDER BY id
            LIMIT ? OFFSET ?
        """;

        List<Ingredient> ingredients = new ArrayList<>();
        int offset = (page - 1) * size;

        try (Connection conn = dataSource.getConnection();
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

                ingredients.add(ingredient);
            }

            return ingredients;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {

        String checkSql = "SELECT COUNT(id) FROM ingredient WHERE LOWER(name) = LOWER(?)";

        String insertSql = """
            INSERT INTO ingredient (name, price, category)
            VALUES (?, ?, ?::category_enum)
        """;

        List<Ingredient> createdIngredients = new ArrayList<>();

        try (Connection conn = dataSource.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                 PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {

                for (Ingredient ingredient : newIngredients) {

                    checkStmt.setString(1, ingredient.getName());
                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next() && rs.getInt(1) > 0) {
                        throw new RuntimeException(
                                "Ingredient already exists: " + ingredient.getName()
                        );
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
}
