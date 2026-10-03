package org.fin.definitions.strategies;

import org.fin.definitions.portfolio.PortfolioState;
import org.fin.definitions.history.DataContext;

public interface PurchaseStrategy {

    TargetAllocations test(DataContext dataContext, PortfolioState portfolioState);

}
