/**
 * Enum for storing the different types of Flags
 * There are "Collisions" where some bits mean a 
 * property depending on the context of class, method
 * or field.  used by AccessFlags class to check access
 *
 * @author Luke Mercuri
 */
enum Flag
{
    ACC_PUBLIC(0x0001),
    ACC_PRIVATE(0x0002),
    ACC_PROTECTED(0x0004),
    ACC_STATIC(0x0008),
    ACC_FINAL(0x0010),
    ACC_SUPER_OR_SYNCHRONISED(0x0020),
    //ACC_SYNCHRONISED(0x0020),
    ACC_VOLATILE_OR_BRIDGE(0x0040),
    //ACC_BRIDGE(0x0040),
    ACC_TRANSIENT_OR_VARARGS(0x0080),
    //ACC_VARARGS(0x0080),
    ACC_NATIVE(0x0100),
    ACC_INTERFACE(0x0200),
    ACC_ABSTRACT(0x0400),
    ACC_STRICT(0x0800),
    ACC_SYNTHETIC(0x1000),
    ACC_ANNOTATION(0x2000),
    ACC_ENUM(0x4000);

    private final int value;

    private Flag(final int v){
    	value = v;
    }

    public int val(){ return value; }
}
