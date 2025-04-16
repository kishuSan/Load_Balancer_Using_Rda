package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.*;

import java.util.*;

public class CloudSimGA {
    public static void main(String[] args) {
        int numUsers = 1;
        Calendar calendar = Calendar.getInstance();
        boolean traceFlag = false;

        CloudSim.init(numUsers, calendar, traceFlag);

        Datacenter datacenter = createDatacenter("Datacenter_0");
        DatacenterBroker broker = createBroker();
        int brokerId = broker.getId();

        List<Vm> vmlist = createVMs(brokerId, 10);
        List<Cloudlet> cloudletList = createCloudlets(brokerId, 40);

        broker.submitVmList(vmlist);
        broker.submitCloudletList(cloudletList);

        List<Integer> jobs = new ArrayList<>();
        for (Cloudlet cloudlet : cloudletList) {
            jobs.add((int) cloudlet.getCloudletLength());
        }

        List<Integer> VMs = new ArrayList<>();
        for (Vm vm : vmlist) {
            VMs.add(vm.getId());
        }

        int populationSize = 20;
        int generations = 2000;
        List<Integer> bestSolution = GA.geneticAlgorithm(jobs, VMs, populationSize, generations);

        assignCloudletsToVMs(broker, cloudletList, bestSolution);

        CloudSim.startSimulation();
        CloudSim.stopSimulation();

        printResults(cloudletList);
        calculateAverageResponseTime(cloudletList);
        calculateMakespan(cloudletList);
    }

    private static Datacenter createDatacenter(String name) {
        List<Host> hostList = new ArrayList<>();
        int mips = 1000;
        int ram = 2048; // 2 GB RAM
        long storage = 1000000; // 1 TB storage
        int bw = 10000;
        int pesNumber = 2; // Number of CPU cores

        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerSimple(mips)));

        hostList.add(new Host(
                0, new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bw), storage, peList,
                new VmSchedulerTimeShared(peList)
        ));

        LinkedList<Storage> storageList = new LinkedList<>();
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                "x86", "Linux", "Xen",
                hostList, 10.0, 3.0, 0.05, 0.1, 0.1
        );

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return datacenter;
    }

    private static DatacenterBroker createBroker() {
        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return broker;
    }

    private static List<Vm> createVMs(int brokerId, int count) {
        List<Vm> vmlist = new ArrayList<>();
        int mips = 1000;
        int ram = 512;
        long bw = 1000;
        long size = 10000;
        int pesNumber = 1;

        for (int i = 0; i < count; i++) {
            vmlist.add(new Vm(i, brokerId, mips, pesNumber, ram, bw, size, "Xen", new CloudletSchedulerTimeShared()));
        }
        return vmlist;
    }

    private static List<Cloudlet> createCloudlets(int brokerId, int count) {
        List<Cloudlet> cloudletList = new ArrayList<>();
        int length = 40000;
        int fileSize = 300;
        int outputSize = 300;
        int pesNumber = 1;

        UtilizationModel utilizationModel = new UtilizationModelFull();

        for (int i = 0; i < count; i++) {
            Cloudlet cloudlet = new Cloudlet(i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet.setUserId(brokerId);
            cloudletList.add(cloudlet);
        }
        return cloudletList;
    }

    private static void assignCloudletsToVMs(DatacenterBroker broker, List<Cloudlet> cloudletList, List<Integer> assignments) {
        for (int i = 0; i < cloudletList.size(); i++) {
            cloudletList.get(i).setVmId(assignments.get(i));
        }
    }

    private static void printResults(List<Cloudlet> list) {
        for (Cloudlet cloudlet : list) {
            System.out.println("Cloudlet " + cloudlet.getCloudletId() + " executed on VM " + cloudlet.getVmId());
        }
    }

    private static void calculateAverageResponseTime(List<Cloudlet> cloudletList) {
        double totalResponseTime = 0;
        int executedCloudlets = 0;

        for (Cloudlet cloudlet : cloudletList) {
            if (cloudlet.getFinishTime() > 0) { // Ensure only executed cloudlets are counted
                totalResponseTime += cloudlet.getFinishTime() - cloudlet.getExecStartTime();
                executedCloudlets++;
            }
        }

        double averageResponseTime = (executedCloudlets > 0) ? (totalResponseTime / executedCloudlets) : 0;
        System.out.println("Average Response Time: " + averageResponseTime);
    }

    private static void calculateMakespan(List<Cloudlet> cloudletList) {
        double makespan = 0;

        for (Cloudlet cloudlet : cloudletList) {
            double finishTime = cloudlet.getFinishTime();
            if (finishTime > makespan) {
                makespan = finishTime;
            }
        }

        System.out.println("Makespan: " + makespan);
    }
}