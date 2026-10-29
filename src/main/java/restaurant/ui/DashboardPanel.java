package restaurant.ui;

import restaurant.dao.OrderDAO;
import restaurant.dao.TableDAO;
import restaurant.model.RestaurantTable;
import restaurant.util.Session;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/** Premium, database-backed restaurant operations overview. */
public class DashboardPanel extends JPanel {
    private final OrderDAO orders = new OrderDAO();
    private final TableDAO tables = new TableDAO();
    private final JLabel greeting = new JLabel();
    private final JLabel revenue = new JLabel("—");
    private final JLabel orderCount = new JLabel("—");
    private final JLabel averageOrder = new JLabel("—");
    private final JLabel occupancy = new JLabel("—");
    private final JPanel orderRows = new JPanel();
    private final JPanel tableCards = new JPanel(new GridLayout(0, 4, 12, 12));

    private static final Color INK = new Color(45, 40, 35);
    private static final Color MUTED = new Color(132, 123, 112);
    private static final Color PAPER = new Color(255, 254, 251);
    private static final Color LINE = new Color(229, 224, 215);
    private static final Color OLIVE = new Color(100, 111, 81);
    private static final Font BODY = new Font("Segoe UI", Font.PLAIN, 13);

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.CANVAS);
        setBorder(new EmptyBorder(26, 34, 28, 34));

        JPanel page = new JPanel(new BorderLayout(0, 23));
        page.setOpaque(false);
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JPanel welcome = new JPanel(new BorderLayout()); welcome.setOpaque(false);
        JPanel welcomeCopy = new JPanel(); welcomeCopy.setOpaque(false); welcomeCopy.setLayout(new BoxLayout(welcomeCopy, BoxLayout.Y_AXIS));
        greeting.setFont(new Font("Georgia", Font.PLAIN, 30)); greeting.setForeground(INK);
        JLabel subtitle = new JLabel("Here's what's happening at Table & Thyme today.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14)); subtitle.setForeground(MUTED);
        welcomeCopy.add(greeting); welcomeCopy.add(Box.createVerticalStrut(6)); welcomeCopy.add(subtitle);
        JButton refresh = UITheme.button("Refresh", "secondary"); refresh.addActionListener(e -> refreshData());
        welcome.add(welcomeCopy, BorderLayout.WEST); welcome.add(refresh, BorderLayout.EAST);
        top.add(welcome); top.add(Box.createVerticalStrut(22));
        top.add(sectionLabel("TODAY'S PERFORMANCE")); top.add(Box.createVerticalStrut(10));
        top.add(buildPerformance());
        page.add(top, BorderLayout.NORTH);

        JPanel lower = new JPanel(new GridBagLayout()); lower.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints(); c.fill = GridBagConstraints.BOTH; c.weighty = 1; c.gridy = 0;
        c.gridx = 0; c.weightx = .62; c.insets = new Insets(0, 0, 0, 18); lower.add(buildRecentOrders(), c);
        c.gridx = 1; c.weightx = .38; c.insets = new Insets(0, 0, 0, 0); lower.add(buildTableOverview(), c);
        page.add(lower, BorderLayout.CENTER);
        add(page, BorderLayout.CENTER);
        refreshData();
    }

    private JPanel buildPerformance() {
        JPanel row = new JPanel(new GridBagLayout()); row.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints(); c.fill = GridBagConstraints.BOTH; c.gridy = 0; c.weighty = 1;
        JPanel revenuePanel = new JPanel(); revenuePanel.setOpaque(true); revenuePanel.setBackground(UITheme.ESPRESSO);
        revenuePanel.setBorder(new EmptyBorder(20, 24, 19, 24)); revenuePanel.setLayout(new BoxLayout(revenuePanel, BoxLayout.Y_AXIS));
        JLabel eyebrow = sectionLabel("TODAY'S REVENUE"); eyebrow.setForeground(new Color(204, 183, 151));
        revenue.setFont(new Font("Georgia", Font.PLAIN, 39)); revenue.setForeground(new Color(255, 252, 245));
        JLabel caption = new JLabel("Paid orders recorded today"); caption.setFont(BODY); caption.setForeground(new Color(195, 185, 171));
        revenuePanel.add(eyebrow); revenuePanel.add(Box.createVerticalStrut(10)); revenuePanel.add(revenue); revenuePanel.add(Box.createVerticalStrut(4)); revenuePanel.add(caption);
        c.gridx = 0; c.weightx = 1.2; c.insets = new Insets(0,0,0,18); row.add(revenuePanel, c);
        JPanel supporting = new JPanel(new GridLayout(1, 3, 0, 0)); supporting.setBackground(PAPER);
        supporting.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, LINE));
        supporting.add(supportMetric(orderCount, "ORDERS"));
        supporting.add(supportMetric(averageOrder, "AVERAGE ORDER"));
        supporting.add(supportMetric(occupancy, "TABLE OCCUPANCY"));
        c.gridx = 1; c.weightx = 1; c.insets = new Insets(0,0,0,0); row.add(supporting, c);
        return row;
    }

    private JPanel supportMetric(JLabel value, String label) {
        JPanel p = new JPanel(); p.setOpaque(false); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS)); p.setBorder(new EmptyBorder(18, 16, 15, 8));
        value.setFont(new Font("Georgia", Font.PLAIN, 24)); value.setForeground(INK);
        JLabel l = sectionLabel(label); l.setForeground(MUTED);
        p.add(Box.createVerticalGlue()); p.add(value); p.add(Box.createVerticalStrut(7)); p.add(l); p.add(Box.createVerticalGlue());
        return p;
    }

    private JPanel buildRecentOrders() {
        JPanel panel = new JPanel(new BorderLayout(0, 10)); panel.setOpaque(false);
        JPanel heading = new JPanel(new BorderLayout()); heading.setOpaque(false);
        JLabel title = sectionLabel("RECENT ORDERS"); title.setForeground(INK);
        JLabel all = new JLabel("LATEST ACTIVITY"); all.setFont(new Font("Segoe UI", Font.PLAIN, 10)); all.setForeground(MUTED);
        heading.add(title, BorderLayout.WEST); heading.add(all, BorderLayout.EAST); panel.add(heading, BorderLayout.NORTH);
        orderRows.setOpaque(true); orderRows.setBackground(PAPER); orderRows.setLayout(new BoxLayout(orderRows, BoxLayout.Y_AXIS));
        panel.add(orderRows, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildTableOverview() {
        JPanel panel = new JPanel(new BorderLayout(0, 10)); panel.setOpaque(false);
        JLabel title = sectionLabel("TABLE OVERVIEW"); title.setForeground(INK); panel.add(title, BorderLayout.NORTH);
        tableCards.setOpaque(false); panel.add(tableCards, BorderLayout.CENTER);
        return panel;
    }

    private static JLabel sectionLabel(String text) {
        JLabel l = new JLabel(text); l.setFont(new Font("Segoe UI", Font.BOLD, 10)); l.setForeground(UITheme.GOLD); return l;
    }

    private void refreshData() {
        try {
            int hour = java.time.LocalTime.now().getHour();
            String salutation = hour < 12 ? "Good morning" : hour < 17 ? "Good afternoon" : "Good evening";
            greeting.setText(salutation + ", " + DashboardFrame.displayName(Session.getCurrentUser()));
            NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
            double sales = orders.getTodaySalesTotal();
            List<Object[]> history = orders.getOrderHistory();
            LocalDate today = LocalDate.now();
            long todayOrders = history.stream().filter(r -> r.length > 4 && r[4] instanceof Timestamp ts && ts.toLocalDateTime().toLocalDate().equals(today)).count();
            revenue.setText(currency.format(sales)); orderCount.setText(todayOrders + " Orders");
            averageOrder.setText(currency.format(todayOrders == 0 ? 0 : sales / todayOrders));
            List<RestaurantTable> allTables = tables.getAllTables();
            long occupiedCount = allTables.stream().filter(t -> t.getStatus() == RestaurantTable.Status.OCCUPIED).count();
            occupancy.setText(allTables.isEmpty() ? "0%" : Math.round(100.0 * occupiedCount / allTables.size()) + "%");
            renderOrders(history, currency);
            renderTables(allTables);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Dashboard data could not be loaded. Check the database connection and try Refresh.\n" + ex.getMessage(), "Dashboard error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void renderOrders(List<Object[]> history, NumberFormat currency) {
        orderRows.removeAll();
        int count = Math.min(6, history.size());
        for (int i = 0; i < count; i++) {
            Object[] r = history.get(i);
            int id = ((Number) r[0]).intValue(); String customer = String.valueOf(r[1]);
            String type = "DINE_IN".equals(String.valueOf(r[2])) ? "Dine In" : "Takeaway";
            String status = String.valueOf(r[3]); if ("PENDING".equals(status)) status = "New";
            Timestamp ts = r[4] instanceof Timestamp t ? t : null;
            String time = ts == null ? "—" : ts.toLocalDateTime().format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH));
            String amount = currency.format(r[5] instanceof Number n ? n.doubleValue() : 0);
            JPanel row = new JPanel(new BorderLayout(10, 0)); row.setOpaque(true); row.setBackground(PAPER);
            row.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,LINE), new EmptyBorder(12,15,12,15)));
            JPanel left = new JPanel(); left.setOpaque(false); left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
            JLabel top = new JLabel("#" + id + "    " + customer); top.setFont(new Font("Segoe UI", Font.PLAIN, 13)); top.setForeground(INK);
            JLabel detail = new JLabel(type + "  ·  " + status); detail.setFont(new Font("Segoe UI", Font.PLAIN, 11)); detail.setForeground(MUTED);
            left.add(top); left.add(Box.createVerticalStrut(4)); left.add(detail);
            JPanel right = new JPanel(); right.setOpaque(false); right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
            JLabel price = new JLabel(amount); price.setFont(new Font("Segoe UI", Font.BOLD, 13)); price.setForeground(INK); price.setAlignmentX(Component.RIGHT_ALIGNMENT);
            JLabel when = new JLabel(time); when.setFont(new Font("Segoe UI", Font.PLAIN, 11)); when.setForeground(MUTED); when.setAlignmentX(Component.RIGHT_ALIGNMENT);
            right.add(price); right.add(Box.createVerticalStrut(4)); right.add(when);
            row.add(left, BorderLayout.CENTER); row.add(right, BorderLayout.EAST); orderRows.add(row);
        }
        if (count == 0) {
            JLabel empty = new JLabel("No orders to display yet."); empty.setFont(BODY); empty.setForeground(MUTED); empty.setBorder(new EmptyBorder(20,15,20,15)); orderRows.add(empty);
        }
        orderRows.revalidate(); orderRows.repaint();
    }

    private void renderTables(List<RestaurantTable> all) {
        tableCards.removeAll();
        for (RestaurantTable t : all) {
            String state = t.getStatus() == null ? "UNKNOWN" : t.getStatus().name();
            Color stateColor = "AVAILABLE".equals(state) ? OLIVE : "OCCUPIED".equals(state) ? new Color(151, 105, 68) : new Color(120, 105, 137);
            JPanel card = new JPanel(); card.setOpaque(true); card.setBackground(PAPER); card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,LINE), new EmptyBorder(12,13,11,8)));
            JLabel number = new JLabel(String.format("%02d", t.getTableNumber())); number.setFont(new Font("Georgia", Font.PLAIN, 28)); number.setForeground(INK);
            JLabel table = new JLabel("TABLE"); table.setFont(new Font("Segoe UI", Font.BOLD, 9)); table.setForeground(MUTED);
            JLabel seats = new JLabel(t.getCapacity() + (t.getCapacity() == 1 ? " seat" : " seats")); seats.setFont(BODY); seats.setForeground(MUTED);
            JLabel status = new JLabel(state); status.setFont(new Font("Segoe UI", Font.BOLD, 9)); status.setForeground(stateColor);
            card.add(table); card.add(Box.createVerticalStrut(2)); card.add(number); card.add(Box.createVerticalStrut(6)); card.add(seats); card.add(Box.createVerticalStrut(8)); card.add(status);
            tableCards.add(card);
        }
        tableCards.revalidate(); tableCards.repaint();
    }

    /** Shared legacy table styling hook used by the other application modules. */
    static void styleTable(JTable table) {
        table.setRowHeight(38);
        table.setFont(BODY);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(241, 235, 224));
        table.setSelectionForeground(INK);
        table.setShowVerticalLines(false);
        table.setGridColor(LINE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        table.getTableHeader().setBackground(new Color(246, 243, 237));
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
    }
}
