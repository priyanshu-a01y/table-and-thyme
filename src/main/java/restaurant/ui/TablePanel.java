package restaurant.ui;

import restaurant.dao.TableDAO;
import restaurant.model.RestaurantTable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TablePanel extends JPanel {
    private static final Color PRIMARY = new Color(44, 62, 80);
    private static final Color ACCENT = new Color(52, 152, 219);
    private static final Color BG_COLOR = new Color(236, 240, 241);
    private static final Font FONT_MAIN = new Font("Segoe UI", Font.PLAIN, 14);

    private TableDAO tableDAO;
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> statusFilter;
    private JPanel floorCards;

    public TablePanel() {
        tableDAO = new TableDAO();
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Table Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 23));
        titleLabel.setForeground(PRIMARY);
        JLabel subtitle = new JLabel("A live view of your dining room"); subtitle.setForeground(UITheme.MUTED);
        JPanel titleBlock = new JPanel(); titleBlock.setOpaque(false); titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(titleLabel); titleBlock.add(Box.createVerticalStrut(4)); titleBlock.add(subtitle);
        titleBlock.setBorder(new EmptyBorder(0, 0, 17, 0)); add(titleBlock, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(BG_COLOR);

        // Top Bar
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        topBar.setBackground(BG_COLOR);

        statusFilter = new JComboBox<>(new String[]{"All", "AVAILABLE", "OCCUPIED", "RESERVED"});
        statusFilter.setFont(FONT_MAIN);
        statusFilter.addActionListener(e -> loadData());

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> loadData());

        JButton addBtn = new JButton("+ Add New");
        addBtn.setBackground(new Color(39, 174, 96));
        addBtn.setForeground(Color.WHITE);
        addBtn.addActionListener(e -> showFormDialog(null));

        JButton editBtn = new JButton("Edit");
        editBtn.setBackground(ACCENT);
        editBtn.setForeground(Color.WHITE);
        editBtn.addActionListener(e -> editSelected());

        JButton deleteBtn = new JButton("Delete");
        deleteBtn.setBackground(new Color(231, 76, 60));
        deleteBtn.setForeground(Color.WHITE);
        deleteBtn.addActionListener(e -> deleteSelected());

        topBar.add(new JLabel("Status Filter:"));
        topBar.add(statusFilter);
        topBar.add(refreshBtn);
        topBar.add(Box.createHorizontalStrut(20));
        topBar.add(addBtn);
        topBar.add(editBtn);
        topBar.add(deleteBtn);

        centerPanel.add(topBar, BorderLayout.NORTH);

        // Retain the data model for the existing selection and edit workflow; the floor is rendered as cards.
        String[] columns = {"ID", "Table Number", "Capacity", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        DashboardPanel.styleTable(table);
        table.setFont(FONT_MAIN);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);

        // Apply Custom Renderer for Status column
        table.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());

        floorCards = new JPanel(new GridLayout(0, 3, 14, 14)); floorCards.setOpaque(false);
        JPanel floor = new JPanel(new BorderLayout()); floor.setOpaque(false); floor.add(floorCards, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(floor); scrollPane.setBorder(BorderFactory.createEmptyBorder()); scrollPane.getViewport().setOpaque(false); scrollPane.setOpaque(false);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        if (floorCards != null) floorCards.removeAll();
        String filter = (String) statusFilter.getSelectedItem();

        try {
            List<RestaurantTable> tables;
            if ("All".equals(filter)) {
                tables = tableDAO.getAllTables();
            } else {
                tables = tableDAO.getTablesByStatus(filter);
            }

            if(tables != null) {
                for (RestaurantTable t : tables) {
                    Object[] row = {
                            t.getTableId(),
                            t.getTableNumber(),
                            t.getCapacity(),
                            t.getStatus().name()
                    };
                    tableModel.addRow(row);
                    floorCards.add(createTableCard(t, tableModel.getRowCount() - 1));
                }
            }
            floorCards.revalidate(); floorCards.repaint();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading tables: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createTableCard(RestaurantTable item, int row) {
        JPanel card = new JPanel(new BorderLayout(0, 14)); card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(226, 224, 218)), new EmptyBorder(17, 17, 16, 17)));
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
        String number = String.format(java.util.Locale.ROOT, "TABLE %02d", item.getTableNumber());
        JLabel tableNo = new JLabel(number); tableNo.setFont(new Font("Segoe UI", Font.BOLD, 15)); tableNo.setForeground(PRIMARY);
        String status = item.getStatus() == null ? "UNKNOWN" : item.getStatus().name();
        JLabel badge = new JLabel(status); badge.setOpaque(true); badge.setFont(new Font("Segoe UI", Font.BOLD, 10));
        Color bg = "AVAILABLE".equals(status) ? new Color(232, 244, 236) : "OCCUPIED".equals(status) ? new Color(249, 236, 224) : "RESERVED".equals(status) ? new Color(239, 235, 248) : new Color(240, 240, 240);
        Color fg = "AVAILABLE".equals(status) ? new Color(45, 127, 91) : "OCCUPIED".equals(status) ? new Color(161, 99, 38) : new Color(103, 78, 145);
        badge.setBackground(bg); badge.setForeground(fg); badge.setBorder(new EmptyBorder(5, 8, 5, 8));
        top.add(tableNo, BorderLayout.WEST); top.add(badge, BorderLayout.EAST); card.add(top, BorderLayout.NORTH);
        JLabel capacity = new JLabel(item.getCapacity() + " seats"); capacity.setFont(new Font("Segoe UI", Font.PLAIN, 13)); capacity.setForeground(UITheme.MUTED); card.add(capacity, BorderLayout.CENTER);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { table.setRowSelectionInterval(row, row); }
        });
        return card;
    }

    private void editSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) table.getValueAt(selectedRow, 0);
            try {
                RestaurantTable t = tableDAO.getTableById(id);
                if (t != null) showFormDialog(t);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error fetching table: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a table to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) table.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this table?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    if (tableDAO.deleteTable(id)) loadData();
                    else JOptionPane.showMessageDialog(this, "Table could not be deleted. It may be linked to an order.", "Delete failed", JOptionPane.ERROR_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error deleting table: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a table to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showFormDialog(RestaurantTable existingTable) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), existingTable == null ? "Add Table" : "Edit Table", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(350, 250);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 15));
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        formPanel.add(new JLabel("Table Number:"));
        JTextField numberField = new JTextField(existingTable != null ? String.valueOf(existingTable.getTableNumber()) : "");
        formPanel.add(numberField);

        formPanel.add(new JLabel("Capacity:"));
        JTextField capacityField = new JTextField(existingTable != null ? String.valueOf(existingTable.getCapacity()) : "");
        formPanel.add(capacityField);

        formPanel.add(new JLabel("Status:"));
        JComboBox<RestaurantTable.Status> statusCombo = new JComboBox<>(RestaurantTable.Status.values());
        if (existingTable != null && existingTable.getStatus() != null) {
            statusCombo.setSelectedItem(existingTable.getStatus());
        }
        formPanel.add(statusCombo);

        dialog.add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton saveBtn = new JButton("Save");
        saveBtn.setBackground(ACCENT);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            try {
                int tableNum = Integer.parseInt(numberField.getText().trim());
                int capacity = Integer.parseInt(capacityField.getText().trim());
                
                if (tableNum <= 0 || capacity <= 0) {
                    JOptionPane.showMessageDialog(dialog, "Table number and capacity must be positive integers.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check duplicate table number
                if (existingTable == null || existingTable.getTableNumber() != tableNum) {
                    if (tableDAO.isTableNumberExists(tableNum)) {
                        JOptionPane.showMessageDialog(dialog, "Table number already exists.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                RestaurantTable.Status status = (RestaurantTable.Status) statusCombo.getSelectedItem();
                RestaurantTable t = existingTable != null ? existingTable : new RestaurantTable();
                t.setTableNumber(tableNum);
                t.setCapacity(capacity);
                t.setStatus(status);

                boolean saved = existingTable == null ? tableDAO.addTable(t) : tableDAO.updateTable(t);
                if (!saved) {
                    JOptionPane.showMessageDialog(dialog, "Table could not be saved. Check the database and try again.", "Save failed", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                dialog.dispose();
                loadData();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid integers for Table Number and Capacity.", "Validation Error", JOptionPane.ERROR_MESSAGE);
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

    class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (value != null) {
                String status = value.toString();
                if (status.equals("AVAILABLE")) {
                    c.setForeground(new Color(39, 174, 96));
                } else if (status.equals("OCCUPIED")) {
                    c.setForeground(new Color(231, 76, 60));
                } else if (status.equals("RESERVED")) {
                    c.setForeground(new Color(230, 126, 34)); // Orange
                }
                if (isSelected) {
                    c.setForeground(Color.WHITE); // Keep white if selected for readability
                }
            }
            return c;
        }
    }
}
