package school.hei.ingredientsrptd5.service;

import org.springframework.stereotype.Service;
import school.hei.ingredientsrptd5.entity.Dish;
import school.hei.ingredientsrptd5.repository.DishRepository;

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
}
