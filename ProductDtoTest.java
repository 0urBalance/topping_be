import org.balanceus.topping.application.dto.ProductRequestDto;
import java.math.BigDecimal;
import java.util.UUID;

public class ProductDtoTest {
    public static void main(String[] args) {
        ProductRequestDto dto = new ProductRequestDto();
        dto.setCategory("signature");
        
        System.out.println("Category: " + dto.getCategory());
        System.out.println("ProductType: " + dto.getProductType());
        System.out.println("ProductCategory: " + dto.getProductCategory());
        
        // Test with other categories
        dto.setCategory("cafe");
        System.out.println("\nCafe - ProductCategory: " + dto.getProductCategory());
        
        dto.setCategory("food");
        System.out.println("Food - ProductCategory: " + dto.getProductCategory());
        
        dto.setCategory("unknown");
        System.out.println("Unknown - ProductCategory: " + dto.getProductCategory());
    }
}
EOF < /dev/null
