package org.series;

public class IrregularTimeGrid implements TimeGrid{

    private final long[] timestamps;
/*
  * NOTE D'ARCHITECTURE & PERFORMANCE :
            L'utilisation de l'interface TimeGrid permet d'évoluer vers des implémentations
            sans stockage de tableau physique (O(1) mémoire). Par exemple, une implémentation
            calculant mathématiquement les timestamps par sauts cycliques de week-ends,
            complétée par une micro-table de sauts (indexée) pour les jours fériés.
 */

    public IrregularTimeGrid(long[] timestamps) throws NullValueException {
        this.validate();
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

    public void validate() throws NullValueException {
        if (size() == 0) {
            throw new IllegalArgumentException("Time serie cannot be empty");
        }

        for (int i = 0; i < size(); i++) {
            if (i > 0 && getTimeStamp(i) <= getTimeStamp(i - 1)) {
                throw new IllegalArgumentException("Timestamps must be strictly increasing. Violation at index " + i);
            }
        }
    }
}
