package org.cloudbus.cloudsim.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.List;

class Deer {
    int index; // uniID
    double[] position;
    double fitness;

    Deer(int index, double[] position, double fitness) {
        this.index = index;
        this.position = position;
        this.fitness = fitness;
    }
}

public class RDA {
    private int populationSize = 100;
    private int numMales = 15;     
    private int numHinds = populationSize - numMales;          	// Number of hinds (calculated as populationSize - numMales)
    private int numStags;           // Number of stags (calculated as numMales - numCommanders)
    private int numCommanders;
    private int numIterations = 100; 
    private int numVMs;           
    private int numCloudlets;
    private int uniID = 0;
    private double bestFitness = Double.MAX_VALUE;  // Best fitness value
    private double[] bestPosition;  				// Best position overall
    private List<Deer> males = new ArrayList<>();
    private List<Deer> hinds = new ArrayList<>();
    private List<Deer> commanders = new ArrayList<>();
    private List<Deer> stags = new ArrayList<>();
    private List<Deer> fitnessPool = new ArrayList<>();

    // Algorithm parameters
    private double alpha = 0.9;     // % of hinds a commander mates with in his harem
    private double beta = 0.4;      // % of hinds a commander mates with in another harem
    private double gamma = 0.7;     // Fraction of males selected as commanders
    private double UB;       		// Upper bound of the search space
    private double LB = 0.0;        // Lower bound of the search space

    void set_alpha(float x) {this.alpha = x;}
    void set_gamma(float x) {this.gamma = x;}
    void set_beta(float x) {this.beta = x;}
    
    public RDA(int numVMs, int numCloudlets) {
    	if (numVMs <= 0 || numCloudlets <= 0) {
            throw new IllegalArgumentException("numVMs and numCloudlets must be > 0");
        }
        this.numVMs = numVMs;
        this.numCloudlets = numCloudlets;
        this.numCommanders = Math.max(1, (int) Math.round(gamma * numMales)); // Ensure >=1 commander
        this.numStags = numMales - numCommanders;
        this.bestPosition = new double[numCloudlets];
        this.UB = numVMs;
        initializePopulation();
    }

    // Step 1: Initialize population
    private void initializePopulation() {
        Random rand = new Random();
        List<Deer> allIndividuals = new ArrayList<>();
        
        while(uniID++ < populationSize){
            double[] pos = new double[numCloudlets];
            for (int j = 0; j < numCloudlets; j++) {
                pos[j] = LB + (UB - LB) * rand.nextDouble();
            }
            double fit = evaluateFitness(pos);
            allIndividuals.add(new Deer(uniID, pos, fit));
        }

        allIndividuals.sort(Comparator.comparingDouble(ind -> ind.fitness));

        males = allIndividuals.subList(0, numMales);
        hinds = new ArrayList<>(allIndividuals.subList(numMales, populationSize));

        commanders = males.subList(0, numCommanders);
        stags = males.subList(numCommanders, numMales);

        updateBestSolution(allIndividuals);
    }
    
 // Step 2: Fitness function (example: load balancing)
//    private double evaluateFitness(double[] position) {
//        // Example function - load balancing
//        int[] workload = new int[numVMs];
//        double totalExecutionTime = 0;
//
//        // VM's processing power (MIPS)
//        int[] vmMips = {1000, 2500, 1000, 2000, 2300};
//
//        // Calculate workload and execution time
//        for (int i = 0; i < position.length; i++) {
//            int vm = Math.floorMod((int) position[i], numVMs); // Ensure VM index is within bounds
//            workload[vm]++;
//            totalExecutionTime += (500.0 / vmMips[vm]);
//        }
//
//        // Variance and load balancing factor
//        double mean = Arrays.stream(workload).average().orElse(0);
//        double variance = 0;
//        for (int load : workload) {
//            variance += Math.pow(load - mean, 2);
//        }
//        double loadBalanceFactor = Math.sqrt(variance / numVMs);
//
//        // Objective function: Minimize execution time and load imbalance
//        return totalExecutionTime + loadBalanceFactor;
//    }

