package restaurant.ui;

import restaurant.dao.InventoryDAO;
import restaurant.model.InventoryItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class InventoryPanel extends JPanel {

    private InventoryDAO inventoryDAO;
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JToggleButton lowStockToggle;
    private JLabel warningLabel;
    private JLabel totalItemsValue, lowStockValue, outOfStockValue;

    private final Color PRIMARY_COLOR = new Color(44, 62, 80);
    private final Color ACCENT_COLOR = new Color(52, 152, 219);
    private final Color BG_COLOR = new Color(236, 240, 241);

    public InventoryPanel() {
        inventoryDAO = new InventoryDAO();

        setLayout(new BorderLayout(10, 10));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Inventory");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 23));
        titleLabel.setForeground(PRIMARY_COLOR);
        JLabel subtitle = new JLabel("Stock control and ingredient overview"); subtitle.setForeground(UITheme.MUTED);

        warningLabel = new JLabel("");
        warningLabel.setForeground(new Color(231, 76, 60));
        warningLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JPanel titleBlock = new JPanel(); titleBlock.setOpaque(false); titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(titleLabel); titleBlock.add(Box.createVerticalStrut(4)); titleBlock.add(subtitle);
        titlePanel.add(titleBlock, BorderLayout.WEST);
        titlePanel.add(warningLabel, BorderLayout.EAST);
        add(titlePanel, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 14)); content.setOpaque(false);
        content.add(createSummaryCards(), BorderLayout.NORTH); content.add(createMainPanel(), BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);

        loadInventoryData();
        checkLowStockWarning();
    }

    private JPanel createSummaryCards() {
        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0)); cards.setOpaque(false);
        totalItemsValue = new JLabel("—"); lowStockValue = new JLabel("—"); outOfStockValue = new JLabel("—");
        cards.add(summaryCard("Total Items", totalItemsValue)); cards.add(summaryCard("Low Stock", lowStockValue)); cards.add(summaryCard("Out of Stock", outOfStockValue));
        return cards;
    }

    private JPanel summaryCard(String title, JLabel value) {
        JPanel card = new JPanel(new BorderLayout(0, 8)); card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UITheme.LINE), new EmptyBorder(13, 16, 13, 16)));
        JLabel caption = new JLabel(title); caption.setFont(new Font("Segoe UI", Font.BOLD, 11)); caption.setForeground(UITheme.MUTED);
        value.setFont(new Font("Segoe UI", Font.BOLD, 21)); value.setForeground(UITheme.NAVY);
        card.add(caption, BorderLayout.NORTH); card.add(value, BorderLayout.CENTER); return card;
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topBar.setBackground(Color.WHITE);

        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JButton searchBtn = new JButton("Search");
        searchBtn.setBackground(PRIMARY_COLOR);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.setFocusPainted(false);
        
        JButton addBtn = new JButton("Add New Item");
        addBtn.setBackground(new Color(39, 174, 96));
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        
        lowStockToggle = new JToggleButton("Show Low Stock");
        lowStockToggle.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        topBar.add(new JLabel("Search:"));
        topBar.add(searchField);
        topBar.add(searchBtn);
        topBar.add(addBtn);
        topBar.add(lowStockToggle);

        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"ID", "Ingredient", "Current Stock", "Unit", "Minimum Stock", "Status", "Actions"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        inventoryTable = new JTable(tableModel);
        DashboardPanel.styleTable(inventoryTable);
        inventoryTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        inventoryTable.setRowHeight(25);

        inventoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (String) tableModel.getValueAt(row, 5);
                if (!isSelected) {
                    if ("LOW STOCK".equals(status)) {
                        c.setBackground(new Color(255, 204, 204)); 
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                if (column == 5) {
                    if ("LOW STOCK".equals(status) || "OUT OF STOCK".equals(status)) {
                        c.setForeground(new Color(231, 76, 60));
                        setFont(new Font("Segoe UI", Font.BOLD, 12));
                    } else {
                        c.setForeground(new Color(39, 174, 96));
                        setFont(new Font("Segoe UI", Font.BOLD, 12));
                    }
                } else {
                    c.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                    setFont(new Font("Segoe UI", Font.PLAIN, 12));
                }
                return c;
            }
        });
        inventoryTable.getColumnModel().getColumn(0).setMinWidth(0); inventoryTable.getColumnModel().getColumn(0).setMaxWidth(0); inventoryTable.getColumnModel().getColumn(0).setPreferredWidth(0);

        panel.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.setBackground(Color.WHITE);
        
        JButton editBtn = new JButton("Edit");
        editBtn.setBackground(ACCENT_COLOR);
        editBtn.setForeground(Color.WHITE);
        editBtn.setFocusPainted(false);
        
        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setBackground(new Color(231, 76, 60));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.setFocusPainted(false);
        
        bottomBar.add(editBtn);
        bottomBar.add(deleteBtn);

        panel.add(bottomBar, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> loadInventoryData());
        searchField.addActionListener(e -> loadInventoryData());
        lowStockToggle.addActionListener(e -> loadInventoryData());
        addBtn.addActionListener(e -> showItemDialog(null));
        editBtn.addActionListener(e -> editSelectedItem());
        deleteBtn.addActionListener(e -> deleteSelectedItem());

        return panel;
    }

    private void loadInventoryData() {
        try {
            tableModel.setRowCount(0);
            String keyword = searchField.getText().trim();
            boolean showOnlyLowStock = lowStockToggle.isSelected();
            
            List<InventoryItem> items;
            
            if (showOnlyLowStock) {
                items = inventoryDAO.getLowStockItems();
            } else if (!keyword.isEmpty()) {
                items = inventoryDAO.searchInventory(keyword);
            } else {
                items = inventoryDAO.getAllInventoryItems();
            }

            for (InventoryItem item : items) {
                String status = item.getQuantity() <= 0 ? "OUT OF STOCK" : item.isLowStock() ? "LOW STOCK" : "IN STOCK";
                tableModel.addRow(new Object[]{
                        item.getInventoryId(),
                        item.getItemName(),
                        item.getQuantity(),
                        item.getUnit(),
                        item.getReorderLevel(),
                        status,
                        "Edit  ·  Delete"
                });
            }
            List<InventoryItem> allItems = inventoryDAO.getAllInventoryItems();
            totalItemsValue.setText(String.valueOf(allItems.size()));
            lowStockValue.setText(String.valueOf(allItems.stream().filter(i -> i.getQuantity() > 0 && i.isLowStock()).count()));
            outOfStockValue.setText(String.valueOf(allItems.stream().filter(i -> i.getQuantity() <= 0).count()));
            checkLowStockWarning();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading inventory: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void checkLowStockWarning() {
        try {
            int count = inventoryDAO.getLowStockCount();
            if (count > 0) {
                warningLabel.setText("⚠ Warning: " + count + " items are low in stock!");
            } else {
                warningLabel.setText("");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void editSelectedItem() {
        int row = inventoryTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = (int) tableModel.getValueAt(row, 0);
            List<InventoryItem> items = inventoryDAO.getAllInventoryItems();
            InventoryItem toEdit = items.stream().filter(i -> i.getInventoryId() == id).findFirst().orElse(null);
            if (toEdit != null) {
                showItemDialog(toEdit);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching item: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedItem() {
        int row = inventoryTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        
        int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete '" + name + "'?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            try {
                boolean success = inventoryDAO.deleteInventoryItem(id);
                if (success) {
                    JOptionPane.showMessageDialog(this, "Item deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    loadInventoryData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete item.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting item: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showItemDialog(InventoryItem item) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), item == null ? "Add Inventory Item" : "Edit Inventory Item", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(350, 250);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.setBorder(new EmptyBorder(15, 15, 15, 15));

        JTextField nameField = new JTextField(item != null ? item.getItemName() : "");
        JTextField qtyField = new JTextField(item != null ? String.valueOf(item.getQuantity()) : "0");
        String[] units = {"kg", "liters", "pieces", "packets", "bottles", "dozen"};
        JComboBox<String> unitCombo = new JComboBox<>(units);
        if (item != null) unitCombo.setSelectedItem(item.getUnit());
        JTextField reorderField = new JTextField(item != null ? String.valueOf(item.getReorderLevel()) : "10");

        form.add(new JLabel("Item Name:"));
        form.add(nameField);
        form.add(new JLabel("Quantity:"));
        form.add(qtyField);
        form.add(new JLabel("Unit:"));
        form.add(unitCombo);
        form.add(new JLabel("Reorder Level:"));
        form.add(reorderField);

        dialog.add(form, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveBtn = new JButton("Save");
        saveBtn.setBackground(PRIMARY_COLOR);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        JButton cancelBtn = new JButton("Cancel");
        
        saveBtn.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Item name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int qty = Integer.parseInt(qtyField.getText().trim());
                if (qty < 0) {
                    JOptionPane.showMessageDialog(dialog, "Quantity cannot be negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int reorder = Integer.parseInt(reorderField.getText().trim());
                if (reorder < 0) {
                    JOptionPane.showMessageDialog(dialog, "Reorder level cannot be negative.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                InventoryItem newItem = new InventoryItem();
                newItem.setItemName(name);
                newItem.setQuantity(qty);
                newItem.setUnit((String) unitCombo.getSelectedItem());
                newItem.setReorderLevel(reorder);
                
                boolean success;
                if (item == null) {
                    success = inventoryDAO.addInventoryItem(newItem);
                } else {
                    newItem.setInventoryId(item.getInventoryId());
                    success = inventoryDAO.updateInventoryItem(newItem);
                }
                
                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Item saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadInventoryData();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to save item.", "Error", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid number format for quantity or reorder level.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelBtn.addActionListener(e -> dialog.dispose());
        
        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}
