package restaurant.ui;

import restaurant.dao.MenuCategoryDAO;
import restaurant.dao.MenuItemDAO;
import restaurant.model.MenuCategory;
import restaurant.model.MenuItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class MenuPanel extends JPanel {
    private static final Color PRIMARY = new Color(44, 62, 80);
    private static final Color ACCENT = new Color(52, 152, 219);
    private static final Color BG_COLOR = new Color(236, 240, 241);
    private static final Font FONT_MAIN = new Font("Segoe UI", Font.PLAIN, 14);

    private MenuItemDAO menuItemDAO;
    private MenuCategoryDAO menuCategoryDAO;

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;

    public MenuPanel() {
        menuItemDAO = new MenuItemDAO();
        menuCategoryDAO = new MenuCategoryDAO();

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new JLabel("Menu Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 23));
        titleLabel.setForeground(PRIMARY);
        JLabel subtitle = new JLabel("Curate dishes, categories and availability"); subtitle.setForeground(UITheme.MUTED);
        JPanel titleBlock = new JPanel(); titleBlock.setOpaque(false); titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(titleLabel); titleBlock.add(Box.createVerticalStrut(4)); titleBlock.add(subtitle);
        titleBlock.setBorder(new EmptyBorder(0, 0, 17, 0)); add(titleBlock, BorderLayout.NORTH);

        // Center Panel (Table + Top Bar)
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(BG_COLOR);

        // Top Bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        topBar.setBackground(BG_COLOR);

        searchField = new JTextField(15);
        searchField.setFont(FONT_MAIN);
        JButton searchBtn = new JButton("Search");
        searchBtn.setBackground(ACCENT);
        searchBtn.setForeground(Color.WHITE);
        searchBtn.addActionListener(e -> loadData());

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> {
            searchField.setText("");
            loadCategoryFilter();
            loadData();
        });

        categoryFilter = new JComboBox<>();
        categoryFilter.setFont(FONT_MAIN);
        loadCategoryFilter();
        categoryFilter.addActionListener(e -> loadData());

        JButton addBtn = new JButton("+ Add New");
        addBtn.setBackground(new Color(39, 174, 96)); // Success color
        addBtn.setForeground(Color.WHITE);
        addBtn.addActionListener(e -> showFormDialog(null));

        JButton editBtn = new JButton("Edit");
        editBtn.setBackground(ACCENT);
        editBtn.setForeground(Color.WHITE);
        editBtn.addActionListener(e -> editSelected());

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setBackground(new Color(231, 76, 60)); // Warning color
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.addActionListener(e -> deleteSelected());

        topBar.add(new JLabel("Search:"));
        topBar.add(searchField);
        topBar.add(searchBtn);
        topBar.add(refreshBtn);
        topBar.add(new JLabel("Category:"));
        topBar.add(categoryFilter);
        topBar.add(Box.createHorizontalStrut(20));
        topBar.add(addBtn);
        topBar.add(editBtn);
        topBar.add(deleteBtn);

        centerPanel.add(topBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Item", "Category", "Price", "Availability", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        DashboardPanel.styleTable(table);
        table.setFont(FONT_MAIN);
        table.setRowHeight(42);
        table.getColumnModel().getColumn(0).setMinWidth(0); table.getColumnModel().getColumn(0).setMaxWidth(0); table.getColumnModel().getColumn(0).setPreferredWidth(0);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                if (me.getClickCount() == 2) {
                    editSelected();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        loadData();
    }

    private void loadCategoryFilter() {
        categoryFilter.removeAllItems();
        categoryFilter.addItem("All Categories");
        try {
            List<MenuCategory> categories = menuCategoryDAO.getAllCategories();
            if(categories != null) {
                for (MenuCategory c : categories) {
                    categoryFilter.addItem(c.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        tableModel.setRowCount(0);
        String search = searchField.getText().trim();
        String category = (String) categoryFilter.getSelectedItem();

        try {
            List<MenuItem> items;
            if (!search.isEmpty()) {
                items = menuItemDAO.searchMenuItems(search);
            } else {
                items = menuItemDAO.getAllMenuItems();
            }

            if(items != null) {
                for (MenuItem item : items) {
                    // Category filter logic
                    if (category != null && !category.equals("All Categories")) {
                        if (!category.equals(item.getCategoryName())) {
                            continue;
                        }
                    }

                    Object[] row = {
                            item.getItemId(),
                            item.getName(),
                            item.getCategoryName() != null ? item.getCategoryName() : "N/A",
                            "₹" + String.format("%.2f", item.getPrice()),
                        item.isAvailable() ? "Available" : "Unavailable",
                        "Edit  ·  Delete"
                    };
                    tableModel.addRow(row);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading menu items: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) table.getValueAt(selectedRow, 0);
            try {
                MenuItem item = menuItemDAO.getMenuItemById(id);
                if (item != null) showFormDialog(item);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error fetching item: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) table.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this menu item?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    if (menuItemDAO.deleteMenuItem(id)) loadData();
                    else JOptionPane.showMessageDialog(this, "Menu item could not be deleted. It may already be referenced by an order.", "Delete failed", JOptionPane.ERROR_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error deleting item: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showFormDialog(MenuItem existingItem) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), existingItem == null ? "Add Menu Item" : "Edit Menu Item", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 15));
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        formPanel.add(new JLabel("Name:"));
        JTextField nameField = new JTextField(existingItem != null ? existingItem.getName() : "");
        formPanel.add(nameField);

        formPanel.add(new JLabel("Category:"));
        JComboBox<MenuCategory> catCombo = new JComboBox<>();
        try {
            List<MenuCategory> cats = menuCategoryDAO.getAllCategories();
            if(cats != null) {
                for (MenuCategory c : cats) {
                    catCombo.addItem(c);
                    if (existingItem != null && c.getCategoryId() == existingItem.getCategoryId()) {
                        catCombo.setSelectedItem(c);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        formPanel.add(catCombo);

        formPanel.add(new JLabel("Price:"));
        JTextField priceField = new JTextField(existingItem != null ? String.valueOf(existingItem.getPrice()) : "");
        formPanel.add(priceField);

        formPanel.add(new JLabel("Available:"));
        JCheckBox availableCheck = new JCheckBox();
        availableCheck.setSelected(existingItem == null || existingItem.isAvailable());
        formPanel.add(availableCheck);

        formPanel.add(new JLabel("Description:"));
        JTextField descField = new JTextField(existingItem != null ? existingItem.getDescription() : "");
        formPanel.add(descField);

        dialog.add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton saveBtn = new JButton("Save");
        saveBtn.setBackground(ACCENT);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String priceStr = priceField.getText().trim();
            MenuCategory category = (MenuCategory) catCombo.getSelectedItem();
            
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (category == null) {
                JOptionPane.showMessageDialog(dialog, "Please select a category.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            double price;
            try {
                price = Double.parseDouble(priceStr);
                if (price < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Price must be a valid positive number.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            MenuItem item = existingItem != null ? existingItem : new MenuItem();
            item.setName(name);
            item.setCategoryId(category.getCategoryId());
            item.setCategoryName(category.getName());
            item.setPrice(price);
            item.setAvailable(availableCheck.isSelected());
            item.setDescription(descField.getText().trim());

            try {
                boolean saved = existingItem == null ? menuItemDAO.addMenuItem(item) : menuItemDAO.updateMenuItem(item);
                if (!saved) {
                    JOptionPane.showMessageDialog(dialog, "Menu item could not be saved. Check the database and try again.", "Save failed", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                dialog.dispose();
                loadData();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}
