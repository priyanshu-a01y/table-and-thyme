package restaurant.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Order {
    private int orderId;
    private int customerId;
    private String customerName;
    private Integer tableId;
    private Integer tableNumber;
    private OrderType orderType;
    private OrderStatus status;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<OrderItem> items = new ArrayList<>();

    public enum OrderType { DINE_IN, TAKEAWAY }
    public enum OrderStatus { PENDING, PREPARING, READY, COMPLETED, CANCELLED }

    public Order() {
    }

    public Order(int orderId, int customerId, String customerName, Integer tableId, OrderType orderType, OrderStatus status, Timestamp createdAt, Timestamp updatedAt, List<OrderItem> items) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.tableId = tableId;
        this.orderType = orderType;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.items = items;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Integer getTableNumber() { return tableNumber; }
    public void setTableNumber(Integer tableNumber) { this.tableNumber = tableNumber; }

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public double getSubtotal() {
        double subtotal = 0;
        if (items != null) {
            for (OrderItem item : items) {
                subtotal += item.getItemTotal();
            }
        }
        return subtotal;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId=" + orderId +
                ", customerName='" + customerName + '\'' +
                ", orderType=" + orderType +
                ", status=" + status +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}
