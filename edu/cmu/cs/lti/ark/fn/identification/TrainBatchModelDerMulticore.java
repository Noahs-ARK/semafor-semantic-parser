package edu.cmu.cs.lti.ark.fn.identification;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import mpi.MPI;
import mpi.MPIException;

import riso.numerical.LBFGS;

import edu.cmu.cs.lti.ark.fn.constants.FNConstants;
import edu.cmu.cs.lti.ark.fn.optimization.*;
import edu.cmu.cs.lti.ark.fn.utils.FNModelOptions;
import edu.cmu.cs.lti.ark.util.SerializedObjects;
import gnu.trove.THashMap;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectDoubleHashMap;


public class TrainBatchModelDerMulticore
{
	protected double[] params = null;
	protected String[] eventFiles = null;	
	protected String modelFile = null;
	protected int modelSize = 0;
	protected String mReg = null;
	protected double mLambda = 0.0;
	protected double[] gradients = null;
	public static int rank = 0;
	public static int size = 2;
	public static Logger logger;
	
	public static void main(String[] args)
	{
		args = MPI.Init(args);
		rank = MPI.COMM_WORLD.Rank();
        size = MPI.COMM_WORLD.Size();
		FNModelOptions io = new FNModelOptions(args);
		String alphabetFile = io.alphabetFile.get();
		String eventsFilePrefix = io.eventsFile.get();
		String modelFile = io.modelFile.get();
		String reg = io.reg.get();
		double lambda = io.lambda.get();
		String restartFile = io.restartFile.get();
		if(restartFile.equals("null"))
			restartFile=null;
		String logoutputfile = io.logOutputFile.get();
		FileHandler fh = null;
		if (rank == 0) {
			LogManager logManager = LogManager.getLogManager();
			logManager.reset();
		}
		try {
       	 fh = new FileHandler(logoutputfile + "." + rank, true);
       	 fh.setFormatter(new SimpleFormatter());
            logger = Logger.getLogger("TrainBatch." + rank);
            logger.addHandler(fh);   
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}	
		TrainBatchModelDerMulticore tbm = new TrainBatchModelDerMulticore(alphabetFile, 
				eventsFilePrefix, 
				modelFile, 
				reg, lambda, 
				restartFile);
		tbm.trainModel();
		fh.close();
		MPI.Finalize();
	}	
	
