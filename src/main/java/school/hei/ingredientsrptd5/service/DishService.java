package school.hei.ingredientsrptd5.service;

import org.springframework.stereotype.Service;
import school.hei.ingredientsrptd5.entity.Dish;
import school.hei.ingredientsrptd5.repository.DishRepository;

import java.util.List;

@Service
public class DishService {

    private final DishRepository dishRepository;

    public DishService(DishRepository dishRepository) {
        this.dishRepository = dishRepository;
    }

    public Dish getById(Integer id) {

        Dish dish = dishRepository.findById(id);

        if (dish == null) {
            throw new RuntimeException("Dish not found with id: " + id);
        }

        return dish;
    }

    public Dish save(Dish dishToSave) {

        if (dishToSave == null) {
            throw new RuntimeException("Dish cannot be null");
        }

        if (dishToSave.getName() == null || dishToSave.getDishType() == null) {
            throw new RuntimeException("Dish name and type are required");
        }

        return dishRepository.saveDish(dishToSave);
    }

    public List<Dish> findDishsByIngredientName(String ingredientName) {

        if (ingredientName == null || ingredientName.isBlank()) {
            throw new RuntimeException("Ingredient name is required");
        }

        return dishRepository.findDishsByIngredientName(ingredientName);
    }
}
