import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.net.*;
import java.util.logging.*;

public class peerProcess {

	private int peerId, pos;
	private int numPeers = 0;
	private String hostName;
	private int port;
	private int numPrefNeighbors;
	private int unchokingInterval;
	private int optimisticUnchokingInterval;
	private String fileName;
	private int fileSize;
	private int pieceSize;
	private int numPieces;
	private int numPiecesRec;
	
	private boolean hasFile;
	private byte[] bitfield;
	private long startTime;
	private long timeInterval;
	
	private byte[][] theFile;
	public HashMap<String, Integer> addressToPeerID = new HashMap<String, Integer>();
	public ConcurrentHashMap<Integer, Socket> peerIDToSocket = new ConcurrentHashMap<Integer, Socket>();
	public ArrayList<Integer> handshakeSent = new ArrayList<Integer>();
	private HashSet<Integer> interestedList = new HashSet<Integer>();
	private HashMap<Integer, Integer> neighbors = new HashMap<Integer, Integer>();	//sort by data transmission rate. peer id map to downloading rate
	private HashMap<Integer, Integer> unchokeList = new HashMap<Integer, Integer>(numPrefNeighbors);	//peers that are unchoked
	private HashSet<Integer> chokedByList = new HashSet<Integer>();
	private HashMap<Integer, byte[]> bitfields = new HashMap<Integer, byte[]>();
	private HashSet<Integer> connections = new HashSet<Integer>();
	private Vector<RemotePeerInfo> peerInfoVector;
	
	private final static Logger logger = Logger.getLogger(peerProcess.class.getName());
	Logger l = Logger.getLogger("");

