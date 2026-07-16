package com.lcwd.electronicStore.ElectronicStore.helper;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentAmountHelperTest {

    @Test
    void convertsHighValueRupeeAmountsToPaise() {
        assertEquals(6_999_000L, PaymentAmountHelper.toPaise(69_990L));
        assertEquals(20_000_000L, PaymentAmountHelper.toPaise(200_000L));
    }

    @Test
    void rejectsAmountsThatOverflowLongPaiseStorage() {
        assertThrows(ArithmeticException.class, () -> PaymentAmountHelper.toPaise(Long.MAX_VALUE));
    }
}
