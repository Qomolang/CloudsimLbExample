package main.java.org.cloudsimexample;

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
import org.cloudbus.cloudsim.DatacenterBrokerIndian;
import org.cloudbus.cloudsim.DatacenterBrokerLb;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.File;
import org.cloudbus.cloudsim.HarddriveStorage;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ParameterException;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;

public class CloudsimLbExample {

	private static List<Cloudlet> cloudletList0 = new ArrayList<Cloudlet>();
	private static List<Cloudlet> cloudletList1 = new ArrayList<Cloudlet>();
	private static List<Cloudlet> cloudletList2 = new ArrayList<Cloudlet>();
	
	private static List<Vm> vmList0 = new ArrayList<Vm>();
	private static List<Vm> vmList1 = new ArrayList<Vm>();
	private static List<Vm> vmList2 = new ArrayList<Vm>();
	
	public static void main(String[] args) {
		executeSimulation();
	}
	
	public static void executeSimulation() {
		try {
			int num_user = 3;
			Calendar calendar = Calendar.getInstance();
 			boolean trace_flag = false;
 			
 			CloudSim.init(num_user, calendar, trace_flag);
 			
 			Datacenter datacenter0 = createDatacenter("Datacenter_0");
 			Datacenter datacenter1 = createDatacenter("Datacenter_1");
 			Datacenter datacenter2 = createDatacenter("Datacenter_2");
 			
 			// Criação de ambos os Datacenters Broker.
 			// Broker 0 -> Original Broker
 			// Broker 1 -> Balanced Broker
 			// Broker 2 -> Indian Broker
 			DatacenterBroker broker0 = createDatacenterBroker(0);
 			DatacenterBrokerLb broker1 = createDatacenterBrokerLb(1);
 			DatacenterBrokerIndian broker2 = createDatacenterBrokerIndian(2);
 			
 			int brokerId0 = broker0.getId();
 			int brokerId1 = broker1.getId();
 			int brokerId2 = broker2.getId();
 			
 			CreateVmList(brokerId0, brokerId1, brokerId2);
 			CreateCloudletList(brokerId0, brokerId1, brokerId2);
 			
 			broker0.submitCloudletList(cloudletList0);
 			broker0.submitVmList(vmList0);
 			
 			broker1.submitCloudletList(cloudletList1);
 			broker1.submitVmList(vmList1);
 			
 			broker2.submitCloudletList(cloudletList2);
 			broker2.submitVmList(vmList2);
 			
 			CloudSim.startSimulation();
 			CloudSim.stopSimulation();
 			
 			List<Cloudlet> finalExecutionResults0 = broker0.getCloudletReceivedList();
 			List<Cloudlet> finalExecutionResults1 = broker1.getCloudletReceivedList();
 			List<Cloudlet> finalExecutionResults2 = broker2.getCloudletReceivedList();
 			
 			printCloudletList(finalExecutionResults0);
 			printCloudletList(finalExecutionResults1);
 			printCloudletList(finalExecutionResults2);

 			//writeOutput(finalExecutionResults, "output.csv");
 			
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Surgiram erros.");
		}
	}
	
