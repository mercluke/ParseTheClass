import java.io.*;

/**
 * Parses and stores an entry from the constant pool table (in a Java .class
 * file).
 *
 * @author David Cooper
 */
public abstract class CPEntry
{
    /**
     * Parses a constant pool entry from a DataInputStream, returning an
     * instance of the appropriate class. Any references to other entries
     * remain unresolved until the resolveReferences() method is called.
     */
    public static CPEntry parse(final DataInputStream dis) throws IOException,
                                                     InvalidTagException
    {
        final byte tag = dis.readByte();
        CPEntry entry;

        switch(tag)
        {
            case  7: entry = new ConstantClass(dis);                break;
            case  9: entry = new ConstantFieldRef(dis);             break;
            case 10: entry = new ConstantMethodRef(dis);            break;
            case 11: entry = new ConstantInterfaceMethodRef(dis);   break;
            case  8: entry = new ConstantString(dis);               break;
            case  3: entry = new ConstantInteger(dis);              break;
            case  4: entry = new ConstantFloat(dis);                break;
            case  5: entry = new ConstantLong(dis);                 break;
            case  6: entry = new ConstantDouble(dis);               break;
            case 12: entry = new ConstantNameAndType(dis);          break;
            case  1: entry = new ConstantUtf8(dis);                 break;
            case 15: entry = new ConstantMethodHandle(dis);         break;
            case 16: entry = new ConstantMethodType(dis);           break;
            case 18: entry = new ConstantInvokeDynamic(dis);        break;

            default:
                throw new InvalidTagException(
                    String.format("Invalid tag: 0x%02x", tag));

        }
        return entry;
    }

    /**
     * Resolves references between constant pool entries, once the entire
     * constant pool has been parsed.
     */
    public void resolveReferences(final ConstantPool cp) 
    throws InvalidConstantPoolIndex {}

    /** Returns a string indicating the type of entry. */
    public abstract String getTagString();

    /**
     * Returns a string containing the raw (unresolved) contents of the entry.
     */
    public abstract String getValues();

    /*Same as getValues() but with a given indent level*/
    public String toString(final int tabs)
    {
        return Grosstab.str(tabs) + getValues(); 
    }

    /**
     * Returns the number of entries "taken up" by this entry. This caters for
     * a quirk in the class file format, whereby a Long or Double entry counts
     * as two entries.
     */
    public int getEntryCount() { return 1; }
}


/** Represents a CONSTANT_Utf8 entry (tag == 1). */
class ConstantUtf8 extends CPEntry
{
    final private String bytes;

    public ConstantUtf8(final DataInputStream dis) throws IOException
    {
        final int length = dis.readUnsignedShort();
        final byte[] b = new byte[length];
        dis.readFully(b);
        this.bytes = new String(b);
    }

    public String getBytes()     { return bytes; }
    public String getTagString() { return "Utf8"; }
    public String getValues()
    {
        return bytes.replace("\n","\\n");
    }
}


/** Represents a CONSTANT_Integer entry (tag == 3). */
class ConstantInteger extends CPEntry
{
    final private int value;

    public ConstantInteger(final DataInputStream dis) throws IOException
    {
        this.value = dis.readInt();
    }

    public int getIntValue()     { return value; }
    public String getTagString() { return "Integer"; }
    public String getValues()    { return String.format("%d", value); }
}


/** Represents a CONSTANT_Float entry (tag == 4). */
class ConstantFloat extends CPEntry
{
    private final  float value;

    public ConstantFloat(final DataInputStream dis) throws IOException
    {
        this.value = dis.readFloat();
    }

    public float getFloatValue() { return value; }
    public String getTagString() { return "Float"; }
    public String getValues()    { return String.format("%f", value); }
}


/**
 * Represents a CONSTANT_Long entry (tag == 5). This class overrides
 * getEntryCount() to indicate that a Long entry counts for two entries in the
 * constant pool.
 */
class ConstantLong extends CPEntry
{
    private final long value;

    public ConstantLong(final DataInputStream dis) throws IOException
    {
        this.value = dis.readLong();
    }

    public long getLongValue()   { return value; }
    public String getTagString() { return "Long"; }
    public int getEntryCount()   { return 2; }

    public String getValues() { return String.format("%d", value); }
}


/**
 * Represents a CONSTANT_Double entry (tag == 6). This class overrides
 * getEntryCount() to indicate that a Double entry counts for two entries in
 * the constant pool.
 */
class ConstantDouble extends CPEntry
{
    private final double value;

    public ConstantDouble(final DataInputStream dis) throws IOException
    {
        this.value = dis.readDouble();
    }

    public double getDoubleValue() { return value; }
    public String getTagString()   { return "Double"; }
    public int getEntryCount()     { return 2; }

    public String getValues() { return String.format("%f", value); }
}

/**
 * Represents a CONSTANT_Class entry (tag == 7). This holds a reference to a 
 * Utf8 entry containing a class name (just a raw index until 
 * resolveReferences() is called).
 */
class ConstantClass extends CPEntry
{
    private final int nameIndex;
    private ConstantUtf8 nameEntry = null;