    // Step 2: Fitness function (example: load balancing)
    private double evaluateFitness(double[] position) {
        int[] workload = new int[numVMs];
        double[] processingTime = new double[numVMs];
        double[] utilization = new double[numVMs];
        double totalCost = 0;
        double totalResponseTime = 0;

        int cloudletLength = 500;
        int[] vmMips = new int[numVMs];
        double[] costPerVM = new double[numVMs];

        for (int i = 0; i < numVMs; i++) {
            vmMips[i] = 1000 + (int)(Math.random() * 500);
            costPerVM[i] = 2.0 + (Math.random() * 2.0);
        }

        // Prepare VM task lists for response time calculation
        List<List<Integer>> vmTaskList = new ArrayList<>();
        for (int i = 0; i < numVMs; i++) vmTaskList.add(new ArrayList<>());

        for (int i = 0; i < position.length; i++) {
            int vmId = (int) (position[i] * numVMs) % numVMs;
            workload[vmId]++;
            vmTaskList.get(vmId).add(i); // Assign cloudlet index to VM
        }

        // Estimate response time, execution time, and cost per VM
        for (int vmId = 0; vmId < numVMs; vmId++) {
            double currentTime = 0.0;
            for (int cl : vmTaskList.get(vmId)) {
                double execTime = (double) cloudletLength / vmMips[vmId];
                currentTime += execTime;
                processingTime[vmId] += execTime;
                totalCost += costPerVM[vmId] * execTime;
                totalResponseTime += currentTime; // Response time = time when it finishes
            }
        }

        double makespan = Arrays.stream(processingTime).max().orElse(0);
        double totalUtilization = 0;
        for (int j = 0; j < numVMs; j++) {
            utilization[j] = makespan > 0 ? processingTime[j] / makespan : 0;
            totalUtilization += utilization[j];
        }

        double resourceUtilization = totalUtilization / numVMs;
        double avgResponseTime = totalResponseTime / position.length;

        // Normalization bounds
        double maxPossibleMakespan = numCloudlets * (cloudletLength / Collections.min(Arrays.asList(Arrays.stream(vmMips).boxed().toArray(Integer[]::new))));
        double normMakespan = normalizeMin(makespan, 0, maxPossibleMakespan);
        double normCost = normalizeMin(totalCost, 0, numCloudlets * cloudletLength * Collections.max(Arrays.asList(Arrays.stream(costPerVM).boxed().toArray(Double[]::new))) / 1000);
        double normResponseTime = normalizeMin(avgResponseTime, 0, maxPossibleMakespan);
        double normResourceUtilization = normalizeMax(resourceUtilization, 0, 1);

        double[] weights = {0.25, 0.25, 0.25, 0.25}; // makespan, utilization, cost, response time
        double fitness =
            weights[0] * normMakespan +
            weights[1] * normResourceUtilization +
            weights[2] * normCost +
            weights[3] * normResponseTime;

        return fitness;
    }


    // Helper: Normalize a minimization objective (lower actual value = higher normalized value)
    private double normalizeMin(double value, double min, double max) {
        return (max != min) ? (max - value) / (max - min) : 1.0;
    }

    // Helper: Normalize a maximization objective (higher actual value = higher normalized value)
    private double normalizeMax(double value, double min, double max) {
        return (max != min) ? (value - min) / (max - min) : 1.0;
    }

    // Step 3: Roaring phase for each male (position update)
    private void roaringPhase() {
        Random rand = new Random();
        for (Deer male : males) {
            double a1 = rand.nextDouble();
            double a2 = rand.nextDouble();
            double a3 = rand.nextDouble();
            double[] temp = new double[numCloudlets];

            for (int j = 0; j < numCloudlets; j++) {
                double roar;
                double offset = a1 * (((UB - LB) * a2) + LB);
                if (a3 >= 0.5) roar = male.position[j] + offset;
                else roar = male.position[j] - offset;
                temp[j] = Math.max(LB, Math.min(UB, roar));
            }

            double curr_fitness = evaluateFitness(temp);
            if (curr_fitness < male.fitness) {
                male.position = temp.clone();
                male.fitness = curr_fitness;
            }
        }
    }

