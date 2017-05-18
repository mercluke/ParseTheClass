import java.io.*;

/**
 * Parses and stores the constant pool from a Java .class file.
 *
 * @author David Cooper
 */
public class ConstantPool
{
    private final CPEntry[] entries;

    /**
     * Parses the constant pool, including the length, constructing a
     * ConstantPool object in the process.
     */
    public ConstantPool(final DataInputStream dis) throws InvalidTagException,
                                                    InvalidConstantPoolIndex,
                                                    IOException
    {
        final int len = dis.readUnsignedShort();
        entries = new CPEntry[len];
        int i;

        // Initialise entries to null.
        for(i = 0; i < len; i++)
        {
            entries[i] = null;
        }

        i = 1;
        while(i < len)
        {
            entries[i] = CPEntry.parse(dis);

            // We can't just have i++, because certain entries (Long and
            // Double) count for two entries.
            i += entries[i].getEntryCount();
        }

        // Once the constant pool has been parsed, resolve the various
        // internal references.
        for(i = 0; i < len; i++)
        {
            if(entries[i] != null)
            {
                entries[i].resolveReferences(this);
            }
        }
    }

    /** Retrieves a given constant pool entry. */
    public CPEntry getEntry(final int index) throws InvalidConstantPoolIndex
    {
        if(index < 0 || index > entries.length)
        {
            throw new InvalidConstantPoolIndex(String.format(
                "Invalid constant pool index: %d (not in range [0, %d])",
                index, entries.length));
        }
        else if(entries[index] == null)
        {
            throw new InvalidConstantPoolIndex(String.format(
                "Invalid constant pool index: %d (entry undefined)\n", index));
        }
        return entries[index];
    }

    /** Returns a formatted String representation of the constant pool. */
    public String toString(final int tabs)
    {
        final StringBuffer s = new StringBuffer(Grosstab.str(tabs));
        s.append("Index  Entry type          Entry values\n");
        s.append(Grosstab.str(tabs));
        s.append("---------------------------------------\n");
        for(int i = 1; i < entries.length; i++)
        {
            if(entries[i] != null)
            {
                s.append(Grosstab.str(tabs));
                s.append(String.format("0x%02X   %-18s  %s\n",
                    i, entries[i].getTagString(), entries[i].toString(0)));
            }
        }
        return s.toString();
    }
}

/**
 * Thrown when an invalid index into the constant pool is given. That is,
 * index is zero (or negative), greater than the index of the last entry, or
 * represents the (unused) entry following a Long or Double.
 */
class InvalidConstantPoolIndex extends ClassFileParserException
{
    public InvalidConstantPoolIndex(final String msg) { super(msg); }
}