    public ConstantClass(final DataInputStream dis) throws IOException
    {
        this.nameIndex = dis.readUnsignedShort();
    }

    public void resolveReferences(final ConstantPool cp)
        throws InvalidConstantPoolIndex
    {
        this.nameEntry = (ConstantUtf8)cp.getEntry(nameIndex);
    }

    public int getNameIndex()    { return nameIndex; }
    public String getName()      { return nameEntry.getBytes(); }

    public String getTagString() { return "Class"; }
    public String getValues()
    {
        return String.format("name_index=0x%02x", nameIndex);
    }

    public String toString(final int tabs){
        final StringBuffer s = new StringBuffer(Grosstab.str(tabs));
        s.append((nameEntry == null) ? getValues() : getName());
        return s.toString();
    }
}


/**
 * Represents a CONSTANT_String entry (tag == 8). This holds a reference to a
 * Utf8 entry containing the string itself (just a raw index until
 * resolveReferences() is called).
 */
class ConstantString extends CPEntry
{
    private final int stringIndex;
    private ConstantUtf8 stringEntry = null;

    public ConstantString(final DataInputStream dis) throws IOException
    {
        this.stringIndex = dis.readUnsignedShort();
    }

    public void resolveReferences(final ConstantPool cp)
        throws InvalidConstantPoolIndex
    {
        this.stringEntry = (ConstantUtf8)cp.getEntry(stringIndex);
    }

    public int getStringIndex()  { return stringIndex; }
    public String getString()    { return stringEntry.getBytes(); }

    public String getTagString() { return "String"; }
    public String getValues()
    {
        return String.format("string_index=0x%02x", stringIndex);
    }

    public String toString(final int tabs){
        final StringBuffer s = new StringBuffer(Grosstab.str(tabs)); 
        s.append((stringEntry == null) ? getValues() : getString());  
        return s.toString();      
    }
}


/**
 * Abstract superclass for the three CONSTANT_xxxref entry types. These
 * contain references to a ConstantClass entry and to a ConstantNameAndType
 * entry (both just raw indexes until resolveReferences() is called).
 */
abstract class ConstantRef extends CPEntry
{
    private final int classIndex;
    private final int nameAndTypeIndex;

    private ConstantClass classEntry = null;
    private ConstantNameAndType nameAndTypeEntry = null;

    public ConstantRef(final DataInputStream dis) throws IOException
    {
        this.classIndex = dis.readUnsignedShort();
        this.nameAndTypeIndex = dis.readUnsignedShort();
    }

    public void resolveReferences(final ConstantPool cp)
        throws InvalidConstantPoolIndex
    {
        this.classEntry = (ConstantClass)cp.getEntry(classIndex);
        this.nameAndTypeEntry =
            (ConstantNameAndType)cp.getEntry(nameAndTypeIndex);
    }

    public int getClassIndex()          { return classIndex; }
    public int getNameAndTypeIndex()    { return nameAndTypeIndex; }

    public String getClassName()        { return classEntry.getName(); }
    public String getName()             { return nameAndTypeEntry.getName(); }
    public String getType()             { return nameAndTypeEntry.getType(); }

    public String getValues()
    {
        return String.format("class_index=0x%02x, name_and_type_index=0x%02x",
            classIndex, nameAndTypeIndex);
    }

    /*if name resolved, print it.  else print getValues()*/
    public String toString(final int tabs){
        final StringBuffer s = new StringBuffer(Grosstab.str(tabs)); 
        s.append((classEntry == null || nameAndTypeEntry == null) ? getValues() : 
        ("Class: " + getClassName()));
        s.append((nameAndTypeEntry == null) ? "" : nameAndTypeEntry.toString(tabs));
        return s.toString();
    }
}

/** Represents a CONSTANT_Fieldref entry (tag == 9). */
class ConstantFieldRef extends ConstantRef
{
    public ConstantFieldRef(final DataInputStream dis) throws IOException
    {
        super(dis);
    }

    public String getTagString() { return "Fieldref"; }
}

/** Represents a CONSTANT_Methodref entry (tag == 10). */
class ConstantMethodRef extends ConstantRef
{
    public ConstantMethodRef(final DataInputStream dis) throws IOException
    {
        super(dis);
    }

    public String getTagString() { return "Methodref"; }
}

/** Represents a CONSTANT_InterfaceMethodref entry (tag == 11). */
class ConstantInterfaceMethodRef extends ConstantRef
{
    public ConstantInterfaceMethodRef(final DataInputStream dis) throws IOException
    {
        super(dis);
    }

    public String getTagString() { return "InterfaceMethodref"; }
}

/**
 * Represents a CONSTANT_NameAndType entry (tag == 12). This holds references 
 * to two Utf8 entries containing the "name" and "type" (or descriptor) of a
 * field or method (both just raw indexes until resolveReferences() is
 * called).
 */
class ConstantNameAndType extends CPEntry
{
    private final int nameIndex;
    private final int typeIndex;

    private ConstantUtf8 nameEntry = null;
    private ConstantUtf8 typeEntry = null;

