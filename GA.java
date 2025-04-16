package org.cloudbus.cloudsim.examples;

import java.util.*;

public class GA {

    public static List<Integer> geneticAlgorithm(List<Integer> jobs, List<Integer> VMs, int populationSize, int generations) {
        Random rand = new Random();
        List<List<Integer>> population = new ArrayList<>();

        // Initialize population
        for (int i = 0; i < populationSize; i++) {
            List<Integer> chromosome = new ArrayList<>();
            for (int j = 0; j < jobs.size(); j++) {
                chromosome.add(VMs.get(rand.nextInt(VMs.size())));
            }
            population.add(chromosome);
        }

        // Evolve over generations
        for (int generation = 0; generation < generations; generation++) {
            List<List<Object>> populationFitness = new ArrayList<>();

            // Evaluate fitness using the shared fitness function
            for (List<Integer> chromosome : population) {
                double[] position = chromosome.stream().mapToDouble(Integer::doubleValue).toArray();
                double fitness = FitnessFunction.evaluate(position, VMs.size());
                populationFitness.add(Arrays.asList(chromosome, fitness));
            }

            // Sort based on fitness (lower is better since it's total execution time)
            populationFitness.sort(Comparator.comparingDouble(o -> (double) o.get(1)));

            // Select the best individuals for the next generation
            List<List<Integer>> newPopulation = new ArrayList<>();

            while (newPopulation.size() < populationSize) {
                List<Integer> parent1 = population.get(rand.nextInt(populationSize));
                List<Integer> parent2 = population.get(rand.nextInt(populationSize));

                List<List<Integer>> children = pmxCrossover(parent1, parent2);
                mutate(children.get(0));
                mutate(children.get(1));

                newPopulation.add(children.get(0));
                newPopulation.add(children.get(1));
            }

            population = newPopulation;
        }
        return population.get(0); // Best solution found
    }

    // PMX Crossover
    private static List<List<Integer>> pmxCrossover(List<Integer> parent1, List<Integer> parent2) {
        Random rand = new Random();
        int size = parent1.size();
        int cxpoint1 = rand.nextInt(size);
        int cxpoint2 = rand.nextInt(size);
        if (cxpoint1 > cxpoint2) {
            int temp = cxpoint1;
            cxpoint1 = cxpoint2;
            cxpoint2 = temp;
        }

        List<Integer> child1 = new ArrayList<>(parent1);
        List<Integer> child2 = new ArrayList<>(parent2);

        for (int i = cxpoint1; i < cxpoint2; i++) {
            Collections.swap(child1, i, i);
            Collections.swap(child2, i, i);
        }

        return Arrays.asList(child1, child2);
    }

    // Mutation
    private static void mutate(List<Integer> chromosome) {
        Random rand = new Random();
        int size = chromosome.size();
        int p1 = rand.nextInt(size);
        int p2 = rand.nextInt(size);
        Collections.swap(chromosome, p1, p2);
    }
}