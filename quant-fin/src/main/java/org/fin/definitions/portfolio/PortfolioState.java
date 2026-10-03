package org.fin.definitions.portfolio;

import org.fin.definitions.assets.Instrument;
import org.fin.definitions.history.DataContext;

import java.util.Map;

public interface PortfolioState {
    double getCash();

    double getQuantity(Instrument instrument);

    Map<Instrument, Double> getPositions();

    double getNetAssetValue(DataContext dataContext);
}
