
public class PieceMessage extends Message
{
	public PieceMessage(int index, byte[] piece)
	{
		//create default message
		super();
		
		//create and organize new payload
		//payload = index concatenated with piece as byte array
		byte[] payload = new byte[4 + piece.length];
		byte[] indexByteArray = intToByteArray(index);
		
		//copy arrays
		System.arraycopy(indexByteArray, 0, payload, 0, 4);
		System.arraycopy(piece, 0, payload, 4, piece.length);
		
		//set message values appropriately so that it is a piece message
		this.setLength(1 + payload.length);
		this.setType((byte) 7);
		this.setPayload(payload);
	}
}
