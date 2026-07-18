package org.series.timegrid;

public class IrregularTimeGrid implements TimeGrid {

    private final long[] timestamps;
/*
  * NOTE D'ARCHITECTURE & PERFORMANCE :
            L'utilisation de l'interface TimeGrid permet d'évoluer vers des implémentations
            sans stockage de tableau physique (O(1) mémoire). Par exemple, une implémentation
            calculant mathématiquement les timestamps par sauts cycliques de week-ends,
            complétée par une micro-table de sauts (indexée) pour les jours fériés.
 */

    public IrregularTimeGrid(long[] timestamps) {
        this.validate(timestamps);
        this.timestamps = timestamps.clone();
    }

    @Override
    public int size() {
        return timestamps.length;
    }

    @Override
    public long getTimeStamp(int index) {
        return timestamps[index];
    }

    public void validate(long[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Time serie cannot be empty");
        }

        for (int i = 0; i < values.length; i++) {
            if (i > 0 && values[i] <= values[i - 1]) {
                throw new IllegalArgumentException("Timestamps must be strictly increasing. Violation at index " + i);
            }
        }
    }
}
