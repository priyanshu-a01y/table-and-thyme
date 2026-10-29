package restaurant.dao;

import restaurant.config.DatabaseConnection;
import restaurant.model.InventoryItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {

    public List<InventoryItem> getAllInventoryItems() {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT * FROM inventory";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                items.add(mapResultSetToInventoryItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public InventoryItem getInventoryItemById(int inventoryId) {
        String sql = "SELECT * FROM inventory WHERE inventory_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, inventoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToInventoryItem(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<InventoryItem> searchInventory(String keyword) {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT * FROM inventory WHERE item_name LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToInventoryItem(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public boolean addInventoryItem(InventoryItem item) {
        String sql = "INSERT INTO inventory (item_name, quantity, unit, reorder_level) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getItemName());
            pstmt.setInt(2, item.getQuantity());
            pstmt.setString(3, item.getUnit());
            pstmt.setInt(4, item.getReorderLevel());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateInventoryItem(InventoryItem item) {
        String sql = "UPDATE inventory SET item_name = ?, quantity = ?, unit = ?, reorder_level = ? WHERE inventory_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getItemName());
            pstmt.setInt(2, item.getQuantity());
            pstmt.setString(3, item.getUnit());
            pstmt.setInt(4, item.getReorderLevel());
            pstmt.setInt(5, item.getInventoryId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteInventoryItem(int inventoryId) {
        String sql = "DELETE FROM inventory WHERE inventory_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, inventoryId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<InventoryItem> getLowStockItems() {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT * FROM inventory WHERE quantity < reorder_level";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                items.add(mapResultSetToInventoryItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public int getLowStockCount() {
        String sql = "SELECT COUNT(*) FROM inventory WHERE quantity < reorder_level";
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

    private InventoryItem mapResultSetToInventoryItem(ResultSet rs) throws SQLException {
        InventoryItem item = new InventoryItem();
        item.setInventoryId(rs.getInt("inventory_id"));
        item.setItemName(rs.getString("item_name"));
        item.setQuantity(rs.getInt("quantity"));
        item.setUnit(rs.getString("unit"));
        item.setReorderLevel(rs.getInt("reorder_level"));
        item.setLastUpdated(rs.getTimestamp("last_updated"));
        return item;
    }
}
