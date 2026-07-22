package org.quant.definitions;

public interface GeographicInstrument extends Instrument {
    Country getCountry();
    Currency getCurrency();
}