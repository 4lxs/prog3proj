package psa.pagerank;

import mpi.MPI;

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
     * Sequential PageRank iteration
     * @return maximum difference between the previous and the current rank
     * used to determine convergence
     */
    double sequentialIter() {
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

    /**
     * Parallel PageRank iteration
     * @return maximum difference between the previous and the current rank
     * used to determine convergence
     */
    double parallelIter() {
        double[] new_rank = new double[n];
        double max_diff = 0;
        
        // Parallel implementation using streams
        java.util.stream.IntStream.range(0, n).parallel().forEach(i -> {
            for (int j = 0; j < n; j++) {
                if (adj[j][i]) {
                    synchronized (new_rank) {
                        new_rank[i] += rank[j] / out_degree[j];
                    }
                }
            }
            new_rank[i] = (1 - d) / n + d * new_rank[i];
        });
        
        // Calculate max difference
        for (int i = 0; i < n; i++) {
            max_diff = Math.max(max_diff, Math.abs(new_rank[i] - rank[i]));
        }
        
        rank = new_rank;
        return max_diff;
    }

    /**
     * Distributed PageRank iteration
     * @return maximum difference between the previous and the current rank
     * used to determine convergence
     */
    double distributedIter() {
        int rank_proc = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        
        // Calculate the range of vertices this process will handle
        int chunk_size = n / size;
        int start = rank_proc * chunk_size;
        int end = (rank_proc == size - 1) ? n : start + chunk_size;
        
        double[] new_rank = new double[n];
        double[] local_new_rank = new double[n];
        
        // Each process computes its portion of the new ranks
        for (int i = start; i < end; i++) {
            for (int j = 0; j < n; j++) {
                if (adj[j][i]) {
                    local_new_rank[i] += rank[j] / out_degree[j];
                }
            }
            local_new_rank[i] = (1 - d) / n + d * local_new_rank[i];
        }
        
        // Reduce all local results to get the complete new_rank array
        MPI.COMM_WORLD.Allreduce(local_new_rank, 0, new_rank, 0, n, MPI.DOUBLE, MPI.SUM);
        
        // Calculate maximum difference locally
        double local_max_diff = 0;
        for (int i = start; i < end; i++) {
            local_max_diff = Math.max(local_max_diff, Math.abs(new_rank[i] - rank[i]));
        }
        
        // Find global maximum difference across all processes
        double[] max_diff_array = new double[1];
        double[] local_max_diff_array = {local_max_diff};
        MPI.COMM_WORLD.Allreduce(local_max_diff_array, 0, max_diff_array, 0, 1, MPI.DOUBLE, MPI.MAX);
        
        // Update rank array
        rank = new_rank;
        return max_diff_array[0];
    }

}
