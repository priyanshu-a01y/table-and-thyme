package restaurant.model;

import java.sql.Timestamp;

public class Payment {
    private int paymentId;
    private int orderId;
    private double subtotal;
    private double taxAmount;
    private double discount;
    private double grandTotal;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private Timestamp paidAt;

    public enum PaymentMethod { CASH, CARD, UPI }
    public enum PaymentStatus { PAID, PENDING, REFUNDED }

    public Payment() {
    }

    public Payment(int paymentId, int orderId, double subtotal, double taxAmount, double discount, double grandTotal, PaymentMethod paymentMethod, PaymentStatus status, Timestamp paidAt) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.subtotal = subtotal;
        this.taxAmount = taxAmount;
        this.discount = discount;
        this.grandTotal = grandTotal;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.paidAt = paidAt;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(double grandTotal) {
        this.grandTotal = grandTotal;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Timestamp getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Timestamp paidAt) {
        this.paidAt = paidAt;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId=" + paymentId +
                ", orderId=" + orderId +
                ", grandTotal=" + grandTotal +
                ", status=" + status +
                '}';
    }
}
