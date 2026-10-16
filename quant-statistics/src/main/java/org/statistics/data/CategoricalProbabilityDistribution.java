package org.statistics.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Représente une distribution de probabilité théorique sur des catégories discrètes.
 * Appartient au module statistique/inférence.
 */
public class CategoricalProbabilityDistribution {

    private static final Logger log = LoggerFactory.getLogger(CategoricalProbabilityDistribution.class);

    private final Map<String, Double> probabilitiesByCategory;

    public CategoricalProbabilityDistribution(Map<String, Double> probabilitiesByCategory) {
        if (probabilitiesByCategory == null || probabilitiesByCategory.isEmpty()) {
            log.error("Attempted to initialize CategoricalProbabilityDistribution with null or empty map.");
            throw new IllegalArgumentException("Theoretical probabilities map cannot be null or empty.");
        }

        double sum = 0.0;
        for (Map.Entry<String, Double> entry : probabilitiesByCategory.entrySet()) {
            double p = entry.getValue();
            if (p < 0.0 || p > 1.0) {
                log.error("Invalid probability value for category '{}': {}", entry.getKey(), p);
                throw new IllegalArgumentException("Probabilities must be between 0.0 and 1.0.");
            }
            sum += p;
        }

        if (Math.abs(sum - 1.0) > 1e-6) {
            log.error("Theoretical probabilities do not sum to 1.0 (sum = {})", sum);
            throw new IllegalArgumentException("Theoretical probabilities must sum to 1.0.");
        }

        this.probabilitiesByCategory = new LinkedHashMap<>(probabilitiesByCategory);
        log.debug("Created CategoricalProbabilityDistribution with {} categories.", this.probabilitiesByCategory.size());
    }

    public double getProbability(String category) {
        if (!probabilitiesByCategory.containsKey(category)) {
            log.warn("Requested probability for unknown category: '{}'", category);
            throw new IllegalArgumentException("Unknown category: " + category);
        }
        return probabilitiesByCategory.get(category);
    }

    public Map<String, Double> getProbabilitiesByCategory() {
        return Collections.unmodifiableMap(probabilitiesByCategory);
    }
}