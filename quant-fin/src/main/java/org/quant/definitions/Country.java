package org.quant.definitions;

import lombok.Getter;

public enum Country {
    FR("France");

    @Getter
    private final String name;

    Country(String name) {
        this.name = name;
    }
}