	private static Datacenter createDatacenter(String name) {
		LinkedList<Storage> storageList = new LinkedList<Storage>();
		HarddriveStorage hd = null;
		File file1 = null;
		try {
			hd = new HarddriveStorage(1024);
			file1 = new File("file.txt", 300);
			file1.setOwnerName("brokerId1");
		} catch (ParameterException e) {
			e.printStackTrace();
		}
		hd.addFile(file1);
		storageList.add(hd);
		
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
	
	private static DatacenterBrokerLb createDatacenterBrokerLb(int id) {
		DatacenterBrokerLb broker = null;
		
		try {
			broker = new DatacenterBrokerLb("Broker" + id);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}
	
	private static DatacenterBroker createDatacenterBroker(int id) {
		DatacenterBroker broker = null;
		
		try {
			broker = new DatacenterBroker("Broker" + id);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}
	
	private static DatacenterBrokerIndian createDatacenterBrokerIndian (int id) {
		DatacenterBrokerIndian broker = null;
		
		try {
			broker = new DatacenterBrokerIndian("Broker" + id);
		} catch (Exception e) {
			return null;
		}
		return broker;
	}
	
	private static void CreateVmList(int brokerId0, int brokerId1, int brokerId2) {
		vmList0.add(new Vm(
				0,
				brokerId0,
				500,
				1,
				2048,
				5000,
				500000,
				"XEN",
				new CloudletSchedulerSpaceShared()
			));
		
		vmList0.add(new Vm(
				1,
				brokerId0,
				1000,
				1,
				2048,
				5000,
				500000,
				"XEN",
				new CloudletSchedulerSpaceShared()
			));
		
		vmList0.add(new Vm(
				2,
				brokerId0,
				1500,
				1,
				2048,
				5000,
				500000,
				"XEN",
				new CloudletSchedulerSpaceShared()
			));
		
		vmList0.add(new Vm(
				3,
				brokerId0,
				2000,
				1,
				2048,
				5000,
				500000,
				"XEN",
				new CloudletSchedulerSpaceShared()
			));
		
		// Inicio da vmList1
		vmList1.add(new Vm(
				0,
				brokerId1,
				500,
				1,
				2048,
				5000,
				500000,
				"XEN",
				new CloudletSchedulerSpaceShared()
			));
		
		vmList1.add(new Vm(
				1,
				brokerId1,
				1000,
				1,
				2048,
				5000,
				500000,
				"XEN",
				new CloudletSchedulerSpaceShared()
			));
		
		vmList1.add(new Vm(
				2,
				brokerId1,
				1500,
				1,
				2048,
				5000,
				500000,
				"XEN",
				new CloudletSchedulerSpaceShared()
			));
		
		vmList1.add(new Vm(
				3,
				brokerId1,
				2000,
				1,
				2048,
				5000,
				500000,
				"XEN",
				new CloudletSchedulerSpaceShared()
			));
		
		// Inicio da vmList2
				vmList2.add(new Vm(
						0,
						brokerId2,
						500,
						1,
						2048,
						5000,
						500000,
						"XEN",
						new CloudletSchedulerSpaceShared()
					));
				
				vmList2.add(new Vm(
						1,
						brokerId2,
						1000,
						1,
						2048,
						5000,
						500000,
						"XEN",
						new CloudletSchedulerSpaceShared()
					));
				
				vmList2.add(new Vm(
						2,
						brokerId2,
						1500,
						1,
						2048,
						5000,
						500000,
						"XEN",
						new CloudletSchedulerSpaceShared()
					));
				
				vmList2.add(new Vm(
						3,
						brokerId2,
						2000,
						1,
						2048,
						5000,
						500000,
						"XEN",
						new CloudletSchedulerSpaceShared()
					));
	}
	
	private static void CreateCloudletList(int brokerId0, int brokerId1, int brokerId2) {
		UtilizationModelFull fullUtilize = new UtilizationModelFull();
		
		List<String> fileList = new ArrayList<String>();
		fileList.add("file.txt");
		
		for(int cloudletId = 0; cloudletId < 8; cloudletId++) {
			Cloudlet newCloudlet0 = new Cloudlet(cloudletId, 4000000, 1, 
					300, 400, fullUtilize, fullUtilize, fullUtilize, fileList);
			Cloudlet newCloudlet1 = new Cloudlet(cloudletId, 4000000, 1, 
					300, 400, fullUtilize, fullUtilize, fullUtilize, fileList);
			Cloudlet newCloudlet2 = new Cloudlet(cloudletId, 4000000, 1, 
					300, 400, fullUtilize, fullUtilize, fullUtilize, fileList);
			newCloudlet0.setUserId(brokerId0);
			newCloudlet1.setUserId(brokerId1);
			newCloudlet2.setUserId(brokerId2);
			cloudletList0.add(newCloudlet0);
			cloudletList1.add(newCloudlet1);
			cloudletList2.add(newCloudlet2);
		}
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
		Log.printConcatLine("\n ******** TEMPO TOTAL DE EXECUÇÃO: ", dft.format(totalFinishTime), " ********");
		Log.printConcatLine(" ******** TEMPO TOTAL DE CPU: ", dft.format(totalCPUTime), " ********");
	}
	
	
	private static void writeOutput(List<Cloudlet> list, String name) {
		try (PrintWriter writer = new PrintWriter(new java.io.File(name))) {
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