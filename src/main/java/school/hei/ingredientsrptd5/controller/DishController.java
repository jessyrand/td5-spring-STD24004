package school.hei.ingredientsrptd5.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
