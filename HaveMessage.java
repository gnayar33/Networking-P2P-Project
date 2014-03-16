
public class HaveMessage extends Message
{
	public HaveMessage(int index)
	{
		super((byte) 4, intToByteArray(index));
	}
}
