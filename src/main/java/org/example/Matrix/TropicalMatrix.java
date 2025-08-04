package org.example.Matrix;

import java.util.Arrays;
import java.util.Random;

public class TropicalMatrix extends Matrix{
    public static final int INF = Integer.MAX_VALUE;

    /* ------------------------------------------------------------------ */
    /* Constructors                                                       */
    /* ------------------------------------------------------------------ */

    public TropicalMatrix(int[][] data) {
        super(data);
        for (int[] row : data) {
            for (int entry : row) {
                if (entry < 0) throw new IllegalArgumentException("Elements must be non-negative.");
            }
        }
    }

    public TropicalMatrix(TropicalMatrix other) {
        this(other.matrix);
    }

    public TropicalMatrix(int n, int v) {
        super(n, v);
        if (v < 0) throw new IllegalArgumentException("Value must be non-negative.");
    }

    /** Tropical identity matrix of dimension n (0 on diag, infinity elsewhere). */
    public TropicalMatrix(int n) {
        super(n);
        for (int i = 0; i < n; i++) {
            Arrays.fill(matrix[i], INF);
            matrix[i][i] = 0;
        }
    }

    /** Generates a random tropical matrix:
     * The chance of an entry being 0 is zeroChance, the chance of it being infinity is infChance,
     * otherwise, it will be a random number between 1 and max.
     * */
    public static TropicalMatrix random(int n, int max, double zeroChance, double infChance) {
        if (zeroChance < 0 || zeroChance > 1) throw new IllegalArgumentException("zeroChance must be between 0 and 1.");
        if (infChance < 0 || infChance > 1) throw new IllegalArgumentException("infChance must be between 0 and 1.");
        if (zeroChance + infChance > 1) throw new IllegalArgumentException("zeroChance + infChance must be between 0 and 1.");

        int[][] matrix = new int[n][n];
        infChance += zeroChance;
        Random random = new Random();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double r = 1 - random.nextDouble();
                if (r <= zeroChance) {
                    matrix[i][j] = 0;
                } else if (r <= infChance) {
                    matrix[i][j] = INF;
                } else {
                    matrix[i][j] = random.nextInt(max) + 1;
                }
            }
        }
        return new TropicalMatrix(matrix);
    }

    /* ------------------------------------------------------------------ */
    /* Semiring operations                                                */
    /* ------------------------------------------------------------------ */

    /** Tropical matrix product (min, +) */
    @Override
    public TropicalMatrix times(Matrix other) {
        ensureCompatible(other);
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int min = INF;
                for (int k = 0; k < n; k++) {
                    int sum = addInts(matrix[i][k], other.matrix[k][j]);
                    if (sum < min) min = sum;
                }
                result[i][j] = min;
            }
        }
        return new TropicalMatrix(result);
    }

    @Override
    public void timesInPlace(Matrix other) {
        ensureCompatible(other);
        other = new TropicalMatrix((TropicalMatrix) other); // create a copy in case other == this
        for (int i = 0; i < n; i++) {
            int[] newRow = new int[n];
            for (int j = 0; j < n; j++) {
                int min = INF;
                for (int k = 0; k < n; k++) {
                    int sum = addInts(matrix[i][k], other.matrix[k][j]);
                    if (sum < min) min = sum;
                }
                newRow[j] = min;
            }
            matrix[i] = newRow;
        }
    }

    @Override
    public TropicalMatrix pow(int k) {
        return (TropicalMatrix) pow(k, this, new TropicalMatrix(n));
    }

    /* ------------------------------------------------------------------ */
    /* Utilities                                                          */
    /* ------------------------------------------------------------------ */

    /** Tropical multiplication (∞ + x = ∞). */
    private static int addInts(int x, int y) {
        if (x == INF || y == INF) return INF;
        long z = x + (long) y;
        if (z >= INF) throw new RuntimeException("Overflow.");
        return (int) z;
    }

    public TropicalMatrix normalized() {
        int[][] data = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                data[i][j] = matrix[i][j] == INF ? INF : Math.min(1, matrix[i][j]);
            }
        }
        return new TropicalMatrix(data);
    }

    public BooleanMatrix booleanAbstraction() {
        int[][] data = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                data[i][j] = matrix[i][j] == INF ? 0 : 1;
            }
        }
        return new BooleanMatrix(data);
    }

    public int maxValue() {
        int max = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (matrix[i][j] == INF) continue;
                max = Math.max(max, matrix[i][j]);
            }
        }
        return max;
    }

    @Override
    protected void ensureSameType(Matrix other) {
        if (!(other instanceof TropicalMatrix)) throw new IllegalArgumentException("Incompatible matrix type.");
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TropicalMatrix && super.equals(other);
    }

    @Override
    public String toString() {
        String[][] stringMatrix = new String[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                stringMatrix[i][j] = matrix[i][j] == INF ? "-" : Integer.toString(matrix[i][j]);
            }
        }
        return Matrix.toString(stringMatrix);
    }
}
