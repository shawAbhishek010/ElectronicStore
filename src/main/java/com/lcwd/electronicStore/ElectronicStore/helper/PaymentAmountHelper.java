package com.lcwd.electronicStore.ElectronicStore.helper;

/*
Purpose:
Converts payment amounts between rupees and Razorpay paise safely.
*/
public final class PaymentAmountHelper {

    private static final long PAISE_PER_RUPEE = 100L;

    private PaymentAmountHelper() {
    }

    public static long toPaise(long amountInRupees) {
        return Math.multiplyExact(amountInRupees, PAISE_PER_RUPEE);
    }
}
