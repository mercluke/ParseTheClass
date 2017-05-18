import java.io.*;

//Due to the scope of the assignment there are parts
//Of the class file format that I don't actually use
//This suppresses warnings about these unused variables
@SuppressWarnings("PMD.UnusedPrivateField")
public class ExceptionHandler {

	private final int startPC;
	private final int endPC;
	private final int handlerPC;
	private final CPEntry catchType;

	public ExceptionHandler(final DataInputStream dis, final ConstantPool cp) throws IOException,
															InvalidConstantPoolIndex
	{
		startPC = dis.readUnsignedShort();
		endPC = dis.readUnsignedShort();
		handlerPC = dis.readUnsignedShort();
		catchType = cp.getEntry(dis.readUnsignedShort());
	}
}
