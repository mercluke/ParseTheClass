import java.io.*;

/**
 * Stores an Attribute of a [Class|Field|Method]
 * Due to time constraints and the fact the assignment doesn't 
 * require support, This only supports two attribute types
 *
 * @author Luke Mercuri
 */
public abstract class AttributeInfo
{

    /*Given a DataInputStream pointing the the start of an 
    attribute_info struct and a reference to the containing
    class's Constant Pool, instantiate the relevant Attribute type
    */
    public static AttributeInfo parse(final DataInputStream dis, final ConstantPool cp) throws IOException,
                                                     InvalidConstantPoolIndex,
                                                     CodeParsingException
    {

        final String name = ((ConstantUtf8)cp.getEntry(dis.readUnsignedShort())).getBytes();
        AttributeInfo attribute;

        //This should be a case but apparently the labs still run java 6...
        if("Code".equals(name)){
            attribute = new CodeAttribute(dis, cp);
        }
        else if("SourceFile".equals(name)){
            attribute = new SourceFileAttribute(dis, cp);
        }
        else{
            attribute = new OtherAttribute(dis, name);
        }

        return attribute;
    }

    /** Returns a string indicating the type of entry. */
    public abstract String getNameString();

    public abstract String getValues();

    /*same as getValues but with a specifiable indent level*/
    public String toString(final int tabs)
    {
        return Grosstab.str(tabs) + getValues(); 
    }

    public abstract int getLength();
}

/*Only relevant to Classes, attribute specifies 
the java file this class was compiled from*/
class SourceFileAttribute extends AttributeInfo
{
    private final String sourcefileName;
    private final int length;

    public SourceFileAttribute(final DataInputStream dis, final ConstantPool cp) throws IOException,
                                                                InvalidConstantPoolIndex
    {
        /*Lookup filename in constant pool*/
        length = dis.readUnsignedShort() << 16 | dis.readUnsignedShort();
        sourcefileName = ((ConstantUtf8)cp.getEntry(dis.readUnsignedShort())).getBytes();
    }  

    public String getValues() { return sourcefileName; }
    public String getNameString(){ return "SourceFile"; }  
    public int getLength() { return length; }

}


//Attributes not implimented by this project
class OtherAttribute extends AttributeInfo
{
    private final String bytes;
    private final String name;
    private final int length;

    /*figure out how big attribute is and store in a byte array*/
    public OtherAttribute(final DataInputStream dis, final String name) throws IOException
    {
        length = dis.readUnsignedShort() << 16 | dis.readUnsignedShort();
        final byte b[] = new byte[length];
        dis.readFully(b);
        this.bytes = new String(b);
        this.name = name;
    }

    public String getValues()
    {
        return bytes.replace("\n","\\n");
    }

    public String getNameString(){ return name; }

    public int getLength() { return length; }
}
