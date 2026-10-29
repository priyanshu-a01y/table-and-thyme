package restaurant.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Decimal-safe billing arithmetic shared by invoices and focused tests. */
public final class BillingCalculator {
    private static final BigDecimal TAX_RATE = new BigDecimal("0.05");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private BillingCalculator() { }

    public static BigDecimal lineTotal(double unitPrice, int quantity) {
        if (!Double.isFinite(unitPrice) || unitPrice < 0 || quantity <= 0) throw new IllegalArgumentException("Item quantity and unit price must be valid.");
        return BigDecimal.valueOf(unitPrice).multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
    }

    public static Totals calculate(BigDecimal subtotal, BigDecimal discountPercent) {
        if (subtotal == null || subtotal.signum() <= 0) throw new IllegalArgumentException("Subtotal must be greater than zero.");
        if (discountPercent == null || discountPercent.signum() < 0 || discountPercent.compareTo(HUNDRED) > 0)
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        BigDecimal roundedSubtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal discount = roundedSubtotal.multiply(discountPercent).divide(HUNDRED, 2, RoundingMode.HALF_UP);
        BigDecimal taxable = roundedSubtotal.subtract(discount);
        BigDecimal tax = taxable.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        return new Totals(roundedSubtotal, discount, tax, taxable.add(tax).setScale(2, RoundingMode.HALF_UP));
    }

    public record Totals(BigDecimal subtotal, BigDecimal discount, BigDecimal tax, BigDecimal total) { }
}
