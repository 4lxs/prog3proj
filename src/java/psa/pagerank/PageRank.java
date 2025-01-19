package psa.pagerank;

public class PageRank {
    boolean[][] adj;
    double[] rank;
    int[] out_degree;
    double d;
    int n;

    PageRank(boolean[][] adj, double damping_factor) {
        assert adj.length == adj[0].length;
        this.adj = adj;
        n = adj.length;
        d = damping_factor;
        rank = new double[n];
        for (int i = 0; i < n; i++) {
            rank[i] = 1.0 / n;
        }
        out_degree = new int[n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (adj[i][j]) {
                    out_degree[i]++;
                }
            }
        }
    }

    /**
     * 
     * @return maximum difference between the previous and the current rank
     * used to determine convergence
     */
    double iter() {
        double[] new_rank = new double[n];
        double max_diff = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (adj[j][i]) {
                    new_rank[i] += rank[j] / out_degree[j];
                }
            }
            new_rank[i] = (1 - d) / n + d * new_rank[i];
            max_diff = Math.max(max_diff, Math.abs(new_rank[i] - rank[i]));
        }
        rank = new_rank;
        return max_diff;
    }

}
