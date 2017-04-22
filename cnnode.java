import java.util.*;
import java.util.Map.Entry;
import java.net.*;

public class cnnode {
	public static enum LinkType {SEND, RECEIVER};
	public static enum MessageType {PROBE, ACK, UPDATE};
	public static HashMap<Integer, Link>linkmap = new HashMap<>();
	public static int selfPort;
    public static DatagramSocket cnSock;
    //ptoiMap: key: port number, value: port ID
    public static HashMap<Integer, Integer> ptoiMap=new HashMap<>();
    //itop: index: port ID, value: port number
    public static int[] itop = new int[16];
    public static int knownNumber;
    public static double[][] table;
	
	public static int[] expectSeq;
	public static cnGBN[] sendGBN;
	
	public static boolean isActivated = false;
	public Timer routingTimer;
	public Timer statusTimer;
	public RoutingTask routingTask;
	public StatusTask statusTask;
	public GBNProbe probe;
	
	
	
	class RoutingTask extends TimerTask {
		public void run() {
			probe.interrupt();
			Boolean has_changed = updateLink();
			if (has_changed) {
				sendTable();
				printTable();
			}
			probe = new GBNProbe();
			probe.start();
		}
	}
	
	class StatusTask extends TimerTask {
		public void run() {
			if (sendGBN.length == 0) return;
			
			for (int i = 0; i < sendGBN.length; i++) {
				System.out.println("[" +cnpublicMethods.timeString()+ "] Link to " +
						sendGBN[i].peerPort + ": " + sendGBN[i].sentNum + " packets sent, " +sendGBN[i].lossNum() + " packets lost, loss rate " + sendGBN[i].lossRate());
			}
		}
	}
	
	class GBNProbe extends Thread {
		public void run() {
			if (sendGBN.length == 0) return;
			
			for (int i = 0; i < sendGBN.length; i++) {
				sendGBN[i].startTimer();
			}
			
			while (true) {
				for (int i = 0; i < sendGBN.length; i++) {
					sendGBN[i].sendProb();
				}
				try {
					sleep(5);
				} catch (InterruptedException e) {
					for (int i = 0; i < sendGBN.length; i++) {
						sendGBN[i].cancelTimer();
					}
					return;
				}
			}
		}
	}
	
	public cnnode(){

		routingTimer = new Timer();
		statusTimer = new Timer();
		routingTask = new RoutingTask();
		statusTask = new StatusTask();
		probe = new GBNProbe();
	}
	
