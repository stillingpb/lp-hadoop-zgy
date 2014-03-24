package v3.info;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class InfoRun {

	private static final String BASE = "/info";
	private static final String GRAPH_PATH = BASE + "/graph";

	private static final String TMP_GRAPH_PATH = BASE + "/graph.tmp";
	private static final String TMP_INFO_PATH = BASE + "/info.tmp";

	private static final String SWAP_PATH = BASE + "/tmp";
	private static final String GRAPH_SWAP = SWAP_PATH + "/graph";
	private static final String INFO_SWAP = SWAP_PATH + "/info";

	/**
	 * 系统运行的监听器
	 */
	private static RunMonitor monitor = new RunMonitor();

	/**
	 * 将hadoop中存有数据的文件夹存储到指定文件中
	 * 
	 * @param fs
	 * @param swapFolder
	 * @param file
	 * @throws IOException
	 */
	private static void swapFolder2File(FileSystem fs, String swapFolder,
			String file) throws IOException {
		String dataFile = swapFolder + "/part-r-00000";
		Path filePath = new Path(file);
		Path dataFilePath = new Path(dataFile);

		fs.delete(filePath);
		fs.rename(dataFilePath, filePath);
		fs.delete(new Path(swapFolder), true);
	}

	/**
	 * 初始化图，生出便于处理的图数据
	 * 
	 * @param conf
	 * @param fs
	 * @throws Exception
	 */
	private static void initGraph(Configuration conf, FileSystem fs)
			throws Exception {
		Job job = new Job(conf, "graph init");
		job.setJarByClass(InfoRun.class);

		job.setMapperClass(InitGraphMapper.class);
		job.setReducerClass(InitGraphReducer.class);

		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(IntWritable.class);

		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);

		FileInputFormat.setInputPaths(job, new Path(GRAPH_PATH));
		FileOutputFormat.setOutputPath(job, new Path(GRAPH_SWAP));

		job.waitForCompletion(true);

		swapFolder2File(fs, GRAPH_SWAP, TMP_GRAPH_PATH);

		// 设置顶点统计计数器
		Counter vertexNumCounter = job.getCounters().findCounter(
				RunMonitor.COUNTER_GROUP, RunMonitor.VERTEX_NUM_COUNTER);
		monitor.setVertexNumCounter(vertexNumCounter);

		// 设置边统计计数器
		Counter sideNumCounter = job.getCounters().findCounter(
				RunMonitor.COUNTER_GROUP, RunMonitor.SIDE_NUM_COUNTER);
		monitor.setSideNumCounter(sideNumCounter);
	}

	/**
	 * 初始化信息传播的状态文件
	 * 
	 * @param conf
	 * @param fs
	 * @throws Exception
	 */
	private static void initInfoSpread(Configuration conf, FileSystem fs)
			throws Exception {
		conf.setInt("info.init.beginvertex",
				PropertyManager.BEGIN_SPREAD_VERTEX);

		Job job = new Job(conf, "info spread init");
		job.setJarByClass(InfoRun.class);

		job.setMapperClass(InitInfoSpreadMapper.class);
		job.setReducerClass(InitInfoSpreadReducer.class);

		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(IntWritable.class);

		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);

		FileInputFormat.setInputPaths(job, new Path(GRAPH_PATH));
		FileOutputFormat.setOutputPath(job, new Path(INFO_SWAP));

		job.waitForCompletion(true);

		swapFolder2File(fs, INFO_SWAP, TMP_INFO_PATH);
	}

	/**
	 * 进行信息传播
	 * 
	 * @param conf
	 * @param fs
	 * @param currentRound
	 * @throws Exception
	 */
	private static void spreadInfo(Configuration conf, FileSystem fs,
			int currentRound) throws Exception {
		conf.set("info.spread.infofile", TMP_INFO_PATH);
		conf.setInt("info.spread.currentround", currentRound);
		conf.setFloat("info.spread.alpha", PropertyManager.ALPHA_PARAM);
		conf.setEnum("info.spread.beta", PropertyManager.BETA_PARAM);

		Job job = new Job(conf, "info spread round " + currentRound);
		job.setJarByClass(InfoRun.class);

		job.setMapperClass(InfoMapper.class);
		job.setReducerClass(InfoReducer.class);

		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(InfoParam.class);

		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);

		FileInputFormat.setInputPaths(job, new Path(TMP_GRAPH_PATH));
		FileOutputFormat.setOutputPath(job, new Path(INFO_SWAP));

		job.waitForCompletion(true);

		swapFolder2File(fs, INFO_SWAP, TMP_INFO_PATH);

		// 设置本轮次信息传播统计计数器
		Counter spreadNumCounter = job.getCounters().findCounter(
				RunMonitor.COUNTER_GROUP, RunMonitor.SPREAD_NUM_COUNTER);
		monitor.setSpreadCounter(spreadNumCounter);
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);
		initGraph(conf, fs);
		initInfoSpread(conf, fs);

		for (int round = 1; round <= PropertyManager.END_SPREAD_ROUND; round++) {
			monitor.setCurrentRound(round);
			spreadInfo(conf, fs, round);
			monitor.printRoundInfo();
		}
		monitor.printRunInfo();
	}
}
