/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das 
 * Language Technologies Institute, 
 * Carnegie Mellon University, 
 * All Rights Reserved.
 * 
 * FirstScan.java is part of SEMAFOR 2.0.
 * 
 * SEMAFOR 2.0 is free software: you can redistribute it and/or modify  it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 * 
 * SEMAFOR 2.0 is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along
 * with SEMAFOR 2.0.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package edu.cmu.cs.lti.ark.fn.identification;
import edu.cmu.cs.lti.ark.fn.data.prep.ParsePreparation;
import edu.cmu.cs.lti.ark.fn.wordnet.WordNetRelations;
import edu.cmu.cs.lti.ark.util.SerializedObjects;
import gnu.trove.THashMap;
import gnu.trove.THashSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.StringUtils;


public class FirstScan
{
	public static class Map extends Mapper<LongWritable, Text, Text, IntWritable>
	{	
		private THashMap<String, THashSet<String>> mFrameMap=null;
		private WordNetRelations mWnr = null;
		private ArrayList<String> mListOfParses = null;	
		
		public void setup(Context context) throws IOException, InterruptedException
		{ 
			Configuration conf = context.getConfiguration();
			Path[] paramFiles = null;
			try
			{
				paramFiles = DistributedCache.getLocalCacheFiles(conf);
			}
			catch (IOException ioe)
			{
				System.err.println("Caught exception while getting cached files: " + StringUtils.stringifyException(ioe));
			}
			System.out.println("paramfiles:"+paramFiles);
			String fnMapFile = paramFiles[0].toString();
			mFrameMap = (THashMap<String, THashSet<String>>)SerializedObjects.readSerializedObject(fnMapFile);
			
			String parsesFile = paramFiles[1].toString();
			mListOfParses = ParsePreparation.readSentencesFromFile(parsesFile);
			
			String wnDir = conf.get("wn.dir");
			System.out.println("wnDir:"+wnDir);
			System.out.println("Wordnet directory:"+wnDir);
			setupWNOnHadoop(context,wnDir);
		}		
		
		
		public void setupWNOnHadoop(Context context, String wnDir) throws IOException
		{
			Configuration conf = context.getConfiguration();
			FileSystem hdfs = FileSystem.get(conf);
			String hd = hdfs.getHomeDirectory().toString();
			String wd = hdfs.getWorkingDirectory().toString();
			System.out.println("Home directory:"+hd);
			System.out.println("Working directory"+wd);
			Path p = new Path(wnDir); 
			FileStatus[] arr = hdfs.listStatus(p);
			System.out.println("Contents:");
			String configFile = null;
			String stopFile = null;
			for(FileStatus fs:arr)
			{
				String fileName = fs.getPath().toString();
				if(fileName.contains("config"))
					configFile=fileName;
				else if(fileName.contains("stopwords"))
					stopFile=fileName;
			}
			WordNetRelations wnr = new WordNetRelations(stopFile,configFile);
		}
		
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
		{
			String line = value.toString();
			if(line.equals("reg"))
				return;
			String[] tabSeparated = line.split("\t");
			String text = tabSeparated[0]+"\t"+tabSeparated[1];
			int val = new Integer(tabSeparated[2]);
			context.setStatus("Done with text:"+text);
			char first = text.charAt(0);
			if((first>='a'&&first<='z')||(first>='A'&&first<='Z')||(first>='0'&&first<='9'))
			{
				context.write(new Text(text), (new IntWritable(val)));
			}
		}
	}
	
	public static class Reduce extends Reducer<Text, IntWritable, Text, IntWritable>
	{	
		public void reduce(Text arg0, Iterable<IntWritable> arg1, Context context) throws IOException, InterruptedException
		{
			System.out.println("Text:"+arg0.toString());
			int totalCount=0;
			for(IntWritable count:arg1)
			{
				totalCount+=count.get();
			}
			context.write(arg0, new IntWritable(totalCount));
			context.setStatus("Done with key:"+arg0.toString());
		}	
	}	
	
	public static void main(String[] args1) throws Exception
	{
		Configuration conf = new Configuration();
		GenericOptionsParser gop = new GenericOptionsParser(conf,args1);
		String[] args = gop.getRemainingArgs();
		System.out.println("Arguments:");
		for(String arg:args)
			System.out.println(arg);
		String wnDir = args[6];
		System.out.println("wnDir:"+wnDir);
		conf.set("wn.dir", wnDir);
		Job job = new Job(conf, "AlphabetCreation");
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		long fileSize = new Long(args[3]);
		int numMapTasks = new Integer(args[2]);
		long splitSize = fileSize/numMapTasks;
		System.out.println("Split size:"+splitSize);
		FileInputFormat.setMaxInputSplitSize(job, splitSize);
				
		job.setJarByClass(FirstScan.class);
		job.setMapperClass(Map.class);
		job.setCombinerClass(Reduce.class);
		job.setReducerClass(Reduce.class);
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
		
		
		String fnMapFile = args[5];
		System.out.println("fnMapFile:"+fnMapFile);
		DistributedCache.addCacheFile(new Path(fnMapFile).toUri(), job.getConfiguration());
		
		String parseFile = args[4];
		System.out.println("parseFile:"+parseFile);
		DistributedCache.addCacheFile(new Path(parseFile).toUri(), job.getConfiguration());
		
		
		boolean job1Success = job.waitForCompletion(true);	
		if (! job1Success) {
			System.err.println("Job1 failed, exiting");
			System.exit(-1);
		}
	}	
}
