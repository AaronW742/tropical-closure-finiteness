package org.example;

import org.example.Matrix.BooleanMatrix;
import org.example.Matrix.TropicalMatrix;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DecisionAlgorithms {

    /**
     * Iteratively builds all words of a certain length.
     * If the instance is bounded, this will terminate, because some iteration will not add new products.
     * If this doesn't terminate within timeoutSeconds, it assumes that the instance is unbounded.
     * Without the timeout, this would never terminate on an unbounded instance, effectively "semi-deciding" the problem.
     * */
    public static boolean semiDecide(TropicalMatrix[] matrices, double timeoutSeconds) {
        if (matrices == null || matrices.length < 1) throw new IllegalArgumentException("Matrices is null or empty.");
        int n = matrices[0].size();

        Set<TropicalMatrix> partialSet = new HashSet<>();   // only keeps results from words with the same length (from last iteration)
        Set<TropicalMatrix> fullSet = new HashSet<>();      // keeps results from words of all lengths
        partialSet.add(new TropicalMatrix(n));
        fullSet.add(new TropicalMatrix(n));

        long endTime = System.currentTimeMillis() + Math.round(timeoutSeconds * 1000);
        while (System.currentTimeMillis() < endTime) {
            Set<TropicalMatrix> next = new HashSet<>();
            for (TropicalMatrix m1 : partialSet) {
                for (TropicalMatrix m2 : matrices) {
                    next.add(m1.times(m2));
                }
            }
            if (partialSet.equals(next) || fullSet.containsAll(next)) return true;
            fullSet.addAll(next);
            partialSet = next;
        }

        return false;
    }

    /**
     * Essentially the same as semiDecide():
     * The first entry in the returned list is 1 or 0 corresponding to true or false of semiDecide(),
     * the second entry is the maximum value (less than infinity) of all entries in all words,
     * that was found either before returning true (in which case it is the correct bound)
     * or before the timeout (in which case it may be used as an indicator that the timeout is set too low).
     * */
    public static List<Integer> semiDecideMaxValue(TropicalMatrix[] matrices, double timeoutSeconds) {
        if (matrices == null || matrices.length < 1) throw new IllegalArgumentException("Matrices is null or empty.");
        int n = matrices[0].size();

        Set<TropicalMatrix> partialSet = new HashSet<>();
        Set<TropicalMatrix> fullSet = new HashSet<>();
        partialSet.add(new TropicalMatrix(n));
        fullSet.add(new TropicalMatrix(n));

        boolean bounded = false;
        long endTime = System.currentTimeMillis() + Math.round(timeoutSeconds * 1000);
        while (System.currentTimeMillis() < endTime) {
            Set<TropicalMatrix> next = new HashSet<>();
            for (TropicalMatrix m1 : partialSet) {
                for (TropicalMatrix m2 : matrices) {
                    next.add(m1.times(m2));
                }
            }
            if (partialSet.equals(next) || fullSet.containsAll(next)) {
                bounded = true;
                break;
            }
            fullSet.addAll(next);
            partialSet = next;
        }
        int max = 0;
        for (TropicalMatrix m : fullSet) {
            max = Math.max(max, m.maxValue());
        }
        return List.of(bounded ? 1 : 0, max);
    }

    /**
     * Efficient algorithm to decide the problem in the special case that the input is only a single matrix.
     * */
    public static boolean decideOneMatrix(TropicalMatrix matrix) {
        int n = matrix.size();

        boolean[] zeroLoop = new boolean[n];
        TropicalMatrix product = new TropicalMatrix(n);
        for (int i = 0; i < n; i++) {
            product.timesInPlace(matrix);
            for (int j = 0; j < n; j++) {
                if (product.get(j, j) == 0) zeroLoop[j] = true;
            }
        }

        BooleanMatrix abstraction = matrix.booleanAbstraction();
        BooleanMatrix reachable = abstraction.transitiveClosure();
        for (int i = 0; i < n; i++) {
            if (reachable.get(i, i) == 0) continue;
            boolean zeroLoopIsReachable = false;
            for (int j = 0; j < n; j++) {
                if (reachable.get(i, j) == 1 && zeroLoop[j] && reachable.get(j, i) == 1) {
                    zeroLoopIsReachable = true;
                    break;
                }
            }
            if (!zeroLoopIsReachable) return false;
        }
        return true;
    }

    /**
     * This is a full algorithm for the general problem (more than one matrix allowed),
     * IF the proposed bound is proven to be correct ((dimension - 1) * 2 * maxValue * matrices.length).
     * It essentially does the same thing as semiDecide(), without the need for a timeout, but instead terminating
     * when the bound is exceeded (which will eventually happen if the instance is unbounded).
     * */
    public static boolean decideWithBound(TropicalMatrix[] matrices) {
        if (matrices == null || matrices.length < 1) throw new IllegalArgumentException("Matrices array is null or empty.");
        int dimension = matrices[0].size();
        int maxValue = 0;
        for (TropicalMatrix matrix : matrices) {
            maxValue = Math.max(maxValue, matrix.maxValue());
        }

        final int bound = (dimension - 1) * 2 * maxValue * matrices.length;

        Set<TropicalMatrix> partialSet = new HashSet<>();   // only keeps results from words with the same length (-> from last iteration)
        Set<TropicalMatrix> fullSet = new HashSet<>();      // keeps results from words of all lengths (-> from all iterations)
        partialSet.add(new TropicalMatrix(dimension));
        fullSet.add(new TropicalMatrix(dimension));

        while (true) {
            Set<TropicalMatrix> next = new HashSet<>();
            for (TropicalMatrix m1 : partialSet) {
                for (TropicalMatrix m2 : matrices) {
                    next.add(m1.times(m2));
                }
            }

            if (partialSet.equals(next) || fullSet.containsAll(next)) {
                return true;
            }

            int max = 0;
            for (TropicalMatrix m : next) {
                max = Math.max(max, m.maxValue());
            }

            if (max > bound) {
                return false;
            }

            fullSet.addAll(next);
            partialSet = next;
        }
    }


    /**
     * This algorithm is outdated, because the proposed condition is not equivalent to the unboundedness of the instance:
     * If this returns true, the instance is bounded. However, on some bounded instances this will return false,
     * because the proposed condition is too strong.
     * */
    @Deprecated
    public static boolean decide(TropicalMatrix[] matrices) {
        if (matrices == null || matrices.length < 1) throw new IllegalArgumentException("Matrices array is null or empty.");
        int n = matrices[0].size();

        BooleanMatrix zeroIntersection = new BooleanMatrix(n, 1);   // (i, j) edge has weight 0 in all matrices iff zeroIntersection[i][j] == 1
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (TropicalMatrix matrix : matrices) {
                    if (matrix.get(i, j) > 0) {
                        zeroIntersection.set(i, j, 0);
                        break;
                    }
                }
            }
        }

        boolean[] zeroLoop = new boolean[n];    // given an arbitrary word (of specific length or a multiple), node i can reach itself with only 0-edges iff zeroLoop[i] == true
        BooleanMatrix product = new BooleanMatrix(zeroIntersection);    // only used to calculate zeroLoop
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (product.get(j, j) == 1) {
                    zeroLoop[j] = true;
                }
            }
            product.timesInPlace(zeroIntersection);
        }

        BooleanMatrix edgeUnion = new BooleanMatrix(n, 0); // there is an (i, j) edge in some matrix iff edgeUnion[i][j] == 1
        BooleanMatrix edgeIntersection = new BooleanMatrix(n, 1); // there is an (i, j) edge in every matrix iff edgeUnion[i][j] == 1
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                for (TropicalMatrix matrix : matrices) {
                    if (matrix.get(i, j) < Integer.MAX_VALUE) {
                        edgeUnion.set(i, j, 1);
                    } else {
                        edgeIntersection.set(i, j, 0);
                    }
                }
            }
        }

        BooleanMatrix pathUnion = edgeUnion.transitiveClosure();
        BooleanMatrix pathIntersection = edgeIntersection.transitiveClosure();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (pathUnion.get(i, j) == 0) continue;

                boolean wordWithLoopExists = false;
                for (int k = 0; k < n; k++) {
                    if (pathUnion.get(i, k) == 0 || pathUnion.get(k, k) == 0 || pathUnion.get(k, j) == 0) continue;
                    wordWithLoopExists = true;
                    break;
                }

                if (!wordWithLoopExists) continue;

                boolean allWordsPassThroughZeroLoop = false;
                for (int k = 0; k < n; k++) {
                    if (pathIntersection.get(i, k) == 0 || !zeroLoop[k] || pathIntersection.get(k, j) == 0) continue;
                    allWordsPassThroughZeroLoop = true;
                }

                if (!allWordsPassThroughZeroLoop) return false;
            }
        }

        return true;
    }
}
