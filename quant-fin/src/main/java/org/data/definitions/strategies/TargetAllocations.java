package org.data.definitions.strategies;

import org.data.definitions.assets.Purchasable;

import java.util.Map;

public final class TargetAllocations {
    private final Map<Purchasable, Double> targetWeights;

    public TargetAllocations(Map<Purchasable, Double> targetWeights) {
        this.targetWeights = Map.copyOf(targetWeights);
    }

    public double getTargetWeight(Purchasable purchasable) {
        return targetWeights.getOrDefault(purchasable, 0.0);
    }

    public Map<Purchasable, Double> getAll() {
        return targetWeights;
    }
}
