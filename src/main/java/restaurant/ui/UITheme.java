package restaurant.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

/** Shared Table & Thyme visual system. */
final class UITheme {
    static final Color ESPRESSO = new Color(39, 34, 30);
    static final Color NAVY = ESPRESSO;
    static final Color INK = new Color(47, 43, 39);
    static final Color MUTED = new Color(127, 119, 108);
    static final Color CANVAS = new Color(246, 243, 237);
    static final Color PAPER = new Color(255, 254, 251);
    static final Color GOLD = new Color(170, 133, 83);
    static final Color LINE = new Color(230, 225, 216);
    static final Color OLIVE = new Color(91, 105, 78);
    static final Color DANGER = new Color(147, 77, 72);
    static final Font FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private UITheme() {}

    static void install() {
        UIManager.put("Panel.background", CANVAS);
        UIManager.put("OptionPane.background", PAPER);
        UIManager.put("Button.font", FONT); UIManager.put("TextField.font", FONT);
        UIManager.put("ComboBox.font", FONT); UIManager.put("Label.font", FONT);
        UIManager.put("TextArea.font", FONT); UIManager.put("Spinner.font", FONT);
        UIManager.put("ComboBox.background", PAPER); UIManager.put("ScrollPane.background", PAPER);
        UIManager.put("TabbedPane.font", new Font("Segoe UI", Font.BOLD, 12));
        UIManager.put("TabbedPane.background", CANVAS); UIManager.put("TabbedPane.selected", PAPER);
        UIManager.put("TabbedPane.contentAreaColor", PAPER); UIManager.put("TabbedPane.focus", GOLD);
    }

    static JButton button(String text, String kind) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12)); b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(10, 16, 10, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.setOpaque(true);
        b.setContentAreaFilled(true);
        if (kind.equals("primary")) { b.setBackground(ESPRESSO); b.setForeground(PAPER); }
        else if (kind.equals("danger")) { b.setBackground(DANGER); b.setForeground(Color.WHITE); }
        else { b.setBackground(CANVAS); b.setForeground(INK); b.setBorder(new EmptyBorder(10, 14, 10, 14)); }
        return b;
    }

    static JLabel eyebrow(String text) {
        JLabel label = new JLabel(text.toUpperCase());
        label.setFont(new Font("Segoe UI", Font.BOLD, 10)); label.setForeground(GOLD);
        return label;
    }

    static void styleTree(Component component) {
        if (component instanceof JPanel p && p.getBackground().equals(new Color(236,240,241))) p.setBackground(CANVAS);
        if (component instanceof JButton b) {
            String text = b.getText() == null ? "" : b.getText().toLowerCase();
            boolean danger = text.contains("delete") || text.contains("logout") || text.contains("sign out") || text.contains("remove");
            boolean primary = text.contains("add") || text.contains("create") || text.contains("save") || text.contains("pay") || text.contains("login") || text.contains("sign in") || text.contains("place order") || text.contains("generate bill");
            b.setFont(new Font("Segoe UI", Font.BOLD, 12)); b.setFocusPainted(false);
            b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.setBorder(new EmptyBorder(9, 14, 9, 14)); b.setOpaque(true);
            b.setBackground(danger ? DANGER : primary ? ESPRESSO : PAPER); b.setForeground(danger || primary ? Color.WHITE : INK);
        }
        if (component instanceof JTextField f) { f.setFont(FONT); f.setBackground(PAPER); f.setForeground(INK); f.setCaretColor(GOLD); f.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE), new EmptyBorder(8, 10, 8, 10))); }
        if (component instanceof JComboBox<?> combo) { combo.setFont(FONT); combo.setBackground(PAPER); combo.setForeground(INK); }
        if (component instanceof JSpinner spinner) { spinner.setFont(FONT); spinner.setBorder(BorderFactory.createLineBorder(LINE)); }
        if (component instanceof JTextArea area) { area.setFont(FONT); area.setForeground(INK); area.setBackground(PAPER); area.setBorder(new EmptyBorder(10, 12, 10, 12)); }
        if (component instanceof JScrollPane scroll) { scroll.setBorder(BorderFactory.createEmptyBorder()); scroll.getViewport().setBackground(PAPER); }
        if (component instanceof JLabel label && label.getForeground().equals(new Color(44,62,80))) label.setForeground(INK);
        if (component instanceof JTable t) {
            t.setFont(FONT); t.setRowHeight(44); t.setShowVerticalLines(false); t.setShowHorizontalLines(true);
            t.setGridColor(LINE); t.setIntercellSpacing(new Dimension(0, 1));
            t.setSelectionBackground(new Color(241, 235, 224)); t.setSelectionForeground(INK); t.setFillsViewportHeight(true);
            t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focus, int row, int col) {
                    Component c = super.getTableCellRendererComponent(table, value, selected, focus, row, col); c.setFont(FONT);
                    c.setBackground(selected ? new Color(241,235,224) : PAPER); c.setForeground(INK);
                    if (c instanceof JLabel l) l.setBorder(new EmptyBorder(0, 12, 0, 8)); return c;
                }
            });
            t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 10)); t.getTableHeader().setBackground(CANVAS);
            t.getTableHeader().setForeground(MUTED); t.getTableHeader().setPreferredSize(new Dimension(0, 38)); t.getTableHeader().setReorderingAllowed(false);
            t.setBorder(BorderFactory.createEmptyBorder());
        }
        if (component instanceof Container c) for (Component child : c.getComponents()) styleTree(child);
    }
}
