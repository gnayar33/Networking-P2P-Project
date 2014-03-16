
import java.io.*;
import java.net.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;

public class P2PNode{
	int peerID;
	int welcomeSocketNumber = 1111;
	public Thread th1;
	public Thread th2;
	Socket clientSocket;
	DataOutputStream outToServer;
	BufferedReader inFromServer;
	ArrayList<ConnectionData> tcpConn;
	public P2PNode(int welcomeSocketNumber, int numPreviousPeers){
		//run server phase
		ArrayList<ConnectionData> tcpConn = new ArrayList<ConnectionData>();
		peerID = numPreviousPeers;
		System.out.println("Building peer: "+peerID);
		if(numPreviousPeers!=0){
			this.welcomeSocketNumber = welcomeSocketNumber;
			ServerSocket welcomeSocket;
			Socket connectionSocket;
			th1 = new welcomeThread(welcomeSocketNumber,numPreviousPeers, peerID);
			th1.start();

		}

	}

	public Thread getTH1(){
		return th1;
	}

	public Thread getTH2(){
		return th2;
	}

	public int getPeerID(){
		return peerID;
	}

	public void connectToNewPeer(int serverSocketNumber, int peerContacted){
		th2 = new clientThread(serverSocketNumber,peerID,peerContacted);
		//save to connectionData;
		th2.start();
	}

	// public boolean sendMessageToPeer(int serverSocketNumber, int peerContacted, byte[] message){
	// 	try{

	// 		//String transmission = "Peer " + peerID + " contacting " + peerContacted;
	// 	  	outToServer.writeBytes(message);

	// 	  	String returnTransmission = inFromServer.readLine();
	// 	  	System.out.println(returnTransmission);

	// 	  	return true;
	//   	}catch(Exception e){
	//   		return false;
	//   	}
	// }
}
class clientThread extends Thread{
	int serverSocketNumber;
	int peer1;
	int peer2;
	DataOutputStream outToServer;
	BufferedReader inFromServer;
	Socket clientSocket;
	public clientThread(int serverSocketNumber,int peer1,int peer2){
		this.serverSocketNumber = serverSocketNumber;
		this.peer1 = peer1;
		this.peer2 = peer2;
		try{
			clientSocket = new Socket("localhost", serverSocketNumber);
		  	outToServer = new DataOutputStream(clientSocket.getOutputStream());
		  	inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
	  	}catch(Exception e){
	  		
	  	}	
	}
	public void run(){
		try{
			String transmission = "Peer " + peer1 + " contacting " + peer2;
		  	outToServer.writeBytes(transmission + "\n");

		  	String returnTransmission = inFromServer.readLine();
		  	System.out.println(returnTransmission);
		    if(peer2 == (peer1 + 1)){
		  		//outToServer.writeBytes("END"+"\n");
		  	}
	  	}catch(Exception e){
	  	}	
	}
}
class serverThread extends Thread{
	Socket connectionSocket;
	BufferedReader inFromClient;
	DataOutputStream outToClient;
	public serverThread(Socket connectionSocket){
		this.connectionSocket = connectionSocket;
		System.out.println("connection sets up");
	}

	public void run(){
		try{
			inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			outToClient = new DataOutputStream(connectionSocket.getOutputStream());
			String temp;
			while(!((temp = inFromClient.readLine()).equals("END"))){
				outToClient.writeBytes(temp+": PROCESSED\n");
				System.out.println("RECIEVED: "+temp);
			}
			System.out.println("CONNECTION CLOSES");
			connectionSocket.close();
		}catch(Exception e){

		}
	}
}

class welcomeThread extends Thread{
	int welcomeSocketNumber;
	int numPreviousPeers;
	int peerID;
	ArrayList<ConnectionData> server = new ArrayList<ConnectionData>();
	private static Lock lock = new ReentrantLock();
	public welcomeThread(int welcomeSocketNumber, int numPreviousPeers, int peerID){
		this.welcomeSocketNumber = welcomeSocketNumber;
		this.numPreviousPeers = numPreviousPeers;
		this.peerID = peerID;
	}
	public void run(){
		if(welcomeThread.lock.tryLock()){
		try{
				ServerSocket welcomeSocket = new ServerSocket(welcomeSocketNumber);
				int numConnectionsMade = 0;
				while(numConnectionsMade<numPreviousPeers){

					Socket connectionSocket = welcomeSocket.accept();
					new serverThread(connectionSocket).start();
					numConnectionsMade++;
				}
				welcomeSocket.close();
				System.out.println("Closing Peer: "+peerID);
			}
			catch(Exception e){
				System.out.println("WELCOME SOCKET CREATION FAILED");
			}finally{
				welcomeThread.lock.unlock();
			}
		}
	}
}

class ConnectionData{
	DataOutputStream outToServer;
	BufferedReader inFromServer;
	Socket clientSocket;
	Socket connectionSocket;
	BufferedReader inFromClient;
	DataOutputStream outToClient;
	public ConnectionData(){
	}
	public ConnectionData(DataOutputStream outToServer, BufferedReader inFromServer, Socket clientSocket,Socket connectionSocket, BufferedReader inFromClient, DataOutputStream outToClient){
		this.outToServer = outToServer;
		this.inFromServer = inFromServer;
		this.clientSocket = clientSocket;
		this.connectionSocket = connectionSocket;
		this.inFromClient = inFromClient;
		this.outToClient = outToClient;
	}
	public void setClient(DataOutputStream outToServer, BufferedReader inFromServer, Socket clientSocket){
		this.outToServer = outToServer;
		this.inFromServer = inFromServer;
		this.clientSocket = clientSocket;
	}
	public void setServer(Socket connectionSocket, BufferedReader inFromClient, DataOutputStream outToClient){
		this.connectionSocket = connectionSocket;
		this.inFromClient = inFromClient;
		this.outToClient = outToClient;
	}
		public DataOutputStream getOutToServer() {
		return outToServer;
	}
	public void setOutToServer(DataOutputStream outToServer) {
		this.outToServer = outToServer;
	}
	public BufferedReader getInFromServer() {
		return inFromServer;
	}
	public void setInFromServer(BufferedReader inFromServer) {
		this.inFromServer = inFromServer;
	}
	public Socket getClientSocket() {
		return clientSocket;
	}
	public void setClientSocket(Socket clientSocket) {
		this.clientSocket = clientSocket;
	}
	public Socket getConnectionSocket() {
		return connectionSocket;
	}
	public void setConnectionSocket(Socket connectionSocket) {
		this.connectionSocket = connectionSocket;
	}
	public BufferedReader getInFromClient() {
		return inFromClient;
	}
	public void setInFromClient(BufferedReader inFromClient) {
		this.inFromClient = inFromClient;
	}
	public DataOutputStream getOutToClient() {
		return outToClient;
	}
	public void setOutToClient(DataOutputStream outToClient) {
		this.outToClient = outToClient;
	}

}