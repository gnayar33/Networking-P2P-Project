
public class Message 
{
	
	int messageLength;
	byte messageType;
	byte[] payload;
	/*
	 types:
	 choke: 0
	 unchoke: 1
	 interested: 2
	 not interested: 3
	 have: 4
	 bitfield: 5
	 request: 6
	 piece: 7
	 */
	
	
	/*
	int indexPayload;
	byte[] piecePayload;
	byte[] bitfieldPayload;
	*/
	
	//Default Message Constructor
	public Message()
	{
		this.messageLength = 0;
		this.messageType = (byte) 0xff;
		this.payload = null;
	}
	
	//Message without payload (types 0-3)
	public Message(byte messageType)
	{
		this.messageLength = 1;
		this.messageType = messageType;
		this.payload = null;
	}
	
	//Message with payload (types 4-7)
	public Message(byte messageType, byte[] payload)
	{
		this.messageLength = 1 + payload.length;
		this.messageType = messageType;
		this.payload = payload;
	}
	
	//Constructor for message given a byte array in the message format:
	//(4bytes) length + (1byte) type + (nbytes) payload
	public Message(byte[] message)
	{
		//valid message and payload
		if(message != null && message.length > 5)
		{
			byte[] mlen = new byte[] { message[0], message[1], message[2], message[3] };
			byte[] tempPayload = new byte[message.length - 5];
			System.arraycopy(message, 5, tempPayload, 0, tempPayload.length);
			
			this.messageLength = byteArrayToInt(mlen);
			this.messageType = message[4];
			this.payload = tempPayload;
		}
		//valid message and no payload
		if (message != null && message.length == 5)
		{
			byte[] mlen = new byte[] { message[0], message[1], message[2], message[3] };
			
			this.messageLength = byteArrayToInt(mlen);
			this.messageType = message[4];
			this.payload = null;
		}
		//not a valid message
		else
		{
			this.messageLength = 0;
			this.messageType = (byte) 0xff;
			this.payload = null;
		}
	}
	
	/*
	//Message without payload (types 0-3)
	public Message(byte messageType)
	{
		this.messageLength = 1;
		this.messageType = messageType;
		this.payload = null;
	}
	
	//Message with index payload (types 4,6)
	public Message(byte messageType, int index)
	{
		this.messageLength = 5;
		this.messageType = messageType;
		this.indexPayload = index;
		
		piecePayload = null;
		bitfieldPayload = null;
	}
	
	//Message with bitfield payload (type 5)
	public Message(byte[] bitfieldPayload)
	{
		this.messageLength = 1 + bitfieldPayload.length;
		this.messageType = messageType;
		this.bitfieldPayload = bitfieldPayload;
		
		indexPayload = 0;
		piecePayload = null;
	}
	
	//Message containing index and "piece" payload (type 7)
	public Message(int indexPayload, byte[] piecePayload)
	{	
		//messageLength = 1 (type) + 4 (int) + piecePayloadLength
		this.messageLength = 5 + piecePayload.length;
		this.messageType = 7;
		this.piecePayload = piecePayload;
		this.indexPayload = indexPayload;
		
		bitfieldPayload = null;
	}
	*/
	
	// get/set methods
	public int getLength()
	{
		return messageLength;
	}
	
	public byte getType()
	{
		return messageType;
	}
	
	public byte[] getPayload()
	{
		return payload;
	}
	
	public void setLength(int messageLength)
	{
		this.messageLength = messageLength;
	}
	
	public void setType(byte messageType)
	{
		this.messageType = messageType;
	}
	
	public void setPayload(byte[] payload)
	{
		this.payload = payload;
	}
	
	public String toString()
	{
		return "Length: " + messageLength + " Type: " + messageType;
	}
	
	public byte[] toByteArray()
	{
		byte[] result;
		byte[] messageLengthByteArray = intToByteArray(messageLength);
		
		if (payload != null)
		{
			result = new byte[5 + payload.length];
			System.arraycopy(messageLengthByteArray, 0, result, 0, messageLengthByteArray.length);
			result[4] = messageType;
			System.arraycopy(payload, 0, result, 5, payload.length);
		}
		else
		{
			result = new byte[5];
			System.arraycopy(messageLengthByteArray, 0, result, 0, messageLengthByteArray.length);
			result[4] = messageType;
		}
		
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
