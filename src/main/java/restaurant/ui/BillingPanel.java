package restaurant.ui;

import restaurant.dao.PaymentDAO;
import restaurant.model.Order;
import restaurant.model.OrderItem;
import restaurant.model.Payment;
import restaurant.service.BillingService;
import restaurant.service.OrderService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.print.PrinterException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/** Billing workspace. Invoice generation and tender collection are separate actions. */
public class BillingPanel extends JPanel {
    private final OrderService orderService = new OrderService();
    private final BillingService billingService = new BillingService();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final JTextField orderIdField = new JTextField(7);
    private final JTextField discountField = new JTextField("0", 5);
    private final JComboBox<Payment.PaymentMethod> methodCombo = new JComboBox<>(Payment.PaymentMethod.values());
    private final JLabel orderInfo = new JLabel("Load an order to begin.");
    private final DefaultTableModel paymentModel = new DefaultTableModel(
            new String[]{"Payment ID", "Order ID", "Subtotal", "Tax", "Discount", "Grand Total", "Method", "Status", "Paid Date"}, 0) {
        @Override public boolean isCellEditable(int row, int col) { return false; }
    };
    private final JEditorPane preview = new JEditorPane("text/html", emptyPreview("Invoice preview"));
    private final JButton generateButton = new JButton("Generate Invoice");
    private final JButton collectButton = new JButton("Collect Payment");
    private final NumberFormat currency = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private Order loadedOrder;
    private List<OrderItem> loadedItems = List.of();
    private Payment storedPayment;
    private String invoiceText = "";
    private boolean updatingDiscount;

