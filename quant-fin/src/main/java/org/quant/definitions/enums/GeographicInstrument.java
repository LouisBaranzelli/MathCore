package org.quant.definitions.enums;

public interface GeographicInstrument extends Instrument {
    Country getCountry();
    Currency getCurrency();
}