package restaurant.model;

public class RestaurantTable {
    private int tableId;
    private int tableNumber;
    private int capacity;
    private Status status;

    public enum Status { AVAILABLE, OCCUPIED, RESERVED }

    public RestaurantTable() {
    }

    public RestaurantTable(int tableId, int tableNumber, int capacity, Status status) {
        this.tableId = tableId;
        this.tableNumber = tableNumber;
        this.capacity = capacity;
        this.status = status;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Table " + tableNumber;
    }
}
