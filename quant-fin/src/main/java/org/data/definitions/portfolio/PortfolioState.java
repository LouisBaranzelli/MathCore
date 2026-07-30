package org.data.definitions.portfolio;

import org.data.definitions.assets.Instrument;
import org.data.definitions.history.DataContext;

import java.util.Map;

public interface PortfolioState {
    double getCash();

    double getQuantity(Instrument instrument);

    Map<Instrument, Double> getPositions();

    double getNetAssetValue(DataContext dataContext);
}
