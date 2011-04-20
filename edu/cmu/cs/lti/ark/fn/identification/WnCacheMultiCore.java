package edu.cmu.cs.lti.ark.fn.identification;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import mpi.MPI;

import edu.cmu.cs.lti.ark.fn.data.prep.ParsePreparation;
import edu.cmu.cs.lti.ark.fn.utils.FNModelOptions;
import edu.cmu.cs.lti.ark.fn.wordnet.WordNetRelations;
import edu.cmu.cs.lti.ark.util.SerializedObjects;
import edu.cmu.cs.lti.ark.util.ds.map.IntCounter;
import edu.cmu.cs.lti.ark.util.nlp.parse.DependencyParse;
import gnu.trove.THashMap;
import gnu.trove.THashSet;
import gnu.trove.TObjectIdentityHashingStrategy;


public class WnCacheMultiCore
{
	private THashMap<String, THashSet<String>> mFrameMap=null;
	private WordNetRelations mWnr = null;
	private ArrayList<String> mListOfParses = null;
	private String mFrameElementsFile = null;
	private String mWnCacheFile=null;
	private String mLemmaCacheFile = null;
	private THashMap<String,THashSet<String>> cache = null;
	private THashMap<String,String> lemmaCache = null;
	private int mStart = 0;
	private int mEnd = 0;
	public static final int TAG = 0;
	public static int rank = 0;
	static Logger logger = null;
		
