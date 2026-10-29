package restaurant.ui;

import restaurant.dao.CustomerDAO;
import restaurant.dao.InventoryDAO;
import restaurant.dao.OrderDAO;
import restaurant.dao.PaymentDAO;
import restaurant.model.Customer;
import restaurant.model.InventoryItem;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;

/** A compact report workspace with a persistent report navigator and database-backed views. */
public class ReportsPanel extends JPanel {
    private static final String[] REPORTS = {"Order History", "Daily Sales", "Popular Items", "Payment Summary", "Inventory Status", "Customers"};
    private final OrderDAO orderDAO = new OrderDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();
    private final CustomerDAO customerDAO = new CustomerDAO();
    private final JPanel view = new JPanel(new CardLayout());
    private final JPanel navigator = new JPanel();
    private final JLabel heading = new JLabel("Order History");
    private final JLabel description = new JLabel("A clear view of restaurant activity and performance");
    private final NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public ReportsPanel() {
        setLayout(new BorderLayout(18, 18));
        setBackground(UITheme.CANVAS);
        setBorder(new EmptyBorder(24, 28, 26, 28));

        JPanel header = new JPanel(new BorderLayout(12, 8));
        header.setOpaque(false);
        JPanel titles = new JPanel(); titles.setOpaque(false); titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        heading.setFont(new Font("Segoe UI", Font.BOLD, 23)); heading.setForeground(UITheme.INK);
        description.setFont(new Font("Segoe UI", Font.PLAIN, 13)); description.setForeground(UITheme.MUTED);
        titles.add(heading); titles.add(Box.createVerticalStrut(4)); titles.add(description);
        JButton refresh = UITheme.button("Refresh report", "secondary");
        refresh.addActionListener(e -> showReport(selected));
        header.add(titles, BorderLayout.WEST); header.add(refresh, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        navigator.setLayout(new BoxLayout(navigator, BoxLayout.Y_AXIS));
        navigator.setBackground(Color.WHITE);
        navigator.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UITheme.LINE), new EmptyBorder(12, 10, 12, 10)));
        JPanel body = new JPanel(new BorderLayout(16, 0)); body.setOpaque(false);
        body.add(navigator, BorderLayout.WEST);
        view.setOpaque(false); body.add(view, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);

        for (String name : REPORTS) {
            JPanel placeholder = reportCard();
            view.add(placeholder, name);
            JButton item = new JButton(name);
            item.setHorizontalAlignment(SwingConstants.LEFT); item.setFont(new Font("Segoe UI", Font.BOLD, 12));
            item.setFocusPainted(false); item.setBorder(new EmptyBorder(12, 12, 12, 12));
            item.setMaximumSize(new Dimension(190, 44)); item.setAlignmentX(Component.LEFT_ALIGNMENT);
            item.addActionListener(e -> showReport(name)); navigator.add(item); navigator.add(Box.createVerticalStrut(4));
        }
        navigator.setPreferredSize(new Dimension(190, 0));
        showReport(REPORTS[0]);
    }

    private String selected = REPORTS[0];

    private void showReport(String name) {
        selected = name;
        heading.setText(name);
        description.setText(switch (name) {
            case "Order History" -> "A complete record of recent dining and takeaway orders";
            case "Daily Sales" -> "Daily revenue and order volume at a glance";
            case "Popular Items" -> "Menu items guests order most often";
            case "Payment Summary" -> "Payments grouped by tender method";
            case "Inventory Status" -> "Ingredient levels against their reorder thresholds";
            default -> "Your customer directory and contact details";
        });
        for (Component component : navigator.getComponents()) {
            if (component instanceof JButton button) {
                boolean active = name.equals(button.getText());
                button.setBackground(active ? new Color(246, 241, 231) : Color.WHITE);
                button.setForeground(active ? UITheme.NAVY : UITheme.MUTED);
                button.setOpaque(true);
            }
        }
        JPanel card = reportCard();
        try {
            if ("Daily Sales".equals(name)) buildDailySales(card);
            else buildTableReport(card, name);
        } catch (RuntimeException ex) {
            card.add(emptyState("Report unavailable", "Check the database connection, then refresh this report."), BorderLayout.CENTER);
            ex.printStackTrace();
        }
        view.removeAll();
        view.add(card, name);
        ((CardLayout) view.getLayout()).show(view, name);
        view.revalidate(); view.repaint();
    }

    private JPanel reportCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14)); card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UITheme.LINE), new EmptyBorder(18, 20, 18, 20)));
        return card;
    }

    private void buildTableReport(JPanel card, String name) {
        String[] columns;
        List<Object[]> rows;
        switch (name) {
            case "Order History" -> { columns = new String[]{"Order ID", "Customer", "Type", "Status", "Date", "Amount"}; rows = orderDAO.getOrderHistory(); }
            case "Popular Items" -> { columns = new String[]{"Rank", "Item", "Quantity", "Revenue"}; rows = orderDAO.getMostOrderedItems(); }
            case "Payment Summary" -> { columns = new String[]{"Method", "Transactions", "Amount"}; rows = paymentDAO.getPaymentSummary(); }
            case "Inventory Status" -> {
                columns = new String[]{"Ingredient", "Current Stock", "Unit", "Minimum Stock", "Status"};
                rows = inventoryDAO.getAllInventoryItems().stream().map(item -> new Object[]{item.getItemName(), item.getQuantity(), item.getUnit(), item.getReorderLevel(), stockStatus(item)}).toList();
            }
            default -> {
                columns = new String[]{"Customer", "Orders", "Total Spent"};
                rows = customerDAO.getCustomerOrderSummary().stream().map(r -> new Object[]{r[1], r[2], r[3]}).toList();
            }
        }
        if ("Popular Items".equals(name)) {
            java.util.ArrayList<Object[]> ranked = new java.util.ArrayList<>();
            for (int i = 0; i < rows.size(); i++) { Object[] r = rows.get(i); ranked.add(new Object[]{i + 1, r[0], r[1], r[3]}); }
            rows = ranked;
        }
        JTable table = createTable(columns, name);
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        for (Object[] row : rows) {
            Object[] displayRow = row.clone();
            if ("Order History".equals(name) && displayRow.length > 3 && "PENDING".equals(displayRow[3])) displayRow[3] = "NEW";
            model.addRow(displayRow);
        }
        if (rows.isEmpty()) card.add(emptyState("No records yet", "Records will appear here when they are available."), BorderLayout.CENTER);
        else { JScrollPane scroll = new JScrollPane(table); scroll.setBorder(BorderFactory.createEmptyBorder()); card.add(scroll, BorderLayout.CENTER); }
        JLabel foot = new JLabel(rows.size() + " records"); foot.setFont(new Font("Segoe UI", Font.PLAIN, 11)); foot.setForeground(UITheme.MUTED); card.add(foot, BorderLayout.SOUTH);
    }

    private void buildDailySales(JPanel card) {
        List<Object[]> allOrders = orderDAO.getOrderHistory();
        LocalDate today = LocalDate.now();
        double sales = 0; int count = 0;
        for (Object[] row : allOrders) {
            if (row.length > 5 && row[4] instanceof Timestamp ts && ts.toLocalDateTime().toLocalDate().equals(today)) {
                count++; if (row[5] instanceof Number n) sales += n.doubleValue();
            }
        }
        JPanel content = new JPanel(new BorderLayout(0, 18)); content.setOpaque(false);
        JPanel metrics = new JPanel(new GridLayout(1, 3, 12, 0)); metrics.setOpaque(false);
        metrics.add(metric("Today's Sales", money.format(sales)));
        metrics.add(metric("Orders", String.valueOf(count)));
        metrics.add(metric("Average Order Value", money.format(count == 0 ? 0 : sales / count)));
        content.add(metrics, BorderLayout.NORTH);

        List<Object[]> daily = orderDAO.getDailySalesReport();
        JPanel chart = new JPanel(); chart.setBackground(Color.WHITE); chart.setLayout(new BoxLayout(chart, BoxLayout.Y_AXIS));
        chart.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UITheme.LINE), new EmptyBorder(16, 18, 16, 18)));
        JLabel chartTitle = new JLabel("Sales by day"); chartTitle.setFont(new Font("Segoe UI", Font.BOLD, 14)); chartTitle.setForeground(UITheme.INK); chartTitle.setAlignmentX(Component.LEFT_ALIGNMENT); chart.add(chartTitle); chart.add(Box.createVerticalStrut(14));
        double max = daily.stream().filter(r -> r.length > 2 && r[2] instanceof Number).mapToDouble(r -> ((Number) r[2]).doubleValue()).max().orElse(0);
        for (int i = 0; i < Math.min(7, daily.size()); i++) {
            Object[] row = daily.get(i); double amount = row[2] instanceof Number n ? n.doubleValue() : 0;
            JPanel line = new JPanel(new BorderLayout(12, 0)); line.setOpaque(false); line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34)); line.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel date = new JLabel(String.valueOf(row[0])); date.setPreferredSize(new Dimension(100, 24)); date.setForeground(UITheme.MUTED); date.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            JProgressBar bar = new JProgressBar(0, 100); bar.setValue(max <= 0 ? 0 : (int) Math.round(amount * 100 / max)); bar.setStringPainted(false); bar.setBorderPainted(false); bar.setForeground(UITheme.GOLD); bar.setBackground(new Color(246, 244, 239)); bar.setPreferredSize(new Dimension(150, 12));
            JLabel value = new JLabel(money.format(amount)); value.setPreferredSize(new Dimension(100, 24)); value.setHorizontalAlignment(SwingConstants.RIGHT); value.setFont(new Font("Segoe UI", Font.BOLD, 12)); value.setForeground(UITheme.INK);
            line.add(date, BorderLayout.WEST); line.add(bar, BorderLayout.CENTER); line.add(value, BorderLayout.EAST); chart.add(line); chart.add(Box.createVerticalStrut(7));
        }
        if (daily.isEmpty()) chart.add(emptyState("No sales recorded", "Daily sales will appear once orders are recorded."));
        content.add(chart, BorderLayout.CENTER); card.add(content, BorderLayout.CENTER);
    }

    private JPanel metric(String label, String value) {
        JPanel box = new JPanel(new BorderLayout(0, 10)); box.setBackground(new Color(250, 249, 246)); box.setBorder(new EmptyBorder(14, 16, 14, 16));
        JLabel l = new JLabel(label); l.setFont(new Font("Segoe UI", Font.BOLD, 11)); l.setForeground(UITheme.MUTED);
        JLabel v = new JLabel(value); v.setFont(new Font("Segoe UI", Font.BOLD, 20)); v.setForeground(UITheme.NAVY);
        box.add(l, BorderLayout.NORTH); box.add(v, BorderLayout.CENTER); return box;
    }

    private JTable createTable(String[] columns, String report) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) { @Override public boolean isCellEditable(int row, int col) { return false; } };
        JTable table = new JTable(model); UITheme.styleTree(table); table.setRowHeight(42);
        if ("Order History".equals(report)) {
            table.getColumnModel().getColumn(4).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                    if (v instanceof Timestamp ts) v = ts.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, h:mm a", Locale.ENGLISH));
                    return super.getTableCellRendererComponent(t, v, s, f, r, c);
                }
            });
        }
        int amountColumn = "Order History".equals(report) ? 5 : "Payment Summary".equals(report) ? 2 : "Customers".equals(report) ? 2 : "Popular Items".equals(report) ? 3 : -1;
        if (amountColumn >= 0) table.getColumnModel().getColumn(amountColumn).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                if (v instanceof Number n) v = money.format(n.doubleValue());
                return super.getTableCellRendererComponent(t, v, s, f, r, c);
            }
        });
        int statusColumn = "Order History".equals(report) ? 3 : "Inventory Status".equals(report) ? 4 : -1;
        if (statusColumn >= 0) table.getColumnModel().getColumn(statusColumn).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean selected, boolean focus, int row, int col) {
                Component component = super.getTableCellRendererComponent(t, v, selected, focus, row, col);
                if (!selected && v != null) {
                    String status = v.toString().toUpperCase(Locale.ROOT);
                    if (status.contains("OUT OF STOCK") || status.contains("CANCEL")) { component.setForeground(new Color(151, 59, 53)); component.setBackground(new Color(250, 235, 233)); }
                    else if (status.contains("LOW STOCK") || status.contains("PENDING") || status.contains("PREPARING")) { component.setForeground(new Color(157, 103, 36)); component.setBackground(new Color(250, 243, 228)); }
                    else { component.setForeground(new Color(47, 119, 83)); component.setBackground(new Color(235, 245, 238)); }
                    setFont(new Font("Segoe UI", Font.BOLD, 11));
                }
                return component;
            }
        });
        return table;
    }

    private String stockStatus(InventoryItem item) {
        if (item.getQuantity() <= 0) return "OUT OF STOCK";
        if (item.isLowStock()) return "LOW STOCK";
        return "IN STOCK";
    }

    private JPanel emptyState(String title, String message) {
        JPanel panel = new JPanel(); panel.setOpaque(false); panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JLabel h = new JLabel(title); h.setFont(new Font("Segoe UI", Font.BOLD, 15)); h.setForeground(UITheme.INK); h.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel m = new JLabel(message); m.setFont(UITheme.FONT); m.setForeground(UITheme.MUTED); m.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(Box.createVerticalGlue()); panel.add(h); panel.add(Box.createVerticalStrut(5)); panel.add(m); panel.add(Box.createVerticalGlue()); return panel;
    }
}