    public BillingPanel() {
        currency.setMinimumFractionDigits(2); currency.setMaximumFractionDigits(2);
        setLayout(new BorderLayout(14, 14)); setBackground(UITheme.CANVAS);
        setBorder(new EmptyBorder(18, 20, 18, 20));
        add(buildControls(), BorderLayout.NORTH);
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildInvoice(), buildHistory());
        split.setResizeWeight(0.55); split.setBorder(null); split.setDividerSize(8);
        add(split, BorderLayout.CENTER);
        discountField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refreshDraftPreview(); }
            public void removeUpdate(DocumentEvent e) { refreshDraftPreview(); }
            public void changedUpdate(DocumentEvent e) { refreshDraftPreview(); }
        });
        refreshPayments();
    }

    private JPanel card(String title) {
        JPanel p = new JPanel(new BorderLayout(10, 10)); p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(UITheme.LINE), new EmptyBorder(14, 15, 15, 15)));
        JLabel heading = new JLabel(title); heading.setFont(new Font("Georgia", Font.PLAIN, 17)); heading.setForeground(UITheme.INK);
        p.add(heading, BorderLayout.NORTH); return p;
    }

    private JPanel buildControls() {
        JPanel panel = card("Billing");
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 3)); row.setOpaque(false);
        row.add(new JLabel("Order ID")); row.add(orderIdField);
        JButton load = button("Load Order", UITheme.ESPRESSO); load.addActionListener(e -> loadOrder()); row.add(load);
        row.add(new JLabel("Discount %")); row.add(discountField);
        row.add(new JLabel("Payment Method")); row.add(methodCombo);
        generateButton.setBackground(UITheme.GOLD); generateButton.setForeground(Color.WHITE); generateButton.setFocusPainted(false);
        generateButton.addActionListener(e -> generateInvoice()); row.add(generateButton);
        collectButton.setBackground(UITheme.ESPRESSO); collectButton.setForeground(Color.WHITE); collectButton.setFocusPainted(false); collectButton.setEnabled(false);
        collectButton.addActionListener(e -> collectPayment()); row.add(collectButton);
        panel.add(row, BorderLayout.CENTER);
        orderInfo.setForeground(UITheme.MUTED); orderInfo.setBorder(new EmptyBorder(4, 3, 0, 0)); panel.add(orderInfo, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildInvoice() {
        JPanel panel = card("Invoice Preview");
        preview.setEditable(false); preview.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        JScrollPane scroll = new JScrollPane(preview); scroll.setBorder(BorderFactory.createLineBorder(UITheme.LINE));
        panel.add(scroll, BorderLayout.CENTER);
        JButton copy = button("Copy Invoice", UITheme.ESPRESSO); copy.addActionListener(e -> copyInvoice());
        JButton print = button("Print Invoice", UITheme.GOLD); print.addActionListener(e -> printInvoice());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); footer.setOpaque(false); footer.add(copy); footer.add(print);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildHistory() {
        JPanel panel = card("Payment History");
        JTable history = new JTable(paymentModel);
        DashboardPanel.styleTable(history);
        history.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        history.setRowHeight(30);
        history.setFillsViewportHeight(true);
        history.setAutoCreateRowSorter(true);
        DefaultTableCellRenderer right = new DefaultTableCellRenderer(); right.setHorizontalAlignment(SwingConstants.RIGHT);
        int[] widths = {88, 78, 104, 92, 104, 116, 100, 96, 168};
        for (int i = 0; i < widths.length; i++) {
            history.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            if (i >= 2 && i <= 5) history.getColumnModel().getColumn(i).setCellRenderer(right);
        }
        JScrollPane scroll = new JScrollPane(history);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        panel.add(scroll, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); bottom.setOpaque(false);
        JButton refresh = button("Refresh History", UITheme.ESPRESSO); refresh.addActionListener(e -> refreshPayments()); bottom.add(refresh);
        panel.add(bottom, BorderLayout.SOUTH);
        return panel;
    }

    private JButton button(String label, Color color) {
        JButton b = new JButton(label); b.setBackground(color); b.setForeground(Color.WHITE); b.setFocusPainted(false); return b;
    }

    private void loadOrder() {
        clearLoadedOrder();
        try {
            int id = Integer.parseInt(orderIdField.getText().trim());
            if (id <= 0) throw new IllegalArgumentException("Order ID must be a positive number.");
            Order order = orderService.getOrderById(id);
            if (order == null) throw new IllegalArgumentException("Order not found.");
            loadedOrder = order;
            loadedItems = order.getItems() == null ? List.of() : List.copyOf(order.getItems());
            if (loadedItems.isEmpty()) throw new IllegalStateException("This order has no items and cannot be billed.");
            orderInfo.setText(String.format("Customer: %s  |  Type: %s  |  Table: %s  |  Kitchen: %s  |  Date: %s",
                    order.getCustomerName(), order.getOrderType(), order.getTableNumber() == null ? "Takeaway" : order.getTableNumber(), order.getStatus(), order.getCreatedAt()));
            storedPayment = billingService.getPaymentByOrderId(id);
            if (storedPayment != null) {
                setDiscountFromPayment(storedPayment);
                showInvoice(storedPayment, false);
                setStoredInvoiceControls(storedPayment);
            } else {
                discountField.setEnabled(true); methodCombo.setEnabled(true);
                refreshDraftPreview();
            }
        } catch (NumberFormatException e) {
            clearLoadedOrder(); showError("Enter a valid numeric order ID.");
        } catch (Exception e) {
            clearLoadedOrder(); showError("Unable to load order: " + usefulMessage(e));
        }
    }

    private void clearLoadedOrder() {
        loadedOrder = null; loadedItems = List.of(); storedPayment = null; invoiceText = "";
        orderInfo.setText("Load an order to begin."); collectButton.setEnabled(false); generateButton.setEnabled(false);
        discountField.setEnabled(true); methodCombo.setEnabled(true);
        preview.setText(emptyPreview("No order loaded"));
    }

    private void refreshDraftPreview() {
        if (updatingDiscount || loadedOrder == null || storedPayment != null) return;
        try {
            BigDecimal discount = new BigDecimal(discountField.getText().trim());
            Payment draft = billingService.previewInvoice(loadedOrder.getOrderId(), discount, selectedMethod());
            showInvoice(draft, true);
            generateButton.setEnabled(true);
            collectButton.setEnabled(false);
        } catch (Exception e) {
            generateButton.setEnabled(false); collectButton.setEnabled(false); invoiceText = "";
            preview.setText(errorPreview("Enter a discount from 0 to 100 to preview this invoice."));
        }
    }

    private void generateInvoice() {
        if (loadedOrder == null) { showError("Load an order first."); return; }
        try {
            BigDecimal discount = new BigDecimal(discountField.getText().trim());
            Payment payment = billingService.generateBill(loadedOrder.getOrderId(), discount, selectedMethod());
            storedPayment = payment;
            showInvoice(payment, false); setStoredInvoiceControls(payment); refreshPayments();
            JOptionPane.showMessageDialog(this, payment.getStatus() == Payment.PaymentStatus.PAID
                    ? "This order is already paid. Existing invoice and payment details are shown."
                    : "Invoice saved. Collect payment when the customer is ready.", "Invoice", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) { showError("Invoice could not be generated: " + usefulMessage(e)); }
    }

    private void collectPayment() {
        if (loadedOrder == null || storedPayment == null || storedPayment.getStatus() != Payment.PaymentStatus.PENDING) {
            showError("Generate an unpaid invoice before collecting payment."); return;
        }
        collectButton.setEnabled(false);
        try {
            Payment paid = billingService.collectPayment(loadedOrder.getOrderId(), selectedMethod());
            storedPayment = paid; showInvoice(paid, false); setStoredInvoiceControls(paid);
            refreshPayments();
            if (paid.getStatus() == Payment.PaymentStatus.PAID)
                JOptionPane.showMessageDialog(this, "Payment recorded successfully.", "Payment", JOptionPane.INFORMATION_MESSAGE);
            else throw new IllegalStateException("The payment was not marked paid.");
        } catch (Exception e) {
            collectButton.setEnabled(storedPayment != null && storedPayment.getStatus() == Payment.PaymentStatus.PENDING);
            showError("Payment could not be recorded: " + usefulMessage(e));
        }
    }

    private Payment.PaymentMethod selectedMethod() {
        Payment.PaymentMethod method = (Payment.PaymentMethod) methodCombo.getSelectedItem();
        if (method == null) throw new IllegalArgumentException("Select a payment method.");
        return method;
    }

    private void setStoredInvoiceControls(Payment payment) {
        discountField.setEnabled(false);
        boolean unpaid = payment.getStatus() == Payment.PaymentStatus.PENDING && validTotal(payment);
        collectButton.setEnabled(unpaid);
        methodCombo.setEnabled(unpaid);
        generateButton.setEnabled(false);
    }

    private void setDiscountFromPayment(Payment payment) {
        updatingDiscount = true;
        try {
            BigDecimal subtotal = BigDecimal.valueOf(payment.getSubtotal());
            BigDecimal percent = subtotal.signum() == 0 ? BigDecimal.ZERO
                    : BigDecimal.valueOf(payment.getDiscount()).multiply(new BigDecimal("100")).divide(subtotal, 2, java.math.RoundingMode.HALF_UP);
            discountField.setText(percent.stripTrailingZeros().toPlainString());
        } finally { updatingDiscount = false; }
    }

    private boolean validTotal(Payment payment) {
        return Double.isFinite(payment.getGrandTotal()) && payment.getGrandTotal() >= 0
                && Double.isFinite(payment.getSubtotal()) && payment.getSubtotal() > 0;
    }

    private void showInvoice(Payment payment, boolean draft) {
        invoiceText = buildInvoiceText(payment, draft);
        StringBuilder rows = new StringBuilder();
        for (OrderItem item : loadedItems) {
            BigDecimal line = restaurant.service.BillingCalculator.lineTotal(item.getUnitPrice(), item.getQuantity());
            rows.append("<tr><td>").append(escape(item.getItemName() == null ? "Item" : item.getItemName()))
                    .append("</td><td class='num'>").append(item.getQuantity()).append("</td><td class='num'>")
                    .append(escape(formatCurrency(BigDecimal.valueOf(item.getUnitPrice())))).append("</td><td class='num'>")
                    .append(escape(formatCurrency(line))).append("</td></tr>");
        }
        String table = loadedOrder.getTableNumber() == null ? "N/A" : String.valueOf(loadedOrder.getTableNumber());
        String status = draft ? "DRAFT · UNPAID" : String.valueOf(payment.getStatus());
        String html = "<html><head><style>body{font-family:Segoe UI,Arial;color:#302a24;background:#fbfaf7;padding:22px}" +
                ".brand{color:#986f36;letter-spacing:2px;font-size:12px}.muted{color:#766f65}table{width:100%;border-collapse:collapse;margin:18px 0}th{background:#f1eee7;text-align:left}th,td{padding:8px;border-bottom:1px solid #e7e1d6}.num{text-align:right;white-space:nowrap}.total{font-size:20px;color:#986f36}</style></head><body>" +
                "<div class='brand'>TABLE &amp; THYME</div><h2>Restaurant Management</h2><hr>" +
                "<b>Invoice Number:</b> " + loadedOrder.getOrderId() + "<br><b>Invoice Date:</b> " + escape(String.valueOf(loadedOrder.getCreatedAt())) +
                "<br><b>Order ID:</b> " + loadedOrder.getOrderId() + "<br><b>Customer:</b> " + escape(String.valueOf(loadedOrder.getCustomerName())) +
                "<br><b>Order Type:</b> " + loadedOrder.getOrderType() + "<br><b>Table Number:</b> " + table +
                "<table><tr><th>Item Name</th><th class='num'>Quantity</th><th class='num'>Unit Price</th><th class='num'>Line Total</th></tr>" + rows + "</table>" +
                "<div>Subtotal <span style='float:right'>" + escape(formatCurrency(payment.getSubtotal())) + "</span></div>" +
                "<div>Discount <span style='float:right'>−" + escape(formatCurrency(payment.getDiscount())) + "</span></div>" +
                "<div>Tax (5%) <span style='float:right'>" + escape(formatCurrency(payment.getTaxAmount())) + "</span></div>" +
                "<h2 class='total'>Grand Total <span style='float:right'>" + escape(formatCurrency(payment.getGrandTotal())) + "</span></h2>" +
                "<hr><b>Payment:</b> " + status + (draft ? "" : " · " + payment.getPaymentMethod()) + "</body></html>";
        preview.setText(html); preview.setCaretPosition(0);
    }

    private String buildInvoiceText(Payment payment, boolean draft) {
        StringBuilder text = new StringBuilder("TABLE & THYME\nRestaurant Management\n")
                .append("Invoice Number: ").append(loadedOrder.getOrderId()).append("\nInvoice Date: ").append(loadedOrder.getCreatedAt())
                .append("\nOrder ID: ").append(loadedOrder.getOrderId()).append("\nCustomer: ").append(loadedOrder.getCustomerName())
                .append("\nOrder Type: ").append(loadedOrder.getOrderType()).append("\nTable Number: ")
                .append(loadedOrder.getTableNumber() == null ? "N/A" : loadedOrder.getTableNumber())
                .append("\n----------------------------------------------------------------\nItem Name                  Qty     Unit Price    Line Total\n");
        for (OrderItem item : loadedItems) {
            BigDecimal line = restaurant.service.BillingCalculator.lineTotal(item.getUnitPrice(), item.getQuantity());
            text.append(String.format(Locale.ROOT, "%-25s %3d %13s %13s%n", item.getItemName(), item.getQuantity(),
                    formatCurrency(BigDecimal.valueOf(item.getUnitPrice())), formatCurrency(line)));
        }
        text.append("----------------------------------------------------------------\n")
                .append(String.format(Locale.ROOT, "Subtotal: %49s%nDiscount: %49s%nTax (5%%): %48s%nGrand Total: %46s%n",
                        formatCurrency(payment.getSubtotal()), formatCurrency(payment.getDiscount()), formatCurrency(payment.getTaxAmount()), formatCurrency(payment.getGrandTotal())))
                .append("Payment: ").append(draft ? "DRAFT / UNPAID" : payment.getStatus() + " / " + payment.getPaymentMethod());
        return text.toString();
    }

    private void copyInvoice() {
        if (invoiceText.isBlank()) { showError("Load an order to preview an invoice before copying."); return; }
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(invoiceText), null);
            JOptionPane.showMessageDialog(this, "Current invoice copied to the clipboard.", "Invoice", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) { showError("Invoice could not be copied: " + usefulMessage(e)); }
    }

    private void printInvoice() {
        if (invoiceText.isBlank()) { showError("Load an order to preview an invoice before printing."); return; }
        try {
            boolean printed = preview.print(null, null, true, null, null, true);
            if (printed) JOptionPane.showMessageDialog(this, "Invoice sent to the selected printer.", "Print Invoice", JOptionPane.INFORMATION_MESSAGE);
        } catch (PrinterException e) { showError("Invoice could not be printed: " + usefulMessage(e)); }
    }

    private void refreshPayments() {
        try {
            paymentModel.setRowCount(0);
            for (Payment payment : paymentDAO.getAllPayments()) paymentModel.addRow(new Object[]{payment.getPaymentId(), payment.getOrderId(),
                    formatCurrency(payment.getSubtotal()), formatCurrency(payment.getTaxAmount()), formatCurrency(payment.getDiscount()),
                    formatCurrency(payment.getGrandTotal()), payment.getPaymentMethod(), payment.getStatus(),
                    payment.getPaidAt() == null ? "—" : payment.getPaidAt().toLocalDateTime().toString().replace('T', ' ')});
        } catch (Exception e) { showError("Payment history could not be loaded: " + usefulMessage(e)); }
    }

    private String formatCurrency(double value) { return formatCurrency(BigDecimal.valueOf(value)); }
    private String formatCurrency(BigDecimal value) { return currency.format(value); }
    private String escape(String value) { return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;"); }
    private String usefulMessage(Exception e) { return e.getMessage() == null || e.getMessage().isBlank() ? e.getClass().getSimpleName() : e.getMessage(); }
    private void showError(String message) { JOptionPane.showMessageDialog(this, message, "Billing Error", JOptionPane.ERROR_MESSAGE); }
    private String emptyPreview(String message) { return "<html><body style='font-family:Segoe UI;padding:20px;color:#766f65'>" + escape(message) + "</body></html>"; }
    private String errorPreview(String message) { return "<html><body style='font-family:Segoe UI;padding:20px;color:#9a4d3b'>" + escape(message) + "</body></html>"; }
}
