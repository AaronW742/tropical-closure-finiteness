package org.example.Matrix;

import java.util.Arrays;
import java.util.Random;

public class BooleanMatrix extends Matrix {

    /* ------------------------------------------------------------------ */
    /* Constructors                                                       */
    /* ------------------------------------------------------------------ */

    public BooleanMatrix(int[][] data) {
        super(data);
        for (int[] row : data) {
            for (int entry : row) {
                if (entry != 0 && entry != 1) throw new IllegalArgumentException("Elements must be 0 or 1.");
            }
        }
    }

    public BooleanMatrix(BooleanMatrix other) {
        this(other.matrix);
    }

    public BooleanMatrix(int n, int v) {
        super(n, v);
        if (v != 0 && v != 1) throw new IllegalArgumentException("Value must be 0 or 1.");
    }

    /** Boolean identity matrix of dimension n (1 on diag, 0 elsewhere). */
    public BooleanMatrix(int n) {
        super(n);
        for (int i = 0; i < n; i++) {
            Arrays.fill(matrix[i], 0);
            matrix[i][i] = 1;
        }
    }

    public static BooleanMatrix random(int n, double oneChance) {
        if (oneChance < 0 || oneChance > 1) throw new IllegalArgumentException("oneChance must be between 0 and 1.");

        int[][] matrix = new int[n][n];
        Random random = new Random();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                double r = 1 - random.nextDouble();
                matrix[i][j] = r <= oneChance ? 1 : 0;
            }
        }
        return new BooleanMatrix(matrix);
    }

    /* ------------------------------------------------------------------ */
    /* Semiring operations                                                */
    /* ------------------------------------------------------------------ */

    @Override
    public BooleanMatrix times(Matrix other) {
        ensureCompatible(other);
        int[][] result = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    if (matrix[i][k] == 1 && other.matrix[k][j] == 1) {
                        result[i][j] = 1;
                        break;
                    }
                }
            }
        }
        return new BooleanMatrix(result);
    }

    @Override
    public void timesInPlace(Matrix other) {
        ensureCompatible(other);
        other = new BooleanMatrix((BooleanMatrix) other);   // create a copy in case other == this
        for (int i = 0; i < n; i++) {
            int[] newRow = new int[n];
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    if (matrix[i][k] == 1 && other.matrix[k][j] == 1) {
                        newRow[j] = 1;
                        break;
                    }
                }
            }
            matrix[i] = newRow;
        }
    }

    @Override
    public BooleanMatrix pow(int k) {
        return (BooleanMatrix) pow(k, this, new BooleanMatrix(n));
    }

    // here, transitive closure is reachability with 1 or more steps (not 0!!)
    public BooleanMatrix transitiveClosure() {
        BooleanMatrix result = new BooleanMatrix(this);
        BooleanMatrix product = new BooleanMatrix(this);
        for (int i = 2; i <= n; i++) {
            product.timesInPlace(this); // stores reachability with i edges
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    result.matrix[j][k] |= product.matrix[j][k];
                }
            }
        }
        return result;
    }

    /* ------------------------------------------------------------------ */
    /* Utilities                                                          */
    /* ------------------------------------------------------------------ */

    @Override
    protected void ensureSameType(Matrix other) {
        if (!(other instanceof BooleanMatrix)) throw new IllegalArgumentException("Incompatible matrix type.");
    }
}
