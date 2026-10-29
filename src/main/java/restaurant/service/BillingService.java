package restaurant.service;

import restaurant.dao.OrderDAO;
import restaurant.dao.PaymentDAO;
import restaurant.model.Order;
import restaurant.model.OrderItem;
import restaurant.model.Payment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

/** Coordinates invoice creation and payment collection independently of kitchen status. */
public class BillingService {
    private static final BigDecimal TAX_RATE = new BigDecimal("0.05");
    private final OrderDAO orderDAO = new OrderDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();

    /** Builds a non-persistent invoice preview using the existing 5% tax rule. */
    public Payment previewInvoice(int orderId, BigDecimal discountPercent, Payment.PaymentMethod method) {
        Order order = requireBillableOrder(orderId);
        List<OrderItem> items = orderDAO.getOrderItems(orderId);
        if (items == null || items.isEmpty()) throw new IllegalStateException("Orders with no items cannot be billed.");
        if (method == null) throw new IllegalArgumentException("Select a payment method.");
        BigDecimal subtotal = BigDecimal.ZERO;
        for (OrderItem item : items) {
            if (item.getQuantity() <= 0 || !Double.isFinite(item.getUnitPrice()) || item.getUnitPrice() < 0)
                throw new IllegalStateException("Order contains an invalid item quantity or price.");
            subtotal = subtotal.add(BillingCalculator.lineTotal(item.getUnitPrice(), item.getQuantity()));
        }
        BillingCalculator.Totals totals = BillingCalculator.calculate(subtotal, discountPercent);
        Payment invoice = new Payment();
        invoice.setOrderId(orderId);
        invoice.setSubtotal(totals.subtotal().doubleValue());
        invoice.setDiscount(totals.discount().doubleValue());
        invoice.setTaxAmount(totals.tax().doubleValue());
        invoice.setGrandTotal(totals.total().doubleValue());
        invoice.setPaymentMethod(method);
        invoice.setStatus(Payment.PaymentStatus.PENDING);
        return invoice;
    }

    /** Creates or returns the single persisted invoice for a valid order. */
    public Payment generateBill(int orderId, BigDecimal discountPercent, Payment.PaymentMethod method) {
        Payment preview = previewInvoice(orderId, discountPercent, method);
        Payment existing = paymentDAO.getPaymentByOrderId(orderId);
        if (existing != null) return existing;
        return paymentDAO.createInvoice(preview);
    }

    /** Compatibility overload for existing callers. */
    public Payment generateBill(int orderId, double discountPercent, Payment.PaymentMethod method) {
        if (!Double.isFinite(discountPercent)) throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        return generateBill(orderId, BigDecimal.valueOf(discountPercent), method);
    }

    /** Collects exactly once, recording the tender method selected at collection time. */
    public Payment collectPayment(int orderId, Payment.PaymentMethod method) {
        if (method == null) throw new IllegalArgumentException("Select a payment method.");
        Payment payment = paymentDAO.getPaymentByOrderId(orderId);
        if (payment == null) throw new IllegalStateException("Generate an invoice before collecting payment.");
        if (payment.getStatus() == Payment.PaymentStatus.PAID) return payment;
        if (payment.getStatus() != Payment.PaymentStatus.PENDING)
            throw new IllegalStateException("This invoice cannot be paid in its current status.");
        return paymentDAO.markPaid(payment.getPaymentId(), method);
    }

    /** Compatibility overload for existing callers; uses the invoice's selected method. */
    public Payment collectPayment(int orderId) {
        Payment payment = paymentDAO.getPaymentByOrderId(orderId);
        if (payment == null) throw new IllegalStateException("Generate an invoice before collecting payment.");
        return collectPayment(orderId, payment.getPaymentMethod());
    }

    public Payment getPaymentByOrderId(int orderId) { return paymentDAO.getPaymentByOrderId(orderId); }
    public List<Payment> getAllPayments() { return paymentDAO.getAllPayments(); }

    public String generateBillText(int orderId) {
        Order order = requireBillableOrder(orderId);
        List<OrderItem> items = orderDAO.getOrderItems(orderId);
        if (items == null || items.isEmpty()) throw new IllegalStateException("Orders with no items cannot be billed.");
        Payment payment = paymentDAO.getPaymentByOrderId(orderId);
        BigDecimal subtotal = BigDecimal.ZERO;
        StringBuilder sb = new StringBuilder("TABLE & THYME\nRestaurant Management\n")
                .append("Invoice Number: ").append(orderId).append("\nInvoice Date: ").append(order.getCreatedAt())
                .append("\nOrder ID: ").append(orderId).append("\nCustomer: ").append(order.getCustomerName())
                .append("\nOrder Type: ").append(order.getOrderType())
                .append(order.getTableNumber() == null ? "\nTable: Takeaway\n" : "\nTable: " + order.getTableNumber() + "\n")
                .append("----------------------------------------------------------------\nItem                         Qty    Unit Price    Line Total\n");
        for (OrderItem item : items) {
            BigDecimal line = BillingCalculator.lineTotal(item.getUnitPrice(), item.getQuantity());
            subtotal = subtotal.add(line);
            sb.append(String.format(Locale.ROOT, "%-26s %3d %12s %13s%n", item.getItemName(), item.getQuantity(), currency(item.getUnitPrice()), currency(line)));
        }
        BigDecimal discount = payment == null ? BigDecimal.ZERO : BigDecimal.valueOf(payment.getDiscount());
        BigDecimal tax = payment == null ? subtotal.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP) : BigDecimal.valueOf(payment.getTaxAmount());
        BigDecimal total = payment == null ? subtotal.add(tax) : BigDecimal.valueOf(payment.getGrandTotal());
        sb.append("----------------------------------------------------------------\n")
                .append(String.format(Locale.ROOT, "Subtotal: %45s%nDiscount: %45s%nTax (5%%): %44s%nGrand Total: %42s%n", currency(subtotal), currency(discount), currency(tax), currency(total)))
                .append("Payment Status: ").append(payment == null ? "UNPAID" : payment.getStatus());
        if (payment != null) sb.append(" | Method: ").append(payment.getPaymentMethod());
        return sb.toString();
    }

    private Order requireBillableOrder(int orderId) {
        Order order = orderDAO.getOrderById(orderId);
        if (order == null) throw new IllegalArgumentException("Order not found.");
        if (order.getStatus() == Order.OrderStatus.CANCELLED) throw new IllegalStateException("Cancelled orders cannot be billed.");
        return order;
    }

    private String currency(double amount) { return currency(BigDecimal.valueOf(amount)); }
    private String currency(BigDecimal amount) { return "₹" + amount.setScale(2, RoundingMode.HALF_UP).toPlainString(); }
}