    public ConstantNameAndType(final DataInputStream dis) throws IOException
    {
        this.nameIndex = dis.readUnsignedShort();
        this.typeIndex = dis.readUnsignedShort();
    }

    public void resolveReferences(final ConstantPool cp)
        throws InvalidConstantPoolIndex
    {
        this.nameEntry = (ConstantUtf8)cp.getEntry(nameIndex);
        this.typeEntry = (ConstantUtf8)cp.getEntry(typeIndex);
    }

    public int getNameIndex()    { return nameIndex; }
    public int getTypeIndex()    { return typeIndex; }

    public String getName()      { return nameEntry.getBytes(); }
    public String getType()      { return typeEntry.getBytes(); }

    public String getTagString() { return "NameAndType"; }
    public String getValues()
    {
        return String.format("name_index=0x%02x, type_index=0x%02x",
            nameIndex, typeIndex);
    }

    public String toString(final int tabs){
        final StringBuffer s = new StringBuffer(Grosstab.str(tabs)); 
        s.append((nameEntry == null) ? getValues() : " Name: ");
        s.append(getName());
        s.append(" Type: ");
        s.append(getType());
        return s.toString();
    }
}


/**
 * Represents a CONSTANT_MethodHandle entry (tag == 15).
 */
class ConstantMethodHandle extends CPEntry
{
    private final byte kind;
    private final int index;
    private CPEntry entry = null;
    
    public ConstantMethodHandle(final DataInputStream dis) throws IOException
    {
        this.kind = dis.readByte();
        this.index = dis.readUnsignedShort();
    }
    
    public void resolveReferences(final ConstantPool cp)
        throws InvalidConstantPoolIndex
    {
        this.entry = cp.getEntry(index);
    }
    
    public byte getKind()     { return kind; }
    public int getIndex()     { return index; }
    public CPEntry getEntry() { return entry; }
    
    public String getTagString() { return "MethodHandle"; }
    public String getValues()
    {
        return String.format("reference_kind=%d, reference_index=0x%02x", 
                             kind, index);
    }

    public String toString(final int tabs){
        final StringBuffer s = new StringBuffer(Grosstab.str(tabs)); 
        s.append((entry == null) ? getValues() : entry.toString(tabs));
        return s.toString();
    }
}


/**
 * Represents a CONSTANT_MethodType entry (tag == 16).
 */
class ConstantMethodType extends CPEntry
{
    private final int index;
    private ConstantUtf8 entry = null;
    
    public ConstantMethodType(final DataInputStream dis) throws IOException
    {
        this.index = dis.readUnsignedShort();
    }
    
    public void resolveReferences(final ConstantPool cp)
        throws InvalidConstantPoolIndex
    {
        this.entry = (ConstantUtf8)cp.getEntry(index);
    }
    
    public int getIndex()   { return index; }
    public String getType() { return entry.getBytes(); }
    
    public String getTagString() { return "MethodType"; }
    public String getValues()
    {
        return String.format("descriptor_index=0x%02x", index);
    }

    public String toString(final int tabs){
        final StringBuffer s = new StringBuffer(Grosstab.str(tabs)); 
        s.append((entry == null) ? getValues() : getType());
        return s.toString();
    }
}


/**
 * Represents a CONSTANT_InvokeDynamic entry (tag == 18). Such entries are used
 * in conjunction with the invokedynamic instruction to determine which method 
 * should actually be invoked.
 */
class ConstantInvokeDynamic extends CPEntry
{
    private final int bootstrapMethodIndex;
    private final int nameAndTypeIndex;
    
    private ConstantNameAndType nameAndTypeEntry = null;

    public ConstantInvokeDynamic(final DataInputStream dis) throws IOException
    {
        this.bootstrapMethodIndex = dis.readUnsignedShort();
        this.nameAndTypeIndex = dis.readUnsignedShort();
    }

    public void resolveReferences(final ConstantPool cp)
        throws InvalidConstantPoolIndex
    {
        this.nameAndTypeEntry =
            (ConstantNameAndType)cp.getEntry(nameAndTypeIndex);
    }

    public int getBootstrapMethodIndex() { return bootstrapMethodIndex; }
    public int getNameAndTypeIndex()     { return nameAndTypeIndex; }
    public String getName()              { return nameAndTypeEntry.getName(); }
    public String getType()              { return nameAndTypeEntry.getType(); }

    public String getTagString() { return "InvokeDynamic"; }
    public String getValues()
    {
        return String.format(
            "bootstrap_method_attr_index=0x%02x, name_and_type_index=0x%02x",
            bootstrapMethodIndex, nameAndTypeIndex);
    }

    public String toString(final int tabs){
        return Grosstab.str(tabs) + String.format(
            "Bootstrap method: %s, Name: %s, Type: %s",
            bootstrapMethodIndex, getName(), getType());
    }
}


/**
 * Thrown when an unknown tag value is encountered (i.e. one that does not
 * indicate a known constant pool entry type.)
 */
class InvalidTagException extends ClassFileParserException
{
    public InvalidTagException(final String msg) { super(msg); }
}
