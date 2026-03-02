package com.beta.loyalty.redemptions.dto;

import java.util.UUID;

public final class RedemptionCodeHelper {
    private RedemptionCodeHelper() {}
    public static String generate() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
