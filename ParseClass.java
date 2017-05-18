import java.io.*;
import java.util.*;

/**
 * Parses multiple Class files and reports 
 * various statistics about them
 *
 * @author Luke Mercuri
 */
 @SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class ParseClass
{

    public static void main(final String[] args)
    {
        /*Check supplied argumants for validity*/
        if(args.length > 1 && 
            args[0].length() == 2 && args[0].charAt(0) == '-')
        {
            ClassFile classes[] = new ClassFile[args.length-1];


            /*Attempt to open and parse each specified classfile*/
            for(int i = 1; i < args.length; i++)
            {
                try
                {
                    String fileName = args[i];


                    //Append ".class" if not given in filename
                    if(!(fileName.contains(".")))
                    {
                        fileName += ".class";
                    }

                    classes[i-1] = new ClassFile(fileName);

                }
                /*Error opening/reading file*/
                catch(IOException e)
                {
                    System.out.printf("Cannot read \"%s\": %s\n",
                        args[i], e.getMessage());
                }
                /*Malformed or invalid class file*/
                catch(ClassFileParserException e)
                {
                    System.out.printf("Class file format error in \"%s\": %s\n",
                        args[i], e.getMessage());
                }

                
            }

            //O(N^{2}), get hype
            for(ClassFile cf: classes){
                cf.resolveChildren(classes);
            }


            /*Choose relevant option*/
            switch (args[0].charAt(1)){
                        case 'm': 
                        case 'M':
                            methods(classes); break;
                        case 'c': 
                        case 'C':
                            calls(classes); break;
                        case 'o':
                        case 'O': 
                            overrides(classes); break;
                        case 'v':
                        case 'V':
                            //ºSeCReT* option
                            verbose(classes); break;
                        default: usage();
            }
        }
        /*Invalid args supplied*/
        else
        {
            usage();
        }
    }



    /*
    For each class specified, list it's methods 
    as well as the size of each method's code attribute
    (if a code attribute is present)
    */
    public static void methods(final ClassFile[] classes){
        int totalNumMethods = 0;
        int totalMethodCodeSize = 0;

        //Loop through classes
        for(ClassFile cf: classes){
            final int numMethods = cf.methodCount();
            int methodCodeSize = 0;

            //Determine whether this is a Class or an Interface
            System.out.printf(cf.isInterface() ? 
                "\nInterface: %s\n" : "\nClass: %s\n", cf.getName());

            //Loop through each method and print out details
            for(int i = 0; i < numMethods; i++){
                final MethodInfo method = cf.getMethod(i);

                System.out.println(method.toString(1));
                methodCodeSize += method.getCodeSize();
            }

            //Average size of all methods' code attributes within this class
            System.out.printf(Grosstab.str(1) + "Class average code section size: %.2f bytes\n\n\n", 
                    methodCodeSize/(double)numMethods);
            totalNumMethods += numMethods;
            totalMethodCodeSize += methodCodeSize;
        }
        //Average size of all code attributes among all parsed classes
        System.out.printf("Overall average code section size: %.2f bytes\n\n\n", 
                    totalMethodCodeSize/(double)totalNumMethods);
    }



    /*
    For each method in each class, list all calls made (with the exception of invokedynamic)
    */
    public static void calls(final ClassFile[] classes){

        int numTotalCalls = 0;
        int numTotalMethods = 0;

        //Iterate over classes
        for(ClassFile cf: classes){
            System.out.printf(cf.isInterface() ? 
                "\nInterface: %s\n" : "\nClass: %s\n", cf.getName());

            int numCalls = 0;

            final int numMethods = cf.methodCount();

            //iterate over methods
            for(int m = 0; m < numMethods; m++){
                final MethodInfo method = cf.getMethod(m);

                System.out.printf("%sMethod: %s\n", Grosstab.str(1) ,method.getFriendlyDescriptor());

                //get the code attribute of the method
                final CodeAttribute code = method.getCode();

                //check that there was a code attribute (can be zero or one)
                if(code != null){
                    try{
                        //retreive and print out all method calls found in this code attribute
                        final String[] methodCalls = code.getMethodCalls(cf);
                        
                        System.out.println(Grosstab.str(2) + "Calls:");

                        //At least one call is made
                        if(methodCalls.length > 0){
                            for(int c = 0; c < methodCalls.length; c++){
                                System.out.println(Grosstab.str(3) + methodCalls[c]);
                            }
                        }
                        //no calls are made (what a useless method)
                        else{
                            System.out.println(Grosstab.str(3) + "<No methods called>");
                        }

                        numCalls+=methodCalls.length;
                    }
                    catch(InvalidConstantPoolIndex e){
                        System.out.println(e.getMessage());
                    }
                }
            }

            //Average number of calls among that class
            System.out.printf("%sAverage number of calls per method for this class: %.2f\n\n", 
                Grosstab.str(1), (double)numCalls/numMethods);
            numTotalCalls += numCalls;
            numTotalMethods += numMethods;
        }
        //Average number of calls of all classes
        System.out.printf("Average number of calls per method for all classes: %.2f\n\n", 
                (double)numTotalCalls/numTotalMethods);
    }


    /*
    Find each method in each class, find all overriding methods in subclasses
    */
    public static void overrides(final ClassFile[] classes){
        
        int totalNumMethods = 0;
        int totalNumMethodOverrides = 0;

        //iterate over classes
        for(ClassFile cf: classes){
            System.out.printf(cf.isInterface() ? 
                "\nInterface: %s\n" : "\nClass: %s\n", cf.getName());
   
            final int numMethods = cf.methodCount();
            int numMethodOverrides = 0;

            //iterate over methods
            for(int m = 0; m < numMethods; m++){
                final MethodInfo method = cf.getMethod(m);

                System.out.printf("%sMethod: %s\n", Grosstab.str(1) ,method.getFriendlyDescriptor());

                //cannot be overridden if final, or private
                if(!method.checkAccess(Flag.ACC_FINAL) &&
                    !method.checkAccess(Flag.ACC_PRIVATE)){

                    //Find children that override given method
                    final List<ClassFile> overridingChildren = cf.findOverridesForMethod(method);
                    
                    //print each child out
                    for(ClassFile child: overridingChildren){
                            System.out.printf("%sOverridden in: %s\n", 
                              Grosstab.str(2), child.getName() );
                    }

                    numMethodOverrides += overridingChildren.size();
                }
            }

            if(numMethodOverrides != 0){
                System.out.printf("%sAverage number of overrides per method: %.2f\n", 
                Grosstab.str(1), numMethodOverrides/(double)numMethods);
            }

            totalNumMethods += numMethods;
            totalNumMethodOverrides += numMethodOverrides;
        }

        if(totalNumMethodOverrides != 0){
                System.out.printf("\nOverall average number of overrides per method: %.2f\n\n", 
                totalNumMethodOverrides/(double)totalNumMethods);
            }
    }


    //The toString for ClassFile has some handy stuff such 
    //as printing the constant pool, was helpful for debugging
    public static void verbose(final ClassFile[] classes){
        for(ClassFile cf: classes){
            System.out.printf("\nClass: %s\n", cf.toString(1));
        }

    }


    //Tell user how to invoke options
    public static void usage(){
        System.out.println("Usage: java ClassFileParser -[m|c|o] filenames...");
    }
}
