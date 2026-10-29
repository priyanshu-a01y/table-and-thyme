package restaurant.dao;

import restaurant.config.DatabaseConnection;
import restaurant.model.MenuItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MenuItemDAO {

    public List<MenuItem> getAllMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT mi.*, mc.name AS category_name FROM menu_items mi JOIN menu_categories mc ON mi.category_id = mc.category_id";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                items.add(mapResultSetToMenuItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public MenuItem getMenuItemById(int itemId) {
        String sql = "SELECT mi.*, mc.name AS category_name FROM menu_items mi JOIN menu_categories mc ON mi.category_id = mc.category_id WHERE mi.item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMenuItem(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<MenuItem> searchMenuItems(String keyword) {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT mi.*, mc.name AS category_name FROM menu_items mi JOIN menu_categories mc ON mi.category_id = mc.category_id WHERE mi.name LIKE ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToMenuItem(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public List<MenuItem> getMenuItemsByCategory(int categoryId) {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT mi.*, mc.name AS category_name FROM menu_items mi JOIN menu_categories mc ON mi.category_id = mc.category_id WHERE mi.category_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, categoryId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(mapResultSetToMenuItem(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public List<MenuItem> getAvailableMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        String sql = "SELECT mi.*, mc.name AS category_name FROM menu_items mi JOIN menu_categories mc ON mi.category_id = mc.category_id WHERE mi.available = true";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                items.add(mapResultSetToMenuItem(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public boolean addMenuItem(MenuItem item) {
        String sql = "INSERT INTO menu_items (name, category_id, price, available, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getName());
            pstmt.setInt(2, item.getCategoryId());
            pstmt.setDouble(3, item.getPrice());
            pstmt.setBoolean(4, item.isAvailable());
            pstmt.setString(5, item.getDescription());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateMenuItem(MenuItem item) {
        String sql = "UPDATE menu_items SET name = ?, category_id = ?, price = ?, available = ?, description = ? WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getName());
            pstmt.setInt(2, item.getCategoryId());
            pstmt.setDouble(3, item.getPrice());
            pstmt.setBoolean(4, item.isAvailable());
            pstmt.setString(5, item.getDescription());
            pstmt.setInt(6, item.getItemId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteMenuItem(int itemId) {
        String sql = "DELETE FROM menu_items WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, itemId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private MenuItem mapResultSetToMenuItem(ResultSet rs) throws SQLException {
        MenuItem item = new MenuItem();
        item.setItemId(rs.getInt("item_id"));
        item.setName(rs.getString("name"));
        item.setCategoryId(rs.getInt("category_id"));
        item.setCategoryName(rs.getString("category_name"));
        item.setPrice(rs.getDouble("price"));
        item.setAvailable(rs.getBoolean("available"));
        item.setDescription(rs.getString("description"));
        return item;
    }
}
