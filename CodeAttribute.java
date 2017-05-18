import java.io.*;
import java.util.*;

/**
 * Represents a Code Attribute of a Method
 *
 * @author Luke Mercuri
 */
//Due to the scope of the assignment there are parts
//Of the class file format that I don't actually use
//This suppresses warnings about these unused variables
@SuppressWarnings("PMD.UnusedPrivateField")
class CodeAttribute extends AttributeInfo
{
    private final int length;
    private final int maxStack;
    private final int maxLocals;
    private final int codeLength;
    private final byte code[];
    private final List<Instruction> instructions = new ArrayList<Instruction>();
    private final ExceptionHandler exceptionHandlers[];
    private final AttributeInfo attributes[];

    public CodeAttribute(final DataInputStream dis, final ConstantPool cp) throws IOException,
                                                            InvalidConstantPoolIndex,
                                                            CodeParsingException
    {
        /*Read size information about the code attribute*/
        length = dis.readUnsignedShort() << 16 | dis.readUnsignedShort();
        maxStack = dis.readUnsignedShort();
        maxLocals = dis.readUnsignedShort();
        /*Actual size of jvm bytecode*/
        codeLength = dis.readUnsignedShort() << 16 | dis.readUnsignedShort();
        code = new byte[codeLength];

        dis.readFully(code);

        /*loop through code and find all instructions*/
        for(int i = 0; i < code.length;){
            final Instruction instruction = new Instruction(code, i);
            /*size of instruction will vary with instruction type*/
            i += instruction.getSize();
            instructions.add(instruction);
        }

        /*Information about where exceptions can be thrown/caught within code*/
        exceptionHandlers = new ExceptionHandler[dis.readUnsignedShort()];
        for(int i = 0; i < exceptionHandlers.length; i++){
            exceptionHandlers[i] = new ExceptionHandler(dis, cp);
         }

         /*Code attribute can have nested attributes*/
        attributes = new AttributeInfo[dis.readUnsignedShort()];
        for(int i = 0; i < attributes.length; i++){
            attributes[i] = AttributeInfo.parse(dis, cp);
         }
    }

    public String getValues(){ 
        return new String(code);
    }

    /*Find all method calls present within this code attribute*/
    public String[] getMethodCalls(final ClassFile cf) throws InvalidConstantPoolIndex{

        final List<ConstantRef> methodRefs = new ArrayList<ConstantRef>();

        /*the different types of opcodes
        that represent the method calls*/
        final Opcode callTypes[] = 
        new Opcode[] {Opcode.INVOKEVIRTUAL,
                Opcode.INVOKESPECIAL,
                Opcode.INVOKESTATIC,
                Opcode.INVOKEINTERFACE};

        /*Loop through all instructions for calls*/
        for(Instruction instruction: instructions){
            if(Arrays.asList(callTypes).contains(instruction.getOpcode())){
                final int index = instruction.getExtraBytes()[1];
                /*add methodRef to list of methods called*/
                methodRefs.add(cf.getMethodRefFromCPIndex(index));
            }
        }

        /*Apparently there is a more elegant 
        List.toArray() method but I get exceptions 
        being thrown all about the place when i use it*/
        String[] methodArray = new String[methodRefs.size()];

        for(int i = 0; i < methodRefs.size(); i++){
            methodArray[i] = cf.getFullMethodNameFromRef(methodRefs.get(i));
        }

        return methodArray;
    }

    /*String representation of code attribute*/
    public String toString(final int tabs){
        final StringBuffer s = new StringBuffer("\n");
        s.append(Grosstab.str(tabs));
        s.append("Code:\n");
                
        for(int i = 0; i < instructions.size(); i++)
        {
            s.append(instructions.get(i).toString(tabs+1));
            s.append('\n');
        }
       
        return s.toString();
    }

    public String getNameString(){ return "Code"; }

    public int getLength() { return codeLength; }
}
