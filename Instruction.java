import java.util.*;

/**
 * Represents a single JVM instruction, consisting of an opcode plus zero or
 * more extra bytes.
 *
 * (Requires Java 1.6.)
 *
 * @author David Cooper
 */
public class Instruction
{
    private final int offset;
    private final Opcode opcode;
    private final byte[] extraBytes;
    private final String[] byteLabels;

    /**
     * Constructs an Instruction object, retrieving the opcode and any extra
     * bytes associated with it from a byte array at a given offset.
     */
    public Instruction(final byte[] code, final int offset) throws CodeParsingException
    {
        this.offset = offset;
        opcode = Opcode.getOpcode(code[offset]);
        if(opcode == null)
        {
            throw new CodeParsingException(
                String.format("Invalid opcode: 0x%02x", code[offset]));
        }

        byteLabels = opcode.getByteLabels(code, offset);
        extraBytes = Arrays.copyOfRange(
            code, offset + 1, offset + opcode.getSize(code, offset));
    }

    /**
     * Returns the offset of this instruction within the original code array.
     */
    public int getOffset()
    {
        return offset;
    }

    /**
     * Returns the opcode, from which the opcode mnemonic can be retrieved
     * (e.g. instruction.getOpcode().getMnemonic()).
     */
    public Opcode getOpcode()
    {
        return opcode;
    }

    /** Returns the number of bytes occupied by this instruction. */
    public int getSize()
    {
        return 1 + extraBytes.length;
    }

    /**
     * Returns a array containing the bytes making up this instruction, minus
     * the opcode itself.
     */
    public byte[] getExtraBytes()
    {
        return Arrays.copyOf(extraBytes, extraBytes.length);
    }

    /** Returns a formatted String representation of this instruction. */
    public String toString(final int tabs)
    {
        final StringBuffer s = new StringBuffer(Grosstab.str(tabs));
        s.append(opcode.getMnemonic());
        
        final String operandString = getOperandString();
        if(operandString.length() > 0)
        {
            s.append(':');
            s.append(operandString);
        }

        return s.toString();
    }

    /**
     * Returns a formatted String representation of the extra bytes of this
     * instruction (i.e. without the opcode/mnemonic).
     */
    public String getOperandString()
    {
        final StringBuilder sb = new StringBuilder();
        for(int i = 0; i < extraBytes.length; i++)
        {
            if(i < byteLabels.length)
            {
                sb.append(byteLabels[i]).append("=");
            }
            sb.append(String.format("%02x ", extraBytes[i]));
        }
        return sb.toString();
    }
}


/** Thrown when the code array appears to be invalid. */
class CodeParsingException extends ClassFileParserException
{
    public CodeParsingException(final String msg)
    {
        super(msg);
    }

    public CodeParsingException(final String msg, final Throwable cause)
    {
        super(msg, cause);
    }
}
