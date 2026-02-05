package com.akida.ecommerce.controller;

import com.akida.ecommerce.Enumarators.OrderStatus;
import com.akida.ecommerce.models.Order;
import com.akida.ecommerce.serviceimpl.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/secured/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderServiceImpl orderService;



    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestBody Order orderDTO) {
        Order createdOrder = orderService.createOrder(orderDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    //get order by id
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        if (order != null) {
            return ResponseEntity.ok(order);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    //update order status
    @PutMapping("/{id}/{status}")
    public ResponseEntity<OrderStatus> updateOrderStatus(@PathVariable Long id, @PathVariable OrderStatus status) {
        orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(status);

    }

    //get all orders
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

//    @GetMapping("/user/{userId}")
//    public ResponseEntity<List<Order>> getUserOrders(@PathVariable Long userId) {
//        List<Order> orders = orderService.getOrdersByUser(userId);
//        return ResponseEntity.ok(orders);
//    }
}
