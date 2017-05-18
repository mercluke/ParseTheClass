import java.io.*;
import java.util.*;

/**
 * Parses and stores a Java .class file.
 *
 * @author Luke Mercuri, David Cooper
 */
public class ClassFile
{
    /*These fields were present in code supplied by Dave Cooper*/
    private final String filename;
    private final long magic;
    private final int minorVersion;
    private final int majorVersion;
    private final ConstantPool constantPool;
    /*End Dave fields*/


    /*These fields were added by Luke Mercuri*/
    private final AccessFlags accessFlags;
    private final ConstantClass thisClass;
    private final ConstantClass superClass;
    private final ConstantClass interfaces[];
    private final FieldInfo fields[];
    private final MethodInfo methods[];
    private final AttributeInfo attributes[];
    private final List<ClassFile> children = new ArrayList<ClassFile>();
    /*End Luke fields*/

    // ...

    /**
     * Parses a class file and constructs a ClassFile object. At present, this
     * only parses the header and constant pool.
     */
    public ClassFile(final String filename) throws ClassFileParserException,
                                             IOException
    {
        /*Dave's existing code*/
        final DataInputStream dis =
            new DataInputStream(new FileInputStream(filename));

        this.filename = filename;
        magic = (long)dis.readUnsignedShort() << 16 | dis.readUnsignedShort();
        minorVersion = dis.readUnsignedShort();
        majorVersion = dis.readUnsignedShort();
        constantPool = new ConstantPool(dis);
        /*End Dave code*/


        /*
        Read in access flags for class
        eg: is it public? is it an interface? etc.
        */
        accessFlags = new AccessFlags(dis.readUnsignedShort());

        /*Name of current class*/
        thisClass = (ConstantClass)constantPool.getEntry(dis.readUnsignedShort());
        
        /*
        Name of class extended by this class
        All classes which do not specify a superclass
        implicitly extend Object
        */
        superClass = (ConstantClass)constantPool.getEntry(dis.readUnsignedShort());
        
        /*
        List of 0..* interfaces implimented by this class
        */
        interfaces = new ConstantClass[dis.readUnsignedShort()];

        //Each interface is a reference to an entry in the Constant Pool
        for(int i = 0; i < interfaces.length; i++)
        {
            interfaces[i] = (ConstantClass)constantPool.getEntry(dis.readUnsignedShort());
        }

        /*Read in number of fields contained by Class*/
        fields = new FieldInfo[dis.readUnsignedShort()];
        for(int i = 0; i < fields.length; i++)
        {
            fields[i] = new FieldInfo(dis, constantPool);
        }
        
        /*Read in number of methods contained by Class*/
        methods = new MethodInfo[dis.readUnsignedShort()];

         for(int i = 0; i < methods.length; i++){
            methods[i] = new MethodInfo(dis, constantPool);
         }
        
        /*Read in attributes of Class (such as filename)*/
        attributes = new AttributeInfo[dis.readUnsignedShort()];

         for(int i = 0; i < attributes.length; i++){
            attributes[i] = AttributeInfo.parse(dis, constantPool);
         }
        
    }

    /*Vanilla Accessors*/
    public String getName(){
        return thisClass.getName();
    }

    public MethodInfo getMethod(final int index){
        return methods[index];
    }

    public int methodCount(){
        return methods.length;
    }

    public ClassFile getChildClass(final int index){
        return children.get(index);
    }

    public int childCount(){
        return children.size();
    }

    /*retreive a given entry from the ConstantPool*/
    public CPEntry getCPEntry(final int index) throws InvalidConstantPoolIndex{
        return constantPool.getEntry(index);
    }

    /*Get a MethodRef form a given entry into the Constant Pool*/
    public ConstantRef getMethodRefFromCPIndex(final int index) throws InvalidConstantPoolIndex{
        return (ConstantRef)getCPEntry(index);

    }

    /*Used for retreiving the name of 
    a method given a Constant Pool index*/
    public String getFullMethodNameFromRef(final ConstantRef ref)throws InvalidConstantPoolIndex{
        final String s = ref.getClassName() + "." + ref.getName();


        /*replace slashes with dots to 
        look more like java source code*/
        return s.replace("/",".");
    }



