package restaurant.dao;

import restaurant.config.DatabaseConnection;
import restaurant.model.Payment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PaymentDAO {
    public Payment getPaymentByOrderId(int orderId) {
        String sql = "SELECT * FROM payments WHERE order_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? mapResultSetToPayment(rs) : null; }
        } catch (SQLException e) { throw new IllegalStateException("Unable to load invoice/payment details.", e); }
    }

    public List<Payment> getAllPayments() {
        List<Payment> payments = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM payments ORDER BY payment_id DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) payments.add(mapResultSetToPayment(rs));
            return payments;
        } catch (SQLException e) { throw new IllegalStateException("Unable to load payment history.", e); }
    }

    /** Inserts an unpaid invoice. The unique order_id constraint makes retries race-safe. */
    public Payment createInvoice(Payment invoice) {
        String sql = "INSERT INTO payments (order_id, subtotal, tax_amount, discount, grand_total, payment_method, status, paid_at) VALUES (?, ?, ?, ?, ?, ?, 'PENDING', NULL)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, invoice.getOrderId());
            ps.setBigDecimal(2, java.math.BigDecimal.valueOf(invoice.getSubtotal()));
            ps.setBigDecimal(3, java.math.BigDecimal.valueOf(invoice.getTaxAmount()));
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(invoice.getDiscount()));
            ps.setBigDecimal(5, java.math.BigDecimal.valueOf(invoice.getGrandTotal()));
            ps.setString(6, invoice.getPaymentMethod().name());
            if (ps.executeUpdate() != 1) throw new IllegalStateException("Invoice was not saved.");
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new IllegalStateException("Invoice was saved but its identifier was not returned.");
                invoice.setPaymentId(keys.getInt(1));
            }
            return invoice;
        } catch (SQLException e) {
            // Concurrent creation can hit payments.order_id UNIQUE; return that persisted invoice.
            if (e.getErrorCode() == 1062) {
                Payment existing = getPaymentByOrderId(invoice.getOrderId());
                if (existing != null) return existing;
            }
            throw new IllegalStateException("Invoice could not be saved.", e);
        }
    }

    /** Atomically changes only an unpaid invoice to paid; never touches the kitchen order. */
    public Payment markPaid(int paymentId) { return markPaid(paymentId, null); }

    /** Atomically records the selected tender and marks one pending invoice paid. */
    public Payment markPaid(int paymentId, Payment.PaymentMethod method) {
        String update = method == null
                ? "UPDATE payments SET status = 'PAID', paid_at = CURRENT_TIMESTAMP WHERE payment_id = ? AND status = 'PENDING'"
                : "UPDATE payments SET status = 'PAID', payment_method = ?, paid_at = CURRENT_TIMESTAMP WHERE payment_id = ? AND status = 'PENDING'";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(update)) {
                    if (method == null) ps.setInt(1, paymentId);
                    else { ps.setString(1, method.name()); ps.setInt(2, paymentId); }
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM payments WHERE payment_id = ? FOR UPDATE")) {
                    ps.setInt(1, paymentId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalStateException("Invoice not found.");
                        Payment payment = mapResultSetToPayment(rs);
                        if (payment.getStatus() != Payment.PaymentStatus.PAID)
                            throw new IllegalStateException("Invoice could not be marked paid.");
                        conn.commit();
                        return payment;
                    }
                }
            } catch (SQLException | RuntimeException e) {
                try { conn.rollback(); } catch (SQLException rollbackError) { e.addSuppressed(rollbackError); }
                if (e instanceof SQLException sqlException) throw sqlException;
                throw (RuntimeException) e;
            }
        } catch (SQLException e) { throw new IllegalStateException("Payment could not be saved.", e); }
    }
    /** Compatibility method; new billing flow uses createInvoice and markPaid. */
    public boolean addPayment(Payment payment) {
        try { createInvoice(payment); return true; }
        catch (RuntimeException e) { return false; }
    }

    public boolean updatePaymentStatus(int paymentId, String status) {
        String sql = "UPDATE payments SET status = ?, paid_at = CASE WHEN ? = 'PAID' THEN CURRENT_TIMESTAMP ELSE paid_at END WHERE payment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status); ps.setString(2, status); ps.setInt(3, paymentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new IllegalStateException("Payment status could not be updated.", e); }
    }

    public List<Object[]> getPaymentSummary() {
        List<Object[]> rows = new ArrayList<>();
        String sql = "SELECT payment_method, COUNT(*), SUM(grand_total) FROM payments WHERE status = 'PAID' GROUP BY payment_method";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) rows.add(new Object[]{rs.getString(1), rs.getInt(2), rs.getBigDecimal(3)});
            return rows;
        } catch (SQLException e) { throw new IllegalStateException("Unable to load payment summary.", e); }
    }

    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setPaymentId(rs.getInt("payment_id")); payment.setOrderId(rs.getInt("order_id"));
        payment.setSubtotal(rs.getBigDecimal("subtotal").doubleValue());
        payment.setTaxAmount(rs.getBigDecimal("tax_amount").doubleValue());
        payment.setDiscount(rs.getBigDecimal("discount").doubleValue());
        payment.setGrandTotal(rs.getBigDecimal("grand_total").doubleValue());
        String method = rs.getString("payment_method"); if (method != null) payment.setPaymentMethod(Payment.PaymentMethod.valueOf(method));
        String status = rs.getString("status"); if (status != null) payment.setStatus(Payment.PaymentStatus.valueOf(status));
        payment.setPaidAt(rs.getTimestamp("paid_at"));
        return payment;
    }
}
