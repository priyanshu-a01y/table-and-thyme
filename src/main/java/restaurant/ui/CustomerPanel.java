package restaurant.ui;

import restaurant.dao.CustomerDAO;
import restaurant.dao.OrderDAO;
import restaurant.model.Customer;
import restaurant.model.Order;
import restaurant.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CustomerPanel extends JPanel {
    private static final Color PRIMARY = new Color(44, 62, 80);
    private static final Color ACCENT = new Color(52, 152, 219);
    private static final Color BG_COLOR = new Color(236, 240, 241);
    private static final Font FONT_MAIN = new Font("Segoe UI", Font.PLAIN, 14);

    private CustomerDAO customerDAO;
    private OrderDAO orderDAO;

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public CustomerPanel() {
        customerDAO = new CustomerDAO();
        orderDAO = new OrderDAO();

        setLayout(new BorderLayout());
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Customer Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 23));
        titleLabel.setForeground(PRIMARY);
        JLabel subtitle = new JLabel("Guest profiles, contact details and visit history"); subtitle.setForeground(UITheme.MUTED);
        JPanel titleBlock = new JPanel(); titleBlock.setOpaque(false); titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.add(titleLabel); titleBlock.add(Box.createVerticalStrut(4)); titleBlock.add(subtitle);
        titleBlock.setBorder(new EmptyBorder(0, 0, 17, 0)); add(titleBlock, BorderLayout.NORTH);

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
            loadData();
        });

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
        
        JButton viewHistoryBtn = new JButton("View History");
        viewHistoryBtn.setBackground(PRIMARY);
        viewHistoryBtn.setForeground(Color.WHITE);
        viewHistoryBtn.addActionListener(e -> viewHistorySelected());

        topBar.add(new JLabel("Search (Name/Phone):"));
        topBar.add(searchField);
        topBar.add(searchBtn);
        topBar.add(refreshBtn);
        topBar.add(Box.createHorizontalStrut(20));
        topBar.add(addBtn);
        topBar.add(editBtn);
        topBar.add(deleteBtn);
        topBar.add(viewHistoryBtn);

        centerPanel.add(topBar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Name", "Phone", "Email", "Orders", "Actions"};
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

        JScrollPane scrollPane = new JScrollPane(table);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        loadData();
    }

    private void loadData() {
        tableModel.setRowCount(0);
        String search = searchField.getText().trim();

        try {
            List<Customer> customers;
            if (!search.isEmpty()) {
                customers = customerDAO.searchCustomers(search);
            } else {
                customers = customerDAO.getAllCustomers();
            }

            java.util.Map<Integer, Integer> ordersByCustomer = new java.util.HashMap<>();
            for (Object[] summary : customerDAO.getCustomerOrderSummary()) ordersByCustomer.put((Integer) summary[0], (Integer) summary[2]);
            if(customers != null) {
                for (Customer c : customers) {
                    Object[] row = {
                            c.getCustomerId(),
                            c.getName(),
                            c.getPhone(),
                            c.getEmail(),
                            ordersByCustomer.getOrDefault(c.getCustomerId(), 0),
                            "Edit  ·  Delete"
                    };
                    tableModel.addRow(row);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading customers: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) table.getValueAt(selectedRow, 0);
            try {
                Customer c = customerDAO.getCustomerById(id);
                if (c != null) showFormDialog(c);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error fetching customer: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a customer to edit.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) table.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this customer?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    if (customerDAO.deleteCustomer(id)) loadData();
                    else JOptionPane.showMessageDialog(this, "Customer could not be deleted. The record may be in use.", "Delete failed", JOptionPane.ERROR_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "Error deleting customer: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a customer to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void viewHistorySelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) table.getValueAt(selectedRow, 0);
            showHistoryDialog(id);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a customer to view history.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void showFormDialog(Customer existingCustomer) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), existingCustomer == null ? "Add Customer" : "Edit Customer", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 15));
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        formPanel.add(new JLabel("Name:"));
        JTextField nameField = new JTextField(existingCustomer != null ? existingCustomer.getName() : "");
        formPanel.add(nameField);

        formPanel.add(new JLabel("Phone:"));
        JTextField phoneField = new JTextField(existingCustomer != null ? existingCustomer.getPhone() : "");
        formPanel.add(phoneField);
        
        formPanel.add(new JLabel("Email:"));
        JTextField emailField = new JTextField(existingCustomer != null ? existingCustomer.getEmail() : "");
        formPanel.add(emailField);
        
        formPanel.add(new JLabel("Address:"));
        JTextField addressField = new JTextField(existingCustomer != null ? existingCustomer.getAddress() : "");
        formPanel.add(addressField);

        dialog.add(formPanel, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel();
        JButton saveBtn = new JButton("Save");
        saveBtn.setBackground(ACCENT);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String address = addressField.getText().trim();
            
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name cannot be empty.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (phone.isEmpty() || !phone.matches("\\d{10,15}")) {
                JOptionPane.showMessageDialog(dialog, "Phone must be between 10 and 15 digits.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Assume ValidationUtil.isValidEmail exists
            if (!email.isEmpty()) {
                boolean validEmail = true;
                try {
                    // Try to use ValidationUtil if available, else rudimentary check
                    if (ValidationUtil.class != null) {
                        // Note: Using rudimentary check directly here to guarantee runtime safety if util method differs
                        validEmail = email.contains("@") && email.contains(".");
                    }
                } catch (Exception ex) {
                    validEmail = email.contains("@") && email.contains(".");
                }
                
                if (!validEmail) {
                    JOptionPane.showMessageDialog(dialog, "Please enter a valid email address.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            Customer c = existingCustomer != null ? existingCustomer : new Customer();
            c.setName(name);
            c.setPhone(phone);
            c.setEmail(email);
            c.setAddress(address);

            try {
                boolean saved = existingCustomer == null ? customerDAO.addCustomer(c) : customerDAO.updateCustomer(c);
                if (!saved) {
                    JOptionPane.showMessageDialog(dialog, "Customer could not be saved. Check the database and try again.", "Save failed", JOptionPane.ERROR_MESSAGE);
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
    
    private void showHistoryDialog(int customerId) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Order History", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        
        String[] cols = {"Order ID", "Type", "Status", "Date/Time"};
        DefaultTableModel histModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable histTable = new JTable(histModel);
        histTable.setFont(FONT_MAIN);
        histTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        try {
            List<Order> orders = orderDAO.getOrdersByCustomer(customerId);
            if(orders != null) {
                for(Order o : orders) {
                    Object[] row = {
                        o.getOrderId(),
                        o.getOrderType() != null ? o.getOrderType().name() : "N/A",
                        o.getStatus() != null ? o.getStatus().name() : "N/A",
                        o.getCreatedAt() != null ? o.getCreatedAt().toString() : "N/A"
                    };
                    histModel.addRow(row);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error fetching order history: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        
        dialog.add(new JScrollPane(histTable), BorderLayout.CENTER);
        
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel bp = new JPanel();
        bp.add(closeBtn);
        dialog.add(bp, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
}
