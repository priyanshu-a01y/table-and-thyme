package restaurant.model;

import java.sql.Timestamp;

public class InventoryItem {
    private int inventoryId;
    private String itemName;
    private int quantity;
    private String unit;
    private int reorderLevel;
    private Timestamp lastUpdated;

    public InventoryItem() {
    }

    public InventoryItem(int inventoryId, String itemName, int quantity, String unit, int reorderLevel, Timestamp lastUpdated) {
        this.inventoryId = inventoryId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unit = unit;
        this.reorderLevel = reorderLevel;
        this.lastUpdated = lastUpdated;
    }

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(int reorderLevel) {
        this.reorderLevel = reorderLevel;
    }

    public Timestamp getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Timestamp lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isLowStock() {
        return quantity < reorderLevel;
    }

    @Override
    public String toString() {
        return "InventoryItem{" +
                "itemName='" + itemName + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                ", reorderLevel=" + reorderLevel +
                '}';
    }
}
