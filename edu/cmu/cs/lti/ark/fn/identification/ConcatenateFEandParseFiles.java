package edu.cmu.cs.lti.ark.fn.identification;

import java.io.BufferedReader;
import java.io.BufferedWriter;

import edu.cmu.cs.lti.ark.util.BasicFileIO;



public class ConcatenateFEandParseFiles {
	public static void main(String[] args) {
		String supFEFile = args[0];
		String supParseFile = args[1];
		String unLabFEFile = args[2];
		String unLabParseFile = args[3];
		String outputFEFile = args[4];
		String outputParseFile = args[5];
		/*
		 * write the parse file
		 */
		BufferedReader supReader = 
			BasicFileIO.openFileToRead(supParseFile);
		BufferedWriter parseWriter = 
			BasicFileIO.openFileToWrite(outputParseFile);
		String line = BasicFileIO.getLine(supReader);
		int offset = 0;
		while (line != null) {
			BasicFileIO.writeLine(parseWriter, line);
			offset++;
			line = BasicFileIO.getLine(supReader);
		}
		BasicFileIO.closeFileAlreadyRead(supReader);
		supReader = 
			BasicFileIO.openFileToRead(unLabParseFile);
		line = BasicFileIO.getLine(supReader);
		while (line != null) {
			BasicFileIO.writeLine(parseWriter, line);
			line = BasicFileIO.getLine(supReader);
		}
		BasicFileIO.closeFileAlreadyRead(supReader);
		BasicFileIO.closeFileAlreadyWritten(parseWriter);
		
		/*
		 * write the first part of the FE file
		 */
		BufferedWriter feWriter = BasicFileIO.openFileToWrite(outputFEFile);
		supReader = 
			BasicFileIO.openFileToRead(supFEFile);
		line = BasicFileIO.getLine(supReader);
		while (line != null) {
			BasicFileIO.writeLine(feWriter, line);
			line = BasicFileIO.getLine(supReader);
		}
		BasicFileIO.closeFileAlreadyRead(supReader);
		/*
		 * second part
		 */
		supReader = 
			BasicFileIO.openFileToRead(unLabFEFile);
		line = BasicFileIO.getLine(supReader);
		while (line != null) {
			String[] toks = line.trim().split("\t");
			toks[5] = "" + (new Integer(toks[5])+offset);
			String outLine = "";
			for (int i = 0; i < toks.length; i ++) {
				outLine += toks[i] + "\t";
			}
			outLine = outLine.trim();
			BasicFileIO.writeLine(feWriter, outLine);
			line = BasicFileIO.getLine(supReader);
		}
		BasicFileIO.closeFileAlreadyWritten(feWriter);
		BasicFileIO.closeFileAlreadyRead(supReader);
	}
}