package v3.label;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Mapper;

import v3.label.OptimizeController.OptimizeState;

/**
 * the main worker of lp algorithm
 * 
 */
public class LabelMapper extends Mapper<LongWritable, Text, IntWritable, Text> {

	/**
	 * 所有顶点的标签，只能被getVertex()方法使用
	 */
	private Map<Integer, Integer> allLabels = new HashMap<Integer, Integer>();// <vertex,label>
	private Map<Integer, Integer> labelState = new HashMap<Integer, Integer>();// <vertex,state>

	private ILabelChoosen labelChoose; // 选择标签的算法很多，具体选哪种，由参数决定

	private boolean asynchronousLabelChange;// 是否采用异步方式改变标签

	private Counter lableChangedVertex; // 标签变化节点的计数器
	private Counter optimizeVertexCounter; // 节点优化计数器

	private boolean optimizeShutdown; // 优化状态是否关闭
	private OptimizeState optimizeState; // 优化状态

	private IntWritable outVertex = new IntWritable();
	private Text outLabelAndState = new Text(); // <vertex,label,state>
	private static final int CHANGE_STATE = 1; // 标签发生变化
	private static final int UNCHANGE_STATE = 0; // 标签未发生变化

	public void setup(Context context) throws IOException, InterruptedException {
		Configuration conf = context.getConfiguration();
		// 进行标签选择时使用的算法
		String labelChooseMethod = conf.get("lp.label.choose.method");
		labelChoose = LabelChoosenFactory
				.createLabelChoosenFactory(labelChooseMethod);

		// 获取标签改变的方式，是异步还是同步
		asynchronousLabelChange = conf.getBoolean("lp.label.asynchronous",
				false);

		optimizeShutdown = conf.getBoolean("lp.optimize.shutdown", true);
		optimizeState = conf.getEnum("lp.optimize.state",
				OptimizeState.NOT_IN_STATE); // 获取当前算法所处于的优化状态

		// 读取标签文件，将标签全部读到内存中
		String strPath = conf.get("lp.optimize.label.tmp");
		Path labelPath = new Path(strPath);
		FileSystem fs = FileSystem.get(context.getConfiguration());
		if (fs.exists(labelPath)) { // 如果label文件存在,就把记录的标签读进来
			FSDataInputStream inStream = fs.open(labelPath);
			String str;
			while ((str = inStream.readLine()) != null) {
				StringTokenizer token = new StringTokenizer(str);
				int vertex = Integer.parseInt(token.nextToken());
				int label = Integer.parseInt(token.nextToken());
				int state = Integer.parseInt(token.nextToken());
				allLabels.put(vertex, label);
				labelState.put(vertex, state);
			}
			inStream.close();
		}
		// 设定统计顶点标签变化的计数器
		lableChangedVertex = context.getCounter(JudgeMonitor.COUNTER_GROUP,
				JudgeMonitor.COUNTER_VERTEX_LABEL_CHANGED);
		optimizeVertexCounter = context.getCounter(JudgeMonitor.COUNTER_GROUP,
				JudgeMonitor.COUNTER_VERTEX_OPTIMIZE);
	}

	/**
	 * @param vertexs
	 *            顶点和它的所有邻居节点
	 */
	public void map(LongWritable key, Text vertexs, Context context)
			throws IOException, InterruptedException {

		// 如果优化状态关闭，将不做任何优化处理
		if (optimizeShutdown) {
			notInStateMap(key, vertexs, context);
			return;
		}

		// 不同的状态用不同的map来处理
		else if (optimizeState == OptimizeState.NOT_IN_STATE)
			notInStateMap(key, vertexs, context);
		else if (optimizeState == OptimizeState.SET_STATE)
			setStateMap(key, vertexs, context);
		else if (optimizeState == OptimizeState.OPTIMIZE_STATE)
			optimizeSateMap(key, vertexs, context);
	}

