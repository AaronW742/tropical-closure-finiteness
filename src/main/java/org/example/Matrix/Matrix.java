package org.example.Matrix;

import java.util.Arrays;
import java.util.Objects;

public abstract class Matrix {
    protected final int n;            // dimension
    protected final int[][] matrix;

    /* ------------------------------------------------------------------ */
    /* Constructors                                                       */
    /* ------------------------------------------------------------------ */

    /** From 2d array */
    public Matrix(int[][] data) {
        Objects.requireNonNull(data, "Matrix must be non-null.");
        this.n = data.length;
        if (n == 0) throw new IllegalArgumentException("Matrix dimension must be non-zero.");
        if (data[0].length != n) throw new IllegalArgumentException("Matrix must be a square matrix.");

        this.matrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            matrix[i] = Arrays.copyOf(data[i], n);
        }
    }

    /** From other matrix */
    public Matrix(Matrix other) {
        this(other.matrix);
    }

    /** With one constant for all values */
    public Matrix(int n, int v) {
        if (n <= 0) throw new IllegalArgumentException("Size must be greater than 0.");
        this.n = n;
        matrix = new int[n][n];
        for (int[] row : matrix) Arrays.fill(row, v);
    }

    public Matrix(int n) {
        if (n <= 0) throw new IllegalArgumentException("Size must be greater than 0.");
        this.n = n;
        matrix = new int[n][n];
    }

    /* ------------------------------------------------------------------ */
    /* Basic accessors                                                    */
    /* ------------------------------------------------------------------ */

    public int size() { return n; }

    public int get(int i, int j) { return matrix[i][j]; }

    public void set(int i, int j, int v) { matrix[i][j] = v; }

    /* ------------------------------------------------------------------ */
    /* Semiring operations                                                */
    /* ------------------------------------------------------------------ */

    public abstract Matrix times(Matrix other); // result should be a separate object, original matrices should remain unchanged

    public abstract void timesInPlace(Matrix other); // this should be overwritten by the result

    // subclasses should implement pow by calling the method below this one
    public abstract Matrix pow(int k);

    // general square and multiply
    protected Matrix pow(int k, Matrix base, Matrix identity) {
        if (k < 0) throw new IllegalArgumentException("Power must be non-negative.");
        int exp = k;
        base = base.times(identity);    // creates copy of base
        Matrix result = identity.times(identity);   // creates copy of identity
        while (exp > 0) {
            if (exp % 2 == 1) result.timesInPlace(base);
            base.timesInPlace(base);
            exp >>= 1;
        }
        return result;
    }

    /* ------------------------------------------------------------------ */
    /* Utilities                                                          */
    /* ------------------------------------------------------------------ */

    protected void ensureCompatible(Matrix other) {
        ensureSameType(other);
        ensureSameSize(other);
    }

    protected void ensureSameSize(Matrix other) {
        if (n != other.n) throw new IllegalArgumentException("Matrix dimensions differ.");
    }

    protected abstract void ensureSameType(Matrix other);

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Matrix otherMatrix) || otherMatrix.size() != n) return false;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (matrix[i][j] != otherMatrix.matrix[i][j]) return false;
            }
        }
        return true;
    }

    // ensures that this works with things like hashsets
    @Override
    public int hashCode() {
        return Arrays.deepHashCode(matrix);
    }

    @Override
    public String toString() {
        String[][] stringMatrix = new String[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                stringMatrix[i][j] = Integer.toString(matrix[i][j]);
            }
        }
        return toString(stringMatrix);
    }

    protected static String toString(String[][] matrix) {
        int n = matrix.length;
        int entrySize = 0;
        for (String[] row : matrix) {
            for (int j = 0; j < n; j++) {
                entrySize = Math.max(entrySize, row[j].length());
            }
        }

        StringBuilder sb = new StringBuilder();
        for (String[] row : matrix) {
            for (String entry : row) sb.append(entry).append(" ".repeat(entrySize - entry.length())).append("  ");
            sb.append('\n');
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
}
