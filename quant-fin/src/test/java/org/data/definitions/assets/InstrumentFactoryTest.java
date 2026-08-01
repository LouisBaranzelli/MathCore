package org.data.definitions.assets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InstrumentFactoryTest {

    @Test
    public void test(){
        Stock stock = Stock.MC;
        assertEquals(stock, InstrumentFactory.from(stock.name()));
    }
}