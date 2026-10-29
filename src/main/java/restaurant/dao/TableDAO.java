package restaurant.dao;

import restaurant.config.DatabaseConnection;
import restaurant.model.RestaurantTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TableDAO {

    public List<RestaurantTable> getAllTables() {
        List<RestaurantTable> tables = new ArrayList<>();
        String sql = "SELECT * FROM restaurant_tables";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                tables.add(mapResultSetToTable(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    public RestaurantTable getTableById(int tableId) {
        String sql = "SELECT * FROM restaurant_tables WHERE table_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tableId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToTable(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<RestaurantTable> getTablesByStatus(String status) {
        List<RestaurantTable> tables = new ArrayList<>();
        String sql = "SELECT * FROM restaurant_tables WHERE status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tables.add(mapResultSetToTable(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }

    public boolean addTable(RestaurantTable table) {
        String sql = "INSERT INTO restaurant_tables (table_number, capacity, status) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, table.getTableNumber());
            pstmt.setInt(2, table.getCapacity());
            pstmt.setString(3, table.getStatus().name());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTable(RestaurantTable table) {
        String sql = "UPDATE restaurant_tables SET table_number = ?, capacity = ?, status = ? WHERE table_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, table.getTableNumber());
            pstmt.setInt(2, table.getCapacity());
            pstmt.setString(3, table.getStatus().name());
            pstmt.setInt(4, table.getTableId());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTable(int tableId) {
        String sql = "DELETE FROM restaurant_tables WHERE table_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tableId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isTableNumberExists(int tableNumber) {
        String sql = "SELECT COUNT(*) FROM restaurant_tables WHERE table_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, tableNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int getAvailableTableCount() {
        String sql = "SELECT COUNT(*) FROM restaurant_tables WHERE status = 'AVAILABLE'";
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

    public int getOccupiedTableCount() {
        String sql = "SELECT COUNT(*) FROM restaurant_tables WHERE status = 'OCCUPIED'";
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

    public boolean updateTableStatus(int tableId, String status) {
        String sql = "UPDATE restaurant_tables SET status = ? WHERE table_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, tableId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private RestaurantTable mapResultSetToTable(ResultSet rs) throws SQLException {
        RestaurantTable table = new RestaurantTable();
        table.setTableId(rs.getInt("table_id"));
        table.setTableNumber(rs.getInt("table_number"));
        table.setCapacity(rs.getInt("capacity"));
        table.setStatus(RestaurantTable.Status.valueOf(rs.getString("status")));
        return table;
    }
}
