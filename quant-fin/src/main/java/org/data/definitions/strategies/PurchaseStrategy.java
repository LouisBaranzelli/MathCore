package org.data.definitions.strategies;

import org.data.definitions.portfolio.PortfolioState;
import org.data.definitions.history.DataContext;

public interface PurchaseStrategy {

    TargetAllocations test(DataContext dataContext, PortfolioState portfolioState);

}
