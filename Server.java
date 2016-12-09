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

public class Server implements Runnable, Constants {
	private DatagramSocket socket = new DatagramSocket(SERVER_PORT);
	private Thread t = new Thread(this);
	private String receivedData = "";
	
	//three way handshake
	private boolean SYN = false;
	private boolean ACK = false;
	private int synNum = 0;
	private int ackNum = 0;
	
	public Server() throws SocketException {
		//sets the socket's timeout length
		socket.setSoTimeout(100);
		
		System.out.println("Server is running...");
		System.out.println("Waiting for client...");
		
		t.start();
	}//constructor
	
	//sends message to the client
	public void send(String message) throws IOException {
		byte[] buf = message.getBytes();
		InetAddress address = InetAddress.getByName(ADDRESS);
		DatagramPacket packet = new DatagramPacket(buf, buf.length, address, CLIENT_PORT);
		socket.send(packet);
	}//send
	
	public void run() {
		while(true) {
			try {
				Thread.sleep(1000*1);
			} catch (InterruptedException e) { }
			
			byte[] buf = new byte[256];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			
			try {
				socket.receive(packet);
			} catch (IOException e) { }
			
			String clientData = new String(buf);
			clientData = clientData.trim();
			
			//if not yet connected with the client
			if(clientData.startsWith("CONNECTING")) {
				System.out.println("Client SYN bit received by server.");
				System.out.println("Server ACK bit sent to client.");
				System.out.println("Server SYN bit sent to client.");
				
				SYN = true;
				try {
					send("CONNECTED");
				} catch (IOException e) { }
			}//if
			
			else if(clientData.startsWith("CONNECTED")) {
				System.out.println("Client ACK bit received by server.");
				
				ACK = true;
				
				if(SYN && ACK) {
					System.out.println("Connected with the client.");
				}//if
			}//else if
			
			//receives data from client
			else if(clientData.startsWith("DATA") && ACK && SYN) {
				String [] tokens = clientData.split("/");
				synNum = Integer.parseInt(tokens[2]);
				
				if(synNum == ackNum) {
					receivedData += tokens[1];
					System.out.println(receivedData);
					try {
						ackNum++;
						send("DATA/"+synNum);
					} catch (IOException e) {}	
				}//if
			}//else if
		}//while
	}//run
	
	public static void main(String[] args) throws SocketException {
		new Server();
	}//main

}//Server