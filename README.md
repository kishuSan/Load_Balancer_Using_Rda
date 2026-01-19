# Red Deer Algorithm (RDA) for Cloud Load Balancing

This research project presents an implementation of the **Red Deer Algorithm (RDA)** for **cloud load balancing**, developed and evaluated using the **CloudSim** simulation framework.

Cloud load balancing is formulated as a **multi-objective optimization problem**, where tasks (cloudlets) must be efficiently mapped to virtual machines (VMs) while satisfying Quality of Service (QoS) constraints. RDA, a nature-inspired metaheuristic modeled on the mating behavior of red deer, is adapted to solve this NP-hard problem effectively.

## Problem Overview

In cloud environments, workloads arrive dynamically and resources are heterogeneous. Poor task distribution can lead to:
- VM overload and underutilization
- Increased makespan and response time
- Higher operational cost
- Degraded QoS for users

Traditional deterministic algorithms struggle to scale under these conditions. This project explores **metaheuristic optimization** as a scalable and adaptive alternative, with a focus on **RDA-based cloudlet-to-VM mapping**.

## Proposed Approach

Each **Red Deer** represents a candidate solution encoding a **cloudlet-to-VM assignment**.  
The algorithm evolves these solutions through biologically inspired phases to balance **exploration** and **exploitation**.

### Key Objectives (QoS-Aware)
- Minimize makespan  
- Minimize response time  
- Reduce execution cost  
- Maximize resource utilization  

A weighted fitness function is used to combine these objectives.

## Red Deer Algorithm Phases

### 1 Population Initialization
- Each red deer is encoded as a 1D array mapping cloudlets to VM IDs.
- Fitness is computed based on QoS parameters.

### 2️ Roaring Phase
- Male red deer explore neighboring solutions.
- Updates are accepted only if fitness improves.

### 3️ Commander Selection
- Top γ% of males are selected as commanders.
- Remaining males act as stags.

### 4️ Fighting Phase
- Commanders and stags compete to generate stronger solutions.
- Best candidate survives.

### 5️ Harem Formation
- Commanders form harems proportional to fitness.

### 6️ Mating Phase
- Commander–hind mating within the same harem  
- Commander–hind mating across harems  
- Stag mating with nearest hind  

### 7️ Selection & Elitism
- Best solutions are preserved.
- Tournament selection ensures convergence.

## Fitness Function

Lower fitness values indicate better solutions.

## Experimental Setup

- Simulator: CloudSim  
- Hosts: 3 heterogeneous hosts  
- Tasks: Variable number of cloudlets  
- Population size: 100  
- Iterations: 100  

### Compared Algorithms
- Red Deer Algorithm (RDA)
- Genetic Algorithm (GA)
- Grey Wolf Optimizer (GWO)

## Results

- Lower makespan compared to GA and GWO  
- Improved response time  
- Better resource utilization  
- Reduced execution cost  
- Faster convergence  

## Tech Stack

- Language: Java  
- Framework: CloudSim  
- Algorithms: RDA, GA, GWO  

## Research Reference

Krish Sharma et al.  
**Load Balancing in the Cloud: A Comparative Evaluation of the Red Deer Algorithm**

## Future Work

- Integrate machine learning techniques  
- Support dependent tasks and workflows  
- Include fairness metrics  
- Evaluate at larger cloud scales  
