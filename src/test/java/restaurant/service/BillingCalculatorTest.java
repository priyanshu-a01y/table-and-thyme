package restaurant.service;

import org.junit.Test;
import java.math.BigDecimal;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class BillingCalculatorTest {
    @Test public void discountIsAppliedBeforeTax() {
        BillingCalculator.Totals t = BillingCalculator.calculate(new BigDecimal("100.00"), new BigDecimal("10"));
        assertEquals(0, new BigDecimal("100.00").compareTo(t.subtotal()));
        assertEquals(0, new BigDecimal("10.00").compareTo(t.discount()));
        assertEquals(0, new BigDecimal("4.50").compareTo(t.tax()));
        assertEquals(0, new BigDecimal("94.50").compareTo(t.total()));
    }
    @Test public void roundsAtCurrencyPrecision() {
        BillingCalculator.Totals t = BillingCalculator.calculate(new BigDecimal("10.05"), new BigDecimal("12.5"));
        assertEquals(0, new BigDecimal("1.26").compareTo(t.discount()));
        assertEquals(0, new BigDecimal("0.44").compareTo(t.tax()));
        assertEquals(0, new BigDecimal("9.23").compareTo(t.total()));
    }
    @Test public void computesLineTotalsFromQuantityAndUnitPrice() {
        assertEquals(0, new BigDecimal("37.50").compareTo(BillingCalculator.lineTotal(12.5, 3)));
    }
    @Test public void rejectsInvalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> BillingCalculator.calculate(new BigDecimal("1"), new BigDecimal("-1")));
        assertThrows(IllegalArgumentException.class, () -> BillingCalculator.calculate(new BigDecimal("1"), new BigDecimal("100.01")));
        assertThrows(IllegalArgumentException.class, () -> BillingCalculator.calculate(new BigDecimal("-0.01"), BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> BillingCalculator.calculate(BigDecimal.ZERO, BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> BillingCalculator.lineTotal(2.5, 0));
    }
}
