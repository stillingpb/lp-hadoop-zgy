package v2.label;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class LabelRun {

	private static String ROOT = "/lp";
	private static String GRAPH_PATH = ROOT + "/graph";
	private static String TMP = ROOT + "/tmp";
	private static String TMP_GRAPH = ROOT + "/graph.tmp";
	private static String TMP_GRAPH_SWAP = TMP + "/graph_swap";
	private static String TMP_LABEL = ROOT + "/label.tmp";
	private static String TMP_LABEL_SWAP = TMP + "/label_swap";
	private static String COMMUNITY = ROOT + "/community";

	private static void swapFolder2File(FileSystem fs, String swapFolder,
			String file) throws IOException {
		String dataFile = swapFolder + "/part-r-00000";
		Path filePath = new Path(file);
		Path dataFilePath = new Path(dataFile);

		fs.delete(filePath);
		fs.rename(dataFilePath, filePath);
		fs.delete(new Path(swapFolder), true);
	}

	private static void removeFolder(FileSystem fs, String folder)
			throws IOException {
		fs.delete(new Path(folder), true);
	}

	private static void runInitJob(Configuration conf, FileSystem fs)
			throws IOException, InterruptedException, ClassNotFoundException {

		/* 第一个mr作业，初始化图 */
		Job job = new Job(conf, "graph init");
		job.setJarByClass(LabelRun.class);

		job.setMapperClass(InitGraphMapper.class);
		job.setReducerClass(InitGraphReducer.class);

		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(IntWritable.class);

		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);

		// FileInputFormat.setInputPaths(job, inputPaths);
		FileInputFormat.setInputPaths(job, new Path(GRAPH_PATH));
		FileOutputFormat.setOutputPath(job, new Path(TMP_GRAPH_SWAP));

		removeFolder(fs, TMP_LABEL_SWAP);
		job.waitForCompletion(true);

		swapFolder2File(fs, TMP_GRAPH_SWAP, TMP_GRAPH);

		/*
		 * 初始化标签文件。 已经废除，在具体标签传递过程中，进行动态生成标签，不需要初始化标签了
		 */
		// new InitLabel(fs, GRAPH_PATH, TMP_LABEL).start();
	}

	/**
	 * 运行标签传播算法
	 * 
	 * @param conf
	 * @param fs
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ClassNotFoundException
	 */
	private static void runLabelPropagation(Configuration conf, FileSystem fs)
			throws IOException, InterruptedException, ClassNotFoundException {
		conf.set("lp.label.tmp", TMP_LABEL);

		Job job = new Job(conf, "label propagation");
		job.setJarByClass(LabelRun.class);

		job.setMapperClass(LabelMapper.class);
		job.setReducerClass(LabelReducer.class);

		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(IntWritable.class);

		job.setNumReduceTasks(1);

		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);

		FileInputFormat.setInputPaths(job, new Path(TMP_GRAPH));
		FileOutputFormat.setOutputPath(job, new Path(TMP_LABEL_SWAP));

		removeFolder(fs, TMP_LABEL_SWAP);
		job.waitForCompletion(true);

		swapFolder2File(fs, TMP_LABEL_SWAP, TMP_LABEL);
	}

	public static void main(String[] args) throws IOException,
			InterruptedException, ClassNotFoundException {
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);

		runInitJob(conf, fs);
		for (int i = 0; i < 1; i++)
			runLabelPropagation(conf, fs);
		fs.rename(new Path(TMP_LABEL), new Path(COMMUNITY));
	}
}