	public static void main(String[] args){
		cnnode cn = new cnnode();
		
		selfPort = Integer.parseInt(args[0]);
		try {
			cnSock = new DatagramSocket(selfPort);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
    	if (!args[1].equalsIgnoreCase("receive")) {
			System.out.println("Error: invalid command line arguments");
    		return;
		}
		
		int i = 2;
		int ID = 0;
		while (!args[i].equalsIgnoreCase("send")) {	
			Link alink = new Link(ID, LinkType.RECEIVER, Double.parseDouble(args[i + 1]), 0);
			linkmap.put(Integer.parseInt(args[i]), alink);
			ID++;
			i = i+ 2;
		}
		
		expectSeq = new int[ID];
		for (int j = 0; j < ID; j++) {
			expectSeq[j] = 0;
		}
		
		ID = 0;
		for (int j = i + 1; j < args.length; j++) {
			if (args[j].equalsIgnoreCase("last")){
				break;
			}
			
			linkmap.put(Integer.parseInt(args[j]), new Link(ID++, LinkType.SEND, 0, 0));
		}
		
		sendGBN = new cnGBN[ID];
		for (int j = 0; j < ID; j++) {
			int port = Integer.parseInt(args[i + 1 + j]);
			sendGBN[j] = new cnGBN(port, 5);
		}
		
		cnReceiver receiver = new cnReceiver();
		receiver.start();
		
		if (args[args.length - 1].equalsIgnoreCase("last")){
			startTable();
			printTable();
			sendTable();
		}
		
		while (true) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (isActivated) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				cn.routingTimer.schedule(cn.routingTask, 5000, 5000);
				cn.statusTimer.schedule(cn.statusTask, 1000, 1000);
				cn.probe.start();
				
				break;
			}
		}
    	
    }
	
	
	
	public boolean updateLink() {
		for (int i = 0; i < sendGBN.length; i++) {
			sendGBN[i].updateLossRate();
			double link_weight = sendGBN[i].currLossRate;
			int link_router_port = sendGBN[i].peerPort;
			Iterator<Entry<Integer, Link>> itr = linkmap.entrySet().iterator();
			while (itr.hasNext()) {
				Map.Entry<Integer, Link> entry = (Map.Entry<Integer, Link>) itr.next();
				int port = (Integer) entry.getKey();
				if (port == link_router_port) {
					Link link = (Link) entry.getValue();
					link.linkWeight = link_weight;
					linkmap.put(port, link);
					break;
				}
			}
		}
		return updateTable();
	}
	
	
	public static boolean updateTable() {
		Boolean isTableChange = false;
		for (int i = 1; i < knownNumber; i++) {
			double d1 = table[0][i];
			double d2 = Double.POSITIVE_INFINITY;
			Iterator<Entry<Integer, Link>> iter = linkmap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry<Integer, Link> entry = (Map.Entry<Integer, Link>) iter.next();
				int link_router_port = (int) entry.getKey();
				double link_router_weight = ((Link) entry.getValue()).linkWeight;
				double d = link_router_weight + table[ptoiMap.get(link_router_port)][i];
				if ((d2 - d)>0.005) {
					d2 = d;
				}
			}
			if (!cnpublicMethods.roundEqual(d1, d2)) {
				table[0][i] = d2;
				isTableChange = true;
			}
		}
		return isTableChange;
	}
	
	public static void startTable() {
		ptoiMap.put(selfPort, 0);
		itop[0] = selfPort;
		knownNumber = 1;
		
		table = new double[16][16];
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				table[i][j] = Double.POSITIVE_INFINITY;
			}
		}
		
		Iterator<Entry<Integer, Link>> itr = linkmap.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<Integer, Link> entry = (Map.Entry<Integer, Link>) itr.next();
			int port = (Integer) entry.getKey();
	
			if (ptoiMap.get(port) == null) {
				ptoiMap.put(port, knownNumber);
				itop[knownNumber] = port;
				knownNumber++;
			}
			int id = ptoiMap.get(port);
			table[0][id] = 0;
		}

		for (int i = 0; i < knownNumber; i++) {
			table[i][i] = 0;
		}
		
		isActivated = true;
	}
	
	
	public static void sendTable() {
		// packet format:
		// 	message_type#self_port@link_port1,link_weight1;...!linkdest_port1,weight1;...
		// PS: there is no ';' in the end of the packet
		String message = "";
		
		//  write link condition
		for (int i = 0; i < sendGBN.length; i++) {
			message += sendGBN[i].peerPort + "," + 
					String.valueOf(sendGBN[i].currLossRate) + ";";
		}
		if (message.length() > 0) {
			message = message.substring(0, message.length() - 1);
		}
		message += "!";
		
		for (int i = 0; i < knownNumber; i++) {
			message += String.valueOf(itop[i]) + "," + String.valueOf(table[0][i]) + ";";
		}
		message = message.substring(0, message.length() - 1);
		message = String.valueOf(selfPort)+"@"+ message;
		message = cnpublicMethods.addType(message, MessageType.UPDATE);
		
		Iterator<Entry<Integer, Link>> itr = linkmap.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<Integer, Link> entry = (Map.Entry<Integer, Link>) itr.next();
			int port = (Integer) entry.getKey();
			cnpublicMethods.sendMessage(cnSock, message, port);
			System.out.println("[" +cnpublicMethods.timeString()+ "]" + " Message sent from Node "+ selfPort + " to Node " + port);
		}
	}
	
	public synchronized static void printTable() {
		System.out.println("[" +cnpublicMethods.timeString()+ "]" +
				" Node " + selfPort + " Routing Table");
		for (int i = 1; i < knownNumber; i++) {
			String doubleString = String.valueOf(cnpublicMethods.roundDouble(table[0][i]));
			if (doubleString.indexOf("0.") == 0) {
				doubleString = doubleString.substring(1);
			}
			System.out.print("- (" + doubleString + ") -> Node " + itop[i]);
			int id = nextHop(i);
			if ((id != i) && (id != -1)) {
				System.out.print("; Next hop -> Node " + itop[id]);
			}
			System.out.println();
		}
	}
	
	private static int nextHop(int destiID) {
		Iterator<Entry<Integer, Link>> itr = linkmap.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<Integer, Link> entry = (Map.Entry<Integer, Link>) itr.next();
			int linkPort = (int) entry.getKey();
			double linkWeight = ((Link) entry.getValue()).linkWeight;
			int linkID = ptoiMap.get(linkPort);
			double d = linkWeight +table[linkID][destiID];
			if (cnpublicMethods.roundEqual(table[0][destiID], d)) {
				return linkID;
			}
		}
		return -1;
	}
}
