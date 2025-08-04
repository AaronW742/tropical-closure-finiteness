package org.example;

import org.example.Matrix.TropicalMatrix;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.example.DecisionAlgorithms.semiDecideMaxValue;
import static org.example.Dijkstra.findMinPathForMaxValue;
import static org.example.TropicalMatrixGenerator.getRandomMatrices;

public class Main {

    public static void main(String[] args) {
        int n = 3;  // matrix dimension
        int numberOfMatrices = 2;
        double timeout = 0.1;   // timeout for semiDecideMaxValue(); should be increased if n or numberOfMatrices is increased (use minUnbounded as an indicator: increase timeout if minUnbounded gets close to maxBounded)
        long intervalNanoSeconds = (long) 2E9;  // (approximate) time between each update in the console in nanoseconds
        long totalNanoSeconds = (long) 180E9;   // (approximate) runtime of this method in nanoseconds

        int maxBounded = 0;     // maximum value found among all bounded instances
        int minUnbounded = Integer.MAX_VALUE;       // "minimum max value" found among all instances that timed out (presumed unbounded)
        TropicalMatrix[] maxInstance = new TropicalMatrix[numberOfMatrices];

        long startTime = System.nanoTime();
        long lastTime = System.nanoTime();

        System.out.println("\n\nSearching for maximum bound in randomly generated instances with:");
        System.out.println("dimension: " + n);
        System.out.println("number of matrices: " + numberOfMatrices);
        System.out.println("maximum allowed value (less than infinity) in each matrix: 1\n");

        int expectedBound = 2*(n-1);
        System.out.println("Old expected bound (2*(dimension-1)*maxValue): " + expectedBound + " (known to be wrong for numberOfMatrices > 1)\n");

        System.out.println("(Search will be stopped after approximately " + formatNanos(totalNanoSeconds) + ")\n");

        int i = 0;
        while (System.nanoTime() - startTime < totalNanoSeconds) {
            TropicalMatrix[] matrices = getRandomMatrices(numberOfMatrices, n, 1);

            List<Integer> semiDecideMaxValueResult = semiDecideMaxValue(matrices, timeout);
            boolean result = semiDecideMaxValueResult.getFirst() == 1;
            int maxValue = semiDecideMaxValueResult.get(1);
            if (result && maxValue > maxBounded) {
                maxBounded = maxValue;
                maxInstance = Arrays.copyOf(matrices, matrices.length);
            } else if (!result && maxValue < minUnbounded) {
                minUnbounded = maxValue;
            }

            long now = System.nanoTime();
            if (now - lastTime > intervalNanoSeconds) {
                lastTime = now;
                System.out.println("minUnbounded == " + minUnbounded
                        + "; maxBounded == " + maxBounded
                        + "; checked " + (i+1)
                        + " instances in " + formatNanos(now-startTime)
                );
            }
            i++;
        }

        if (minUnbounded <= expectedBound) {
            System.out.println("\nWarning!! minUnbounded == " + minUnbounded + " <= theoreticalMax == " + expectedBound);
        }
        System.out.println("\n(Wrong) Expected bound: " + expectedBound);
        System.out.println("Actual bound: " + maxBounded);
        System.out.println("\nExample for actual bound:\n");
        findMinPathForMaxValue(maxInstance);
    }

    public static String formatNanos(long nanoseconds) {
        Duration duration = Duration.ofNanos(nanoseconds);
        long hours = duration.toHours();
        int minutes = duration.toMinutesPart();
        int seconds = duration.toSecondsPart();
        int millis = duration.toMillisPart();
        return(hours > 0 ? hours + "h " : "") + (hours > 0 || minutes > 0 ? minutes + "m " : "") + seconds + "." + (millis/10) + "s";
    }
}