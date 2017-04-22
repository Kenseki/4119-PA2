import java.util.*;
import java.util.Map.Entry;
public class cnReceiver extends Thread{
	Random rand = new Random(System.currentTimeMillis());

	private synchronized void update(String recvString) {
		
		boolean isTableChange = false;
		int fromPort = cnpublicMethods.getPort(recvString);
		recvString = cnpublicMethods.getData(recvString);
		String[] strings = recvString.split("!");
		System.out.println("[" +cnpublicMethods.timeString()+ "]" +" Message received at Node " + cnnode.selfPort + " from Node " + fromPort);

		if (cnnode.table == null) {
			cnnode.startTable();
			isTableChange = true;
		}
		int message_router_id = cnnode.ptoiMap.get(fromPort);
		
		if (strings[0].length() > 0) {
			String[] updatesArr = strings[0].split(";");
			for (int i = 0; i < updatesArr.length; i++) {
				String[] segments = updatesArr[i].split(",");
				int linkPort = Integer.parseInt(segments[0]);
				if (linkPort == cnnode.selfPort) {
					double linkWeight = Double.parseDouble(segments[1]);
					Iterator<Entry<Integer, Link>> itr = cnnode.linkmap.entrySet().iterator();
					while (itr.hasNext()) {
						Map.Entry<Integer, Link> entry = (Map.Entry<Integer, Link>) itr.next();
						int port = (Integer) entry.getKey();
						if (port == fromPort) {
							Link link = (Link) entry.getValue();
							link.linkWeight = linkWeight;
							cnnode.linkmap.put(port, link);
							break;
						}
					}
					break;
				}
			}
		}
		
		String[] updatesArr = strings[1].split(";");
		for (int i = 0; i < updatesArr.length; i++) {
			String[] segments = updatesArr[i].split(",");
			int destiPort = Integer.parseInt(segments[0]);
			if (cnnode.ptoiMap.get(destiPort) == null) {
				cnnode.ptoiMap.put(destiPort, cnnode.knownNumber);
				cnnode.itop[cnnode.knownNumber++] = destiPort;
			}
			int des_router_id = cnnode.ptoiMap.get(destiPort);
			cnnode.table[message_router_id][des_router_id] = Double.parseDouble(segments[1]);
		}
		
		if (cnnode.updateTable()) {
			isTableChange = true;
		}
		
		if (isTableChange) {
			cnnode.printTable();
			cnnode.sendTable();
		}
	}

	private void getACK(String recvString) {
		int port = cnpublicMethods.getPort(recvString);
		recvString = cnpublicMethods.getData(recvString);

		Link link = cnnode.linkmap.get(port);
		if (link.linkType != cnnode.LinkType.SEND) {
			System.err.println("Error: Port " +cnnode.selfPort+ "should not receive ACK from port " + port);
			System.exit(0);
		}

		int seq = Integer.parseInt(recvString);
		cnnode.sendGBN[link.id].succNum++;
		if (seq > cnnode.sendGBN[link.id].winStart) {
			cnnode.sendGBN[link.id].winStart = seq;
		}
	}

	private void getProb(String recvString) {
		int port = cnpublicMethods.getPort(recvString);
		recvString = cnpublicMethods.getData(recvString);

		Link link = cnnode.linkmap.get(port);
		if (link.linkType != cnnode.LinkType.RECEIVER) {
			System.err.println("Error: Port " + cnnode.selfPort+ "should not receive probe from port " + port);
			System.exit(0);
		}

		int seq = Integer.parseInt(recvString);
		String ACKString = "";
		if (!isDrop(link.lossRate)) {
			if (seq == cnnode.expectSeq[link.id]) {
				cnnode.expectSeq[link.id]++;
			}
			ACKString = String.valueOf(cnnode.selfPort)+"@"+String.valueOf(cnnode.expectSeq[link.id]);
			ACKString = cnpublicMethods.addType(ACKString,cnnode.MessageType.ACK);
			cnpublicMethods.sendMessage(cnnode.cnSock, ACKString, port);
		}
	}

	private Boolean isDrop(double lossRate) {
		double r = rand.nextDouble();
		return (r <= lossRate);
	}

	public void run() {
		while (true) {
			String recvString = cnpublicMethods.recvMessage(cnnode.cnSock);
			cnnode.MessageType messageType = cnpublicMethods.getType(recvString);
			recvString = cnpublicMethods.typeRemoved(recvString);

			if (messageType==cnnode.MessageType.UPDATE) {
				update(recvString);
			} else if (messageType == cnnode.MessageType.PROBE) {
				getProb(recvString);
			} else if (messageType == cnnode.MessageType.ACK) {
				getACK(recvString);
			}
		}
	}
}
