
public class BitfieldMessage extends Message
{
	public BitfieldMessage(byte[] bitfield)
	{
		super((byte) 5, bitfield);
	}
}
