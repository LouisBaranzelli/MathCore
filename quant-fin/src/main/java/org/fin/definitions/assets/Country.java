package org.fin.definitions.assets;

import lombok.Getter;

public enum Country {
    FR("France"),
    DE("Allemagne");

    @Getter
    private final String name;

    Country(String name) {
        this.name = name;
    }
}