	public static void main(String[] args)
	{
		args = MPI.Init(args);
		FNModelOptions options = new FNModelOptions(args);
        rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        
        boolean append = true;
        String logoutputfile = options.logOutputFile.get();
        FileHandler fh = null;
        try {
        	 fh = new FileHandler(logoutputfile + "." + rank, append);
        	 fh.setFormatter(new SimpleFormatter());
             logger = Logger.getLogger("WnCacheLog." + rank);
             logger.addHandler(fh);   
        } catch (IOException e) {
        	e.printStackTrace();
        	System.exit(-1);
        }   
        
        String feFile = options.trainFrameElementFile.get();
		ArrayList<String> parses = ParsePreparation.readSentencesFromFile(options.trainParseFile.get());
		THashMap<String, THashSet<String>> frameMap = (THashMap<String, THashSet<String>>)SerializedObjects.readSerializedObject(options.frameNetMapFile.get());
		WordNetRelations wnr = new WordNetRelations(options.stopWordsFile.get(),options.wnConfigFile.get());
		String wnCacheFile = options.wnMapFile.get();
		String lemmaCacheFile = options.lemmaCacheFile.get();
		int start, end;
		int[] intArr = new int[2];
        if (rank == 0) {
        	System.out.println("#" + rank +": This is the master node.");
        	int count = countFELines(feFile);
        	int batch = (int)(Math.ceil((double) count / (double) size));
        	System.out.println("#" + rank +": Dividing " + count +" samples across " + size + " cores.");
        	start = 0;
        	end = batch;
        	for (int i = 1; i < size; i ++) {
        		intArr[0] = i * batch;
        		intArr[1] = i * batch + batch;
                MPI.COMM_WORLD.Send(intArr, 0, intArr.length, MPI.INT, i, TAG);
        	}        	
        } else {
        	MPI.COMM_WORLD.Recv(intArr, 0, intArr.length, MPI.INT, 0, TAG);
            System.out.println("#" + rank + ": Got message from master, " + intArr[0] + "\t" + intArr[1]);
            start = intArr[0];
            end = intArr[1];
        }        
		WnCacheMultiCore wnc = new WnCacheMultiCore(feFile, 
													parses, 
													frameMap, 
													wnr, 
													wnCacheFile, 
													lemmaCacheFile, 
													start,end);
		wnc.wncache();
		try {
			fh.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		MPI.Finalize();
	}	
	
	public static int countFELines(String file) {
		int count = 0;
		try {
			BufferedReader bReader = new BufferedReader(new FileReader(file));
			String line = null;
			while ((line = bReader.readLine()) != null) {
				count++;
			}
			bReader.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return count;
	}
	
	public WnCacheMultiCore(String frameElementsFile, 
							ArrayList<String> parses,
							THashMap<String, THashSet<String>> frameMap, 
							WordNetRelations wnr, 
							String wnCacheFile, 
							String lemmaCacheFile, 
							int start, 
							int end)
	{
		mWnr = wnr;
		mFrameMap=frameMap;
		mListOfParses=parses;
		mFrameElementsFile=frameElementsFile;
		mWnCacheFile=wnCacheFile;
		mLemmaCacheFile=lemmaCacheFile;
		mStart=start;
		mEnd=end;
	}
		
	public void wncache()
	{
		cache = new THashMap<String,THashSet<String>>();
		lemmaCache = new THashMap<String,String>();
		System.out.println("#" + rank + "Caching WN relationships........");
		logger.info("#" + rank + "Caching WN relationships........");
		try
		{
			BufferedReader bReader = new BufferedReader(new FileReader(mFrameElementsFile));
			String line = null;
			int count = 0;
			while((line=bReader.readLine())!=null)
			{
				if(count<mStart)
				{
					count++;
					continue;
				}
				if(count>=mEnd)
				{
					break;
				}
				line=line.trim();
				System.out.println("#" + rank + "Processing line number "+count+":"+line);
				logger.info("#" + rank + "Processing line number "+count+":"+line);
				processLine(line,count);
				count++;
			}
			bReader.close();
		}
		catch(Exception e)
		{
			System.out.println("#" + rank + "Problem in reading fe file. exiting..");
			logger.info("#" + rank + "Problem in reading fe file. exiting..");
			System.exit(0);
		}
		SerializedObjects.writeSerializedObject(lemmaCache, mLemmaCacheFile+"_"+mStart+"_"+mEnd);
		SerializedObjects.writeSerializedObject(cache, mWnCacheFile+"_"+mStart+"_"+mEnd);
	}
	
	private void updateCache(String frame,int[] intTokNums,String[][] data)
	{
		THashSet<String> hiddenUnits = mFrameMap.get(frame);
		DependencyParse parse = DependencyParse.processFN(data, 0.0);
		for (String unit : hiddenUnits)
		{
			updateCacheForOneUnit(frame,intTokNums,unit,data,mWnr,parse);
		}
	}	
	
	private void updateCacheForOneUnit(String mFrameName, 
			 int[] tokenNums, 
			 String hiddenWord, 
			 String[][] parseData, 
			 WordNetRelations wnr, 
			 DependencyParse parse)
	{
		int[] mTokenNums = tokenNums;
		Arrays.sort(mTokenNums);
		
		String hiddenUnitTokens = "";
		String hiddenUnitLemmas = "";
		
		String actualTokens = "";
		String actualLemmas = "";
		
		String[] hiddenToks = hiddenWord.split(" ");
		FeatureExtractor featex = new FeatureExtractor();
		for(int i = 0; i < hiddenToks.length; i ++)
		{
			String[] arr = hiddenToks[i].split("_");
			hiddenUnitTokens+=arr[0]+" ";
			hiddenUnitLemmas+=featex.getLowerCaseLemma(lemmaCache, arr[0], arr[1], wnr)+" ";
		}
		hiddenUnitTokens=hiddenUnitTokens.trim();
		for(int i = 0; i < mTokenNums.length; i ++)
		{
			String lexUnit = parseData[0][mTokenNums[i]];
			String pos = parseData[1][mTokenNums[i]];	
			actualTokens+=lexUnit+" ";
			actualLemmas+=featex.getLowerCaseLemma(lemmaCache,lexUnit, pos,wnr)+" ";
	
		}	
		actualTokens=actualTokens.trim();
		featex.getWNRelations(cache,hiddenUnitTokens, actualTokens,wnr);
		
	}
	
	private void processLine(String line, int index) throws IOException
	{
		String[] toks = line.split("\t");
		int sentNum = new Integer(toks[5]);
		String parseLine = mListOfParses.get(sentNum);
		String frameName = toks[1];
		String[] tokNums = toks[3].split("_");
		int[] intTokNums = new int[tokNums.length];
		for(int j = 0; j < tokNums.length; j ++)
			intTokNums[j] = new Integer(tokNums[j]);
		Arrays.sort(intTokNums);
		StringTokenizer st = new StringTokenizer(parseLine,"\t");
		int tokensInFirstSent = new Integer(st.nextToken());
		String[][] data = new String[5][tokensInFirstSent];
		for(int k = 0; k < 5; k ++)
		{
			data[k]=new String[tokensInFirstSent];
			for(int j = 0; j < tokensInFirstSent; j ++)
			{
				data[k][j]=""+st.nextToken().trim();
			}
		}
		Set<String> set = mFrameMap.keySet();
		int count = 0;
		for(String f:set)
		{
			updateCache(f,intTokNums,data);
			System.out.print(".");
			if (count % 100 == 0) {
				logger.info("frame count: " + count);
			}
			count++;
		}
		System.out.println();
	}	
}