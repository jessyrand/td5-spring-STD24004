package school.hei.ingredientsrptd5.service;

import org.springframework.stereotype.Service;
import school.hei.ingredientsrptd5.entity.Ingredient;
import school.hei.ingredientsrptd5.repository.IngredientRepository;
import school.hei.ingredientsrptd5.repository.StockMovementRepository;

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

    public List<Ingredient> createIngredients(List<Ingredient> newIngredients) {

        if (newIngredients == null || newIngredients.isEmpty()) {
            throw new RuntimeException("Ingredient list cannot be empty");
        }

        return ingredientRepository.createIngredients(newIngredients);
    }
}
