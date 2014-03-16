import java.util.ArrayList;

public class Setup{
	static int numPeers = 3;
	static int sharedWelcomeSocket = 1111;
	public static void main(String[] args){
		ArrayList<P2PNode> peers = new ArrayList<P2PNode>();
		for(int i = 0; i< numPeers; i++){
			P2PNode temp = new P2PNode(sharedWelcomeSocket, i);
			Thread th1 = temp.getTH1();
			int peerID = temp.getPeerID();
			if(!peers.isEmpty()){
				for(int k = 0; k<peers.size(); k++){
					P2PNode prevTemp = peers.get(k);
					prevTemp.connectToNewPeer(sharedWelcomeSocket,temp.peerID);
				}
			}
			peers.add(temp);
			try{
			th1.join();
			}catch(Exception e){

			}

		}
		P2PNode test = peers.get(0);
		//test.sentMessageToPeer()
	}

}