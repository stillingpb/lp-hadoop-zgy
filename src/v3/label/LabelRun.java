package v3.label;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import v3.label.OptimizeController.OptimizeState;

/**
 * 控制标签传递算法的运行
 * 
 */
public class LabelRun {

	// 标识程序运行中需要的文件的路径，包括输出文件，中间文件，结果文件
	private static final String ROOT = "/lp";
	public static final String GRAPH_PATH = ROOT + "/graph"; // 输入文件
	private static final String TMP = ROOT + "/tmp";
	private static final String TMP_GRAPH = ROOT + "/graph.tmp"; // graph中间文件
	private static final String TMP_GRAPH_SWAP = TMP + "/graph_swap";
	private static final String TMP_LABEL_OPTIMIZE = ROOT
			+ "/label.optimize.tmp"; // label 优化算法中间文件
	private static final String TMP_LABEL_SWAP = TMP + "/label_swap";
	// public static final String COMMUNITY = ROOT + "/community"; // 結果文件

	private static final JudgeMonitor judgeMonitor = new JudgeMonitor();// 控制系统的运行状态

	private static final OptimizeController optimizeController = new OptimizeController();

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

	private static void removeFolder(FileSystem fs, String folder)
			throws IOException {
		fs.delete(new Path(folder), true);
	}

	/**
	 * 初始化图，生成<v1,list(v2,v3...vi)>的串
	 * 
	 * @param conf
	 * @param fs
	 * @return
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ClassNotFoundException
	 */
	private static void runInitJob(Configuration conf, FileSystem fs)
			throws IOException, InterruptedException, ClassNotFoundException {

		// 第一个mr作业，初始化图
		Job job = new Job(conf, "graph init");
		job.setJarByClass(LabelRun.class);

		job.setMapperClass(InitGraphMapper.class);
		job.setReducerClass(InitGraphReducer.class);

		job.setMapOutputKeyClass(LongWritable.class);
		job.setMapOutputValueClass(LongWritable.class);

		job.setOutputKeyClass(LongWritable.class);
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

		// 获取统计图中顶点数量的计数器
		Counter vertexNumCounter = job.getCounters().findCounter(
				JudgeMonitor.COUNTER_GROUP, JudgeMonitor.COUNTER_VERTEX_NUM);
		judgeMonitor.setVertexNum(vertexNumCounter);

		// 获取统计图中边数量的计数器
		Counter graphEdgeNumCounter = job.getCounters().findCounter(
				JudgeMonitor.COUNTER_GROUP, JudgeMonitor.COUNTER_EDGES);
		judgeMonitor.setGraphEdgeNum(graphEdgeNumCounter);
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
		conf.set("lp.optimize.label.tmp", TMP_LABEL_OPTIMIZE);

		// 设置标签选择算法
		conf.set("lp.label.choose.method", PropertyManager.LABEL_CHOOSEN_METHOD);

		// 设置标签迭代是否异步进行
		conf.setBoolean("lp.label.asynchronous",
				PropertyManager.LABEL_PROPAGATION_ASYNCHRONOUS);

		// 设置lp算法优化相关参数
		setOptimizeProperty(conf);

		Job job = new Job(conf, "label propagation");
		job.setJarByClass(LabelRun.class);

		job.setMapperClass(LabelMapper.class);
		job.setReducerClass(LabelReducer.class);

		job.setMapOutputKeyClass(LongWritable.class);
		job.setMapOutputValueClass(Text.class);

		job.setNumReduceTasks(1);

		job.setOutputKeyClass(LongWritable.class);
		job.setOutputValueClass(Text.class);

		FileInputFormat.setInputPaths(job, new Path(TMP_GRAPH));
		FileOutputFormat.setOutputPath(job, new Path(TMP_LABEL_SWAP));

		removeFolder(fs, TMP_LABEL_SWAP);
		job.waitForCompletion(true);

		swapFolder2File(fs, TMP_LABEL_SWAP, TMP_LABEL_OPTIMIZE);

		// 获取统计标签发生变化的计数器
		Counter vertexLabelChangedCounter = job.getCounters().findCounter(
				JudgeMonitor.COUNTER_GROUP,
				JudgeMonitor.COUNTER_VERTEX_LABEL_CHANGED);
		judgeMonitor.setLableChangedVertex(vertexLabelChangedCounter);

		// 获取统计免去处理节点的计数器
		Counter optimizeVertexCounter = job.getCounters().findCounter(
				JudgeMonitor.COUNTER_GROUP,
				JudgeMonitor.COUNTER_VERTEX_OPTIMIZE);
		judgeMonitor.setOptimizeVertexCounter(optimizeVertexCounter);
	}

	/**
	 * 设置lp算法优化需要的参数
	 * 
	 * @param conf
	 */
	private static void setOptimizeProperty(Configuration conf) {
		// 设置是否开启lp算法优化
		conf.setBoolean("lp.optimize.shutdown",
				PropertyManager.LPA_OPTIMIZE_SHUTDOWN);
		if (PropertyManager.LPA_OPTIMIZE_SHUTDOWN == true)
			return;
		// // 设置标签迭代优化的上限
		// conf.setFloat("lp.optimize.limit.upper",
		// PropertyManager.LPA_OPTIMIZE_UPPER_LIMIT);
		// // 设置标签迭代优化的下限
		// conf.setFloat("lp.optimize.limit.lower",
		// PropertyManager.LPA_OPTIMIZE_LOWER_LIMIT);

		double unchangedPercent = judgeMonitor.getUnchangedPercent();
		OptimizeState currentState = optimizeController.getState();
		OptimizeState newState = currentState;

		// 设置优化状态
		if (currentState == OptimizeState.NOT_IN_STATE) {
			double lowerLimit = PropertyManager.LPA_OPTIMIZE_LOWER_LIMIT;
			if (lowerLimit <= unchangedPercent)
				newState = OptimizeState.SET_STATE;
		} else if (currentState == OptimizeState.SET_STATE) {
			double upperLimit = PropertyManager.LPA_OPTIMIZE_UPPER_LIMIT;
			if (upperLimit <= unchangedPercent)
				newState = OptimizeState.OPTIMIZE_STATE;
		}
		optimizeController.setState(newState);
		conf.setEnum("lp.optimize.state", newState);
	}

	public static void main(String[] args) throws IOException,
			InterruptedException, ClassNotFoundException {
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);

		runInitJob(conf, fs);

		boolean continueReverse;
		do {
			judgeMonitor.incrementReverseNum(1);

			runLabelPropagation(conf, fs); // 运行lp算法

			judgeMonitor.printOneReverseEffect(optimizeController.getState());
			continueReverse = judgeMonitor.judgeReverseStop();
		} while (continueReverse);

		// fs.rename(new Path(TMP_LABEL), new Path(COMMUNITY));

		judgeMonitor.printResult();
	}
}
