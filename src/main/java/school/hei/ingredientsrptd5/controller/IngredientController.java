package school.hei.ingredientsrptd5.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import school.hei.ingredientsrptd5.entity.Ingredient;
import school.hei.ingredientsrptd5.service.IngredientService;

import java.util.List;

@RestController
public class IngredientController {

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @GetMapping ("/ingredients")
    public ResponseEntity<?> getIngredients(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
            List<Ingredient> ingredients = ingredientService.getAll(page, size);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ingredients);
    }

    @GetMapping("/ingredients/{id}")
    public ResponseEntity<?> getIngredientById(@PathVariable int id) {
        Ingredient ingredient = ingredientService.getById(id);

        if (ingredient == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Ingredient.id=" + id + " is not found"
            );
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ingredient);
    }
}
