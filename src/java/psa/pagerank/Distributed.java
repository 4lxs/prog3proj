package psa.pagerank;

import mpi.MPI;

public class Distributed {

    public static void main(String[] args) {
        MPI.Init(args);
        
        try {
            int nV = Integer.parseInt(args[3]);
            int nD = Integer.parseInt(args[4]);
            double d = Double.parseDouble(args[5]);
            int seed = Integer.parseInt(args[6]);

            boolean[][] adj = new boolean[nV][nV];
            
            if (MPI.COMM_WORLD.Rank() == 0) {
                adj = Main.generateGraph(nV, nD, seed);
                System.out.println("Rank 0: Generated graph with " + nV + " vertices and " + nD + " edges");
            }
            
            boolean[] flatAdj = new boolean[nV * nV];
            if (MPI.COMM_WORLD.Rank() == 0) {
                for (int i = 0; i < nV; i++) {
                    for (int j = 0; j < nV; j++) {
                        flatAdj[i * nV + j] = adj[i][j];
                    }
                }
            }
            
            MPI.COMM_WORLD.Bcast(flatAdj, 0, nV * nV, MPI.BOOLEAN, 0);
            
            if (MPI.COMM_WORLD.Rank() != 0) {
                for (int i = 0; i < nV; i++) {
                    for (int j = 0; j < nV; j++) {
                        adj[i][j] = flatAdj[i * nV + j];
                    }
                }
            }
            
            System.out.println("Rank " + MPI.COMM_WORLD.Rank() + ": Received graph data");
            
            PageRank pr = new PageRank(adj, d);
            int iter = 0;
            
            while (true) {
                double convergence = pr.distributedIter();
                if (convergence <= 1e-5) {
                    break;
                }
                
                if (MPI.COMM_WORLD.Rank() == 0) {
                    System.out.print("Iteration " + iter + ": ");
                    for (int j = 0; j < pr.rank.length; j++) {
                        System.out.print(pr.rank[j] + " ");
                    }
                    System.out.println();
                }
                iter++;
            }
            
            if (MPI.COMM_WORLD.Rank() == 0) {
                System.out.println("Converged in " + iter + " iterations");
                
                if (args.length > 7 && !args[7].isEmpty()) {
                    Main.saveGraph(adj, args[7]);
                    System.out.println("Graph saved to " + args[7]);
                }
                
                if (args.length > 8 && !args[8].isEmpty()) {
                    Main.saveRanks(pr.rank, args[8]);
                    System.out.println("Ranks saved to " + args[8]);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Rank " + MPI.COMM_WORLD.Rank() + " - Error: " + e.getMessage());
            e.printStackTrace();
        }

        MPI.Finalize();
    }
}
