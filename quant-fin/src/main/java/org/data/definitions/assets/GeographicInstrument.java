package org.data.definitions.assets;

public interface GeographicInstrument extends Purchasable {
    Country getCountry();
    Currency getCurrency();
}