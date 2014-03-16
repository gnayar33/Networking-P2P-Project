
public class RequestMessage extends Message
{
	public RequestMessage(int index)
	{
		super((byte) 6, intToByteArray(index));
	}
}
