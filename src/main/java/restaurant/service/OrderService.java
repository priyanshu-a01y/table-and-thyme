package restaurant.service;

import restaurant.dao.OrderDAO;
import restaurant.dao.TableDAO;
import restaurant.model.*;
import java.util.List;

public class OrderService {

    private OrderDAO orderDAO = new OrderDAO();
    private TableDAO tableDAO = new TableDAO(); // Included as per specification

    public int placeOrder(Order order, List<OrderItem> items) {
        if (order == null) {
            throw new IllegalArgumentException("An order is required.");
        }
        if (order.getCustomerId() <= 0) {
            throw new IllegalArgumentException("Select a customer for every order, including takeaway.");
        }
        if (order.getOrderType() == null) {
            throw new IllegalArgumentException("Select an order type.");
        }
        if (order.getOrderType() == Order.OrderType.DINE_IN
                && (order.getTableId() == null || order.getTableId() <= 0)) {
            throw new IllegalArgumentException("Select a table for dine-in orders.");
        }
        if (order.getOrderType() == Order.OrderType.TAKEAWAY && order.getTableId() != null) {
            throw new IllegalArgumentException("Takeaway orders cannot be assigned to a table.");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Add at least one item to the order.");
        }
        return orderDAO.createOrder(order, items);
    }

    public boolean updateOrderStatus(int orderId, String newStatus) {
        Order order = orderDAO.getOrderById(orderId);
        if (order == null || newStatus == null) return false;
        Order.OrderStatus target;
        try {
            target = Order.OrderStatus.valueOf(newStatus);
        } catch (IllegalArgumentException ex) {
            return false;
        }
        boolean allowed = switch (order.getStatus()) {
            case PENDING -> target == Order.OrderStatus.PREPARING || target == Order.OrderStatus.CANCELLED;
            case PREPARING -> target == Order.OrderStatus.READY || target == Order.OrderStatus.CANCELLED;
            case READY -> target == Order.OrderStatus.COMPLETED || target == Order.OrderStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
        if (!allowed) return false;
        return orderDAO.updateOrderStatus(orderId, newStatus);
    }

    public Order getOrderById(int orderId) {
        Order order = orderDAO.getOrderById(orderId);
        if (order != null) {
            List<OrderItem> items = orderDAO.getOrderItems(orderId);
            order.setItems(items);
        }
        return order;
    }

    public List<Order> getAllOrders() {
        return orderDAO.getAllOrders();
    }

    public List<Order> getOrdersByCustomer(int customerId) {
        return orderDAO.getOrdersByCustomer(customerId);
    }

    public List<OrderItem> getOrderItems(int orderId) {
        return orderDAO.getOrderItems(orderId);
    }
}
