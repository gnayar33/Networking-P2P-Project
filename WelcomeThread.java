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
		// String path = System.getProperty("user.dir");
		// File file = new File(path + "/" + peerID + ".txt");
		// BufferedWriter bw;
		// FileWriter fw;
		ServerSocket welcomeSocket;
		try {
			welcomeSocket = new ServerSocket(welcomeSocketNumber);
			// if(!file.exists()) file.createNewFile();
			// fw = new FileWriter(file.getAbsoluteFile(), true);
			// bw = new BufferedWriter(fw);
			// bw.write(peerID + ": setting up serversocket\n");
			// bw.close();
			int numConnectionsMade = 0;
			while(numConnectionsMade < (numPeers - pos)) {		//figure out how many connections need to be made
				Socket connectionSocket = welcomeSocket.accept();
				pp.socketList.add(connectionSocket);
				numConnectionsMade++;
				// fw = new FileWriter(file.getAbsoluteFile(), true);
				// bw = new BufferedWriter(fw);
				// bw.write(peerID + ": connection received from " + connectionSocket.getRemoteSocketAddress() + "\n");
				// bw.close();
			}
			welcomeSocket.close();
			// fw = new FileWriter(file.getAbsoluteFile(), true);
			// bw = new BufferedWriter(fw);
			// bw.write(peerID + ": Closing welcome socket for peer: "+peerID + "\n");
			// bw.close();
		}
		catch(Exception e) {
			// try{
				// StringWriter sw = new StringWriter();
				// PrintWriter pw = new PrintWriter(sw);
				// e.printStackTrace(pw);
				// fw = new FileWriter(file.getAbsoluteFile(), true);
				// bw = new BufferedWriter(fw);
				// bw.write(peerID + ": " + sw.toString());
				// bw.close();
			// }
			// catch(Exception ex) {}
		}
	}
}
