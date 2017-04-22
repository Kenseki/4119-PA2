import java.util.*;
import java.util.Map.Entry;
import java.net.*;

public class dvnode {
    public static int selfPort;
    public static DatagramSocket dvSock;
    //ptodMap: key: neighbor port number, value: distance between self port and neighbor port
    public static HashMap<Integer, Double> ptodMap=new HashMap<>();
    //ptoiMap: key: port number, value: port ID
    public static HashMap<Integer, Integer> ptoiMap=new HashMap<>();
    //itop: index: port ID, value: port number
    public static int[] itop = new int[16];
    public static int knownNumber;
    public static double[][] table;
    
    public static void main(String[] args){
    	selfPort = Integer.parseInt(args[0]);
    	try {
			dvSock = new DatagramSocket(selfPort);
		} catch (SocketException e) {
			e.printStackTrace();
		}
    	
    	int n = 0;
    	while(args[2*n+1] != null && args[2*n+2] != null){
    		ptodMap.put(Integer.parseInt(args[2*n+1]), Double.parseDouble(args[2*n+2]));
    		
    		if (2 * (++n) +2 >= args.length) 
    			break;
    	}
    	
    	dvReceiver receiver = new dvReceiver();
    	receiver.start();
    	
    	if(args[args.length-1].equalsIgnoreCase("last")){
    		startTable();
    		printTable();
    		sendTable();
    	}
    }
    
    public synchronized static void printTable() {
    	System.out.println("[" +dvpublicMethods.timeString()+ "]" + " Node " + selfPort + " Routing Table");
		for (int i = 1; i < knownNumber; i++) {
			String doubleString = String.valueOf(dvpublicMethods.roundDouble(table[0][i]));
			if (doubleString.indexOf("0.") == 0) {
				doubleString = doubleString.substring(1);
			}
			System.out.print("- (" + doubleString + ") -> Node " + itop[i]);
			int nextID = nextHop(i);
			if (nextID != i) {
				System.out.print("; Next hop -> Node " + itop[nextID]);
			}
			System.out.println();
		}
	}
    
    
    public static void sendTable() {
		// packet format: port@dest_port1,weight1;dest_port2,weight2
		String message = "";
		for (int i = 0; i < knownNumber; i++) {
			message += String.valueOf(itop[i]) + "," + String.valueOf(table[0][i]) + ";";
		}
		message = message.substring(0, message.length() - 1);
		message = String.valueOf(selfPort)+"@"+message;
		
		Iterator<Entry<Integer, Double>> itr = ptodMap.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>) itr.next();
			int port = (Integer) entry.getKey();
			dvpublicMethods.sendMessage(dvSock, message, port);
			System.out.println("[" +dvpublicMethods.timeString()+ "]" + " Message sent from Node "+ selfPort + " to Node "+port);
		}
	}
    
    
    private static int nextHop(int destiID) {
		Iterator<Entry<Integer, Double>> itr = ptodMap.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>) itr.next();
			int neighborPort = (int) entry.getKey();
			double neighborWeight = (double) entry.getValue();
			int neighborID = ptoiMap.get(neighborPort);
			double dist = neighborWeight + table[neighborID][destiID];
			if (dvpublicMethods.roundEqual(table[0][destiID], dist)) {
				return neighborID;
			}
		}
		return -1;
	}
    
    public static void startTable(){
    	ptoiMap.put(selfPort, 0);
		itop[0] = selfPort;
		knownNumber = 1;
		table = new double[16][16];
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				table[i][j] = Double.POSITIVE_INFINITY;
			}
		}
		
		Iterator<Entry<Integer, Double>> itr = ptodMap.entrySet().iterator();
		while (itr.hasNext()) {
			Map.Entry<Integer, Double> entry = (Map.Entry<Integer, Double>) itr.next();
			int port = (Integer) entry.getKey();
			if(ptoiMap.get(port) == null){
				ptoiMap.put(port, knownNumber);

				itop[knownNumber] = port;
				knownNumber++;
				
			}
			int id = ptoiMap.get(port);
			table[0][id] = (double) entry.getValue();
		}
		
		for (int i = 0; i < knownNumber; i++) {
			table[i][i] = 0;
		}
    }
    
}
