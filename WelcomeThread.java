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
			String path = System.getProperty("user.dir");
			File file = new File(path + "/out.txt");
			if(!file.exists()) file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(peerID + ": setting up serversocket\n");
			ServerSocket welcomeSocket = new ServerSocket(welcomeSocketNumber);
			int numConnectionsMade = 0;
			while(numConnectionsMade < (numPeers - pos)) {		//figure out how many connections need to be made
				Socket connectionSocket = welcomeSocket.accept();
				//pp.socketList.put(connectionSocket);
				numConnectionsMade++;
				bw.write(peerID + ": connection received from " + connectionSocket.getRemoteSocketAddress() + "\n");
			}
			welcomeSocket.close();
			bw.write(peerID + ": Closing welcome socket for peer: "+peerID + "\n");
			bw.close();
		}
		catch(Exception e){
			System.out.println("WELCOME SOCKET CREATION FAILED");
		}
	}
}
