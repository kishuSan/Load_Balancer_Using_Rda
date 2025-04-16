package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import java.util.*;

public class CloudSimRDA {
    private static List<Cloudlet> cloudletList; // list of processes
    private static List<Vm> vmList; //list of VMs

    public static void main(String[] args) {
    	
    	// First step: Initialize the CloudSim package. It should be called before creating any entities.
    	int num_user = 1; // number of cloud users
    	Calendar calendar = Calendar.getInstance(); // Calendar whose fields have been initialized with the current date and time.
    	boolean trace_flag = false; // trace events
    	 
    	CloudSim.init(num_user, calendar, trace_flag);

        Datacenter datacenter = CloudSimUtils.createDatacenter("Datacenter_1");
        DatacenterBroker broker = CloudSimUtils.createBroker();
        int brokerId = broker.getId(); //broker is the intermediary who allocates the requests of the users to the VMs

        int numVMs = 5; // Number of VMs
        int numCloudlets = 80; // Number of cloudlets(jobs)

        vmList = CloudSimUtils.createVMs(brokerId, numVMs);
        cloudletList = CloudSimUtils.createCloudlets(brokerId, numCloudlets);
        
        System.out.println("Alpha\tbeta\tgamma\tresponseTime\tMakespan");
        
        // Run RDA algorithm
        RDA rda = new RDA(numVMs, numCloudlets);
        int[] vmAssignments = rda.run(); 
                    
        // Assign cloudlets to VMs based on RDA results
        for (int i = 0; i < numCloudlets; i++) {
            cloudletList.get(i).setVmId(vmAssignments[i]);
        }

        broker.submitVmList(vmList);
        broker.submitCloudletList(cloudletList);

        CloudSim.startSimulation();
        CloudSim.stopSimulation();

        // Print results
        System.out.println("\nSimulation Results:");
        CloudSimUtils.calculateAverageResponseTime(cloudletList);
        CloudSimUtils.calculateMakeSpan(cloudletList);


    }
}