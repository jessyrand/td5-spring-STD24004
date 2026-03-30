package school.hei.ingredientsrptd5.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import school.hei.ingredientsrptd5.entity.Dish;
import school.hei.ingredientsrptd5.service.DishService;

import java.util.List;

@RestController
@RequestMapping
public class DishController {

    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @GetMapping("/dishes")
    public ResponseEntity<?> getDishes() {
        List<Dish> dishes = dishService.getAll();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(dishes);
    }

    @PutMapping("/dishes/{id}/ingredients")
    public ResponseEntity<?> updateDishIngredients(
            @PathVariable int id,
            @RequestBody(required = false) List<Integer> ingredientIds
    ) {

        if (ingredientIds == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Request body is required");
        }

        try {
            Dish updatedDish = dishService.updateDishIngredients(id, ingredientIds);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(updatedDish);

        } catch (RuntimeException e) {

            if (e.getMessage().contains("not found")) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(e.getMessage());
            }

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }
}
