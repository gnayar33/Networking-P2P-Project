import java.io.*;
import java.util.*;

public class peerProcess {

	private int peerId;
	private String hostName;
	private int port;
	private int numPrefNeighbors;
	private int unchokingInterval;
	private int optimisticUnchokingInterval;
	private String fileName;
	private int fileSize;
	private int pieceSize;
	private boolean hasFile;
	private byte[] bitfield;
	
	private HashSet<Integer> interestedList = new HashSet<Integer>();		//maybe hashmap with downloading rate
	private HashSet<Integer> preferredNeighbors = new HashSet<Integer>(numPrefNeighbors);
	private HashSet<Integer> connections = new HashSet<Integer>();
	private Vector<RemotePeerInfo> peerInfoVector;

	public static void main(String[] args) {
		peerProcess pp = new peerProcess();
		pp.peerId = Integer.parseInt(args[0]);
		pp.getCommonConfiguration();
		pp.getPeerInfoConfiguration();
		pp.receiveMessage();
	}

	public void receiveMessage() {
		byte[] message = new byte[32];		//received from socket
		byte messageType = message[4];
		byte[] header = Arrays.copyOfRange(message, 0, 5);
		String headerString = Arrays.toString(header);
		if(headerString.equals("HELLO")) {	//handshake
			sendHandshake();
			//send bitfield to peer
			//store peerID in connection list
		}
		else if(messageType == 0) {	//choke
			
		}
		else if(messageType == 1) {	//unchoke
			request();
		}
		else if(messageType == 2) {	//interested
			//add peer to interested list
		}
		else if(messageType == 3) {	//not interested
			//remove peer from interested list
		}
		else if(messageType == 4) {	//have
			//update bitfield of neighbor
			//check bitfield to see if this peer wants that piece
			//send interested message if so
			//else, send not interested message
		}
		else if(messageType == 5) {	//bitfield
			//update bitfield of neighbor
			//check bitfield to see if this peer wants any pieces
			//send interested message if so
			//else, send not interested message
		}
		else if(messageType == 6) {	//request
			piece();
		}
		else if(messageType == 7) {	//piece
			//store data somewhere
		}
	}
	
	public boolean checkBitfield(byte[] bitfield) {
		boolean result = false;
		//returns true if bitfield has a 1 where this.bitfield has a 0
		return result;
	}
	
	public void sendHandshake() {
		
	}
	
	public void sendMessage() {
		
	}
	
	public void choke(int peerId) {
		//send choke message to peer with peerId
		//remove from preferred neighbor list
		//
	}
	
	public void unchoke() {
		//called every y seconds
		//pick k neighbors from interested list giving most data to this peer
		//or randomly if this peer has complete file
		//add to preferred neighbor list if not in already
		//unchoke them if not unchoked already
		//choke neighbors that are no longer preferred
	}
	
	public void optimisticUnchoke() {
		//called every x seconds
		//choke previous optimistically unchoked neighbor unless in preferred neighbor list
		//randomly unchoke neighbor in interested list
		//may become preferred neighbor
	}
	
	public void request() {
		//called when unchoked by a peer or receives piece completely by peer already in preferred neighbor list
		//select piece randomly that this peer needs and has not requested yet
		//send request message for this piece
	}
	
	public void piece() {
		//called when this peer receives request message
	}
	
	public void have() {
		//called when this peer receives a piece completely
		//notify all neighbors this peer has piece
		//check bitfield, send not interested message to some neighbors
		//update bitfield
	}

	public void getCommonConfiguration()
	{
		String st;
		Vector<String> commonInfoVector = new Vector<String>();
		try {
			BufferedReader in = new BufferedReader(new FileReader("Common.cfg"));
			for(int i = 0; i < 6; i++) {
				st = in.readLine();
				String[] tokens = st.split("\\s+");
		    	//System.out.println("tokens begin ----");
			    //for (int x=0; x<tokens.length; x++) {
			    //    System.out.println(tokens[x]);
			    //}
		        //System.out.println("tokens end ----");
			    
			    commonInfoVector.addElement(tokens[1]);
			}
			
			numPrefNeighbors = Integer.parseInt(commonInfoVector.elementAt(0));
			unchokingInterval = Integer.parseInt(commonInfoVector.elementAt(1));
			optimisticUnchokingInterval = Integer.parseInt(commonInfoVector.elementAt(2));
			fileName = commonInfoVector.elementAt(3);
			fileSize = Integer.parseInt(commonInfoVector.elementAt(4));
			pieceSize = Integer.parseInt(commonInfoVector.elementAt(5));
			
			in.close();
		}
		catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}

	public void getPeerInfoConfiguration()
	{
		String st;
		peerInfoVector = new Vector<RemotePeerInfo>();
		try {
			BufferedReader in = new BufferedReader(new FileReader("PeerInfo.cfg"));
			while((st = in.readLine()) != null) {
				
				String[] tokens = st.split("\\s+");
		    	//System.out.println("tokens begin ----");
			    //for (int x=0; x<tokens.length; x++) {
			    //    System.out.println(tokens[x]);
			    //}
		        //System.out.println("tokens end ----");
			
				if(Integer.parseInt(tokens[0]) == peerId) {
					hostName = tokens[1];
					port = Integer.parseInt(tokens[2]);
					int numPieces = fileSize / pieceSize;
		        	if(fileSize % pieceSize > 0)
		        		numPieces += 1;
		        	int numBytes = numPieces / 8;
		        	int leftover = numPieces % 8;
		        	if(leftover > 0) {
		        		numBytes += 1;
		        	}
		        	bitfield = new byte[numBytes];
		        	
		        	if(Integer.parseInt(tokens[3]) == 1) {		//set bitfield to 1 if this peer has file. test this
		        		hasFile = true;
		        		Arrays.fill(bitfield, (byte) 0xFF);
		        		byte lastByte = 0;
		        		for(int i = 0; i < leftover; i++) {
		        			lastByte |= 1 << (8 - i);
		        		}
		        		
		        		bitfield[numBytes - 1] = lastByte;
		        		
		        	}
		        	else {
		        		hasFile = false;
		        		Arrays.fill(bitfield, (byte) 0x00);
		        	}
		        	break;
		        }
		        
			    peerInfoVector.addElement(new RemotePeerInfo(tokens[0], tokens[1], tokens[2]));
			}
			
			in.close();
		}
		catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}
}