    /*Given an array of all ClassFiles parsed, figure 
    out those which Extend or Impliment this class*/
    public void resolveChildren(final ClassFile[] classes){
        
        for(ClassFile cf : classes)
        {
            if(cf != this)
            {
                //this is an interface, see what impliments it
                if(accessFlags.checkAccess(Flag.ACC_INTERFACE) && cf.doesImpliment(thisClass))
                {
                    children.add(cf);
                }
                else if(cf.isChildOf(thisClass))
                {
                    children.add(cf);
                }
            }
        }
    }

    /*Given a method, recursively query all children
    to see which classes override the method*/
    public List<ClassFile> findOverridesForMethod(final MethodInfo method){
        final List<ClassFile> overridingChildren = new ArrayList<ClassFile>();
        for(ClassFile child: children){
            if(child.doesOverrideMethod(method)){
                overridingChildren.add(child);
            }
            overridingChildren.addAll(child.findOverridesForMethod(method));
        }

        return overridingChildren;
    }

    /*Inverse of the above method
    Given a method within a parent class,
    check whether this class overrides it*/
    public final boolean doesOverrideMethod(final MethodInfo parentMethod){

        for(MethodInfo method: methods){

            
            String methodDescriptor = method.getDescriptor();
            String parentMethodDescriptor = parentMethod.getDescriptor();

            //Ignore everything after '(' to support different return types
            methodDescriptor = methodDescriptor.substring(
                0, methodDescriptor.indexOf(')'));
            parentMethodDescriptor = parentMethodDescriptor.substring(
                0, parentMethodDescriptor.indexOf(')'));

            //lazy string compare is best compare
            if((method.getCodeSize() != 0) &&
                (methodDescriptor.equals(parentMethodDescriptor)) && 
                (method.getName().equals(parentMethod.getName())))
            {
                return true;
            }
        }

        return false;
    }

    //Check if you impliment a given interface
    public boolean doesImpliment(final ConstantClass iface){

        /*Loop through each item in interfaces and 
        check if one of them is the iface passed in*/
        for(ConstantClass implimentation : interfaces){
            if(iface.getName().equals(implimentation.getName())){
                return true;
            }
        }

        return false;
    }

    //check if you extend a given class
    public boolean isChildOf(final ConstantClass parentClass){
        return parentClass.getName().equals(superClass.getName());
    }

    public boolean isInterface(){
        return accessFlags.checkAccess(Flag.ACC_INTERFACE);
    }








    /** Returns the contents of the class file as a formatted String. 
    Super gross, I used this for debugging but the 
    -[m|c|o] features do not use this at all*/
    public String toString(final int tabs)
    {
        final StringBuffer s = new StringBuffer(String.format(
            Grosstab.str(tabs) + "Filename: %s\n" +
            Grosstab.str(tabs) + "Magic: 0x%08X\n" +
            Grosstab.str(tabs) + "Class file format version: %d.%d\n\n" +
            Grosstab.str(tabs) + "Constant pool:\n\n%s\n\n" +
            "Flags Set: \n%s\n" +
            Grosstab.str(tabs) + "This Class: %s\n" +
            Grosstab.str(tabs) + "Super Class: %s\n",
            filename, magic, majorVersion, minorVersion, 
            constantPool.toString(tabs+1), 
            accessFlags.toString(tabs+1), 
            thisClass.toString(0), 
            superClass.toString(0)));

            if(interfaces.length > 0)
            {
                s.append("\nInterfaces:\n");
                for(int i = 0; i < interfaces.length; i++)
                {
                    s.append(interfaces[i].toString(tabs+1));
                    s.append('\n');
                }
            }
            if(fields.length > 0)
            {
                s.append('\n'); 
                s.append(Grosstab.str(tabs));
                s.append("Fields:\n");
                for(int i = 0; i < fields.length; i++)
                {
                    s.append(fields[i].toString(tabs+1));
                }
            }

            if(methods.length > 0)
            {
                s.append('\n');
                s.append(Grosstab.str(tabs));
                s.append("Methods:\n");
                for(int i = 0; i < methods.length; i++)
                {
                    s.append(methods[i].toString(tabs+1));
                }
            }

            if(attributes.length > 0)
            {
                s.append('\n');
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

