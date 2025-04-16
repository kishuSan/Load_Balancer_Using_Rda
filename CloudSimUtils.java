package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import java.util.*;

public class CloudSimUtils {

    public static Datacenter createDatacenter(String name) {
        List<Host> hostList = new ArrayList<>();

        // id, processing cores, mips, ram(mb), hardisk(mb), bandwidth(mbsp);
        hostList.add(createHost(1, 4, 5000, 204800, 1048576, 102400));
        hostList.add(createHost(2, 2, 2500, 102400, 1048576, 102400));
        hostList.add(createHost(3, 1, 1000, 51200, 1048576, 102400));
        

        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen"; // virtual machine manager
        
        
        // these are general parameters
        double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
		double costPerBw = 0.1; // the cost of using bw in this resource

		// characteristics of the datacenter such as architecture, operating system, 
        // list of hosts, and costs (like cost per second of using a processing element, 
        // storage cost, etc.)
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

        try {
        	// best effort policy is maintained for vm allocation to the host machines
            return new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), 
            		new LinkedList<>(), 0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    
    public static Host createHost(int id, int cores, int mips, int ram, long storage, int bw) {
        List<Pe> peList = new ArrayList<>(); // list of processing cores in a host
        for (int i = 0; i < cores; i++) {
            peList.add(new Pe(i, new PeProvisionerSimple(mips / cores))); 
            // allocating equal mips(million instructions per second) to every core
        }
        // Creating and returning a host with best effort poilices for all provisioners
        return new Host(id, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw),
                storage, peList, new VmSchedulerTimeShared(peList));
    }
    
    // create broker
    public static DatacenterBroker createBroker() {
        DatacenterBroker broker = null;
        try {
            broker = new DatacenterBroker("Broker");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return broker;
    }
    
    // create vm's
    public static List<Vm> createVMs(int brokerId, int numVMs) {
    	// kept all vms with similar config
        List<Vm> list = new ArrayList<>();
		int mips = 1000;
		long size = 10000; // image size (MB)
		int ram = 20480; // vm memory (MB)
		long bw = 10000;
		int pesNumber = 1; // number of processing elements needed by an vm
		String vmm = "Xen"; // VMM name
		
        for (int i = 0; i < numVMs; i++) {
            list.add(new Vm(i, brokerId, mips, pesNumber, ram, bw, 
            		size, vmm, new CloudletSchedulerTimeShared()));
        }
        
        return list;
    }

    public static List<Cloudlet> createCloudlets(int brokerId, int numCloudlets) {
        List<Cloudlet> list = new ArrayList<>();
		long length = 500;
		long fileSize = 100;
		long outputSize = 100;
        for (int i = 0; i < numCloudlets; i++) {
            Cloudlet cloudlet = new Cloudlet(i, length, 1, fileSize, outputSize,
                    new UtilizationModelFull(), new UtilizationModelFull(), new UtilizationModelFull());
            cloudlet.setUserId(brokerId);
            list.add(cloudlet);
        }
        
        return list;
    }
    
    
    // Calculating results 
    public static void printCloudletResults(List<Cloudlet> cloudletList) {
        for (Cloudlet cloudlet : cloudletList) {
            System.out.println("Cloudlet ID: " + cloudlet.getCloudletId() +
                    " | Assigned VM: " + cloudlet.getVmId() +
                    " | Status: " + cloudlet.getStatus());
        }
    }

    public static void calculateAverageResponseTime(List<Cloudlet> cloudletList) {
        double totalResponseTime = 0;
        int count = 0;

        for (Cloudlet cloudlet : cloudletList) {
            if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
                double responseTime = cloudlet.getFinishTime() - cloudlet.getExecStartTime();
                totalResponseTime += responseTime;
                count++;
            }
        }

        if (count > 0) {
            double avgResponseTime = (totalResponseTime / count);
            System.out.println("Average Response Time: " + avgResponseTime + " seconds");
        } else {
            System.out.println("No successful cloudlets.");
        }
    }

    public static void calculateMakeSpan(List<Cloudlet> cloudletList) {
        double makeSpan = 0;
        for (Cloudlet cloudlet : cloudletList) {
            if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
                makeSpan = Math.max(makeSpan, cloudlet.getFinishTime());
            }
        }
        System.out.println("Make Span: " + makeSpan + " seconds");
    }
}