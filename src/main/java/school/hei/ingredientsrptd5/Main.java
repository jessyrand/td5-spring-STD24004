import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {

        DataRetriever dataRetriever = new DataRetriever();

        List<Ingredient> ingredients = dataRetriever.findIngredients(1, 5);

        System.out.println("===== TEST STOCK TD4 =====");
        Instant t = Instant.parse("2024-01-06T12:00:00Z");

        for (Ingredient ingredient : ingredients) {

            StockValue stock = ingredient.getStockValueAt(t);

            System.out.println(
                    ingredient.getName() +
                            " | stock=" + stock.getQuantity() +
                            " " + stock.getUnit()
            );
        }
    }
}