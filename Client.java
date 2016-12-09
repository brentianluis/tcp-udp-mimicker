/*
 * Author: Luis, Brent Ian
 * Student Number: 2012-46101
 * Lab Section: CMSC 137 CD-3L
 *
 * Project 1:
 * Implements a UDP mimicking TCP packet transfer
 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Client implements Runnable, Constants {
	private DatagramSocket socket = new DatagramSocket(CLIENT_PORT);
	private Thread t = new Thread(this);
	private String serverData;
	private boolean connectedToServer = false;
	
	//three way handshake
	private boolean SYN = false;
	private boolean ACK = false;
	private int synNum = 0;
	private int ackNum = 0;
	
	// packet dropping probability
	private double[] DROP_PROBABILITY = {0.00, 0.25, 0.50, 0.75};
	
	public Client() throws SocketException {
		//sets the socket's timeout length
		socket.setSoTimeout(100);
		
		t.start();
	}//constructor
	
	//sends message to the server
	public void send(String message) throws IOException {
		byte[] buf = message.getBytes();
		InetAddress address = InetAddress.getByName(ADDRESS);
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, SERVER_PORT);
		socket.send(packet);
	}//send
	
	public void run() {
		System.out.println("Connecting to server " + ADDRESS + "...");
		
		//keeps receiving data from the server
		while(true) {
			try {
				Thread.sleep(1000*1);
			} catch (InterruptedException e) { }
			
			byte[] buf = new byte[256];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			
			try {
				socket.receive(packet);
			} catch (IOException e) { }
			
			serverData = new String(buf);
			serverData = serverData.trim();
			
			//if not yet connected to the server
			if(!connectedToServer && serverData.startsWith("CONNECTED")) {				
				System.out.println("Server ACK bit received by client.");
				System.out.println("Server SYN bit received by client.");
				System.out.println("Client ACK bit sent to server.");
				
				ACK = true;
				//if the server accepts the connection, trigger "connectedToServer" flag to true
				if(ACK && SYN) {
					connectedToServer = true;
					System.out.println("Successfully connected to the server!");
					
					try {
						send("CONNECTED");
					} catch (IOException e) { }
				}//if
			}//if
			
			else if(!connectedToServer) {
				System.out.println("Connecting to the server...");
				System.out.println("Client SYN bit sent to server.");
				
				SYN = true;
				
				try {
					send("CONNECTING");
				} catch (IOException e) { }
			}//else if
			
			//if connected to the server
			else {
				if(serverData.startsWith("DATA")) {
					String [] tokens = serverData.split("/");					
					ackNum = Integer.parseInt(tokens[1]);
										
					if(synNum == ackNum) {
						synNum++;
						
						if(DATA.getBytes().length > (WINDOW_SIZE*(synNum+1))) {
							try {
								send("DATA/" + DATA.substring((synNum*WINDOW_SIZE), (((synNum+1)*WINDOW_SIZE)))+"/"+synNum);
							} catch (IOException e) { }
						}//if
						
						else {
							System.out.println("THIS");
							try {
								send("DATA/" + DATA.substring((synNum*WINDOW_SIZE), (DATA.getBytes().length))+"/"+synNum);
								break;
							} catch (IOException e) { }						
						}//else
					}//if	
				}//if
					
				else {
					if(DATA.getBytes().length > (WINDOW_SIZE*(synNum+1))) {
						try {
							send("DATA/" + DATA.substring((synNum*WINDOW_SIZE), (((synNum+1)*WINDOW_SIZE)))+"/"+synNum);
						} catch (IOException e) { }
					}//if
				}//else
			}//else
		}//while
	}//run
	
	public static void main(String[] args) throws SocketException {
		new Client();
	}//main
	
}//Client