package com.ecommerce.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class CsvTestDataReader {
    private CsvTestDataReader() {
    }

    public static Object[][] read(String resourcePath) throws IOException {
        try (InputStream inputStream = CsvTestDataReader.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IllegalStateException(resourcePath + " not found in test resources");
            }

            List<Object[]> rows = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                reader.readLine();
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.isBlank()) {
                        continue;
                    }
                    rows.add(line.split(",", -1));
                }
            }
            return rows.toArray(new Object[0][]);
        }
    }
}
