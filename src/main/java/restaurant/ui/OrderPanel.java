package restaurant.ui;

import restaurant.dao.CustomerDAO;
import restaurant.dao.MenuItemDAO;
import restaurant.dao.OrderDAO;
import restaurant.dao.TableDAO;
import restaurant.model.Customer;
import restaurant.model.MenuItem;
import restaurant.model.Order;
import restaurant.model.OrderItem;
import restaurant.model.RestaurantTable;
import restaurant.service.OrderService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class OrderPanel extends JPanel {

    private OrderService orderService;
    private CustomerDAO customerDAO;
    private TableDAO tableDAO;
    private MenuItemDAO menuItemDAO;
    private OrderDAO orderDAO;

    private JComboBox<String> orderTypeCombo;
    private JComboBox<Customer> customerCombo;
    private JComboBox<RestaurantTable> tableCombo;
    private JComboBox<MenuItem> menuItemCombo;
    private JPanel menuGrid;
    private JSpinner quantitySpinner;
    private JTable orderItemsTable;
    private DefaultTableModel orderItemsTableModel;
    private JLabel subtotalLabel;
    private List<OrderItem> currentOrderItems;

    private JTable allOrdersTable;
    private DefaultTableModel allOrdersTableModel;
    private JComboBox<String> statusFilterCombo;

    private final Color PRIMARY_COLOR = new Color(44, 62, 80);
    private final Color ACCENT_COLOR = new Color(52, 152, 219);
    private final Color BG_COLOR = new Color(236, 240, 241);

    public OrderPanel() {
        orderService = new OrderService();
        customerDAO = new CustomerDAO();
        tableDAO = new TableDAO();
        menuItemDAO = new MenuItemDAO();
        orderDAO = new OrderDAO();
        currentOrderItems = new ArrayList<>();

        setLayout(new BorderLayout(10, 10));
        setBackground(BG_COLOR);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JTabbedPane workspace = new JTabbedPane();
        workspace.addTab("Current Order", createCartPanel());
        workspace.addTab("Order History", createOrderListPanel());
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, createNewOrderPanel(), workspace);
        splitPane.setResizeWeight(0.60);
        splitPane.setDividerLocation(0.60);
        splitPane.setDividerSize(8);
        splitPane.setBorder(null);

        add(splitPane, BorderLayout.CENTER);

        loadInitialData();
    }

    private JPanel createNewOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBorder(new EmptyBorder(18, 18, 18, 14)); panel.setBackground(UITheme.CANVAS);
        JLabel heading = new JLabel("Build an order"); heading.setFont(new Font("Segoe UI", Font.BOLD, 22)); heading.setForeground(UITheme.INK);
        JLabel subheading = new JLabel("SELECT DISHES  /  TABLE SERVICE"); subheading.setForeground(UITheme.MUTED); subheading.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        JPanel title = new JPanel(); title.setOpaque(false); title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS)); title.add(heading); title.add(Box.createVerticalStrut(5)); title.add(subheading);

        orderTypeCombo = new JComboBox<>(new String[]{"DINE_IN", "TAKEAWAY"});
        customerCombo = new JComboBox<>(); tableCombo = new JComboBox<>(); menuItemCombo = new JComboBox<>();
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 99, 1));
        JPanel details = new JPanel(new GridLayout(1, 3, 8, 0)); details.setOpaque(false);
        details.add(labeledControl("SERVICE", orderTypeCombo)); details.add(labeledControl("GUEST", customerCombo)); details.add(labeledControl("TABLE", tableCombo));
        JPanel top = new JPanel(new BorderLayout(0, 12)); top.setOpaque(false); top.add(title, BorderLayout.NORTH); top.add(details, BorderLayout.CENTER);
        panel.add(top, BorderLayout.NORTH);

        JPanel menuSection = new JPanel(new BorderLayout(0, 8)); menuSection.setOpaque(false);
        JPanel menuHead = new JPanel(new BorderLayout()); menuHead.setOpaque(false);
        menuHead.add(UITheme.eyebrow("Menu · available now"), BorderLayout.WEST);
        JPanel qty = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0)); qty.setOpaque(false); qty.add(new JLabel("QTY")); qty.add(quantitySpinner); menuHead.add(qty, BorderLayout.EAST);
        menuSection.add(menuHead, BorderLayout.NORTH);
        menuGrid = new JPanel(new GridLayout(0, 2, 10, 10)); menuGrid.setBackground(UITheme.CANVAS);
        JScrollPane menuScroll = new JScrollPane(menuGrid); menuScroll.setBorder(BorderFactory.createEmptyBorder()); menuScroll.getViewport().setBackground(UITheme.CANVAS);
        menuSection.add(menuScroll, BorderLayout.CENTER); panel.add(menuSection, BorderLayout.CENTER);

        orderTypeCombo.addActionListener(e -> { boolean dineIn = "DINE_IN".equals(orderTypeCombo.getSelectedItem()); tableCombo.setEnabled(dineIn); });
        return panel;
    }

    private JPanel labeledControl(String label, JComponent control) {
        JPanel p = new JPanel(new BorderLayout(0, 4)); p.setOpaque(false); p.add(UITheme.eyebrow(label), BorderLayout.NORTH); p.add(control, BorderLayout.CENTER); return p;
    }

    private void refreshMenuCards() {
        if (menuGrid == null) return;
        menuGrid.removeAll();
        for (int i = 0; i < menuItemCombo.getItemCount(); i++) {
            MenuItem item = menuItemCombo.getItemAt(i);
            JPanel dish = new JPanel(new BorderLayout(0, 7)); dish.setBackground(UITheme.PAPER);
            dish.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UITheme.LINE), new EmptyBorder(12, 13, 12, 13)));
            JLabel name = new JLabel(item.getName()); name.setFont(new Font("Segoe UI", Font.BOLD, 14)); name.setForeground(UITheme.INK);
            JLabel category = new JLabel(item.getCategoryName() == null ? "MENU ITEM" : item.getCategoryName().toUpperCase()); category.setFont(new Font("Segoe UI", Font.PLAIN, 9)); category.setForeground(UITheme.MUTED);
            JPanel text = new JPanel(); text.setOpaque(false); text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS)); text.add(category); text.add(Box.createVerticalStrut(4)); text.add(name);
            JLabel price = new JLabel(String.format("₹%.2f", item.getPrice())); price.setFont(new Font("Segoe UI", Font.BOLD, 13)); price.setForeground(UITheme.OLIVE);
            JButton add = UITheme.button("ADD", "secondary"); add.setBorder(new EmptyBorder(6, 9, 6, 9)); add.addActionListener(e -> { menuItemCombo.setSelectedItem(item); addItemToOrder(); });
            JPanel bottom = new JPanel(new BorderLayout()); bottom.setOpaque(false); bottom.add(price, BorderLayout.WEST); bottom.add(add, BorderLayout.EAST);
            dish.add(text, BorderLayout.CENTER); dish.add(bottom, BorderLayout.SOUTH); menuGrid.add(dish);
        }
        menuGrid.revalidate(); menuGrid.repaint();
    }

    private JLabel stepLabel(String number, String text) {
        JLabel step = new JLabel("  " + number + "    " + text);
        step.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        step.setForeground(new Color(70, 86, 102));
        step.setBorder(new EmptyBorder(9, 8, 9, 8));
        step.setOpaque(true);
        step.setBackground(new Color(248, 247, 243));
        return step;
    }

    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10)); panel.setBackground(Color.WHITE); panel.setBorder(new EmptyBorder(18, 18, 18, 18));
        JLabel heading = new JLabel("Current order"); heading.setFont(new Font("Segoe UI", Font.BOLD, 20)); heading.setForeground(UITheme.INK);
        JLabel subheading = new JLabel("REVIEW  /  CONFIRM  /  SEND"); subheading.setFont(new Font("Segoe UI", Font.PLAIN, 10)); subheading.setForeground(UITheme.MUTED);
        JPanel title = new JPanel(); title.setOpaque(false); title.setLayout(new BoxLayout(title, BoxLayout.Y_AXIS)); title.add(heading); title.add(Box.createVerticalStrut(4)); title.add(subheading); panel.add(title, BorderLayout.NORTH);
        String[] cols = {"Item", "Qty", "Price", "Subtotal"};
        orderItemsTableModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int row, int column) { return false; } };
        orderItemsTable = new JTable(orderItemsTableModel); UITheme.styleTree(orderItemsTable); panel.add(new JScrollPane(orderItemsTable), BorderLayout.CENTER);
        JPanel footer = new JPanel(); footer.setOpaque(false); footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        JPanel removeRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4)); removeRow.setOpaque(false); JButton remove = UITheme.button("Remove selected", "danger"); removeRow.add(remove); footer.add(removeRow);
        subtotalLabel = new JLabel("Subtotal  ₹0.00"); subtotalLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JLabel discount = new JLabel("Discount  ₹0.00"), tax = new JLabel("Tax (5%)  ₹0.00"), total = new JLabel("Grand Total  ₹0.00");
        for (JLabel label : new JLabel[]{subtotalLabel, discount, tax}) { label.setAlignmentX(Component.RIGHT_ALIGNMENT); label.setBorder(new EmptyBorder(4, 0, 4, 4)); footer.add(label); }
        total.setFont(new Font("Segoe UI", Font.BOLD, 17)); total.setForeground(UITheme.NAVY); total.setAlignmentX(Component.RIGHT_ALIGNMENT); total.setBorder(new EmptyBorder(8, 0, 12, 4)); footer.add(total);
        JButton place = UITheme.button("PLACE ORDER", "primary"); place.setFont(new Font("Segoe UI", Font.BOLD, 14)); place.setAlignmentX(Component.RIGHT_ALIGNMENT); footer.add(place);
        remove.addActionListener(e -> removeItemFromOrder()); place.addActionListener(e -> placeOrder()); panel.add(footer, BorderLayout.SOUTH);
        panel.putClientProperty("discount", discount); panel.putClientProperty("tax", tax); panel.putClientProperty("total", total);
        return panel;
    }

    private JPanel createOrderListPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(PRIMARY_COLOR), "Order List", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 14), PRIMARY_COLOR));
        panel.setBackground(Color.WHITE);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);
        statusFilterCombo = new JComboBox<>(new String[]{"All", "PENDING", "PREPARING", "READY", "COMPLETED", "CANCELLED"});
        statusFilterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.setBackground(ACCENT_COLOR);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFocusPainted(false);
        
        topPanel.add(new JLabel("Filter Status:"));
        topPanel.add(statusFilterCombo);
        topPanel.add(refreshBtn);

        panel.add(topPanel, BorderLayout.NORTH);

        String[] cols = {"Order ID", "Customer", "Type", "Status", "Date", "Total"};
        allOrdersTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        allOrdersTable = new JTable(allOrdersTableModel);
        DashboardPanel.styleTable(allOrdersTable);
        allOrdersTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(new JScrollPane(allOrdersTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBackground(Color.WHITE);
        JButton updateStatusBtn = new JButton("Update Status");
        updateStatusBtn.setBackground(PRIMARY_COLOR);
        updateStatusBtn.setForeground(Color.WHITE);
        updateStatusBtn.setFocusPainted(false);
        JButton viewItemsBtn = new JButton("View Items");
        viewItemsBtn.setBackground(ACCENT_COLOR);
        viewItemsBtn.setForeground(Color.WHITE);
        viewItemsBtn.setFocusPainted(false);
        
        bottomPanel.add(updateStatusBtn);
        bottomPanel.add(viewItemsBtn);

        panel.add(bottomPanel, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> loadOrders());
        statusFilterCombo.addActionListener(e -> loadOrders());
        updateStatusBtn.addActionListener(e -> updateOrderStatus());
        viewItemsBtn.addActionListener(e -> viewOrderItems());

        return panel;
    }

    private void loadInitialData() {
        try {
            List<Customer> customers = customerDAO.getAllCustomers();
            for (Customer c : customers) {
                customerCombo.addItem(c);
            }

            List<RestaurantTable> tables = tableDAO.getTablesByStatus("AVAILABLE");
            for (RestaurantTable t : tables) {
                tableCombo.addItem(t);
            }

            List<MenuItem> items = menuItemDAO.getAvailableMenuItems();
            for (MenuItem m : items) {
                menuItemCombo.addItem(m);
            }
            refreshMenuCards();

            loadOrders();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadOrders() {
        try {
            allOrdersTableModel.setRowCount(0);
            List<Order> orders = orderService.getAllOrders();
            String filter = (String) statusFilterCombo.getSelectedItem();

            for (Order o : orders) {
                String actualStatus = o.getStatus().name();
                String displayStatus = "PENDING".equals(actualStatus) ? "NEW" : actualStatus;
                // "PENDING" filter option was renamed to "NEW" in the combo
                String filterMatch = "PENDING".equals(filter) ? "NEW" : filter;
                if ("All".equals(filter) || displayStatus.equals(filterMatch)) {
                    allOrdersTableModel.addRow(new Object[]{
                            o.getOrderId(),
                            o.getCustomerName(),
                            o.getOrderType(),
                            displayStatus,
                            o.getCreatedAt(),
                            String.format("₹%.2f", o.getSubtotal())
                    });
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error loading orders: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addItemToOrder() {
        MenuItem selectedItem = (MenuItem) menuItemCombo.getSelectedItem();
        if (selectedItem == null) return;

        int qty = (int) quantitySpinner.getValue();
        double price = selectedItem.getPrice();
        double total = price * qty;

        OrderItem orderItem = new OrderItem();
        orderItem.setItemId(selectedItem.getItemId());
        orderItem.setItemName(selectedItem.getName());
        orderItem.setQuantity(qty);
        orderItem.setUnitPrice(price);
        orderItem.setItemTotal(total);

        currentOrderItems.add(orderItem);
        updateOrderItemsTable();
    }

    private void removeItemFromOrder() {
        int selectedRow = orderItemsTable.getSelectedRow();
        if (selectedRow >= 0) {
            currentOrderItems.remove(selectedRow);
            updateOrderItemsTable();
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item to remove.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateOrderItemsTable() {
        orderItemsTableModel.setRowCount(0);
        double subtotal = 0;
        for (OrderItem item : currentOrderItems) {
            orderItemsTableModel.addRow(new Object[]{
                    item.getItemName(),
                    item.getQuantity(),
                    String.format("₹%.2f", item.getUnitPrice()),
                    String.format("₹%.2f", item.getItemTotal())
            });
            subtotal += item.getItemTotal();
        }
        double tax = subtotal * 0.05;
        subtotalLabel.setText(String.format("Subtotal  ₹%.2f", subtotal));
        Container parent = SwingUtilities.getAncestorOfClass(JTabbedPane.class, orderItemsTable);
        if (parent instanceof JTabbedPane tabs && tabs.getComponentCount() > 0) {
            JPanel cart = (JPanel) tabs.getComponentAt(0);
            ((JLabel) cart.getClientProperty("discount")).setText("Discount  ₹0.00");
            ((JLabel) cart.getClientProperty("tax")).setText(String.format("Tax (5%%)  ₹%.2f", tax));
            ((JLabel) cart.getClientProperty("total")).setText(String.format("Grand Total  ₹%.2f", subtotal + tax));
        }
    }

    private void placeOrder() {
        try {
            Customer customer = (Customer) customerCombo.getSelectedItem();
            if (customer == null) {
                JOptionPane.showMessageDialog(this, "Please select a customer for every order, including takeaway.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (currentOrderItems.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please add at least one item to the order.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String typeStr = (String) orderTypeCombo.getSelectedItem();
            Order.OrderType orderType = Order.OrderType.valueOf(typeStr);

            Integer tableId = null;
            if (orderType == Order.OrderType.DINE_IN) {
                RestaurantTable table = (RestaurantTable) tableCombo.getSelectedItem();
                if (table == null) {
                    JOptionPane.showMessageDialog(this, "Please select a table for Dine-In.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                tableId = table.getTableId();
            }

            Order order = new Order();
            order.setCustomerId(customer.getCustomerId());
            order.setTableId(tableId);
            order.setOrderType(orderType);
            order.setStatus(Order.OrderStatus.PENDING);

            int orderId = orderService.placeOrder(order, currentOrderItems);
            if (orderId <= 0) {
                JOptionPane.showMessageDialog(this, "The order could not be saved. Check the database connection and order details, then try again.", "Order Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JOptionPane.showMessageDialog(this, "Order placed successfully! Order ID: " + orderId, "Success", JOptionPane.INFORMATION_MESSAGE);
            
            currentOrderItems.clear();
            updateOrderItemsTable();
            quantitySpinner.setValue(1);
            loadOrders();

            // Refresh tables list if table was selected
            if (orderType == Order.OrderType.DINE_IN) {
                tableCombo.removeAllItems();
                for (RestaurantTable t : tableDAO.getTablesByStatus("AVAILABLE")) {
                    tableCombo.addItem(t);
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error placing order: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateOrderStatus() {
        int selectedRow = allOrdersTable.getSelectedRow();
        if (selectedRow >= 0) {
            int orderId = (int) allOrdersTableModel.getValueAt(selectedRow, 0);
            String displayedStatus = (String) allOrdersTableModel.getValueAt(selectedRow, 3);
            Order.OrderStatus currentStatus = "NEW".equals(displayedStatus) ? Order.OrderStatus.PENDING : Order.OrderStatus.valueOf(displayedStatus);
            
            java.util.List<Order.OrderStatus> next = switch (currentStatus) {
                case PENDING -> java.util.List.of(Order.OrderStatus.PREPARING, Order.OrderStatus.CANCELLED);
                case PREPARING -> java.util.List.of(Order.OrderStatus.READY, Order.OrderStatus.CANCELLED);
                case READY -> java.util.List.of(Order.OrderStatus.COMPLETED, Order.OrderStatus.CANCELLED);
                case COMPLETED, CANCELLED -> java.util.List.of();
            };
            if (next.isEmpty()) { JOptionPane.showMessageDialog(this, "This order is already in a final status.", "Status", JOptionPane.INFORMATION_MESSAGE); return; }
            JComboBox<Order.OrderStatus> statusCombo = new JComboBox<>(next.toArray(Order.OrderStatus[]::new));
            
            int result = JOptionPane.showConfirmDialog(this, statusCombo, "Select New Status", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                Order.OrderStatus newStatus = (Order.OrderStatus) statusCombo.getSelectedItem();
                try {
                    boolean success = orderService.updateOrderStatus(orderId, newStatus.name());
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Status updated successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                        loadOrders();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update status.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an order to update.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void viewOrderItems() {
        int selectedRow = allOrdersTable.getSelectedRow();
        if (selectedRow >= 0) {
            int orderId = (int) allOrdersTableModel.getValueAt(selectedRow, 0);
            try {
                List<OrderItem> items = orderService.getOrderItems(orderId);
                
                String[] cols = {"Item Name", "Qty", "Price", "Total"};
                DefaultTableModel model = new DefaultTableModel(cols, 0) {
                    @Override
                    public boolean isCellEditable(int row, int column) { return false; }
                };
                
                for (OrderItem item : items) {
                    model.addRow(new Object[]{
                            item.getItemName(),
                            item.getQuantity(),
                            String.format("₹%.2f", item.getUnitPrice()),
                            String.format("₹%.2f", item.getItemTotal())
                    });
                }
                
                JTable table = new JTable(model);
                table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                JScrollPane scrollPane = new JScrollPane(table);
                scrollPane.setPreferredSize(new Dimension(400, 200));
                
                JOptionPane.showMessageDialog(this, scrollPane, "Items for Order #" + orderId, JOptionPane.PLAIN_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error loading items: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an order to view items.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
}

