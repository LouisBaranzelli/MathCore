package org.quant.definitions.strategies;

import org.quant.definitions.portfolio.PortfolioState;
import org.quant.definitions.history.DataContext;

public interface PurchaseStrategy {

    TargetAllocations test(DataContext dataContext, PortfolioState portfolioState);

}
