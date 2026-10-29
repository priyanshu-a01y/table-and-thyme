package restaurant.dao;

import restaurant.config.DatabaseConnection;
import restaurant.model.Order;
import restaurant.model.OrderItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    public List<Order> getAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, c.name AS customer_name, t.table_number AS table_number FROM orders o LEFT JOIN customers c ON o.customer_id = c.customer_id LEFT JOIN restaurant_tables t ON o.table_id = t.table_id ORDER BY o.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                orders.add(mapResultSetToOrder(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public Order getOrderById(int orderId) {
        String sql = "SELECT o.*, c.name AS customer_name, t.table_number AS table_number FROM orders o LEFT JOIN customers c ON o.customer_id = c.customer_id LEFT JOIN restaurant_tables t ON o.table_id = t.table_id WHERE o.order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) { return rs.next() ? mapResultSetToOrder(rs) : null; }
        } catch (SQLException e) { throw new IllegalStateException("Unable to load order from the database.", e); }
    }
    public List<Order> getOrdersByCustomer(int customerId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, c.name AS customer_name, t.table_number AS table_number FROM orders o LEFT JOIN customers c ON o.customer_id = c.customer_id LEFT JOIN restaurant_tables t ON o.table_id = t.table_id WHERE o.customer_id = ? ORDER BY o.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapResultSetToOrder(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return orders;
    }

    public int createOrder(Order order, List<OrderItem> items) {
        // Keep the persistence boundary consistent with orders.customer_id NOT NULL.
        if (order == null || order.getCustomerId() <= 0 || order.getOrderType() == null
                || order.getStatus() == null || items == null || items.isEmpty()
                || (Order.OrderType.DINE_IN.equals(order.getOrderType())
                    && (order.getTableId() == null || order.getTableId() <= 0))
                || (Order.OrderType.TAKEAWAY.equals(order.getOrderType()) && order.getTableId() != null)) {
            return -1;
        }
        String insertOrderSql = "INSERT INTO orders (customer_id, table_id, order_type, status) VALUES (?, ?, ?, ?)";
        String insertItemSql = "INSERT INTO order_items (order_id, item_id, quantity, unit_price, item_total) VALUES (?, ?, ?, ?, ?)";
        String updateTableSql = "UPDATE restaurant_tables SET status = 'OCCUPIED' WHERE table_id = ?";
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            int generatedOrderId = -1;
            
            try (PreparedStatement pstmtOrder = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmtOrder.setInt(1, order.getCustomerId());
                
                if (order.getTableId() == null || order.getTableId() <= 0) {
                    pstmtOrder.setNull(2, Types.INTEGER);
                } else {
                    pstmtOrder.setInt(2, order.getTableId());
                }
                
                pstmtOrder.setString(3, order.getOrderType().name());
                pstmtOrder.setString(4, order.getStatus().name());
                
                pstmtOrder.executeUpdate();
                
                try (ResultSet rs = pstmtOrder.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedOrderId = rs.getInt(1);
                    }
                }
            }
            
            if (generatedOrderId == -1) {
                conn.rollback();
                return -1;
            }
            
            try (PreparedStatement pstmtItem = conn.prepareStatement(insertItemSql)) {
                for (OrderItem item : items) {
                    pstmtItem.setInt(1, generatedOrderId);
                    pstmtItem.setInt(2, item.getItemId());
                    pstmtItem.setInt(3, item.getQuantity());
                    pstmtItem.setDouble(4, item.getUnitPrice());
                    pstmtItem.setDouble(5, item.getItemTotal());
                    pstmtItem.addBatch();
                }
                pstmtItem.executeBatch();
            }
            
            if (Order.OrderType.DINE_IN.equals(order.getOrderType()) && order.getTableId() != null && order.getTableId() > 0) {
                try (PreparedStatement pstmtTable = conn.prepareStatement(updateTableSql)) {
                    pstmtTable.setInt(1, order.getTableId());
                    pstmtTable.executeUpdate();
                }
            }
            
            conn.commit();
            return generatedOrderId;
            
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return -1;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean updateOrderStatus(int orderId, String status) {
        String selectSql = "SELECT table_id, order_type FROM orders WHERE order_id = ?";
        String updateOrderSql = "UPDATE orders SET status = ? WHERE order_id = ?";
        String updateTableSql = "UPDATE restaurant_tables SET status = 'AVAILABLE' WHERE table_id = ?";
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            Integer tableId = null;
            String orderType = null;
            
            try (PreparedStatement pstmtSelect = conn.prepareStatement(selectSql)) {
                pstmtSelect.setInt(1, orderId);
                try (ResultSet rs = pstmtSelect.executeQuery()) {
                    if (rs.next()) {
                        tableId = rs.getInt("table_id");
                        if (rs.wasNull()) {
                            tableId = null;
                        }
                        orderType = rs.getString("order_type");
                    }
                }
            }
            
            try (PreparedStatement pstmtUpdateOrder = conn.prepareStatement(updateOrderSql)) {
                pstmtUpdateOrder.setString(1, status);
                pstmtUpdateOrder.setInt(2, orderId);
                pstmtUpdateOrder.executeUpdate();
            }
            
            if (("COMPLETED".equals(status) || "CANCELLED".equals(status)) && "DINE_IN".equals(orderType) && tableId != null) {
                try (PreparedStatement pstmtUpdateTable = conn.prepareStatement(updateTableSql)) {
                    pstmtUpdateTable.setInt(1, tableId);
                    pstmtUpdateTable.executeUpdate();
                }
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.*, mi.name AS item_name FROM order_items oi JOIN menu_items mi ON oi.item_id = mi.item_id WHERE oi.order_id = ? ORDER BY oi.order_item_id";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setOrderItemId(rs.getInt("order_item_id")); item.setOrderId(rs.getInt("order_id"));
                    item.setItemId(rs.getInt("item_id")); item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price").doubleValue());
                    item.setItemTotal(rs.getBigDecimal("item_total").doubleValue());
                    item.setItemName(rs.getString("item_name")); items.add(item);
                }
            }
            return items;
        } catch (SQLException e) { throw new IllegalStateException("Unable to load order items from the database.", e); }
    }
    public double getTodaySalesTotal() {
        String sql = "SELECT COALESCE(SUM(p.grand_total), 0) FROM payments p JOIN orders o ON p.order_id = o.order_id WHERE DATE(o.created_at) = CURDATE() AND p.status = 'PAID'";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public int getTotalOrderCount() {
        String sql = "SELECT COUNT(*) FROM orders";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Object[]> getDailySalesReport() {
        List<Object[]> report = new ArrayList<>();
        String sql = "SELECT DATE(o.created_at) as sale_date, COUNT(*) as total_orders, COALESCE(SUM(p.grand_total), 0) as total_sales FROM orders o LEFT JOIN payments p ON o.order_id = p.order_id GROUP BY DATE(o.created_at) ORDER BY sale_date DESC LIMIT 30";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                report.add(new Object[]{
                    rs.getString("sale_date"),
                    rs.getInt("total_orders"),
                    rs.getDouble("total_sales")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return report;
    }

    public List<Object[]> getMostOrderedItems() {
        List<Object[]> report = new ArrayList<>();
        String sql = "SELECT mi.name, SUM(oi.quantity) as total_qty, COUNT(DISTINCT oi.order_id) as order_count, COALESCE(SUM(oi.item_total), 0) as revenue FROM order_items oi JOIN menu_items mi ON oi.item_id = mi.item_id GROUP BY mi.name ORDER BY total_qty DESC LIMIT 10";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                report.add(new Object[]{
                    rs.getString("name"),
                    rs.getInt("total_qty"),
                    rs.getInt("order_count"),
                    rs.getDouble("revenue")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return report;
    }

    public List<Object[]> getOrderHistory() {
        List<Object[]> report = new ArrayList<>();
        String sql = "SELECT o.order_id, c.name, o.order_type, o.status, o.created_at, COALESCE(p.grand_total, 0) as total FROM orders o LEFT JOIN customers c ON o.customer_id = c.customer_id LEFT JOIN payments p ON o.order_id = p.order_id ORDER BY o.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                report.add(new Object[]{
                    rs.getInt("order_id"),
                    rs.getString("name") != null ? rs.getString("name") : "Guest",
                    rs.getString("order_type"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at"),
                    rs.getDouble("total")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return report;
    }

    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        
        int customerId = rs.getInt("customer_id");
        if (!rs.wasNull()) {
            order.setCustomerId(customerId);
        }
        
        int tableId = rs.getInt("table_id");
        if (!rs.wasNull()) {
            order.setTableId(tableId);
        }
        
        order.setOrderType(Order.OrderType.valueOf(rs.getString("order_type")));
        order.setStatus(Order.OrderStatus.valueOf(rs.getString("status")));
        order.setCreatedAt(rs.getTimestamp("created_at"));
        order.setUpdatedAt(rs.getTimestamp("updated_at"));
        order.setCustomerName(rs.getString("customer_name"));
        int tableNumber = rs.getInt("table_number");
        if (!rs.wasNull()) order.setTableNumber(tableNumber);
        return order;
    }
}
