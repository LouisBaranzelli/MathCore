package org.quant.definitions.assets;

public interface FieldInstrument extends Purchasable {
    Field getField();
    Currency getCurrency();
}