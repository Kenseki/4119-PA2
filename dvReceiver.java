import java.util.*;
import java.util.Map.Entry;

public class dvReceiver extends Thread{
	public void run() {
		while (true) {
			String recvMessage = dvpublicMethods.recvMessage(dvnode.dvSock);
			int fromPort = dvpublicMethods.getFromPort(recvMessage);
			recvMessage = dvpublicMethods.getUpdatePorts(recvMessage);
			System.out.println("[" +dvpublicMethods.timeString()+ "]" +" Message received at Node " + dvnode.selfPort + " from Node " + fromPort);
			
			boolean isTableChange = false;
			
			// for the first table initialization
			if (dvnode.table == null) {
				dvnode.startTable();
				isTableChange = true;
			}
			
			// read updating table from nearby
			int fromID = dvnode.ptoiMap.get(fromPort);
			String[] updatesArr = recvMessage.split(";");
			for (int i = 0; i < updatesArr.length; i++) {
				String[] segments = updatesArr[i].split(",");
				int destiPort = Integer.parseInt(segments[0]);
				
				if (dvnode.ptoiMap.get(destiPort) == null) {
					dvnode.ptoiMap.put(destiPort, dvnode.knownNumber);
					dvnode.itop[dvnode.knownNumber++] = destiPort;
				}
				
				int destiID = dvnode.ptoiMap.get(destiPort);
				dvnode.table[fromID][destiID] = Double.parseDouble(segments[1]);
			}
			
			// update self table
			for (int i = 1; i < dvnode.knownNumber; i++) {
				double d1 = dvnode.table[0][i];
				double d2 = Double.POSITIVE_INFINITY;
				Iterator<Entry<Integer, Double>> itr = dvnode.ptodMap.entrySet().iterator();
				while (itr.hasNext()) {
					Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>) itr.next();
					int neighborPort = (int) entry.getKey();
					double neighborWeight = (double) entry.getValue();
					double dist = neighborWeight + dvnode.table[dvnode.ptoiMap.get(neighborPort)][i];
					if ((d2-dist)>0.0000005) {
						d2 = dist;
					}
				}
				if (!dvpublicMethods.roundEqual(d1, d2)) {
					dvnode.table[0][i] = d2;
					isTableChange = true;
				}
			}
			
			if (isTableChange) {
				dvnode.printTable();
				dvnode.sendTable();
			}
		}
	}
}
