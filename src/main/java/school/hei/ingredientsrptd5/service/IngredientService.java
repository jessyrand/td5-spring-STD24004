package school.hei.ingredientsrptd5.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import school.hei.ingredientsrptd5.entity.Ingredient;
import school.hei.ingredientsrptd5.entity.StockMovement;
import school.hei.ingredientsrptd5.entity.StockValue;
import school.hei.ingredientsrptd5.entity.enums.CategoryEnum;
import school.hei.ingredientsrptd5.entity.enums.MovementTypeEnum;
import school.hei.ingredientsrptd5.entity.enums.UnitEnum;
import school.hei.ingredientsrptd5.repository.IngredientRepository;
import school.hei.ingredientsrptd5.repository.StockMovementRepository;

import java.time.Instant;
import java.util.List;

@Service
public class IngredientService {

    private final IngredientRepository ingredientRepository;
    private final StockMovementRepository stockMovementRepository;

    public IngredientService(IngredientRepository ingredientRepository,
                             StockMovementRepository stockMovementRepository) {
        this.ingredientRepository = ingredientRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    public List<Ingredient> getAll(int page, int size) {

        List<Ingredient> ingredients = ingredientRepository.findAll(page, size);

        for (Ingredient ingredient : ingredients) {
            ingredient.setStockMovementList(
                    stockMovementRepository.findByIngredientId(ingredient.getId())
            );
        }

        return ingredients;
    }

    public Ingredient getById(int id) {
        return ingredientRepository.findById(id);
    }

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {

        if (newIngredients == null || newIngredients.isEmpty()) {
            throw new RuntimeException("Ingredient list cannot be empty");
        }

        return ingredientRepository.createIngredients(newIngredients);
    }

    public List<Ingredient> findIngredientsByCriteria(
            String ingredientName,
            CategoryEnum category,
            String dishName,
            int page,
            int size
    ) {

        if (page <= 0 || size <= 0) {
            throw new RuntimeException("Page and size must be positive");
        }

        return ingredientRepository.findIngredientsByCriteria(
                ingredientName,
                category,
                dishName,
                page,
                size
        );
    }

    public Ingredient saveIngredient(Ingredient ingredient) {

        if (ingredient == null) {
            throw new RuntimeException("Ingredient cannot be null");
        }

        if (ingredient.getName() == null || ingredient.getCategory() == null) {
            throw new RuntimeException("Ingredient name and category are required");
        }

        return ingredientRepository.saveIngredient(ingredient);
    }

    public StockValue getStockValueAt(int ingredientId, Instant at, UnitEnum unit) {

        Ingredient ingredient = ingredientRepository.findById(ingredientId);

        if (ingredient == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Ingredient.id=" + ingredientId + " is not found"
            );
        }

        List<StockMovement> movements =
                stockMovementRepository.findByIngredientId(ingredientId);

        double stock = 0.0;

        for (StockMovement sm : movements) {
            if (!sm.getCreationDatetime().isAfter(at)) {

                if (sm.getType() == MovementTypeEnum.IN) {
                    stock += sm.getQuantity();
                } else {
                    stock -= sm.getQuantity();
                }
            }
        }

        StockValue result = new StockValue();
        result.setQuantity(stock);
        result.setUnit(unit);

        return result;
    }
}
