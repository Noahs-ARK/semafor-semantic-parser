package edu.cmu.cs.lti.ark.fn.parsing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import edu.cmu.cs.lti.ark.fn.utils.FNModelOptions;
import edu.cmu.cs.lti.ark.fn.wordnet.WordNetRelations;

public class ParserDriver {

	public static final String SERVER_FLAG = "server";
	/*
	 *  required flags:
	 *  startindex
	 *  endindex
	 *  mstmode
	 * 	mstserver
	 * 	mstport
	 *  posfile
	 *  test-parsefile
	 *  stopwords-file
	 *  wordnet-configfile
	 */
	public static void main(String[] args) {
		FNModelOptions options = new FNModelOptions(args);

		int start = options.startIndex.get();
		int end = options.endIndex.get();

		String mstServerMode = options.mstServerMode.get();	
		Socket kkSocket = null;
		PrintWriter out = null;
		BufferedReader in = null;

		/* Initializing connection to the MST server, if it exists */
		if (mstServerMode.equals(SERVER_FLAG)) {
			String mstServer = options.mstServerName.get();
			int mstPort = options.mstServerPort.get();
			try {
				kkSocket = new Socket(mstServer, mstPort);
				out = new PrintWriter(kkSocket.getOutputStream(),true);
				in = new BufferedReader(new InputStreamReader(kkSocket.getInputStream()));
			} catch (UnknownHostException e) {
				System.err.println("Don't know about host: " + mstServer + ". Exiting.");
				System.exit(-1);
			} catch (IOException e) {
				System.err.println("Couldn't get I/O for the connection to: " + mstServer + ". Exiting.");
				System.exit(-1);
			}
		} else {
			try {
				in = new BufferedReader(new FileReader(options.testParseFile.get()));
			} catch (IOException e) {
				System.err.println("Could not open the test parse file:" + options.testParseFile.get());
				System.exit(-1);
			}
		}

		/* Opening POS tagged file */
		String posFile = options.posTaggedFile.get();
		BufferedReader posReader = null;
		try {
			posReader = new BufferedReader(new FileReader(posFile));
		} catch (IOException e) {
			System.err.println("Could not open POS tagged file: " + posFile + ". Exiting.");
			System.exit(-1);
		}		

		/* Initializing WordNet config file */
		String stopWordsFile = options.stopWordsFile.get();
		String wnConfigFile = options.wnConfigFile.get();
		WordNetRelations wnr = new WordNetRelations(stopWordsFile, wnConfigFile);

		try {
			String posLine = null;
			int count = 0;
			while ((posLine = posReader.readLine()) != null) {
				// checking for start and end
				if (count < start) {
					if (!mstServerMode.equals(SERVER_FLAG)) {
						readCoNLLParse(in);
					}
				}
				if (count >= end) {
					break;
				}
				ArrayList<String> conllParse = null;
				if (!mstServerMode.equals(SERVER_FLAG)) {
					conllParse = readCoNLLParse(in);
				} else {
					out.println(posLine);
					conllParse = readCoNLLParse(in);
					System.out.println(conllParse);
				}				
				count++;				
			}
		} catch (IOException e) {
			System.err.println("Could not read line from pos file:" + posFile + ". Exiting.");
			System.exit(-1);
		}		

		if (in != null) {
			try {
				in.close();
			} catch (IOException e) {
				System.err.println("Could not close input stream. Exiting.");
				System.exit(-1);
			}
		}
		if (out != null) {
			out.close();
		}
		if (kkSocket != null) {
			try {
				kkSocket.close();
			} catch (IOException e) {
				System.err.println("Could not close socket. Exiting.");
				System.exit(-1);
			}
		}
		if (posReader != null) {
			try {
				posReader.close();
			} catch (IOException e) {
				System.err.println("Could not close POS input stream. Exiting.");
				System.exit(-1);
			}
			out.close();
		}
	}

	public static ArrayList<String> readCoNLLParse(BufferedReader bReader)
	{
		ArrayList<String> thisParse = new ArrayList<String>();
		try
		{
			String line=null;
			while((line=bReader.readLine())!=null)
			{
				line=line.trim();
				if(line.equals(""))
				{
					break;
				}
				else
				{
					thisParse.add(line);
				}
			}

		}
		catch(Exception e)
		{
			System.err.println("Could not read CoNLL parse reader. Exiting.");
			System.exit(-1);
		}
		return thisParse;
	}
}