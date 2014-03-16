
public class HandshakeMessage
{
	byte[] HELLO = new byte[] {72, 69, 76, 76, 79};
	byte[] zeroBits = new byte[23];
	int peerID;
	
	public HandshakeMessage(int peerID)
	{
		this.peerID = peerID;
	}
	
	public byte[] toByteArray()
	{
		byte[] result = new byte[32];
		System.arraycopy(HELLO, 0, result, 0, 5);
		System.arraycopy(zeroBits, 0, result, 5, 23);
		System.arraycopy(intToByteArray(peerID), 0, result, 28, 4);
		
		return result;
	}
	
	public static final byte[] intToByteArray(int value) 
	{
	    return new byte[] { (byte)(value >>> 24), (byte)(value >>> 16), (byte)(value >>> 8), (byte)value };
	}
	
	public static final int byteArrayToInt(byte[] fourbytearray) 
	{
	    return   fourbytearray[3] & 0xFF |
	            (fourbytearray[2] & 0xFF) << 8 |
	            (fourbytearray[1] & 0xFF) << 16 |
	            (fourbytearray[0] & 0xFF) << 24;
	}
}
