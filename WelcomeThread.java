import java.io.*;
import java.net.*;
import java.util.*;

public class WelcomeThread extends Thread {
	int welcomeSocketNumber, peerID, pos, numPeers;
	peerProcess pp;
	//ArrayList<ConnectionData> server = new ArrayList<ConnectionData>();
	public WelcomeThread(int welcomeSocketNumber, int pos, int numPeers, int peerID, peerProcess pp) {
		this.welcomeSocketNumber = welcomeSocketNumber;
		this.peerID = peerID;
		this.pp = pp;
		this.numPeers = numPeers;
		this.pos = pos;
	}
	public void run() {
		try {
			File file = new File("/cise/homes/mpham/" + peerID + ".txt");
			if(!file.exists()) file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			ServerSocket welcomeSocket = new ServerSocket(welcomeSocketNumber);
			int numConnectionsMade = 0;
			while(numConnectionsMade < (numPeers - pos)) {		//figure out how many connections need to be made
				Socket connectionSocket = welcomeSocket.accept();
				pp.socketList.add(connectionSocket);
				numConnectionsMade++;
				bw.write("connection received\n");
			}
			welcomeSocket.close();
			bw.write("Closing welcome socket for peer: "+peerID + "\n");
			bw.close();
		}
		catch(Exception e){
			System.out.println("WELCOME SOCKET CREATION FAILED");
		}
	}
}
