import java.util.Calendar;
import java.io.IOException;
import java.net.*;

public class cnpublicMethods {

	public static Boolean roundEqual(double d1, double d2) {
		return Math.abs(d1 - d2) < 0.005;
	}
	
	public static String addType(String sendString, cnnode.MessageType messageType) {
		return String.valueOf(messageType.ordinal()) + '#' + sendString;
	}
	
	public static cnnode.MessageType getType(String recvString) {
		int i = recvString.indexOf('#');
		
		int index = Integer.parseInt(recvString.substring(0, i));
		
		for (cnnode.MessageType sockType : cnnode.MessageType.values()) {
			if (index == sockType.ordinal()) {
				return sockType;
			}
		}
		return null;
	}
	
	public static String typeRemoved(String recvString) {
		int i = recvString.indexOf('#');
		
		if (i == recvString.length() - 1){
			return "";
		}else{
			return recvString.substring(i + 1);
		}
	}
	
	public static String timeString() {
		String timeString = String.valueOf(Calendar.getInstance().getTimeInMillis());
		return timeString.substring(0,timeString.length()-3)+"."+timeString.substring(timeString.length()-3);
	}
	
	public static double roundDouble(double d) {
		return (double) Math.round(d * 100) / 100;		
	}
	
	public static void sendMessage(DatagramSocket socket, String message, int port) {
		byte[] sendByte = new byte[10];
		sendByte = message.getBytes();
		DatagramPacket sendPacket =null; 
		try {
			sendPacket = new DatagramPacket(sendByte, sendByte.length, 
					InetAddress.getByName("127.0.0.1"), port);
			socket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static String recvMessage(DatagramSocket sock) {
		byte[] recvByte = new byte[1024];
		DatagramPacket recvPacket = new DatagramPacket(recvByte, recvByte.length);
		
		try {
			sock.receive(recvPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (recvPacket.getAddress() == null) {
			return null;
		}
		return new String(recvByte, 0, recvPacket.getLength());
	}
	
	
	public static int getPort(String message) {
		int index = message.indexOf('@');
		String fromPort = message.substring(0, index);
		return Integer.parseInt(fromPort);
	}
	
	public static String getData(String message) {
		int index = message.indexOf('@');
		if (index == message.length() - 1){
			return "";
		}else{
			return message.substring(index + 1);
		}
	}
	
	
	
}
