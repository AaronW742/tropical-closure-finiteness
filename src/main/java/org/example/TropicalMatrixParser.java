package org.example;

import org.example.Matrix.TropicalMatrix;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * AI generated code
 * */
public final class TropicalMatrixParser {
    /*
    Example usage

    Copy and paste console output into a multiline string, resulting in:
    String s = """
                1  -  0 \s
                -  0  - \s
                0  1  - \s

                0  -  1 \s
                1  0  - \s
                1  -  -""";

    Then convert to a tropical matrix with parse():
    TropicalMatrix[] matrices = TropicalMatrixParser.parse(s);
    */


    public static final int INF = Integer.MAX_VALUE;

    private static final Pattern SPACE_SPLIT = Pattern.compile("\\s+");
    private static final Pattern BLOCK_SPLIT = Pattern.compile("(?:\\R\\s*\\R)+"); // ≥1 blank line

    public static TropicalMatrix[] parse(String source) {
        List<TropicalMatrix> matrices = new ArrayList<>();

        // 1) split into blocks
        String[] blocks = BLOCK_SPLIT.split(source.strip());

        for (String block : blocks) {
            // 2) split block into trimmed, non‑empty lines
            List<int[]> rows = new ArrayList<>();
            for (String line : block.strip().split("\\R")) {
                if (line.isBlank()) continue;
                String[] tokens = SPACE_SPLIT.split(line.trim());
                int[] row = new int[tokens.length];
                for (int i = 0; i < tokens.length; i++) {
                    row[i] = "-".equals(tokens[i]) ? INF : Integer.parseInt(tokens[i]);
                }
                rows.add(row);
            }

            // 3) verify square & size limit
            int n = rows.size();
            for (int[] r : rows)
                if (r.length != n)
                    throw new IllegalArgumentException("Matrix is not square: " + n + "×" + r.length);

            // 4) build TropicalMatrix
            matrices.add(new TropicalMatrix(rows.toArray(new int[n][n])));
        }

        return matrices.toArray(new TropicalMatrix[0]);
    }
}
