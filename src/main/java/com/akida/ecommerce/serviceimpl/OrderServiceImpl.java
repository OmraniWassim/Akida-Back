package com.akida.ecommerce.serviceimpl;

import com.akida.ecommerce.Enumarators.OrderStatus;
import com.akida.ecommerce.exceptions.EntityNotFoundException;
import com.akida.ecommerce.models.AppUser;
import com.akida.ecommerce.models.Order;
import com.akida.ecommerce.models.OrderItem;
import com.akida.ecommerce.models.Product;
import com.akida.ecommerce.repository.AppUserRepository;
import com.akida.ecommerce.repository.OrderRepository;
import com.akida.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl {

    private final OrderRepository orderRepository;
    private final AppUserRepository userRepository;
    private final ProductRepository productRepository;
    private final WebSocketServiceImpl webSocketService;




    public Order createOrder(Order order) {
        // Validate user exists
        AppUser user = userRepository.findById(order.getAppUser().getId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // Create the order
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(order.getTotalAmount());
        order.setDeliveryAddress(order.getDeliveryAddress());
        order.setAppUser(user);

        // Create order items
        List<OrderItem> orderItems = order.getItems().stream()
                .map(itemDTO -> {
                    Product product = productRepository.findById(itemDTO.getProduct().getId())
                            .orElseThrow(() -> new EntityNotFoundException("Product not found: " + itemDTO.getProduct().getId()));

                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setProduct(product);
                    orderItem.setQuantity(itemDTO.getQuantity());
                    orderItem.setUnitPrice(itemDTO.getUnitPrice());

                    // Update product stock
                    product.setStockQuantity(product.getStockQuantity() - itemDTO.getQuantity());
                    productRepository.save(product);

                    return orderItem;
                })
                .toList();

        order.setItems(orderItems);

        orderRepository.save(order);

        //notify frontend with websocket

        notifyFrontend(order.getId());


        return order;
    }

    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));

        order.setStatus(status);
        orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }


    private void notifyFrontend(Long orderReference) {
        final String entityTopic = "commande";
        webSocketService.sendMessage(entityTopic, String.valueOf(orderReference));
    }

    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + orderId));
    }

}