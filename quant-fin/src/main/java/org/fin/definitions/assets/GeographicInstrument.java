package org.fin.definitions.assets;

public interface GeographicInstrument extends Purchasable {
    Country getCountry();
    Currency getCurrency();
}