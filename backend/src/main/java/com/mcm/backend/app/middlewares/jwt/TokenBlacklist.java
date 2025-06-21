package com.mcm.backend.app.middlewares.jwt;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TokenBlacklist {

    private static final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public static void blacklist(String token) {
        blacklistedTokens.add(token);
    }

    public static boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}
