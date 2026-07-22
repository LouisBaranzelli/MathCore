package org.quant.definitions;

public interface FieldInstrument extends Instrument {
    Field getField();
    Currency getCurrency();
}