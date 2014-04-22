import java.io.*;
import java.net.*;
import java.util.*;

public class WelcomeThread extends Thread {
	int welcomeSocketNumber, peerID, pos, numPeers;
	peerProcess pp;
	public WelcomeThread(int welcomeSocketNumber, int pos, int numPeers, int peerID, peerProcess pp) {
		this.welcomeSocketNumber = welcomeSocketNumber;
		this.peerID = peerID;
		this.pp = pp;
		this.numPeers = numPeers;
		this.pos = pos;
	}
	public void run() {
		ServerSocket welcomeSocket;
		try {
			welcomeSocket = new ServerSocket(welcomeSocketNumber);
			int numConnectionsMade = 0;
			while(numConnectionsMade < (numPeers - pos)) {
				Socket connectionSocket = welcomeSocket.accept();
				int fromPeerID = pp.addressToPeerID.get(connectionSocket.getInetAddress().getCanonicalHostName());//THINK ABOUT CONFLICT HERE
				pp.peerIDToSocket.put(fromPeerID,connectionSocket);
				numConnectionsMade++;
			}
			welcomeSocket.close();
		}
		catch(Exception e) {
		}
	}
}
