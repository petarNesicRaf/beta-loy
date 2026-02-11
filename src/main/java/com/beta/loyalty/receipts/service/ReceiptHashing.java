package com.beta.loyalty.receipts.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

public final class ReceiptHashing {
    private ReceiptHashing() {}

    public static String sha256ReceiptHash(UUID venueId,
                                           String externalReceiptId,
                                           String qrRaw,
                                           String issuedAtIso,
                                           String amountPlain,
                                           String currency) {
        String canonical = String.join("|",
                venueId.toString(),
                nvl(externalReceiptId),
                nvl(issuedAtIso),
                nvl(amountPlain),
                nvl(currency),
                nvl(qrRaw)
        );

        return sha256Hex(canonical);
    }

    private static String nvl(String s) {
        return s == null ? "" : s.trim();
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash receipt canonical string", e);
        }
    }
}
