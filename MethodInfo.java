import java.io.*;
import java.util.*;

/**
 * Represents a Method within a Class
 *
 * @author Luke Mercuri
 */
public class MethodInfo
{


    private final AccessFlags accessFlags;
    private final String name;
    private final String descriptor;
    private final AttributeInfo attributes[];

    /*read properties in from DataInputStream*/
    public MethodInfo(final DataInputStream dis, final ConstantPool cp) throws IOException,
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

    //Method is defined to contain either one or zero Code attributes
    public CodeAttribute getCode(){
        CodeAttribute code  = null;

        for(AttributeInfo attr: attributes){
            if(attr.getNameString().equals("Code")){
                code = (CodeAttribute)attr;
                break;
            }
        }

        return code;
    }


    //Print out an approximation of the 
    //java source code declaration of the method
    public String getFriendlyDescriptor(){
        final String descriptor = getDescriptor();

        final List<String> parameters = new ArrayList<String>();
        int arrayDepth = 0;
        final StringBuffer friendlyDescriptor = new StringBuffer(32);

        //start at 1 to skip initial '('
        for(int i = 1; i < descriptor.length();i++){
            StringBuffer s = new StringBuffer();
            switch(descriptor.charAt(i)){
                case '[':
                    arrayDepth++;
                continue;
                case 'B':
                    s.append("byte");
                break;
                case 'C':
                    s.append("char");
                break;
                case 'D':
                    s.append("double");
                break;
                case 'F':
                    s.append("float");
                break;
                case 'I':
                    s.append("int");
                break;
                case 'J':
                    s.append("long");
                break;
                case 'L':
                    i++;
                    while(i < descriptor.length() && descriptor.charAt(i) != ';'){
                        s.append(descriptor.charAt(i));
                        i++;
                    }
                    //counteract the i++ at end of loop...
                    i--;

                    //strip out package names
                    final int index = s.lastIndexOf("/");
                    if(index != -1){
                        s = new StringBuffer(s.substring(index+1, s.length()));
                    }
                break;
                case 'S':
                    s.append("short");
                break;
                case 'Z':
                    s.append("boolean");
                break;
                case 'V':
                    s.append("void");
                break;
                case ')':
                default:
                    continue;
                }

                //add in however many dimensions array may be
                for(int a = 0; a < arrayDepth; a++){
                    s.append("[]");
                }
                arrayDepth = 0;

                parameters.add(s.toString());
        }


        //Scope:
        if(checkAccess(Flag.ACC_PUBLIC)){
            friendlyDescriptor.append("public ");
        } else if(checkAccess(Flag.ACC_PRIVATE)){
            friendlyDescriptor.append("private ");
        } else if(checkAccess(Flag.ACC_PROTECTED)){
            friendlyDescriptor.append("protected ");
        }

        //abstract
        if(checkAccess(Flag.ACC_ABSTRACT)){
            friendlyDescriptor.append("abstract ");
        }
        //if abstract, cannot be final or static 
        else {
            //static
            if(checkAccess(Flag.ACC_STATIC)){
                friendlyDescriptor.append("static ");
            }
            //final
            if(checkAccess(Flag.ACC_FINAL)){
                friendlyDescriptor.append("final ");
            } 
        }



        //last entry was the return type
        friendlyDescriptor.append(parameters.remove(parameters.size()-1));
        friendlyDescriptor.append(' ');
        friendlyDescriptor.append(getName());
        friendlyDescriptor.append('(');

        //print all but last parameter with trailing comma
        for(int i = 0; i < parameters.size()-1; i++){
            friendlyDescriptor.append(parameters.get(i));
            friendlyDescriptor.append(" _");
            friendlyDescriptor.append(i);
            friendlyDescriptor.append(", ");
        }

        //guard against segfaulting on no parameters
        if(!parameters.isEmpty()){
            friendlyDescriptor.append(parameters.get(parameters.size()-1));
            friendlyDescriptor.append(" _");
            friendlyDescriptor.append((parameters.size()-1));
        }

        friendlyDescriptor.append(')');

        return friendlyDescriptor.toString();

    }

    /*Poll whether this method has a particular access flag set*/
    public boolean checkAccess(final Flag flag){
        return accessFlags.checkAccess(flag);
    }

    public AttributeInfo getAttribute(final int index){ return attributes[index]; }

    public String toString(final int tabs)
    {
        final String s = Grosstab.str(tabs) + "Name: " + name + "\n" +
        Grosstab.str(tabs) + "Descriptor: " + getFriendlyDescriptor() + "\n" +
        Grosstab.str(tabs) + "Code size: " + getCodeSize() + " bytes\n";

        return s;
    }

    /*Returns the size of Code Attribute (if present)*/
    public int getCodeSize(){
        int size = 0;
        for(AttributeInfo attribute: attributes){
            if("Code".equals(attribute.getNameString())){
                size += attribute.getLength();
            }
        }

        return size;
    }

}
