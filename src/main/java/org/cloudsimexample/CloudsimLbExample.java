package main.java.org.cloudsimexample;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import main.java.org.cloudsimexample.util.CompareValues;

import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterBrokerLb;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;

public class CloudsimLbExample {

	public static void main(String[] args) {
		executeSimulation();
	}
	
	public static void executeSimulation() {
		try {
			int num_user = 1;
			Calendar calendar = Calendar.getInstance();
 			boolean trace_flag = false;
 			
 			CloudSim.init(num_user, calendar, trace_flag);
 			
 			Datacenter datacenter0 = createDatacenter("Datacenter_0");
 			DatacenterBrokerLb broker = createDatacenterBrokerLb();
 			 			
 			List<Cloudlet> cloudletList = CreateCloudletList(broker.getId());
 			List<Vm> vmList = CreateVmList(broker.getId());
 			
 			broker.submitCloudletList(cloudletList);
 			 			
 			broker.submitVmList(vmList);
 			
 			CloudSim.startSimulation();
 			CloudSim.stopSimulation();
 			
 			List<Cloudlet> finalExecutionResults = broker.getCloudletReceivedList();
 			
 			printCloudletList(finalExecutionResults);
 			writeOutput(finalExecutionResults);
 			
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Surgiram erros.");
		}
	}
	
	private static Datacenter createDatacenter(String name) {
		LinkedList<Storage> storageList = new LinkedList<Storage>();
		
		List<Pe> peList0 = new ArrayList<Pe>();
		List<Pe> peList1 = new ArrayList<Pe>();
		
		List<Host> hostList = new ArrayList<Host>();
		
		peList0.add(new Pe(0, new PeProvisionerSimple(2000)));
		
		peList1.add(new Pe(1, new PeProvisionerSimple(3000)));
	
		int ram = 4096; // host memory (MB)
		long storage = 1000000; // host storage
		int bw = 10000;
		
		hostList.add(
				new Host(
					0,
					new RamProvisionerSimple(ram),
					new BwProvisionerSimple(bw),
					storage,
					peList0,
					new VmSchedulerTimeShared(peList0)
				)
			);
		
		hostList.add(
				new Host(
					1,
					new RamProvisionerSimple(ram),
					new BwProvisionerSimple(bw),
					storage,
					peList1,
					new VmSchedulerTimeShared(peList1)
				)
			);
		
		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 1.0; // the cost of using memory in this resource
		double costPerStorage = 0.05; // the cost of using storage in this
		double costPerBw = 0.01; // the cost of using bw in this resource

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}
	
	private static DatacenterBrokerLb createDatacenterBrokerLb() {
		DatacenterBrokerLb broker = null;
		
		try {
			broker = new DatacenterBrokerLb("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}
	
	private static List<Vm> CreateVmList(int dcbId) {
		List<Vm> vmList = new ArrayList<Vm>();
		
		vmList.add(new Vm(
				0,
				dcbId,
				500,
				1,
				2048,
				5000,
				500000,
				"XEN",
				new CloudletSchedulerSpaceShared()
			));
		
		vmList.add(new Vm(
				1,
				dcbId,
				1000,
				1,
				2048,
				5000,
				500000,
				"XEN",
				new CloudletSchedulerSpaceShared()
			));
		
		vmList.add(new Vm(
				2,
				dcbId,
				1500,
				1,
				2048,
				5000,
				500000,
				"XEN",
				new CloudletSchedulerSpaceShared()
			));
		
		vmList.add(new Vm(
				3,
				dcbId,
				2000,
				1,
				2048,
				5000,
				500000,
				"XEN",
				new CloudletSchedulerSpaceShared()
			));
		return vmList;
	}
	
	private static List<Cloudlet> CreateCloudletList(int dcbId) {
		List<Cloudlet> cloudletList = new ArrayList<Cloudlet>();
		UtilizationModelFull fullUtilize = new UtilizationModelFull();
		
		for(int cloudletId = 0; cloudletId < 32; cloudletId++) {
			Cloudlet newCloudlet = new Cloudlet(cloudletId, 4000000, 1, 
					300, 400, fullUtilize, fullUtilize, fullUtilize);
			newCloudlet.setUserId(dcbId);
			cloudletList.add(newCloudlet);
		}
		return cloudletList;
	}
	
	private static void printCloudletList(List<Cloudlet> list) {
		// Ordenar cloudlets by vm.
		Collections.sort(list, new CompareValues());
		
		double totalFinishTime = 0;
		double totalCPUTime = 0;
		int size = list.size();
		Cloudlet cloudlet;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
				+ "Data center ID" + indent + "VM ID" + indent + "Time" + indent
				+ "Start Time" + indent + "Finish Time" + indent + "Submission Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			totalCPUTime += cloudlet.getActualCPUTime();
			totalFinishTime += cloudlet.getFinishTime();
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getStatus() == Cloudlet.SUCCESS) {
				Log.print("SUCCESS");

				Log.printLine(indent + indent + cloudlet.getResourceId()
						+ indent + indent + indent + cloudlet.getVmId()
						+ indent + indent
						+ dft.format(cloudlet.getActualCPUTime()) + indent
						+ indent + dft.format(cloudlet.getExecStartTime())
						+ indent + indent
						+ dft.format(cloudlet.getFinishTime())
						+ indent + indent
						+ dft.format(cloudlet.getSubmissionTime()));
			}
		}
		Log.printConcatLine("\n ******** TEMPO TOTAL DE EXECUÇÃO: ", totalFinishTime, " ********");
		Log.printConcatLine("\n ******** TEMPO TOTAL DE CPU: ", totalCPUTime, " ********");
	}
	
	private static void writeOutput(List<Cloudlet> list) {
		try (PrintWriter writer = new PrintWriter(new File("outPut.csv"))) {
			StringBuilder sb = new StringBuilder();
			DecimalFormat dft = new DecimalFormat("###.##");
			sb.append("Cloudlet ID,");
			sb.append("Status,");
			sb.append("Datacenter ID,");
			sb.append("Vm ID,");
			sb.append("Time,");
			sb.append("Finish Time,");
			sb.append("Submission Time");
			sb.append("\n");
			
			for (Cloudlet cl : list) {
				sb.append(dft.format(cl.getCloudletId()) + ",");
				sb.append("SUCCESS,");
				sb.append(dft.format(cl.getResourceId()) + ",");
				sb.append(dft.format(cl.getVmId()) + ",");
				sb.append(dft.format(cl.getActualCPUTime()) + ",");
				sb.append(dft.format(cl.getFinishTime()) + ",");
				sb.append(dft.format(cl.getSubmissionDelay()));
				sb.append("\n");
			 }
			 
			 writer.write(sb.toString());
			 Log.printConcatLine("\nCSV de saída escrito.");
			 
		} catch (FileNotFoundException e) {
			Log.printConcatLine(e.getMessage());
		}
	}
}