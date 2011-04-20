package edu.cmu.cs.lti.ark.fn.clusters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.filecache.DistributedCache;
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
import org.apache.hadoop.util.StringUtils;

import edu.cmu.cs.lti.ark.fn.data.prep.ParsePreparation;


public class CheckPresenceOfSpansInLargeCorpus
{
	public static final int MAX_CONSTIT_LEN=10;
	
	public static class MapKMeans extends Mapper<LongWritable, Text, Text, IntWritable>
	{	
		public String[] spanArray = null;
				
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
			System.out.println("Length of paramfiles:"+paramFiles.length);
			String spanFile = paramFiles[0].toString();
			ArrayList<String> lines = ParsePreparation.readSentencesFromFile(spanFile);
			spanArray = new String[lines.size()];
			lines.toArray(spanArray);
			Arrays.sort(spanArray);
		}
		
		public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
		{
			String string = value.toString();
			String[] toks = string.split("\t");
			String span = toks[0];
			String[] tabSeparated = string.trim().split("\t");
			String text = tabSeparated[0]+"\t"+tabSeparated[1];
			int val = new Integer(tabSeparated[2]);
			if(Arrays.binarySearch(spanArray, span)>=0)	
			{
				context.write(new Text(text), new IntWritable(val));
			}
			context.setStatus(text);
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
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		job.setJarByClass(CheckPresenceOfSpansInLargeCorpus.class);
		job.setMapperClass(MapKMeans.class);
		job.setCombinerClass(ReduceKMeans.class);
		job.setReducerClass(ReduceKMeans.class);
		job.setInputFormatClass(TextInputFormat.class);
		job.setOutputFormatClass(TextOutputFormat.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		DistributedCache.addCacheFile(new Path(args[2]).toUri(), job.getConfiguration());
		boolean job1Success = job.waitForCompletion(true);
		if (! job1Success) {
			System.err.println("Job1 failed, exiting");
			System.exit(-1);
		}
	}	
}