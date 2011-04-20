package edu.cmu.cs.lti.ark.fn.parsing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import edu.cmu.cs.lti.ark.fn.identification.RequiredDataForFrameIdentification;
import edu.cmu.cs.lti.ark.fn.segmentation.MoreRelaxedSegmenter;
import edu.cmu.cs.lti.ark.fn.segmentation.RoteSegmenter;
import edu.cmu.cs.lti.ark.fn.utils.FNModelOptions;
import edu.cmu.cs.lti.ark.fn.wordnet.WordNetRelations;
import edu.cmu.cs.lti.ark.util.SerializedObjects;
import gnu.trove.THashMap;
import gnu.trove.THashSet;

public class ParserDriver {

	public static final String SERVER_FLAG = "server";
	public static final int BATCH_SIZE = 100;
	/*
	 *  required flags:
	 *  mstmode
	 * 	mstserver
	 * 	mstport
	 *  posfile
	 *  test-parsefile
	 *  stopwords-file
	 *  wordnet-configfile
	 *  fnidreqdatafile
	 *  goldsegfile
	 *  userelaxed
	 */
	public static void main(String[] args) {
		FNModelOptions options = new FNModelOptions(args);
		String mstServerMode = options.mstServerMode.get();	
		String mstServer = null;
		int mstPort = -1;

		/* Initializing connection to the MST server, if it exists */
		if (mstServerMode.equals(SERVER_FLAG)) {
			mstServer = options.mstServerName.get();
			mstPort = options.mstServerPort.get();
		}
		/* Initializing WordNet config file */
		String stopWordsFile = options.stopWordsFile.get();
		String wnConfigFile = options.wnConfigFile.get();
		WordNetRelations wnr = new WordNetRelations(stopWordsFile, wnConfigFile);		
		/* Opening POS tagged file */
		String posFile = options.posTaggedFile.get();
		BufferedReader posReader = null;
		try {
			posReader = new BufferedReader(new FileReader(posFile));
		} catch (IOException e) {
			System.err.println("Could not open POS tagged file: " + posFile + ". Exiting.");
			System.exit(-1);
		}		
		runParser(posReader, wnr, options, mstServer, mstPort);
		if (posReader != null) {
			try {
				posReader.close();
			} catch (IOException e) {
				System.err.println("Could not close POS input stream. Exiting.");
				System.exit(-1);
			}
		}
	}

	private static void runParser(BufferedReader posReader, 
			WordNetRelations wnr,
			FNModelOptions options,
			String serverName,
			int serverPort) {
		RequiredDataForFrameIdentification r = 
			(RequiredDataForFrameIdentification)
			SerializedObjects.readSerializedObject(options.fnIdReqDataFile.get());
		THashSet<String> allRelatedWords = r.getAllRelatedWords();
		Map<String, Set<String>> relatedWordsForWord = r.getRelatedWordsForWord();
		Map<String, THashMap<String, Set<String>>> wordNetMap = r.getWordNetMap();
		THashMap<String,THashSet<String>> frameMap = r.getFrameMap();
		THashMap<String,THashSet<String>> cMap = r.getcMap();			
		Map<String, Map<String, Set<String>>> revisedRelationsMap = 
			r.getRevisedRelMap();
		wnr.setRelatedWordsForWord(relatedWordsForWord);
		wnr.setWordNetMap(wordNetMap);

		String goldSegFile = options.goldSegFile.get();
		BufferedReader goldSegReader = null;
		// 0 == gold, 1 == strict, 2 == relaxed
		int segmentationMode = -1;
		if (goldSegFile == null || goldSegFile.equals("null") || goldSegFile.equals("")) {
			if (options.useRelaxedSegmentation.get().equals("yes")) {
				segmentationMode = 2;
			} else {
				segmentationMode = 1;
			}
		} else {
			segmentationMode = 0;
			try {
				goldSegReader = new BufferedReader(new FileReader(goldSegFile));
			} catch (IOException e) {
				System.err.println("Could not open gold segmentation file:" + goldSegFile);
				System.exit(-1);
			}				
		} 
		
		try {
			String posLine = null;
			int count = 0;
			ArrayList<String> posLines = new ArrayList<String>();
			ArrayList<ArrayList<String>> parseSets = new ArrayList<ArrayList<String>>();
			BufferedReader parseReader = null;
			if (serverName == null) {
				parseReader = new BufferedReader(new FileReader(options.testParseFile.get()));
			}
			do {
				int index = 0;
				posLines.clear();
				int size = parseSets.size();
				for (int i = 0; i < size; i++) {
					ArrayList<String> set = parseSets.get(0);
					set.clear();
					parseSets.remove(0);
				}
				parseSets.clear();
				for (index = 0; index < BATCH_SIZE; index++) {
					posLine = posReader.readLine();
					if (posLine == null) {
						break;
					}
					posLines.add(posLine);
					if (serverName == null) {
						ArrayList<String> parse = readCoNLLParse(parseReader);
						parseSets.add(parse);
					}
				}
				if (serverName != null) {
					parseSets = getParsesFromServer(serverName,
													serverPort,
													posLines);
				}
				System.out.println("Size of parse sets:" + parseSets.size());
				for (ArrayList<String> set: parseSets) {
					System.out.println(set);
				}
				count += index;
			} while (posLine != null);
			if (parseReader != null) {
				parseReader.close();
			}
		} catch (IOException e) {
			System.err.println("Could not read line from pos file. Exiting.");
			System.exit(-1);
		}		
	}

	public static ArrayList<ArrayList<String>> 
	getParsesFromServer(String server,
			int port,
			ArrayList<String> posLines) {
		Socket kkSocket = null;
		PrintWriter out = null;
		BufferedReader in = null;
		try {
			kkSocket = new Socket(server, port);
			out = new PrintWriter(kkSocket.getOutputStream(),true);
			in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
		} catch (UnknownHostException e) {
			System.err.println("Don't know about host: " + server);
			System.exit(-1);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: " + server);
			System.exit(-1);
		}
		for (String posLine: posLines) {
			out.println(posLine);
		}		
		out.println("*");
		ArrayList<ArrayList<String>> ret = new ArrayList<ArrayList<String>>();
        String fromServer="";
        try {
        	
        	while ((fromServer = in.readLine()) != null) {
        		fromServer = fromServer.trim();
        		String[] toks = fromServer.split("\t"); 
        		ArrayList<String> list = new ArrayList<String>();
        		for (int t = 0; t < toks.length; t+=10) {
        			String outLine = "";
        			for (int s = 0; s < 10; s++) {
        				outLine += toks[t+s] + "\t";
        			}
        			outLine = outLine.trim();
        			list.add(outLine);
        		}
        		ret.add(list);
        	}
        } catch (IOException e) {
        	System.out.println("Could not read parses from server. Exiting");
        	System.exit(-1);
        }
        out.close();
        try {
        	in.close();
        	kkSocket.close(); 
        } catch (IOException e) {
        	System.err.println("Could not close input channel from server. Exiting.");
        	System.exit(-1);
        }
        return ret;
	}


	public static ArrayList<String> readCoNLLParse(BufferedReader bReader) 	{
		ArrayList<String> thisParse = new ArrayList<String>();
		try {
			String line=null;
			while((line=bReader.readLine())!=null) {
				line=line.trim();
				if(line.equals("")) {
					break;
				}
				else {
					thisParse.add(line);
				}
			}
		}
		catch(Exception e) {
			System.err.println("Could not read CoNLL parse reader. Exiting.");
			System.exit(-1);
		}
		return thisParse;
	}
}