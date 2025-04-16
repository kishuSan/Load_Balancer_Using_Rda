package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;

import java.util.*;

public class CloudSimGWO {
    private static List<Cloudlet> cloudletList;
    private static List<Vm> vmList;

    public static void main(String[] args) {
        CloudSim.init(1, Calendar.getInstance(), false);

        Datacenter datacenter = CloudSimUtils.createDatacenter("Datacenter_1");
        DatacenterBroker broker = CloudSimUtils.createBroker();
        int brokerId = broker.getId();

        int numVMs = 5; // Number of VMs
        int numCloudlets = 80; // Number of cloudlets

        vmList = CloudSimUtils.createVMs(brokerId, numVMs);
        cloudletList = CloudSimUtils.createCloudlets(brokerId, numCloudlets);

        GWO gwo = new GWO(numVMs, numCloudlets);
        double[] bestSolution = gwo.optimize();

        for (int i = 0; i < numCloudlets; i++) {
            cloudletList.get(i).setVmId((int) bestSolution[i]);
        }

        broker.submitVmList(vmList);
        broker.submitCloudletList(cloudletList);

        CloudSim.startSimulation();
        CloudSim.stopSimulation();

        CloudSimUtils.printCloudletResults(cloudletList);
        CloudSimUtils.calculateAverageResponseTime(cloudletList);
        CloudSimUtils.calculateMakeSpan(cloudletList);
    }
}