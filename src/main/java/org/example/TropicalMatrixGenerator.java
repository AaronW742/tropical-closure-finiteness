package org.example;

import org.example.Matrix.TropicalMatrix;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TropicalMatrixGenerator {

    // Get a random instance of the problem with the specified number of matrices, dimension of the matrices and maximum value for the entries
    public static TropicalMatrix[] getRandomMatrices(int numberOfMatrices, int dimension, int maxValue) {
        // use set to avoid duplicates
        Set<TropicalMatrix> matrices = new HashSet<>(numberOfMatrices);
        while (matrices.size() < numberOfMatrices) {
            matrices.add(TropicalMatrix.random(dimension, maxValue, 0.333, 0.333));
        }
        return matrices.toArray(new TropicalMatrix[numberOfMatrices]);
    }

    // Get a list of all the matrices with the specified dimension and maximum value for the entries
    public static List<TropicalMatrix> getAllMatrices(int dimension, int maxValue) {
        final int[] VALUES = new int[maxValue + 2];
        for (int i = 0; i < VALUES.length; i++) {
            VALUES[i] = i;
        }
        VALUES[VALUES.length - 1] = Integer.MAX_VALUE;

        final int MATRIX_COUNT = (int) Math.pow(VALUES.length, dimension * dimension);

        List<TropicalMatrix> all = new ArrayList<>(MATRIX_COUNT);

        for (int i = 0; i < MATRIX_COUNT; i++) {
            int code = i;
            int[][] matrix = new int[dimension][dimension];

            for (int j = 0; j < dimension * dimension; j++) {
                matrix[j / dimension][j % dimension] = VALUES[code % VALUES.length];
                code /= VALUES.length;
            }
            all.add(new TropicalMatrix(matrix));
        }
        return all;
    }
}
