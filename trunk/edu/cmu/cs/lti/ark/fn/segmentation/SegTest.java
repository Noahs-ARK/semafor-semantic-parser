package edu.cmu.cs.lti.ark.fn.segmentation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import edu.cmu.cs.lti.ark.fn.identification.RequiredDataForFrameIdentification;
import edu.cmu.cs.lti.ark.util.SerializedObjects;
import gnu.trove.THashMap;
import gnu.trove.THashSet;

public class SegTest {
	public static void main(String[] args) {
		String parseFile = "lrdata/semeval.fulltrain.sentences.lemma.tags";
		String tokFile = "lrdata/semeval.fulltrain.sentences";
		ArrayList<String> tokenNums = new ArrayList<String>();
		ArrayList<String> orgSentenceLines = new ArrayList<String>();
		ArrayList<String> originalIndices = new ArrayList<String>();
		ArrayList<String> parses = new ArrayList<String>();
		int count = 0;
		int start = 0;
		int end = 10;
		try
		{
			BufferedReader inParses = new BufferedReader(new FileReader(parseFile));
			BufferedReader inOrgSentences = new BufferedReader(new FileReader(tokFile));
			String line = null;
			int dummy = 0;
			while((line=inParses.readLine())!=null)
			{
				String line2 = inOrgSentences.readLine().trim();
				if(count<start)	// skip sentences prior to the specified range
				{
					count++;
					continue;
				}
				parses.add(line.trim());
				orgSentenceLines.add(line2);
				tokenNums.add(""+dummy);	// ?? I think 'dummy' is just the offset of the sentence relative to options.startIndex
				originalIndices.add(""+count);
				if(count==(end-1))	// skip sentences after the specified range
					break;
				count++;
				dummy++;
			}				
			inParses.close();
			inOrgSentences.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		RequiredDataForFrameIdentification r = 
			(RequiredDataForFrameIdentification)SerializedObjects.readSerializedObject("lrdata/reqData.jobj");
		THashSet<String> allRelatedWords = r.getAllRelatedWords();		
		MoreRelaxedSegmenter seg = new MoreRelaxedSegmenter();
		ArrayList<String> segs = seg.findSegmentationForTest(tokenNums, parses, allRelatedWords);
	}
}