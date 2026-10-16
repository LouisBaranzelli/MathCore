package org.statistics.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Représente des données multinomiales catégorisées (ex: classes de notation, intervalles).
 * Appartient au module statistique/inférence.
 */
public class CategoricalMultinomialData {

    private static final Logger log = LoggerFactory.getLogger(CategoricalMultinomialData.class);

    // LinkedHashMap pour préserver l'ordre strict des catégories
    private final Map<String, Integer> countsByCategory;
    private final int totalCount;

    public CategoricalMultinomialData(Map<String, Integer> countsByCategory) {
        if (countsByCategory == null || countsByCategory.isEmpty()) {
            log.error("Attempted to initialize CategoricalMultinomialData with null or empty map.");
            throw new IllegalArgumentException("Les données catégorielles ne peuvent pas être nulles ou vides.");
        }

        int sum = 0;
        for (Map.Entry<String, Integer> entry : countsByCategory.entrySet()) {
            int count = entry.getValue();
            if (count < 0) {
                log.error("Negative count for category '{}': {}", entry.getKey(), count);
                throw new IllegalArgumentException("Les comptages par catégorie doivent être positifs ou nuls.");
            }
            sum += count;
        }

        if (sum == 0) {
            log.error("Total count cannot be zero across categories.");
            throw new IllegalArgumentException("Le nombre total d'observations (n) doit être strictement positif.");
        }

        this.countsByCategory = new LinkedHashMap<>(countsByCategory);
        this.totalCount = sum;
        log.debug("Created CategoricalMultinomialData with {} categories and n = {} total observations.", this.countsByCategory.size(), totalCount);
    }

    public int getCount(String category) {
        if (!countsByCategory.containsKey(category)) {
            log.warn("Requested count for unknown category: '{}'", category);
            throw new IllegalArgumentException("Catégorie inconnue : " + category);
        }
        return countsByCategory.get(category);
    }

    public Map<String, Integer> getCountsByCategory() {
        return Collections.unmodifiableMap(countsByCategory);
    }

    public int getCategoriesCount() {
        return countsByCategory.size();
    }

    public int getTotalCount() {
        return totalCount;
    }
}