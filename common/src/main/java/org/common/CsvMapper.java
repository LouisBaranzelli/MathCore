package org.common;

public interface CsvMapper<T> {

    String toCsvLine(T obj);

    T fromCsvLine(String csvLine);
}
