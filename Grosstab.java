/*
*This is a travesty, I just didn't want to 
*be throwing for loops everywhere for building strings
*with different indentation levels
*/
public class Grosstab{
	
	//pass an int with how many levels to tab indent
	public static String str(final int n){
		return repeatTab("\t", n);
	}

	//Recursive solution for the sake of it
	//lg(n) instead of n
	private static String repeatTab(String s, final int n) {
	   
		//zero long, don't append any tabs
		if( n == 0)
		{
			s = "";
		}
		//append tabs
		//recursively call lg(n) times
		else
		{
			//Double the string
			if(n % 2 == 0)
			{
		   		s = repeatTab(s+s, n/2);
		   	}
		   	//Double plus one
			else
			{
		   		s += repeatTab(s+s, n/2);
			} 
		}

		return s;
	}
}
