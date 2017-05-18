import java.io.*;
import java.util.Set;
import java.util.HashSet;


/**
 * Parses and stores the Access Flags of a class, method or field
 *
 * @author Luke Mercuri
 */
public class AccessFlags{

    /*It's literally impossible to have
    duplicated access flags but w/e
    I used a Set anyway because it was 
    convenient for checkAccess()*/
	private final Set<Flag> flags;
	
	public AccessFlags(int accessFlags) throws InvalidFlagException,
                                                    IOException
    {
    	flags = new HashSet<Flag>();

    	/*perform logical AND on all possible 
        flags to see which flags are set*/
    	for(Flag flag : Flag.values()) {
    		if ((flag.val() & accessFlags) != 0){
    			/*add flag to arraylist and XOR so we 
                can ensure no unknown flags exist*/
    			flags.add(flag);
    			accessFlags ^= flag.val();
    		}
    	}

    	/*
    	*Unknown flags present 
    	*this code should literally never be hit
    	*/
    	if(accessFlags != 0x0000){
    		throw new InvalidFlagException(
    			String.format("Invalid Access Flag: 0x%04X", accessFlags));
    	}
    }

    /*Quick way to check if a class has a certain access*/
    public boolean checkAccess(final Flag flag){
    	return flags.contains(flag);
    }


    /*String representation of set flags*/
    public String toString(final int tabs)
    {
		final StringBuffer s = new StringBuffer();

        for(Flag flag : flags)
        {
        	/*pmd tells me this is more efficient
			than java concatenating strings
            So I may as well do it*/
            s.append(Grosstab.str(tabs));
            s.append(flag.name());
            s.append('\n');
        }
        return s.toString();
	}
}

/*This can literally never be hit because all 32 bits are covered by the various flag types*/
class InvalidFlagException extends ClassFileParserException
{
    public InvalidFlagException(final String msg) { super(msg); }
}
