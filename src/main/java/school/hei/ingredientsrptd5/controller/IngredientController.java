package school.hei.ingredientsrptd5.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import school.hei.ingredientsrptd5.entity.CreateStockMovement;
import school.hei.ingredientsrptd5.entity.Ingredient;
import school.hei.ingredientsrptd5.entity.StockMovement;
import school.hei.ingredientsrptd5.entity.StockValue;
import school.hei.ingredientsrptd5.entity.enums.UnitEnum;
import school.hei.ingredientsrptd5.service.IngredientService;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/ingredients")
public class IngredientController {

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @GetMapping
    public ResponseEntity<?> getIngredients(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
            List<Ingredient> ingredients = ingredientService.getAll(page, size);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ingredients);
    }

    @GetMapping("/{id}")
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

    @GetMapping("/{id}/stock")
    public ResponseEntity<?> getStock(
            @PathVariable int id,
            @RequestParam(required = false) String at,
            @RequestParam(required = false) String unit
    ) {

        if (at == null || unit == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Either mandatory query parameter `at` or `unit` is not provided."
            );
        }

        Instant instant = Instant.parse(at);
        UnitEnum unitEnum = UnitEnum.valueOf(unit);

        StockValue stockValue = ingredientService.getStockValueAt(id, instant, unitEnum);


        return ResponseEntity
                .status(HttpStatus.OK)
                .body(stockValue);
    }

    @GetMapping("/{id}/stockMovements")
    public ResponseEntity<?> getStockMovements(
            @PathVariable int id,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to
    ) {

        if (from == null || to == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Query params 'from' and 'to' are required");
        }

        try {

            Instant fromInstant = Instant.parse(from);
            Instant toInstant = Instant.parse(to);

            List<StockMovement> result =
                    ingredientService.getStockMovementsBetweenTwoDates(id, fromInstant, toInstant);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(result);

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

    @PostMapping("/{id}/stockMovements")
    public ResponseEntity<?> createStockMovements(
            @PathVariable int id,
            @RequestBody(required = false) List<CreateStockMovement> body
    ) {

        if (body == null || body.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Request body is required");
        }

        try {

            List<StockMovement> result =
                    ingredientService.createStockMovements(id, body);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(result);

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
