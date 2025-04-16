package org.cloudbus.cloudsim.examples;

import java.util.Arrays;

public class FitnessFunction {

    public static double evaluate(double[] position, int numVMs) {
        // Example function - load balancing
        int[] workload = new int[numVMs];
        double totalExecutionTime = 0;

        // VM's processing power (MIPS)
        int[] vmMips = {1000, 2500, 1000, 2000, 2300};

        // Calculate workload and execution time
        for (int i = 0; i < position.length; i++) {
            int vm = (int) position[i];
            if (vm >= 0 && vm < numVMs) {
                workload[vm]++;
                totalExecutionTime += (500.0 / vmMips[vm]);
            }
        }

        // Variance and load balancing factor
        double mean = Arrays.stream(workload).average().orElse(0);
        double variance = 0;
        for (int load : workload) {
            variance += Math.pow(load - mean, 2);
        }
        double loadBalanceFactor = Math.sqrt(variance / numVMs);

        // Fitness = Response Time + Load Balance Factor
        return totalExecutionTime + loadBalanceFactor;
    }
}