package org.quant.definitions.portfolio;

import org.quant.definitions.assets.Instrument;
import org.quant.definitions.history.DataContext;

import java.util.Map;

public interface PortfolioState {
    double getCash();

    double getQuantity(Instrument instrument);

    Map<Instrument, Double> getPositions();

    double getNetAssetValue(DataContext dataContext);
}