    // Step 4: Fight between commanders and stags
    private void fightingPhase() {
        Random rand = new Random();
        for (Deer commander : commanders) {
            Deer stag = stags.get(rand.nextInt(numStags));

            double[][] candidates = new double[4][numCloudlets];

            candidates[0] = commander.position.clone();
            candidates[1] = stag.position.clone();

            double[] new1 = new double[numCloudlets];
            double[] new2 = new double[numCloudlets];

            double b1 = rand.nextDouble();
            double b2 = rand.nextDouble();

            for (int j = 0; j < numCloudlets; j++) {
                double avg = (commander.position[j] + stag.position[j]) / 2.0;
                double offset = b1 * (((UB - LB) * b2) + LB);
                new1[j] = Math.max(LB, Math.min(UB, avg + offset));
                new2[j] = Math.max(LB, Math.min(UB, avg - offset));
            }

            candidates[2] = new1;
            candidates[3] = new2;

            double curr_bestFitness = Double.MAX_VALUE;
            double[] curr_bestPosition = candidates[0];

            for (int k = 0; k < 4; k++) {
                double currentFitness = evaluateFitness(candidates[k]);
                if (currentFitness < curr_bestFitness) {
                    curr_bestFitness = currentFitness;
                    curr_bestPosition = candidates[k];
                }
            }

            commander.position = curr_bestPosition.clone();
            commander.fitness = curr_bestFitness;
        }
    }

