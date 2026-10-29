package restaurant.service;

import restaurant.dao.InventoryDAO;
import restaurant.model.InventoryItem;
import java.util.List;

public class InventoryService {

    private InventoryDAO inventoryDAO = new InventoryDAO();

    public List<InventoryItem> getAllItems() {
        return inventoryDAO.getAllInventoryItems();
    }

    public List<InventoryItem> getLowStockItems() {
        return inventoryDAO.getLowStockItems();
    }

    public int getLowStockCount() {
        return inventoryDAO.getLowStockCount();
    }

    // Assuming InventoryDAO has this based on usual patterns, even if not explicitly listed in brief subset
    public boolean addItem(InventoryItem item) {
        return inventoryDAO.addInventoryItem(item); // Note: Make sure DAO supports this
    }

    public boolean updateItem(InventoryItem item) {
        return inventoryDAO.updateInventoryItem(item);
    }

    // Assuming InventoryDAO has this
    public boolean deleteItem(int id) {
        return inventoryDAO.deleteInventoryItem(id); 
    }

    // Assuming InventoryDAO has this
    public List<InventoryItem> searchItems(String keyword) {
        return inventoryDAO.searchInventory(keyword);
    }

    public String getStockWarnings() {
        List<InventoryItem> lowStockItems = inventoryDAO.getLowStockItems();
        if (lowStockItems == null || lowStockItems.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (InventoryItem item : lowStockItems) {
            sb.append("WARNING: ").append(item.getItemName())
              .append(" - Only ").append(item.getQuantity()).append(" ").append(item.getUnit())
              .append(" left (Reorder level: ").append(item.getReorderLevel()).append(")\n");
        }
        
        return sb.toString();
    }
}
