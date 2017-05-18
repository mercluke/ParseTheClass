import java.io.*;

/**
 * Parses and stores an entry from the field pool table (in a Java .class
 * file).
 *
 * @author Luke Mercuri
 */

//Due to the scope of the assignment there are parts
//Of the class file format that I don't actually use
//This suppresses warnings about these unused variables
@SuppressWarnings("PMD.UnusedPrivateField")


/*This project does not actually make any use
whatsoever of the fields but I started implimenting 
them anyway before I realised how little spare time I had
*/
public class FieldInfo
{

    private final AccessFlags accessFlags;
    private final String name;
    private final String descriptor;
    private final AttributeInfo attributes[];

    public FieldInfo(final DataInputStream dis, final ConstantPool cp) throws IOException,
                                                     InvalidFlagException,
                                                     InvalidConstantPoolIndex,
                                                     CodeParsingException
    {
        accessFlags = new AccessFlags(dis.readUnsignedShort());
        name = ((ConstantUtf8)cp.getEntry(dis.readUnsignedShort())).getBytes();
        descriptor = ((ConstantUtf8)cp.getEntry(dis.readUnsignedShort())).getBytes();
        attributes = new AttributeInfo[dis.readUnsignedShort()];

        for(int i = 0; i < attributes.length; i++){
            attributes[i] = AttributeInfo.parse(dis, cp);
        }

    }

    public String getName(){ return name; }
    public String getDescriptor(){ return descriptor; }
    public AttributeInfo getAttribute(final int index){ return attributes[index]; }

    public String toString(final int tabs)
    {
        final StringBuffer s = new StringBuffer(Grosstab.str(tabs));
        s.append("Name: ");
        s.append(name);
        s.append('\n');
        s.append(Grosstab.str(tabs));
        s.append("Descriptor: ");
        s.append(descriptor);
        s.append('\n');
        if(attributes.length > 0)
        {
            s.append(Grosstab.str(tabs));
            s.append("Attributes:\n");
            for(int i = 0; i < attributes.length; i++)
            {
                s.append(attributes[i].toString(tabs+1));
                s.append('\n');
            }
            s.append('\n');
        }

        return s.toString();
    }

}
