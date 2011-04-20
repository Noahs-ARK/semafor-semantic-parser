package edu.cmu.cs.lti.ark.fn.evaluation;

/**
 * Processes command-line options and stores a variety of configuration parameters. 
 * Options should be formatted as argname:value (or simply the argname if boolean).
 * See the code for details.
 * 
 * @author dipanjan
 *
 */
public final class ParseOptions
{
	public String frameNetMapFile = null;
	public String wnConfigFile = null;
	public String stopWordsFile = null;
	
	public String testParseFile = null;
	public String testTokenizedFile = null;
	public String testFEPredictionsFile = null;
	public String segParamFile = null;
	public String idParamFile = null;
	public String allRelatedWordsFile = null;
	public String wnRelatedWordsForWordsFile = null;
	public String wnMapFile = null;
	public String wnHiddenWordsCacheFile = null;
	public String hvCorrespondenceFile = null;
	public String goldFrameTokenFile = null;
	public String tempFile = null;
	public String frameElementsFile = null;
	
	public int startIndex = -1;
	public int endIndex = -1;
	
	public String outputFile = null;
	
	public ParseOptions(String[] args)
	{		
		for(int i = 0; i < args.length; i ++)
		{
			System.out.println(args[i]);
			String[] pair = new String []{
				args[i].substring(0,args[i].indexOf(':')),
				args[i].substring(args[i].indexOf(':')+1),
			};
			if(pair[0].equals("frameNetMapFile"))
			{
				frameNetMapFile = pair[1].trim();
			}
			else if(pair[0].equals("wnConfigFile"))
			{
				wnConfigFile = pair[1].trim();
			}
			else if(pair[0].equals("stopWordsFile"))
			{
				stopWordsFile = pair[1].trim();
			}
			else if(pair[0].equals("testParseFile"))
			{
				testParseFile = pair[1].trim();
			}
			else if(pair[0].equals("testTokenizedFile"))
			{
				testTokenizedFile = pair[1].trim();
			}
			else if(pair[0].equals("testFEPredictionsFile"))
			{
				testFEPredictionsFile = pair[1].trim();
			}
			else if(pair[0].equals("segParamFile"))
			{
				segParamFile = pair[1].trim();
			}			
			else if(pair[0].equals("idParamFile"))
			{
				idParamFile = pair[1].trim();
			}
			else if(pair[0].equals("allRelatedWordsFile"))
			{
				allRelatedWordsFile = pair[1].trim();
			}
			else if(pair[0].equals("startIndex"))
			{
				startIndex = new Integer(pair[1].trim());
			}
			else if(pair[0].equals("endIndex"))
			{
				endIndex = new Integer(pair[1].trim());
			}
			else if(pair[0].equals("outputFile"))
			{
				outputFile = pair[1].trim();
			}
			else if(pair[0].equals("wnRelatedWordsForWordsFile"))
			{
				wnRelatedWordsForWordsFile = pair[1].trim();
			}
			else if(pair[0].equals("wnMapFile"))
			{
				wnMapFile = pair[1].trim();
			}
			else if(pair[0].equals("wnHiddenWordsCacheFile"))
			{
				wnHiddenWordsCacheFile = pair[1].trim();
			}
			else if(pair[0].equals("hvCorrespondenceFile"))
			{
				hvCorrespondenceFile = pair[1].trim();
			}
			else if(pair[0].equals("goldFrameTokenFile"))
			{
				goldFrameTokenFile = pair[1].trim();
			}
			else if(pair[0].equals("tempfile"))
			{
				tempFile = pair[1].trim();
			}
			else if(pair[0].equals("framelementsfile"))
			{
				frameElementsFile = pair[1].trim();
			}
		}	
		
	}	
}