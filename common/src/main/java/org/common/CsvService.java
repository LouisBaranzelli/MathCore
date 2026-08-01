package org.common;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CsvService<T> {
    private final CsvMapper<T> mapper;

    public CsvService(CsvMapper<T> mapper) {

        this.mapper = mapper;
    }

    public List<T> readFromFile(Path path) throws IOException {
        List<T> list = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                T obj = mapper.fromCsvLine(line);
                list.add(obj);
            }
        }
        return list;
    }

    public void writeToFile(Path path, List<T> objects) throws IOException {
        try (BufferedWriter bw = Files.newBufferedWriter(path)) {
            for (T obj : objects) {
                bw.write(mapper.toCsvLine(obj));
                bw.newLine();
            }
        }
    }
}