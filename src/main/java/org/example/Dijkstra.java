package org.example;

import org.example.Matrix.TropicalMatrix;

import java.util.*;

public class Dijkstra {
    static class Edge {
        int target, weight;

        Edge(int target, int weight) {
            this.target = target;
            this.weight = weight;
        }
    }

    static class Graph {
        int vertices;
        List<List<Edge>> adj;

        Graph(int vertices) {
            this.vertices = vertices;
            adj = new ArrayList<>();
            for (int i = 0; i < vertices; i++) {
                adj.add(new ArrayList<>());
            }
        }

        void addEdge(int u, int v, int weight) {
            adj.get(u).add(new Edge(v, weight));
        }

        /**
         * Returns list of 2 lists: the first one only contains total distance, the second one contains the path
         */
        List<List<Integer>> dijkstra(int start, int end) {
            int[] dist = new int[vertices];
            int[] prev = new int[vertices];
            Arrays.fill(dist, Integer.MAX_VALUE);
            Arrays.fill(prev, -1);
            dist[start] = 0;

            PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
            pq.offer(new int[]{start, 0});

            while (!pq.isEmpty()) {
                int[] current = pq.poll();
                int u = current[0], d = current[1];

                if (u == end) break;

                if (d > dist[u]) continue;

                for (Edge edge : adj.get(u)) {
                    int v = edge.target;
                    int weight = edge.weight;
                    if (dist[u] + weight < dist[v]) {
                        dist[v] = dist[u] + weight;
                        prev[v] = u;
                        pq.offer(new int[]{v, dist[v]});
                    }
                }
            }

            if (dist[end] == Integer.MAX_VALUE) return null;
            List<Integer> path = new ArrayList<>();
            for (int at = end; at != -1; at = prev[at]) {
                path.add(at);
            }
            Collections.reverse(path);
            List<List<Integer>> result = new ArrayList<>(2);
            result.add(List.of(dist[end]));
            result.add(path);
            return result;
        }
    }

    /**
     * Given a word over the set of matrices, this calculates the shortest path from start to end
     * (1 <= start, end <= n) in the encoded word. The length of the word is given by wordLength,
     * and word is interpreted as a number with base wordLength, where the first digit is the first letter,
     * the second digit is the second letter, and so on.
     * First, the matrices and the resulting word (matrices multiplied) are printed. Then the sum of weights
     * on the shortest path, the path itself and the weights on the path are printed.
     */
    public static void tropicalDijkstra(TropicalMatrix[] matrices, int start, int end, int wordLength, int word) {
        if (matrices == null || matrices.length < 1) throw new IllegalArgumentException("Matrices is null or empty.");
        int n = matrices[0].size();

        if (start < 1 || start > n || end < 1 || end > n) throw new IllegalArgumentException("Start or end invalid.");

        System.out.println("Matrices:");
        for (TropicalMatrix m : matrices) {
            System.out.println(m + "\n");
        }

        int[] indices = new int[wordLength];
        for (int i = 0; i < wordLength; i++) {
            indices[wordLength - 1 - i] = word % matrices.length;
            word /= matrices.length;
        }

        System.out.println("Word: " + Arrays.stream(indices).map(i -> i + 1).boxed().toList());

        TropicalMatrix resultingMatrix = new TropicalMatrix(n);
        for (int i = 0; i < wordLength; i++) {
            resultingMatrix.timesInPlace(matrices[indices[i]]);
        }
        System.out.println("Resulting Matrix:\n" + resultingMatrix + "\n");

        Graph graph = new Graph((wordLength + 1) * n);
        for (int i = 0; i < wordLength; i++) {
            TropicalMatrix cur = matrices[indices[i]];
            for (int j = 0; j < n; j++) {
                for (int k = 0; k < n; k++) {
                    int weight = cur.get(j, k);
                    if (weight == Integer.MAX_VALUE) continue;
                    graph.addEdge(i * n + j, (i + 1) * n + k, weight);
                }
            }
        }

        List<List<Integer>> dijkstra = graph.dijkstra(start - 1, wordLength * n + end - 1);     // subtract 1, because 1 <= start, end <= n is expected as input
        int distance = dijkstra.getFirst().getFirst();
        List<Integer> path = dijkstra.get(1).stream().map(k -> k % n).toList();
        List<Integer> weights = new ArrayList<>(path.size() - 1);
        for (int i = 0; i < path.size() - 1; i++) {
            weights.add(matrices[indices[i]].get(path.get(i), path.get(i + 1)));
        }
        System.out.println("Shortest path from " + start + " to " + end + ":");
        System.out.println("Total distance: " + distance);
        System.out.println("Path:\t" + path.stream().map(k -> k + 1).toList());
        System.out.println("Weights:  " + weights);
        System.out.println("Matrices: " + Arrays.stream(indices).map(i -> i + 1).boxed().toList() + " (same as \"word\")");
    }

    /**
     * Assuming that the instance passed to this method is bounded (which is checked by semiDecide()),
     * this method will determine the shortest word that produces the maximum value possible,
     * and then call tropicalDijkstra() on that entry to print the shortest path.
     * */
    public static void findMinPathForMaxValue(TropicalMatrix[] matrices) {
        if (matrices == null || matrices.length < 1) throw new IllegalArgumentException("Matrices is null or empty.");
        int n = matrices[0].size();

        List<Integer> semiDecideResult = DecisionAlgorithms.semiDecideMaxValue(matrices, 10);
        if (semiDecideResult.getFirst() == 0)
            throw new IllegalArgumentException("Semi-decide timed out. Either this instance is unbounded or you need to increase timeoutSeconds in this method.");

        int maxValue = semiDecideResult.get(1);

        TropicalMatrix[] last = new TropicalMatrix[]{new TropicalMatrix(n)};
        while (true) {
            TropicalMatrix[] next = new TropicalMatrix[last.length * matrices.length];
            for (int i = 0; i < last.length; i++) {
                for (int j = 0; j < matrices.length; j++) {
                    next[matrices.length * i + j] = last[i].times(matrices[j]);
                }
            }
            for (int i = 0; i < next.length; i++) {
                for (int j = 0; j < n; j++) {
                    for (int k = 0; k < n; k++) {
                        if (next[i].get(j, k) != maxValue) continue;
                        // found word that produces the maximum value -> "i" already encodes the word (if you know the length)
                        tropicalDijkstra(matrices, j+1, k+1, (int) Math.round(Math.log(next.length) / Math.log(matrices.length)), i);
                        return;
                    }
                }
            }
            last = next;
        }
    }

    public static void main(String[] args) {
        String s = """
                0  1  -  - \s
                -  -  -  1 \s
                1  -  0  - \s
                1  -  -  - \s
                
                1  1  -  - \s
                1  -  -  1 \s
                -  1  0  - \s
                -  -  1  -""";
        TropicalMatrix[] matrices = TropicalMatrixParser.parse(s);

        findMinPathForMaxValue(matrices);
    }
}
