package org.cloudbus.cloudsim.examples;

import java.util.*;

public class GWO {
    private int numWolves = 10;
    private int numIterations = 100;
    private int numVMs;
    private int numCloudlets;
    private double[][] positions;
    private double[] fitness;
    private double alphaFitness, betaFitness, deltaFitness;
    private double[] alphaPosition, betaPosition, deltaPosition;
    private double a = 2.0;

    public GWO(int numVMs, int numCloudlets) {
        this.numVMs = numVMs;
        this.numCloudlets = numCloudlets;
        this.positions = new double[numWolves][numCloudlets];
        this.fitness = new double[numWolves];
        this.alphaFitness = Double.MAX_VALUE;
        this.betaFitness = Double.MAX_VALUE;
        this.deltaFitness = Double.MAX_VALUE;
        this.alphaPosition = new double[numCloudlets];
        this.betaPosition = new double[numCloudlets];
        this.deltaPosition = new double[numCloudlets];
        initialize();
    }

    private void initialize() {
        Random rand = new Random();
        for (int i = 0; i < numWolves; i++) {
            for (int j = 0; j < numCloudlets; j++) {
                positions[i][j] = rand.nextInt(numVMs);
            }
            fitness[i] = evaluate(positions[i]);
        }
        updateLeaders();
    }

    private double evaluate(double[] position) {
        int[] workload = new int[numVMs];
        double totalExecutionTime = 0;

        // Processing power (MIPS) for each VM (example, adjust as needed)
        int[] vmMips = {1000, 2500, 1000, 2000, 2300}; // Update based on actual VM capacities

        for (int i = 0; i < position.length; i++) {
            int vm = (int) position[i];

            if (vm >= 0 && vm < numVMs) {
                workload[vm]++;
                totalExecutionTime += (500.0 / vmMips[vm]);
            }
        }

        // Compute load balance factor (variance)
        double mean = Arrays.stream(workload).average().orElse(0);
        double variance = 0;
        for (int load : workload) {
            variance += Math.pow(load - mean, 2);
        }
        double loadBalanceFactor = Math.sqrt(variance / numVMs);

        // Fitness = Response Time + Load Balance Factor
        return totalExecutionTime + loadBalanceFactor;
    }

    private void updateLeaders() {
        for (int i = 0; i < numWolves; i++) {
            if (fitness[i] < alphaFitness) {
                deltaFitness = betaFitness;
                betaFitness = alphaFitness;
                alphaFitness = fitness[i];
                deltaPosition = betaPosition.clone();
                betaPosition = alphaPosition.clone();
                alphaPosition = positions[i].clone();
            } else if (fitness[i] < betaFitness) {
                deltaFitness = betaFitness;
                betaFitness = fitness[i];
                deltaPosition = betaPosition.clone();
                betaPosition = positions[i].clone();
            } else if (fitness[i] < deltaFitness) {
                deltaFitness = fitness[i];
                deltaPosition = positions[i].clone();
            }
        }
    }

    public double[] optimize() {
        for (int iter = 0; iter < numIterations; iter++) {
            a = 2.0 - (2.0 * iter / numIterations);

            for (int i = 0; i < numWolves; i++) {
                for (int j = 0; j < numCloudlets; j++) {
                    double r1 = Math.random();
                    double r2 = Math.random();
                    double A1 = 2 * a * r1 - a;
                    double A2 = 2 * a * Math.random() - a;
                    double A3 = 2 * a * Math.random() - a;
                    double C1 = 2 * r2;
                    double C2 = 2 * Math.random();
                    double C3 = 2 * Math.random();

                    double Dalpha = Math.abs(C1 * alphaPosition[j] - positions[i][j]);
                    double Dbeta = Math.abs(C2 * betaPosition[j] - positions[i][j]);
                    double Ddelta = Math.abs(C3 * deltaPosition[j] - positions[i][j]);

                    double X1 = alphaPosition[j] - A1 * Dalpha;
                    double X2 = betaPosition[j] - A2 * Dbeta;
                    double X3 = deltaPosition[j] - A3 * Ddelta;

                    // Average movement based on Alpha, Beta, Delta Wolves
                    positions[i][j] = (X1 + X2 + X3) / 3;

                    // Ensure VM index stays valid
                    positions[i][j] = Math.max(0, Math.min(numVMs - 1, Math.round(positions[i][j])));
                }
                fitness[i] = evaluate(positions[i]);
            }
            updateLeaders();
        }
        return alphaPosition;
    }
}