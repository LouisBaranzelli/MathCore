package org.series;

import lombok.Getter;

import java.time.ZoneId;
import java.util.Objects;


@Getter
public enum ZoneIdEnum {

    // --- UTC / GMT ---
    UTC("UTC", "Coordinated Universal Time"),
    GMT("GMT", "Greenwich Mean Time"),

    // --- Europe ---
    EUROPE_PARIS("Europe/Paris", "Central European Time (Paris, Frankfurt, Amsterdam)"),
    EUROPE_LONDON("Europe/London", "Greenwich Mean Time / British Summer Time"),
    EUROPE_ZURICH("Europe/Zurich", "Swiss Time (SIX Swiss Exchange)"),
    CET("CET", "Central European Time (Alias)"),

    // --- Amérique du Nord (Bourses US & Canada) ---
    AMERICA_NEW_YORK("America/New_York", "US Eastern Time (NYSE, NASDAQ)"),
    AMERICA_CHICAGO("America/Chicago", "US Central Time (CME Group)"),
    AMERICA_LOS_ANGELES("America/Los_Angeles", "US Pacific Time"),
    AMERICA_TORONTO("America/Toronto", "Toronto Stock Exchange (TSX)"),

    // --- Asie / Pacifique ---
    ASIA_TOKYO("Asia/Tokyo", "Japan Standard Time (TSE)"),
    ASIA_HONG_KONG("Asia/Hong_Kong", "Hong Kong Time (HKEX)"),
    ASIA_SINGAPORE("Asia/Singapore", "Singapore Time (SGX)"),
    ASIA_SHANGHAI("Asia/Shanghai", "China Standard Time"),
    AUSTRALIA_SYDNEY("Australia/Sydney", "Australian Eastern Time (ASX)");

    private final String id;
    private final String description;
    private final ZoneId zoneId;

    ZoneIdEnum(String id, String description) {
        this.id = id;
        this.description = description;
        this.zoneId = ZoneId.of(id); // Instanciation garantie valide au chargement de l'enum
    }

    /**
     * Recherche un ZoneIdEnum à partir d'une chaîne de caractères (ex: "Europe/Paris" ou "UTC").
     */
    public static ZoneIdEnum fromId(String id) {
        Objects.requireNonNull(id, "ZoneId cannot be null");
        for (ZoneIdEnum zone : values()) {
            if (zone.getId().equalsIgnoreCase(id.trim())) {
                return zone;
            }
        }
        throw new IllegalArgumentException("Unknown or unsupported ZoneId: " + id);
    }
}