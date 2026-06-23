package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.enums.ProductType;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

import java.time.LocalDate;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository ;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public void validateData(Long orderId){
        if(Objects.isNull(orderId) || orderId < 0){
            throw new IllegalArgumentException("id number not valid");
        }
    }

    @Transactional
    public ProcessOrderResponse processOrder(Long orderId){

        validateData(orderId);

        Order order = orderRepository.findById(orderId).orElseThrow(() -> new IllegalArgumentException("Order not found"));

        for (Product product : order.getItems()) {
            processProduct(product);
        }

        return new ProcessOrderResponse(order.getId());

    }

    public void processProduct(Product product){
        switch (product.getType()){
            case ProductType.NORMAL:
                processNormalProduct(product);
                break;
            case ProductType.SEASONAL:
                processSeasonalProduct(product);
                break;
            case ProductType.EXPIRABLE:
                processExpirableProduct(product);
                break;
            default:
                throw new IllegalArgumentException("product type: " + product.getType() + "not supported");
        }
    }

    public void processNormalProduct(Product product){
        if (product.getAvailable() > 0) {
            decreaseAvailability(product);
        } else {
            int leadTime = product.getLeadTime();
            if (leadTime > 0) {
                productService.notifyDelay(leadTime, product);
            }
        }
    }

    public void processSeasonalProduct(Product product){
        if ((LocalDate.now().isAfter(product.getSeasonStartDate()) && LocalDate.now().isBefore(product.getSeasonEndDate())
                && product.getAvailable() > 0)) {
            decreaseAvailability(product);
        } else {
            productService.handleSeasonalProduct(product);
        }
    }

    public void processExpirableProduct(Product product){
        if (product.getAvailable() > 0 && product.getExpiryDate().isAfter(LocalDate.now())) {
            decreaseAvailability(product);
        } else {
            productService.handleExpiredProduct(product);
        }
    }

    public void decreaseAvailability(Product product){
        product.setAvailable(product.getAvailable() - 1);
        productRepository.save(product);
    }

}
