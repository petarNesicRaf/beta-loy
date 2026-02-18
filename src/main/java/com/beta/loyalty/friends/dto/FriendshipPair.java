package com.beta.loyalty.friends.dto;

import java.util.UUID;

public final class FriendshipPair {
    private FriendshipPair() {}

    public static Pair canonical(UUID x, UUID y) {
        if (x.compareTo(y) <= 0) return new Pair(x, y);
        return new Pair(y, x);
    }

    public record Pair(UUID a, UUID b) {}

}