    // Step 5: Harem formation
    private int[][] formHarems() {
        int[][] harems = new int[numCommanders][];
        double[] commanderFitness = new double[numCommanders];
        
     // Get the maximum fitness value among commanders
        double maxFitness = Double.MIN_VALUE;
        for (int i = 0; i < numCommanders; i++) {
            if (commanders.get(i).fitness > maxFitness) maxFitness = commanders.get(i).fitness;
        }
        
        // Calculate normalized fitness
        double totalFitness = 0;
        for (int i = 0; i < numCommanders; i++) {
            // Vn = vn - max{vi}
            commanderFitness[i] = Math.abs(commanders.get(i).fitness - maxFitness);
            totalFitness += commanderFitness[i];
        }

        List<Integer> hindIndices = new ArrayList<>();
        for (Deer hind : hinds) hindIndices.add(hind.index);
        Collections.shuffle(hindIndices);

        int assigned = 0;
        for (int i = 0; i < numCommanders; i++) {
            double proportion = (totalFitness > 0) ? (commanderFitness[i] / totalFitness) : 1.0 / numCommanders;
            int haremSize = (i == numCommanders - 1) ? numHinds - assigned : (int) Math.round(proportion * numHinds);
            harems[i] = new int[haremSize];
            for (int j = 0; j < haremSize && assigned < numHinds; j++) {
                harems[i][j] = hindIndices.get(assigned++);
                System.out.print(harems[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("=====");
        return harems;
    }

    // Step 6: Mating phase
    private void matingPhase(int[][] harems) {
        Random rand = new Random();
        for (int i = 0; i < numCommanders; i++) {
            Deer commander = commanders.get(i);
            int intraCount = (int) (harems[i].length * alpha);
            
            int otherHarem = rand.nextInt(numCommanders);
            while(otherHarem == i) otherHarem = rand.nextInt(numCommanders);
            
            int interCount = (int) (harems[otherHarem].length * beta);

            // Intra-harem mating
            for (int j = 0; j < intraCount; j++) {
            	if(harems[i].length == 0) continue;
                int hindIdx = harems[i][rand.nextInt(harems[i].length)];
                mate(commander, getDeerByIndex(hindIdx));
            }

            // Inter-harem mating
            for (int j = 0; j < interCount; j++) {
                if (harems[otherHarem].length == 0) continue;
                int hindIdx = harems[otherHarem][rand.nextInt(harems[otherHarem].length)];
                mate(commander, getDeerByIndex(hindIdx)); 
            }
        }

        // Stag mating
        for (Deer stag : stags) {
            Deer nearestHind = findNearestHind(stag);
            mate(stag, nearestHind);
        }
    }

    private void mate(Deer parent1, Deer parent2) {
        Random rand = new Random();
        double c = rand.nextDouble();
        double[] offspring = new double[numCloudlets];
        if(parent2 == null) return;

        for (int i = 0; i < numCloudlets; i++) {
            offspring[i] = (parent1.position[i] + parent2.position[i]) / 2 + c * (UB - LB);
            offspring[i] = Math.max(LB, Math.min(UB, offspring[i]));
        }
        
        double offspringFitness = evaluateFitness(offspring);
        fitnessPool.add(new Deer(uniID++, offspring, offspringFitness));
    }

    // helper
    private Deer getDeerByIndex(int index) {
        for (Deer ind : hinds) if (ind.index == index) return ind;
        for (Deer ind : males) if (ind.index == index) return ind;
        return null;
    }

    // Find the nearest hind for stag mating
    private Deer findNearestHind(Deer stag) {
        Deer nearest = hinds.get(0);
        double minDist = Double.MAX_VALUE;
        for (Deer hind : hinds) {
            double dist = 0.0;
            for (int i = 0; i < numCloudlets; i++) {
                dist += Math.pow(stag.position[i] - hind.position[i], 2);
            }
            dist = Math.sqrt(dist);
            if (dist < minDist) {
                minDist = dist;
                nearest = hind;
            }
        }
        return nearest;
    }
    
    private void selectNextGen() {
		fitnessPool.addAll(hinds);
    	fitnessPool.sort(Comparator.comparingDouble(ind -> ind.fitness));
        hinds = new ArrayList<>(fitnessPool.subList(0, numHinds));
        fitnessPool.clear();
    }
    
    private void tournamentSelection() {
        Random rand = new Random();
        fitnessPool.addAll(hinds);
        hinds.clear();
        
        for (int i = 0; i < numHinds; i++) {
            List<Deer> tournament = new ArrayList<>();
            for (int j = 0; j < 5; j++) {
                Deer randomHind = fitnessPool.get(rand.nextInt(fitnessPool.size()));
                tournament.add(randomHind);
            }

            tournament.sort(Comparator.comparingDouble(d -> d.fitness));
            hinds.add(tournament.get(0)); // Select best from tournament
        }
    }


    // Step 7: Update best solution
    private void updateBestSolution(List<Deer> allIndividuals) {
        for (Deer ind : allIndividuals) {
            if (ind.fitness < bestFitness) {
                bestFitness = ind.fitness;
                bestPosition = ind.position.clone();
            }
        }
    }

    // step 3.1
    private void selectCommanders() {
        males.sort(Comparator.comparingDouble(ind -> ind.fitness));
        // allocate a view of the male list to the commanders and stags
        // which means any changes to the commanders or stags would affect the males list.
        commanders = males.subList(0, numCommanders);
        stags = males.subList(numCommanders, numMales);
    }
    
    // Step 8: Main simulation loop
    public int[] run() {
        for (int iter = 0; iter < numIterations; iter++) {
        	System.out.println("iter : " + iter);
            roaringPhase();
            selectCommanders();
            fightingPhase();
            int[][] harems = formHarems();
            matingPhase(harems);
//            selectNextGen();
            tournamentSelection();

            List<Deer> allIndividuals = new ArrayList<>();
            allIndividuals.addAll(males);
            allIndividuals.addAll(hinds);
            updateBestSolution(allIndividuals);
        }

        int[] vmAssignments = new int[numCloudlets];
        for (int i = 0; i < numCloudlets; i++) {
            vmAssignments[i] = (int) Math.floor(bestPosition[i] * numVMs) % numVMs;
        }
        return vmAssignments;
    }

}