	private void setFeatNum(String alphabetFile) {
		try
		{
			BufferedReader bReader = new BufferedReader(new FileReader(alphabetFile));
			String line = bReader.readLine().trim();
			line=line.trim();
			modelSize = (new Integer(line))+1;
			bReader.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	private void setEventFiles(String eventsFilePrefix) {
		File dirFile = new File(eventsFilePrefix);	
		FilenameFilter filter = new FilenameFilter(){
			public boolean accept(File dir, String name) {
				return name.startsWith("feats")&&name.contains(".jobj");
			}			
		};
		String[] files = dirFile.list(filter);
		Comparator<String> comp = new Comparator<String>(){
			public int compare(String o1, String o2) {
				o1=o1.substring(0,o1.length()-8);
				o2=o2.substring(0,o2.length()-8);
				int lastIndex = o1.lastIndexOf("_");
				int i1 = new Integer(o1.substring(lastIndex+1));
				lastIndex = o2.lastIndexOf("_");
				int i2 = new Integer(o2.substring(lastIndex+1));
				if(i1<i2)
					return -1;
				else if(i1==i2)
					return 0;
				else 
					return 1;
			}
		};
		Arrays.sort(files,comp);
		int batch = (int)(Math.ceil((double) files.length / (double) size));
		int start = rank * batch;
		int end = files.length < (rank * batch + batch) ? files.length : (rank * batch + batch);
		eventFiles = new String[end - start];		
		for(int i = start; i < end; i ++)
		{
			eventFiles[i - start] = dirFile.getAbsolutePath()+"/"+files[i];
		}
		System.out.println();
		System.out.println("Total number of datapoints:"+eventFiles.length);
		logger.info("Total number of datapoints:"+eventFiles.length);
	}
	
	private void initParams(String restartFile) {
		for(int i = 0; i < modelSize; i ++)
			params[i] = 1.0;
		if(restartFile!=null)
		{
			int i = 0;
			try
			{
				BufferedReader bReader = new BufferedReader(new FileReader(restartFile));
				String line = null;
				while((line=bReader.readLine())!=null)
				{
					params[i]=new Double(line.trim());
					i++;
				}
				bReader.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				System.exit(0);
			}
		}
	}
	
	public TrainBatchModelDerMulticore(String alphabetFile, 
			String eventsFilePrefix, 
			String modelFile0, 
			String reg, 
			double lambda, 
			String restartFile)
	{
		setFeatNum(alphabetFile);
		modelFile = modelFile0;
		setEventFiles(eventsFilePrefix);
		mReg = reg;
		mLambda= lambda/size;
		params = new double[modelSize];
		if (rank == 0) {
			initParams(restartFile);
		}
	}
	
	
	
	public void trainModel()
	{
		try
		{
			runCustomLBFGS();
		}
		catch(Exception e)
		{
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public void saveModel(String modelFile)
	{
		try
		{
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(modelFile));
			for(int i = 0; i < modelSize; i ++)		
			{
				bWriter.write(params[i]+"\n");
			}
			bWriter.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private double getValuesAndGradients() {
		try {
			MPI.COMM_WORLD.Bcast(params, 0, params.length, MPI.DOUBLE, 0);
		} catch (MPIException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		int numDataPoints = eventFiles.length;
		double value = 0.0;
		Arrays.fill(gradients, 0.0);
		double[] gradients1 = new double[gradients.length];
		for (int index = 0; index < numDataPoints; index ++) {
			int[][][] featureArray=(int[][][])SerializedObjects.readSerializedObject(eventFiles[index]);
			int featArrLen = featureArray.length;
			double exp[][] = new double[featArrLen][];
			double sumExp[] = new double[featArrLen];
			double totalExp = 0.0;
			for(int i = 0; i < featArrLen; i ++) {
				exp[i] = new double[featureArray[i].length];
				sumExp[i] = 0.0;		
				for(int j = 0; j < exp[i].length; j ++) {
					double weiFeatSum = 0.0;
					int[] feats = featureArray[i][j];
					for (int k = 0; k < feats.length; k++)
					{
						weiFeatSum += params[feats[k]];
					}
					exp[i][j] = Math.exp(weiFeatSum);
					sumExp[i] += exp[i][j];
				}
				totalExp += sumExp[i];
			}
			value -= Math.log(sumExp[0]) - Math.log(totalExp);			
			for(int i = 0; i < featArrLen; i ++) {
				for(int j = 0; j < exp[i].length; j ++) {
					double Y = 0.0;
					if(i==0) {
						Y = exp[i][j]/sumExp[i];
					}
					double YMinusP = Y - (exp[i][j]/totalExp);
					int[] feats = featureArray[i][j];
					for(int k = 0; k < feats.length; k ++) {
						gradients[feats[k]] -= YMinusP;
					}
				}
			}
			if (index % 100 == 0) { System.out.print("."); logger.info(""+index);};
		}
		if (mReg.equals("reg")) {
			for (int i = 0; i < params.length; ++i) {
				double weight = params[i];
				value += mLambda * (weight * weight);
				gradients[i] += 2 * mLambda * weight;
			}
		}
//		if (rank != 0) {
//			logger.info("Sending gradients to:" + 0);
//			MPI.COMM_WORLD.Send(gradients, 0, gradients.length, MPI.DOUBLE, 0, 0);
//		} else {
//			for (int i = 1; i < size; i ++) {
//				logger.info("Receiving gradients from:" + i);
//				MPI.COMM_WORLD.Recv(gradients1, 0, gradients1.length, MPI.DOUBLE, i, 0);
//				for (int j = 0; j < gradients.length; j ++) {
//					gradients[j] += gradients1.length;
//				}
//			}
//		}
//		if (rank != 0) {
//			temp[0] = value;
//			MPI.COMM_WORLD.Send(temp, 0, 1, MPI.DOUBLE, 0, 0);
//		} else {
//			for (int i = 1; i < size; i ++) {
//				MPI.COMM_WORLD.Recv(temp, 0, 1, MPI.DOUBLE, i, 0);
//				value += temp[0];
//			}
//		}
		double[] valArray = new double[1];
		valArray[0] = value;
		double[] temp = new double[1];
		MPI.COMM_WORLD.Reduce(gradients, 0, gradients1, 0, gradients.length, MPI.DOUBLE, MPI.SUM, 0);
		MPI.COMM_WORLD.Reduce(valArray, 0, temp, 0, 1, MPI.DOUBLE, MPI.SUM, 0);
		if (rank == 0) {
			gradients = gradients1;
			valArray = temp;
		}
		System.out.println();
		return valArray[0];
	}
	
	public void runCustomLBFGS() throws Exception
	{   
		double[] diagco = new double[modelSize];
		int[] iprint = new int[2];
		iprint[0] = FNConstants.m_debug?1:-1;
		iprint[1] = 0; //output the minimum level of info
		int[] iflag = new int[1];
		iflag[0] = 0;
		gradients = new double[modelSize];
		int iteration = 0;
		do {
			Arrays.fill(gradients, 0.0);
			logger.info("Starting iteration:" + iteration);
			double m_value = getValuesAndGradients();
			if (rank == 0) {
				System.out.println("Function Value:"+m_value);
				logger.info("Function value:"+m_value);
				LBFGS.lbfgs(modelSize,
						FNConstants.m_num_corrections, 
						params, 
						m_value,
						gradients, 
						false, //true if we're providing the diag of cov matrix Hk0 (?)
						diagco, //the cov matrix
						iprint, //type of output generated
						FNConstants.m_eps,
						FNConstants.xtol, //estimate of machine precision
						iflag //i don't get what this is about
				);
			}
			logger.info("Finished iteration:"+iteration);
			iteration++;
			if (rank == 0) {
				if(iteration%FNConstants.save_every_k==0)
					saveModel(modelFile+"_"+iteration);
			}
			MPI.COMM_WORLD.Bcast(iflag, 0, iflag.length, MPI.INT, 0);
		} while (iteration <= FNConstants.m_max_its&&iflag[0] != 0);
		if (rank == 0) {
			saveModel(modelFile);
		}
	}	
}