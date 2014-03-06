package v1.label;

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

	private static void runInitJob(Configuration conf, FileSystem fs)
			throws IOException, InterruptedException, ClassNotFoundException {
		Job job = new Job(conf, "graph init");
		job.setJarByClass(LabelRun.class);

		job.setMapperClass(InitMapper.class);
		job.setReducerClass(InitReducer.class);

		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(IntWritable.class);

		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);

		// FileInputFormat.setInputPaths(job, inputPaths);
		FileInputFormat.setInputPaths(job, new Path(GRAPH_PATH));
		FileOutputFormat.setOutputPath(job, new Path(TMP_GRAPH_SWAP));

		job.waitForCompletion(true);

		swapFolder2File(fs, TMP_GRAPH_SWAP, TMP_GRAPH);
	}

	private static void runLabelPropagation(Configuration conf, FileSystem fs)
			throws IOException, InterruptedException, ClassNotFoundException {
		conf.set("lp.label.tmp", TMP_LABEL);

		Job job = new Job(conf, "label propagation");
		job.setJarByClass(LabelRun.class);

		job.setMapperClass(LabelMapper.class);
		job.setReducerClass(LabelReducer.class);

		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(IntWritable.class);

		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);

		FileInputFormat.setInputPaths(job, new Path(TMP_GRAPH));
		FileOutputFormat.setOutputPath(job, new Path(TMP_LABEL_SWAP));

		job.waitForCompletion(true);

		swapFolder2File(fs, TMP_LABEL_SWAP, TMP_LABEL);
	}

	public static void main(String[] args) throws IOException,
			InterruptedException, ClassNotFoundException {
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);
		runInitJob(conf, fs);
		for (int i = 0; i < 4; i++)
			runLabelPropagation(conf, fs);
		fs.rename(new Path(TMP_LABEL), new Path(COMMUNITY));
	}
}