	private void optimizeSateMap(LongWritable key, Text vertexs, Context context)
			throws IOException, InterruptedException {

		StringTokenizer token = new StringTokenizer(vertexs.toString());

		// 优化迭代，未发生标签变化的节点不需要更新标签了
		int vertex = Integer.parseInt(token.nextToken());
		outVertex.set(vertex);
		if (labelState.get(vertex) == UNCHANGE_STATE) {
			optimizeVertexCounter.increment(1);
			outLabelAndState
					.set(getVertexLabel(vertex) + "\t" + UNCHANGE_STATE); // 能被处理的节点，都是被标识为改变过的标签
			context.write(outVertex, outLabelAndState);
			return;
		}

		int oldLabel = getVertexLabel(outVertex.get());
		Map<Integer, Integer> neighberLabels = getNeighberLabels(token); // 获取邻居节点的标签
		int newLabel = labelChoose.chooseLabel(neighberLabels,
				getVertexLabel(outVertex.get())); // 选择出顶点新的标签

		// 修改顶点的标签
		asynchronizedLabelChange(outVertex.get(), newLabel);

		if (oldLabel != newLabel) { // 如果标签发生了变化
			lableChangedVertex.increment(1);
		}

		outLabelAndState.set(newLabel + "\t" + CHANGE_STATE); // 能被处理的节点，都是被标识为改变过的标签
		context.write(outVertex, outLabelAndState);
	}

	private void setStateMap(LongWritable key, Text vertexs, Context context)
			throws IOException, InterruptedException {
		StringTokenizer token = new StringTokenizer(vertexs.toString());
		outVertex.set(Integer.parseInt(token.nextToken())); // 获取顶点

		int oldLabel = getVertexLabel(outVertex.get());
		Map<Integer, Integer> neighberLabels = getNeighberLabels(token); // 获取邻居节点的标签
		int newLabel = labelChoose.chooseLabel(neighberLabels,
				getVertexLabel(outVertex.get())); // 选择出顶点新的标签

		// 修改顶点的标签
		asynchronizedLabelChange(outVertex.get(), newLabel);

		if (oldLabel != newLabel) { // 如果标签发生了变化
			lableChangedVertex.increment(1);
			outLabelAndState.set(newLabel + "\t" + CHANGE_STATE);
		} else {
			outLabelAndState.set(newLabel + "\t" + UNCHANGE_STATE);
		}
		context.write(outVertex, outLabelAndState);
	}

	private void notInStateMap(LongWritable key, Text vertexs, Context context)
			throws IOException, InterruptedException {
		StringTokenizer token = new StringTokenizer(vertexs.toString());
		outVertex.set(Integer.parseInt(token.nextToken())); // 获取顶点

		int oldLabel = getVertexLabel(outVertex.get());
		Map<Integer, Integer> neighberLabels = getNeighberLabels(token); // 获取邻居节点的标签
		int newLabel = labelChoose.chooseLabel(neighberLabels,
				getVertexLabel(outVertex.get())); // 选择出顶点新的标签

		// 修改顶点的标签
		asynchronizedLabelChange(outVertex.get(), newLabel);

		// 如果标签发生了变化
		if (oldLabel != newLabel)
			lableChangedVertex.increment(1);

		outLabelAndState.set(newLabel + "\t" + UNCHANGE_STATE);
		context.write(outVertex, outLabelAndState);
	}

	/**
	 * 获得邻居节点, <label,appealTimes>
	 * 
	 * @param token
	 * @return
	 */
	private Map<Integer, Integer> getNeighberLabels(StringTokenizer token) {
		// 将顶点的所有邻居节点遍历出来，放到map中
		Map<Integer, Integer> neighberLabels = new HashMap<Integer, Integer>(); // <label,appealTimes>
		neighberLabels.put(outVertex.get(), 1);
		while (token.hasMoreTokens()) {
			int neig = Integer.parseInt(token.nextToken()); // 获取邻居节点
			int label = getVertexLabel(neig); // 获取邻居节点的标签

			// 统计标签出现过的次数
			if (neighberLabels.containsKey(label))
				neighberLabels.put(label, neighberLabels.get(label) + 1);
			else
				neighberLabels.put(label, 1);
		}
		return neighberLabels;
	}

	/**
	 * 异步变化顶点的标签
	 * 
	 * @param vertex
	 * @param newLabel
	 */
	private void asynchronizedLabelChange(int vertex, int newLabel) {
		if (asynchronousLabelChange) // 如果需要异步更新顶点的标签
			allLabels.put(vertex, newLabel);
	}

	/**
	 * 获取节点的标签
	 * 
	 * @param vertex
	 * @return label
	 */
	private int getVertexLabel(int vertex) {
		if (!allLabels.containsKey(vertex)) {
			allLabels.put(vertex, vertex);
			labelState.put(vertex, UNCHANGE_STATE);
		}
		return allLabels.get(vertex);
	}

	/**
	 * Called once at the end of the task.
	 */
	public void cleanup(Context context) throws IOException,
			InterruptedException {
		// NOTHING
	}
}
