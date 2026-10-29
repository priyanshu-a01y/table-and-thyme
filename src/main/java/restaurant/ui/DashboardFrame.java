package restaurant.ui;

import restaurant.model.User;
import restaurant.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/** Main application shell. */
public class DashboardFrame extends JFrame {
    static final Color NAVY = UITheme.ESPRESSO;
    static final Color BLUE = UITheme.GOLD;
    static final Color CANVAS = UITheme.CANVAS;
    static final Color INK = UITheme.INK;
    private final AuthService authService;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final JLabel pageTitle = new JLabel("Dashboard");
    private final Map<String, NavItem> navigation = new LinkedHashMap<>();

    public DashboardFrame(AuthService authService) {
        UITheme.install();
        this.authService = authService;
        setTitle("Table & Thyme | Restaurant Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1160, 760));
        setSize(1460, 920);
        setLocationRelativeTo(null);
        getContentPane().setBackground(CANVAS);
        setLayout(new BorderLayout());

        content.setBackground(CANVAS);
        content.add(new DashboardPanel(), "Dashboard");
        content.add(new MenuPanel(), "Menu");
        content.add(new TablePanel(), "Tables");
        content.add(new CustomerPanel(), "Customers");
        content.add(new OrderPanel(), "Orders");
        content.add(new BillingPanel(), "Billing");
        content.add(new InventoryPanel(), "Inventory");
        content.add(new ReportsPanel(), "Reports");
        for (Component component : content.getComponents()) UITheme.styleTree(component);

        add(createSidebar(), BorderLayout.WEST);
        JPanel right = new JPanel(new BorderLayout()); right.setBackground(CANVAS);
        right.add(createHeader(), BorderLayout.NORTH); right.add(content, BorderLayout.CENTER);
        add(right, BorderLayout.CENTER);
        showModule("Dashboard");
    }

    private JPanel createSidebar() {
        JPanel side = new JPanel(new BorderLayout()); side.setBackground(NAVY);
        side.setPreferredSize(new Dimension(258, 0)); side.setBorder(new EmptyBorder(29, 19, 20, 18));
        JPanel upper = new JPanel(); upper.setOpaque(false); upper.setLayout(new BoxLayout(upper, BoxLayout.Y_AXIS));
        JLabel brand = new JLabel("TABLE & THYME"); brand.setFont(new Font("Georgia", Font.PLAIN, 22)); brand.setForeground(new Color(250, 246, 237));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT); upper.add(brand);
        JLabel caption = new JLabel("RESTAURANT MANAGEMENT"); caption.setFont(new Font("Segoe UI", Font.BOLD, 9)); caption.setForeground(new Color(177, 163, 143));
        caption.setBorder(new EmptyBorder(7, 1, 30, 0)); caption.setAlignmentX(Component.LEFT_ALIGNMENT); upper.add(caption);
        String[] items = {"Dashboard", "Menu", "Tables", "Customers", "Orders", "Billing", "Inventory", "Reports"};
        for (String name : items) {
            NavItem item = new NavItem(name); item.setAlignmentX(Component.LEFT_ALIGNMENT);
            item.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) { showModule(name); }
                @Override public void mouseEntered(MouseEvent e) { if (!item.active) item.setBackground(new Color(53, 47, 41)); }
                @Override public void mouseExited(MouseEvent e) { if (!item.active) item.setBackground(NAVY); }
            });
            navigation.put(name, item); upper.add(item); upper.add(Box.createVerticalStrut(5));
        }
        side.add(upper, BorderLayout.NORTH);
        User user = authService.getCurrentUser(); String name = displayName(user);
        JPanel profile = new JPanel(); profile.setOpaque(false); profile.setLayout(new BoxLayout(profile, BoxLayout.Y_AXIS));
        profile.setBorder(new EmptyBorder(15, 0, 0, 0));
        JPanel identity = new JPanel(new FlowLayout(FlowLayout.LEFT, 11, 0)); identity.setOpaque(false); identity.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel avatar = new JLabel(initials(name), SwingConstants.CENTER); avatar.setOpaque(true); avatar.setBackground(new Color(76, 67, 56)); avatar.setForeground(new Color(229, 211, 179));
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 12)); avatar.setPreferredSize(new Dimension(37, 37));
        JPanel names = new JPanel(); names.setOpaque(false); names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        JLabel userName = new JLabel(name); userName.setForeground(Color.WHITE); userName.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JLabel role = new JLabel(user == null ? "" : roleName(user.getRole())); role.setForeground(new Color(174, 163, 147)); role.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        names.add(userName); names.add(Box.createVerticalStrut(3)); names.add(role); identity.add(avatar); identity.add(names); profile.add(identity);
        JButton logout = new JButton("Sign out"); logout.setHorizontalAlignment(SwingConstants.LEFT); logout.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        logout.setForeground(new Color(205, 195, 181)); logout.setBackground(NAVY); logout.setOpaque(false); logout.setContentAreaFilled(false); logout.setBorder(new EmptyBorder(15, 3, 5, 0)); logout.setFocusPainted(false);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); logout.setAlignmentX(Component.LEFT_ALIGNMENT);
        logout.addActionListener(e -> { authService.logout(); new LoginFrame().setVisible(true); dispose(); });
        profile.add(Box.createVerticalStrut(11)); profile.add(logout); side.add(profile, BorderLayout.SOUTH);
        return side;
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout()); header.setBackground(UITheme.PAPER);
        header.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,UITheme.LINE), new EmptyBorder(17, 31, 17, 31)));
        JPanel copy = new JPanel(); copy.setOpaque(false); copy.setLayout(new BoxLayout(copy, BoxLayout.Y_AXIS));
        pageTitle.setFont(new Font("Georgia", Font.PLAIN, 20)); pageTitle.setForeground(INK);
        JLabel eyebrow = new JLabel("TABLE & THYME   /   RESTAURANT OPERATIONS"); eyebrow.setFont(new Font("Segoe UI", Font.PLAIN, 9)); eyebrow.setForeground(UITheme.MUTED);
        copy.add(eyebrow); copy.add(Box.createVerticalStrut(3)); copy.add(pageTitle); header.add(copy, BorderLayout.WEST);
        User user = authService.getCurrentUser();
        JLabel profile = new JLabel(displayName(user) + "    ·    " + (user == null ? "" : roleName(user.getRole())));
        profile.setFont(new Font("Segoe UI", Font.PLAIN, 12)); profile.setForeground(UITheme.MUTED); header.add(profile, BorderLayout.EAST);
        return header;
    }

    private void showModule(String name) {
        cards.show(content, name); pageTitle.setText(name);
        navigation.forEach((key, item) -> item.setActive(key.equals(name)));
    }

    static String displayName(User user) {
        if (user == null) return "User";
        String fullName = user.getFullName();
        if (fullName != null && !fullName.isBlank()) return fullName.trim();
        return user.getUsername();
    }

    private static String initials(String name) {
        if (name == null || name.isBlank()) return "U";
        String[] parts = name.trim().split("\\s+");
        return parts.length > 1 ? (parts[0].substring(0,1) + parts[parts.length - 1].substring(0,1)).toUpperCase() : parts[0].substring(0,1).toUpperCase();
    }

    private static String roleName(User.Role role) { return role == User.Role.ADMIN ? "Administrator" : role == User.Role.STAFF ? "Staff" : ""; }

    private static final class NavItem extends JPanel {
        private final String name; private boolean active;
        NavItem(String name) {
            this.name = name; setLayout(new BorderLayout(13,0)); setOpaque(true); setBackground(NAVY);
            setBorder(new EmptyBorder(10, 12, 10, 10)); setMaximumSize(new Dimension(Integer.MAX_VALUE, 43)); setPreferredSize(new Dimension(220,43));
            Icon icon = new NavigationIcon(name); JLabel glyph = new JLabel(icon); glyph.setForeground(new Color(184, 173, 157));
            JLabel label = new JLabel(name); label.setFont(new Font("Segoe UI", Font.PLAIN, 13)); label.setForeground(new Color(211, 203, 191));
            add(glyph, BorderLayout.WEST); add(label, BorderLayout.CENTER);
        }
        void setActive(boolean selected) {
            active = selected; setBackground(selected ? new Color(59, 51, 44) : NAVY);
            JLabel label = (JLabel) getComponent(1); label.setForeground(selected ? new Color(232, 211, 176) : new Color(211,203,191));
            JLabel glyph = (JLabel) getComponent(0); glyph.setForeground(selected ? new Color(214, 183, 136) : new Color(184,173,157));
            setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, selected ? 2 : 0, 0, 0, UITheme.GOLD), new EmptyBorder(10, selected ? 10 : 12, 10, 10)));
            repaint();
        }
    }

    /** Hand-drawn Java2D line icons; no font glyphs or external assets. */
    private static final class NavigationIcon implements Icon {
        private final String name;
        NavigationIcon(String name) { this.name = name; }
        public int getIconWidth() { return 18; } public int getIconHeight() { return 18; }
        public void paintIcon(Component c, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create(); g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(c.getForeground()); g.setStroke(new BasicStroke(1.45f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (name) {
                case "Dashboard" -> { g.drawRoundRect(x+2,y+2,6,6,2,2); g.drawRoundRect(x+10,y+2,6,6,2,2); g.drawRoundRect(x+2,y+10,6,6,2,2); g.drawRoundRect(x+10,y+10,6,6,2,2); }
                case "Menu" -> { g.drawOval(x+4,y+2,10,10); g.drawLine(x+5,y+14,x+13,y+14); g.drawLine(x+6,y+16,x+12,y+16); }
                case "Tables" -> { g.drawRoundRect(x+2,y+3,14,8,4,4); g.drawLine(x+4,y+11,x+4,y+16); g.drawLine(x+14,y+11,x+14,y+16); g.drawLine(x+3,y+16,x+6,y+16); g.drawLine(x+12,y+16,x+15,y+16); }
                case "Customers" -> { g.drawOval(x+6,y+2,6,6); g.drawArc(x+3,y+8,12,9,0,180); }
                case "Orders" -> { g.drawRoundRect(x+4,y+2,10,14,2,2); g.drawLine(x+6,y+6,x+12,y+6); g.drawLine(x+6,y+9,x+12,y+9); g.drawLine(x+6,y+12,x+10,y+12); }
                case "Billing" -> { g.drawRoundRect(x+2,y+3,14,12,2,2); g.drawLine(x+2,y+7,x+16,y+7); g.drawLine(x+5,y+11,x+9,y+11); }
                case "Inventory" -> { g.drawRect(x+3,y+5,12,11); g.drawLine(x+3,y+8,x+15,y+8); g.drawLine(x+9,y+5,x+9,y+16); g.drawLine(x+6,y+2,x+12,y+2); }
                default -> { g.drawLine(x+3,y+15,x+3,y+9); g.drawLine(x+8,y+15,x+8,y+5); g.drawLine(x+13,y+15,x+13,y+2); g.drawLine(x+2,y+16,x+16,y+16); }
            }
            g.dispose();
        }
    }
}