	public static void main(String[] args) {
		
		
		
		peerProcess pp = new peerProcess();
		if(args.length != 1) {
			System.out.println("usage: java peerProcess [peerID]");
			System.exit(0);
		}
		pp.peerId = Integer.parseInt(args[0]);
		pp.getCommonConfiguration();
		pp.getPeerInfoConfiguration();
		
		
		try{
			FileHandler fh = new FileHandler("log/log_peer_" + pp.peerId + ".log",false);
			fh.setFormatter(new SimpleFormatter());
			pp.l.addHandler(fh);
		}
		catch(Exception e){}
		
		pp.l.log(Level.INFO,"Logging Start");
		
		try {
			if(pp.hasFile) {
				FileInputStream fis = new FileInputStream(pp.fileName);
				for(int i = 0; i < pp.numPieces - 1; i++) {
					byte[] currPiece = new byte[pp.pieceSize];
					fis.read(currPiece);
					pp.theFile[i] = currPiece;
				}
				byte[] lastPiece = new byte[fis.available()];
				fis.read(lastPiece);
				pp.theFile[pp.numPieces - 1] = lastPiece;
				fis.close();
				pp.l.log(Level.INFO,"have file, wrote to byte array");
				pp.numPiecesRec = pp.numPieces;
			}
			else {
				pp.numPiecesRec = 0;
			}
		}
		catch(Exception e) {}
		
		//System.out.println("hi from " + pp.peerId + " pos " + pp.pos + " peerInfo Size " + pp.peerInfoVector.size() + " numPeers " + pp.numPeers);
		try{
			pp.setUpServer();
			pp.setUpClient();
			pp.l.log(Level.WARNING, "Sockets should be set up");	
		}catch(Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			pp.l.log(Level.SEVERE, sw.toString());
		}

		try {
			long startTime = System.currentTimeMillis();
			String path = System.getProperty("user.dir");
			
			
			
			/*while(pp.bitfields.size() < pp.numPeers - 1) {
				for(int p: pp.peerIDToSocket.keySet()) {
					InputStream in = pp.peerIDToSocket.get(p).getInputStream();
					if(in.available() >= 5) {
						byte[] header = new byte[5];
						in.read(header);
						pp.interpretMessage(header, in, p);
					}
				}
			}*/
			
			long unchokeTime = System.currentTimeMillis();
			long optUnchokeTime = System.currentTimeMillis();
			while(!pp.allHaveFile()) {	//should be not all peers have file
				long currentTime = System.currentTimeMillis();
				if(currentTime - unchokeTime >= pp.unchokingInterval * 1000) {
					pp.l.log(Level.WARNING, "REGULAR UNCHOKE TIME DIFFERENCE: " + (currentTime - unchokeTime));
					unchokeTime = System.currentTimeMillis();
					pp.unchoke();
				}
				if(currentTime - optUnchokeTime >= pp.optimisticUnchokingInterval * 1000) {
					pp.l.log(Level.WARNING, "OPT UNCHOKE TIME DIFFERENCE: " + (currentTime - optUnchokeTime));
					optUnchokeTime = System.currentTimeMillis();
					pp.optimisticUnchoke();
				}
				for(int p: pp.peerIDToSocket.keySet()) {
					InputStream in = pp.peerIDToSocket.get(p).getInputStream();
					if(in.available() >= 5) {
						byte[] header = new byte[5];
						in.read(header);
						String headerString = new String(header);
						String hello = new String(new byte[] {72, 69, 76, 76, 79});
						File file = new File(path + "/" + pp.peerId + ".txt");
						if(!file.exists()) file.createNewFile();
						FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write("Header: " + headerString + " Size: " + in.available() + "\n");
						bw.close();
						pp.interpretMessage(header, in, p);
					}
				}
			}
			pp.l.log(Level.WARNING, "EXITED WHILE LOOP");
			Thread.sleep(10000);
			File dlFile = new File(path + "/peer_" + pp.peerId + "/" + pp.fileName);
			File dlFileDir = dlFile.getParentFile();
			if(null != dlFileDir) {
				dlFileDir.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(dlFile);
			for(int i = 0; i < pp.numPieces; i++) {
				fos.write(pp.theFile[i]);
			}
			fos.close();
		}
		catch(Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			pp.l.log(Level.SEVERE, sw.toString());
			/*try {
				String path = System.getProperty("user.dir");
				File file = new File(path + "/" + pp.peerId + ".txt");
				if(!file.exists()) file.createNewFile();
				FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
				PrintWriter pw = new PrintWriter(fw);
				pw.println("EXCEPTION loop: ");
				e.printStackTrace(pw);
				//pw.write("EXCEPTION loop: " + e.toString() + "\n");
				pw.close();
			}
			catch(IOException ex) {}*/
		}

		/*long startTime = System.currentTimeMillis();
		while(pp.peerIDToSocket.size() < pp.numPeers-1){
			try{Thread.sleep(1000);}catch(Exception e){}
			long endTime = System.currentTimeMillis();
			if((endTime-startTime)>10000) {
				break;
			}
		}*/
		pp.printConnections();
		try {
			for(int p: pp.peerIDToSocket.keySet()) {
				while(!(pp.peerIDToSocket.get(p).isClosed()))
				pp.peerIDToSocket.get(p).close();
			}
			Runtime.getRuntime().exec("exit");
		}
		catch(Exception e) {}
	}

	public void setUpServer() {
		(new WelcomeThread(port, pos, numPeers, peerId, this)).start();
	}
	
	public void setUpClient() throws Exception{
		for(int i = 1; i < pos; i++) {
			RemotePeerInfo peerInfo = peerInfoVector.get(i - 1);
			int destination = Integer.parseInt(peerInfo.peerId);
			Socket clientSocket = new Socket(peerInfo.peerAddress, Integer.parseInt(peerInfo.peerPort));
			l.log(Level.INFO, "Peer " + peerId + " makes a connection to " + destination);
			peerIDToSocket.put(destination, clientSocket);
			handshakeSent.add(destination);
			sendHandshake(destination);
			l.log(Level.INFO, "Peer " + peerId + " sent handshake to peer " + destination);
		}
	}

	public void printConnections(){
		try{	
			String path = System.getProperty("user.dir");
			File file = new File(path + "/" + peerId + ".txt");
			if(!file.exists()) file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write("Sockets active: "+peerIDToSocket.size()+" ID: "+peerId+"/"+numPeers+"\n");
			for(Integer p: peerIDToSocket.keySet()){
				bw.write(p +": address"+peerIDToSocket.get(p).getInetAddress().getCanonicalHostName()+"\n");
			}
			bw.write("number of handshakes: " + handshakeSent.size()+"\n");
			for(int i = 0; i<handshakeSent.size();i++){
				bw.write(handshakeSent.get(i)+"\n");
			}
			bw.write("interested: \n");
			for(int i : interestedList){
				bw.write(i + "\n");
			}
			bw.close();
		}catch(IOException e){

		}

	}

	public void interpretMessage(byte[] header, InputStream in, int peerID) throws Exception{
		byte messageType = header[4];
		String headerString = new String(header);
		if(headerString.equals("HELLO")) {	//handshake
			byte[] peerIdBytes = new byte[4];
			byte[] message = new byte[27];
			in.read(message);
			System.arraycopy(message,23,peerIdBytes,0,4);
			int receivedPeerID = HandshakeMessage.byteArrayToInt(peerIdBytes);
			if(!handshakeSent.contains(receivedPeerID)) {
				handshakeSent.add(receivedPeerID);
				sendHandshake(receivedPeerID);
			}
			l.log(Level.WARNING, "Peer " + peerId + " received handshake from peer " + peerID);
			sendBitfield(receivedPeerID);
		}
		else if(messageType == 0) {	//choke
			l.log(Level.INFO, "Peer " + peerId + " is choked by " + peerID);
			chokedByList.add(peerID);
		}
		else if(messageType == 1) {	//unchoke
			l.log(Level.INFO, "Peer " + peerId + " is unchoked by " + peerID);
			chokedByList.remove(peerID);
			byte[] peerBitfield = bitfields.get(peerID);
			if(checkBitfield(peerBitfield)) {
				ArrayList<Integer> pieces = new ArrayList<Integer>();
				for(int i = 0; i < peerBitfield.length - 1; i++) {
					l.log(Level.WARNING, "LOOPING");
					for(int j = 0; j < 8; j++) {
						byte mask = (byte) (1 << j);
						if(((bitfield[i] & mask) == 0) && ((peerBitfield[i] & mask) == mask)) {
							pieces.add(i * 8 + j);
						}
					}
				}
				int leftover = numPieces % 8;
				for(int i = 0; i < leftover; i++) {
					byte mask = (byte) (1 << i);
					if(((bitfield[bitfield.length - 1] & mask) == 0) && ((peerBitfield[peerBitfield.length - 1] & mask) == mask)) {
						pieces.add((bitfield.length - 1) * 8 + i);
					}
				}
				int pieceIndex = (int) (Math.random() * pieces.size());
				l.log(Level.INFO, "BITFIELD SIZE: " + peerBitfield.length);
				l.log(Level.INFO, "PIECES SIZE: " + pieces.size());
				sendRequest(peerID, pieces.get(pieceIndex));
				l.log(Level.WARNING, "Requesting piece index " + pieces.get(pieceIndex) + " from peer " + peerID);
			}
			else {
				sendNotInterested(peerID);
			}
		}
		else if(messageType == 2) {	//interested
			l.log(Level.INFO, "Peer " + peerId + " received an 'interested' message from " + peerID);
			interestedList.add(peerID);
		}
		else if(messageType == 3) {	//not interested
			l.log(Level.INFO, "Peer " + peerId + " received a 'not interested message' from " + peerID);
			interestedList.remove(peerID);
		}
		else if(messageType == 4) {	//have
			byte[] pieceIndexBytes = new byte[4];
			in.read(pieceIndexBytes);
			int pieceIndex = Message.byteArrayToInt(pieceIndexBytes);
			l.log(Level.INFO, "Peer " + peerId + " received a 'have' message from " + peerID + " for the piece " + pieceIndex);
			int byteNum = pieceIndex / 8;
			int bit = pieceIndex % 8;
			byte[] peerBitfield = bitfields.get(peerID);
			peerBitfield[byteNum] |= 1 << bit;
			if(checkBitfield(peerBitfield)) {
				sendInterested(peerID);
			}
			else {
				sendNotInterested(peerID);
			}
			//update bitfield of neighbor
			//check bitfield to see if this peer wants that piece
			//send interested message if so
			//else, send not interested message
		}
		else if(messageType == 5) {	//bitfield
			int messageLength = Message.byteArrayToInt(new byte[] {header[0], header[1], header[2], header[3]});
			byte[] peerBitfield = new byte[messageLength - 1];
			in.read(peerBitfield);
			bitfields.put(peerID, peerBitfield);	//update bitfield of neighbor
			//check bitfield to see if this peer wants any pieces
			if(checkBitfield(peerBitfield)) {
				sendInterested(peerID);
			}
			else {
				sendNotInterested(peerID);
			}
			//send interested message if so
			//else, send not interested message
		}
		else if(messageType == 6) {	//request
			byte[] pieceIndexBytes = new byte[4];
			in.read(pieceIndexBytes);
			int pieceIndex = Message.byteArrayToInt(pieceIndexBytes);
			sendPiece(peerID, pieceIndex, theFile[pieceIndex]);
			l.log(Level.WARNING, "Sent piece index " + pieceIndex + " to peer " + peerID);
		}
		else if(messageType == 7) {	//piece
			numPiecesRec++;
			int messageLength = Message.byteArrayToInt(new byte[] {header[0], header[1], header[2], header[3]});
			byte[] pieceIndexBytes = new byte[4];
			in.read(pieceIndexBytes);
			int pieceIndex = Message.byteArrayToInt(pieceIndexBytes);
			l.log(Level.INFO, "Peer " + peerId + " has downloaded the piece " + pieceIndex + " from " + peerID + "\nNow the number of pieces it has is " + numPiecesRec);
			byte[] piece = new byte[messageLength - 5];
			in.read(piece);
			theFile[pieceIndex] = piece;
			sendHave(peerID, pieceIndex);
			int byteNum = pieceIndex / 8;
			int bit = pieceIndex % 8;
			bitfield[byteNum] |= 1 << bit;
			for(int i = 0; i < peerInfoVector.size(); i++) {
				int otherPeerID = Integer.parseInt(peerInfoVector.get(i).peerId);
				l.log(Level.WARNING, "BITFIELDS GET SIZE: " + bitfields.get(otherPeerID).length);
				if(checkBitfield(bitfields.get(otherPeerID))) {
					sendInterested(otherPeerID);
				}
				else {
					sendNotInterested(otherPeerID);
				}
			}
			byte[] peerBitfield = bitfields.get(peerID);
			if(checkBitfield(peerBitfield) && !chokedByList.contains(peerID)) {
				ArrayList<Integer> pieces = new ArrayList<Integer>();
				for(int i = 0; i < peerBitfield.length - 1; i++) {
					for(int j = 0; j < 8; j++) {
						byte mask = (byte) (1 << j);
						if(((bitfield[i] & mask) == 0) && ((peerBitfield[i] & mask) == mask)) {
							pieces.add(i * 8 + j);
						}
					}
				}
				int leftover = numPieces % 8;
				for(int i = 0; i < leftover; i++) {
					byte mask = (byte) (1 << i);
					if(((bitfield[bitfield.length - 1] & mask) == 0) && ((peerBitfield[peerBitfield.length - 1] & mask) == mask)) {
						pieces.add((bitfield.length - 1) * 8 + i);
					}
				}
				int newPieceIndex = (int) (Math.random() * pieces.size());
				l.log(Level.INFO, "NEW PIECE INDEX: " + newPieceIndex);
				l.log(Level.INFO, "PIECES SIZE: " + pieces.size());
				sendRequest(peerID, pieces.get(newPieceIndex));
				l.log(Level.WARNING, "Requesting piece index " + pieces.get(newPieceIndex) + " from peer " + peerID);
			}
			if(haveFile(peerId)) {
				hasFile = true;
				l.log(Level.INFO, "Peer " + peerId + " has downloaded the complete file");
				l.log(Level.WARNING, "Do all peers have file? " + allHaveFile());
			}
			//store data somewhere
			//send have message to all peers
			//check bitfields, send notinterested
		}
	}
	
	public boolean checkBitfield(byte[] peerBitfield) throws Exception{		//true if peerbitfield has 1 where bitfield has 0
		boolean result = false;
		for(int i = 0; i < peerBitfield.length - 1; i++) {
			if(result) {
				break;
			}
			for(int j = 0; j < 8; j++) {
				byte mask = (byte) (1 << j);
				if(((bitfield[i] & mask) == 0) && ((peerBitfield[i] & mask) == mask)) {
					result = true;
					break;
				}
			}
		}
		int leftover = numPieces % 8;
		for(int i = 0; i < leftover; i++) {
			byte mask = (byte) (1 << i);
			if(((bitfield[bitfield.length - 1] & mask) == 0) && ((peerBitfield[peerBitfield.length - 1] & mask) == mask)) {
				result = true;
				break;
			}
		}
		return result;
	}
	
	public boolean allHaveFile() throws Exception {		//returns false if 0 found in any bitfield
		for(int i : bitfields.keySet()) {
			if(!haveFile(i) || !hasFile) {
				return false;
			}
		}
		return true;
	}
	
	public boolean haveFile(int peerID) throws Exception {	//returns false if 0 found in peerID's bitfield
		byte[] currBitfield;
		if(peerID == peerId) {
			currBitfield = bitfield;
		}
		else {
			currBitfield = bitfields.get(peerID);
		}
		for(int j = 0; j < currBitfield.length - 1; j++) {
			for(int k = 0; k < 8; k++) {
				byte mask = (byte) (1 << k);
				if(((currBitfield[j] & mask) == 0)) {
					return false;
				}
			}
		}
		int leftover = numPieces % 8;
		for(int j = 0; j < leftover; j++) {
			byte mask = (byte) (1 << j);
			if(((currBitfield[currBitfield.length - 1] & mask) == 0)) {
				return false;
			}
		}
		return true;
	}
	
	public void sendHandshake(int toPeerID) throws Exception {
		OutputStream out = peerIDToSocket.get(toPeerID).getOutputStream();
		out.write((new HandshakeMessage(this.peerId)).toByteArray());
	}
	
	public void sendBitfield(int toPeerID) throws Exception {
		OutputStream out = peerIDToSocket.get(toPeerID).getOutputStream();
		out.write((new BitfieldMessage(this.bitfield)).toByteArray());
	}
	
	public void sendInterested(int toPeerID) throws Exception {
		OutputStream out = peerIDToSocket.get(toPeerID).getOutputStream();
		out.write((new InterestedMessage()).toByteArray());
	}
	
	public void sendNotInterested(int toPeerID) throws Exception {
		OutputStream out = peerIDToSocket.get(toPeerID).getOutputStream();
		out.write((new NotInterestedMessage()).toByteArray());
	}
	
	public void sendChoke(int toPeerID) throws Exception{
		OutputStream out = peerIDToSocket.get(toPeerID).getOutputStream();
		out.write((new ChokeMessage()).toByteArray());
	}
	
	public void sendUnchoke(int toPeerID) throws Exception{
		OutputStream out = peerIDToSocket.get(toPeerID).getOutputStream();
		out.write((new UnchokeMessage()).toByteArray());
	}
	
	public void sendRequest(int toPeerID, int pieceIndex) throws Exception{
		OutputStream out = peerIDToSocket.get(toPeerID).getOutputStream();
		out.write((new RequestMessage(pieceIndex)).toByteArray());
	}
	
	public void sendPiece(int toPeerID, int pieceIndex, byte[] piece) throws Exception{
		OutputStream out = peerIDToSocket.get(toPeerID).getOutputStream();
		out.write((new PieceMessage(pieceIndex, piece)).toByteArray());
	}
	
	public void sendHave(int toPeerID, int pieceIndex) throws Exception{
		OutputStream out = peerIDToSocket.get(toPeerID).getOutputStream();
		out.write((new HaveMessage(pieceIndex)).toByteArray());
	}
	
	public void unchoke() throws Exception{
		if(!hasFile) {
			LinkedHashMap<Integer, Integer> sortedNeighbors = sortByDLRate(neighbors);
			
			for(int i : unchokeList.keySet()) {
				unchokeList.put(i, 0);
			}
			
			int count = 0;
			for(int i : sortedNeighbors.keySet()) {
				if(interestedList.contains(i)) {
					count++;
					neighbors.put(i, 0);	//reset download rate
					if(!unchokeList.containsKey(i)) {
						sendUnchoke(i);
					}
					unchokeList.put(i, 1);
				}
				if(count >= numPrefNeighbors) {
					break;
				}
			}
			for(int i : unchokeList.keySet()) {
				if(unchokeList.get(i) == 0) {
					sendChoke(i);
					unchokeList.remove(i);
				}
			}
		}
		else {	//randomly choose
			for(int i : unchokeList.keySet()) {
				sendChoke(i);
			}
			unchokeList.clear();
			int peerID, randIndex;
			int numPeersGenerated = 0;
			do {
				randIndex = (int) (Math.random() * peerInfoVector.size());
				peerID = Integer.parseInt(peerInfoVector.get(randIndex).peerId);
			}
			while(!interestedList.contains(peerID));
			unchokeList.put(peerID, 1);
			sendUnchoke(peerID);
		}
		String prefNeighbors = "";
		for(int i : unchokeList.keySet()) {
			prefNeighbors = prefNeighbors + i + ",";
		}
		l.log(Level.INFO, "Peer " + peerId + " has the preferred neighbors " + prefNeighbors);
		//called every unchokingInterval seconds
		//pick k neighbors from interested list giving most data to this peer
		//or randomly if this peer has complete file
		//add to preferred neighbor list if not in already
		//unchoke them if not unchoked already
		//choke neighbors that are no longer preferred
	}
	
	public void optimisticUnchoke() throws Exception{
		//called every x seconds
		//randomly unchoke neighbor in interested list
		int peerID, randIndex;
		do {
			randIndex = (int) (Math.random() * peerInfoVector.size());
			peerID = Integer.parseInt(peerInfoVector.get(randIndex).peerId);
		}
		while(!interestedList.contains(peerID));
		unchokeList.put(peerID, 1);
		l.log(Level.INFO, "Peer " + peerId + " has the optimistically-unchoked neighbor " + peerID);
		//may become preferred neighbor
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
				numPeers++;
				String[] tokens = st.split("\\s+");
		    		/*System.out.println("tokens begin ----");
				for (int x=0; x<tokens.length; x++) {
					System.out.println(tokens[x]);
				}
				System.out.println("tokens end ----");*/
			
				if(Integer.parseInt(tokens[0]) == peerId) {
					pos = numPeers;
					hostName = tokens[1];
					port = Integer.parseInt(tokens[2]);
					numPieces = fileSize / pieceSize;
		        		if(fileSize % pieceSize > 0)
		        			numPieces += 1;
		        		int numBytes = numPieces / 8;
		        		int leftover = numPieces % 8;
		        		if(leftover > 0) {
		        			numBytes += 1;
		        		}
		        		theFile = new byte[numPieces][];
		        		bitfield = new byte[numBytes];

		        		if(Integer.parseInt(tokens[3]) == 1) {		//set bitfield to 1 if this peer has file. test this
						hasFile = true;
		        			Arrays.fill(bitfield, (byte) 0xFF);
		     		   		byte lastByte = 0;
		    	    			for(int i = 0; i < leftover; i++) {
		        				lastByte |= 1 << i;
		        			}
		        		
		        			bitfield[numBytes - 1] = lastByte;
		        			
		        		}
		        		else {
		        			hasFile = false;
		        			Arrays.fill(bitfield, (byte) 0x00);
		        		}
				}
				else {
					peerInfoVector.addElement(new RemotePeerInfo(tokens[0], tokens[1], tokens[2]));
					addressToPeerID.put(tokens[1],Integer.parseInt(tokens[0]));
					numPieces = fileSize / pieceSize;
		        		if(fileSize % pieceSize > 0)
		        			numPieces += 1;
		        		int numBytes = numPieces / 8;
		        		int leftover = numPieces % 8;
		        		if(leftover > 0) {
		        			numBytes += 1;
		        		}
					byte[] tempBitfield = new byte[numBytes];
					if(Integer.parseInt(tokens[3]) == 1) {		//set bitfield to 1 if this peer has file. test this
		        			Arrays.fill(tempBitfield, (byte) 0xFF);
		     		   		byte lastByte = 0;
		    	    			for(int i = 0; i < leftover; i++) {
		        				lastByte |= 1 << i;
		        			}
		        		
		        			tempBitfield[numBytes - 1] = lastByte;
		        			
		        		}
		        		else {
		        			Arrays.fill(tempBitfield, (byte) 0x00);
		        		}
					bitfields.put(Integer.parseInt(tokens[0]), tempBitfield);
				}
			}
			in.close();
		}
		catch (Exception ex) {
			System.out.println(ex.toString());
		}
	}
	
	public LinkedHashMap<Integer, Integer> sortByDLRate(HashMap<Integer, Integer> passedMap) throws Exception{
		ArrayList<Integer> mapKeys = new ArrayList<Integer>(passedMap.keySet());
		ArrayList<Integer> mapValues = new ArrayList<Integer>(passedMap.values());
		Collections.sort(mapValues);
		Collections.sort(mapKeys);
	
		LinkedHashMap<Integer, Integer> sortedMap = new LinkedHashMap<Integer, Integer>();
	    
		Iterator<Integer> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			int val = valueIt.next();
			Iterator<Integer> keyIt = mapKeys.iterator();
		
			while (keyIt.hasNext()) {
				int key = keyIt.next();
				int comp1 = passedMap.get(key);
				int comp2 = val;
		
				if (comp1 == comp2) {
					passedMap.remove(key);
					mapKeys.remove(key);
					sortedMap.put(key, val);
					break;
			    	}
			}
	    	}
	    	return sortedMap;
	}
}
