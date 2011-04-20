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


public class FilterConstituents
{
	public static final int MAX_CONSTIT_LEN=10;
	public static class MapKMeans extends Mapper<LongWritable, Text, Text, IntWritable>
	{	
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
		{
			String line = value.toString();
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
		Job job = new Job(conf, "FilterConstituents");
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.setJarByClass(FilterConstituents.class);
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