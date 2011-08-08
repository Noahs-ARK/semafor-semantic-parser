/*******************************************************************************
 * Copyright (c) 2011 Dipanjan Das 
 * Language Technologies Institute, 
 * Carnegie Mellon University, 
 * All Rights Reserved.
 * 
 * GetConstituentsFromParses.java is part of SEMAFOR 2.0.
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
package edu.cmu.cs.lti.ark.fn.clusters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import edu.cmu.cs.lti.ark.util.nlp.parse.DependencyParse;


public class GetConstituentsFromParses
{
	public static final int MAX_CONSTIT_LEN=20;
	public static class MapKMeans extends Mapper<LongWritable, Text, Text, IntWritable>
	{	
		public ArrayList<String> getLines(String file, Context context) throws IOException
		{
			Configuration conf = context.getConfiguration();
			FileSystem hdfs = FileSystem.get(conf);
			Path p = new Path(file); 
			FSDataInputStream dis = hdfs.open(p);
			ArrayList<String> res = new ArrayList<String>();
			BufferedReader bReader = new BufferedReader(new InputStreamReader(dis));
			String line = null;
			while((line=bReader.readLine())!=null)
				res.add(line.trim());
			bReader.close();
			return res;
		}		
		
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
		{
			System.out.println("In map method. Filename:"+value.toString());
			Configuration conf = context.getConfiguration();
			FileSystem hdfs = FileSystem.get(conf);
			Path p = new Path(value.toString()); 
			FSDataInputStream dis = hdfs.open(p);
			BufferedReader bReader = new BufferedReader(new InputStreamReader(dis));
			String line = null;
			while((line=bReader.readLine())!=null)
			{
				line = line.trim();
				String[] tabSplitToks = line.split("\t");
				String sent = tabSplitToks[0].trim();
				StringTokenizer st = new StringTokenizer(sent," \t");
				ArrayList<String[]> toks = new ArrayList<String[]>();
				while(st.hasMoreTokens())
				{
					String tok = st.nextToken().trim();
					if(tok.equals(""))
						continue;
					String[] toksInWord = new String[5];
					String[] arr = tok.trim().split("/");
					String dep = arr[arr.length-1];
					String par = arr[arr.length-2];
					String pos = arr[arr.length-3];
					String word = arr[0];
					for(int j = 1; j < arr.length-3; j ++)
					{
						word+="/"+arr[j];
					}					
					toksInWord[0]=word.toLowerCase();
					toksInWord[1]=pos;
					toksInWord[2]=dep;
					toksInWord[3]=par;
					toksInWord[4]="O";
					toks.add(toksInWord);
				}
				int len = toks.size();
				String[][] data = new String[5][len];
				for(int k = 0; k < 5; k ++)
				{
					data[k]=new String[len];
					for(int j = 0; j < len; j ++)
					{
						data[k][j]=""+toks.get(j)[k];
					}
				}
				DependencyParse parse = DependencyParse.processFN(data, 0.0);
				DependencyParse[] nodes = DependencyParse.getIndexSortedListOfNodes(parse);
				boolean[][] spanMat = new boolean[nodes.length - 1][nodes.length - 1];
				int[][] heads = new int[nodes.length-1][nodes.length-1];
				findSpans(spanMat,heads,nodes);
				for(int i = 0; i < spanMat.length; i ++)
				{
					for(int j = 0; j < spanMat.length; j ++)
					{
						if(i==j)
							continue;
						if((j-i)+1 > MAX_CONSTIT_LEN)
							continue;
						if(spanMat[i][j])
						{
							ArrayList<String> words = new ArrayList<String>();
							int headIndex = -1;
							int count = 0;
							for(int k = i; k<=j; k ++)
							{
								String w = data[0][k];
								String POS = data[1][k];
								if(POS.equals("NNP"))
									w="<NNP>";								
								words.add(w);
								if(heads[i][j]==k)
									headIndex=count;
								count++;
							}							
							int wordSize = words.size();
							count = 0;
							String phrase = "";
							while(count<wordSize)
							{
								String w=words.get(count);
								if(w.equals("<NNP>"))
								{
									phrase+="<NNP> ";
									if(count==wordSize-1)
										;
									else
									{
										while(words.get(count+1).equals("<NNP>"))
										{
											if(headIndex>count)
												headIndex--;
											count++;
											if(count==wordSize-1)
												break;
										}
									}										
								}	
								else
								{
									phrase+=w+" ";
								}
								count++;
							}
							phrase=phrase.trim();
							context.setStatus(phrase+"\t"+headIndex);
							context.write(new Text(phrase+"\t"+headIndex), new IntWritable(1));
						}
					}
				}				
				context.setStatus("Done with:"+line);
			}
			bReader.close();
			System.out.println("Finished with file:"+value.toString());
		}
		
		public static void findSpans(boolean[][] spanMat, int[][] heads, DependencyParse[] nodes) {
			int[] parent = new int[nodes.length - 1];
			int left[] = new int[parent.length];
			int right[] = new int[parent.length];
			for (int i = 0; i < parent.length; i++) {
				parent[i] = (nodes[i + 1].getParentIndex() - 1);
				left[i] = i;
				right[i] = i;
			}
			for (int i = 0; i < parent.length; i++) {
				int index = parent[i];
				while (index >= 0) {
					if (left[index] > i) {
						left[index] = i;
					}
					if (right[index] < i) {
						right[index] = i;
					}
					index = parent[index];
				}
			}
			for (int i = 0; i < parent.length; i++)
			{
				spanMat[left[i]][right[i]] = true;
				heads[left[i]][right[i]] = i;
			}
			
			// single words
			for (int i = 0; i < parent.length; i++) {
				spanMat[i][i] = true;
				heads[i][i]=i;
			}			
		}
	}
	
	public static class ReduceKMeans extends Reducer<Text, IntWritable, Text, IntWritable>
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
		Job job = new Job(conf, "GetConstituentsFromParses");
		FileInputFormat.addInputPath(job, new Path(args[0]));
		long fileSize = new Long(args[1]);
		int numMapTasks = new Integer(args[2]);
		long splitSize = fileSize/numMapTasks;
		System.out.println("Split size:"+splitSize);
		FileInputFormat.setMaxInputSplitSize(job, splitSize);
		FileOutputFormat.setOutputPath(job, new Path(args[3]));
		job.setJarByClass(GetConstituentsFromParses.class);
		job.setMapperClass(MapKMeans.class);
		job.setCombinerClass(ReduceKMeans.class);
		job.setReducerClass(ReduceKMeans.class);
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		boolean job1Success = job.waitForCompletion(true);
		if (! job1Success) {
			System.err.println("Job1 failed, exiting");
			System.exit(-1);
		}
	}	
}
