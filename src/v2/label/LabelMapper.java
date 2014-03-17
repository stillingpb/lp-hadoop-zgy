package v2.label;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * the main worker of lp algorithm
 * 
 */
public class LabelMapper extends
		Mapper<LongWritable, Text, IntWritable, IntWritable> {

	/**
	 * 所有顶点的标签，只能被getVertex()方法使用
	 */
	private Map<Integer, Integer> allLabels = new HashMap<Integer, Integer>();// <vertex,label>

	private ILabelChoosen labelChoose; // 选择标签的算法很多，具体选哪种，由参数决定

	private boolean asynchronousLabelChange;// 是否采用异步方式改变标签

	private Counter lableChangedVertex;

	public void setup(Context context) throws IOException, InterruptedException {
		// 进行标签选择时使用的算法
		String labelChooseMethod = context.getConfiguration().get(
				"lp.label.choose.method");
		labelChoose = LabelChoosenFactory
				.createLabelChoosenFactory(labelChooseMethod);

		// 获取标签改变的方式，是异步还是同步
		asynchronousLabelChange = context.getConfiguration().getBoolean(
				"lp.label.asynchronous", false);

		// 读取标签文件，将标签全部读到内存中
		String strPath = context.getConfiguration().get("lp.label.tmp");
		Path labelPath = new Path(strPath);
		FileSystem fs = FileSystem.get(context.getConfiguration());
		if (fs.exists(labelPath)) { // 如果label文件存在,就把记录的标签读进来
			FSDataInputStream inStream = fs.open(labelPath);
			String str = inStream.readLine();
			while (str != null) {
				StringTokenizer token = new StringTokenizer(str);
				int vertex = Integer.parseInt(token.nextToken());
				int label = Integer.parseInt(token.nextToken());
				allLabels.put(vertex, label);
				str = inStream.readLine();
			}
			inStream.close();
		}

		// 设定统计顶点标签变化的计数器
		lableChangedVertex = context.getCounter(JudgeMonitor.COUNTER_GROUP,
				JudgeMonitor.COUNTER_VERTEX_LABEL_CHANGED);
	}

	private IntWritable outVertex = new IntWritable();
	private IntWritable outLabel = new IntWritable(); // <vertex,label>

	/**
	 * @param vertexs
	 *            顶点和它的所有邻居节点
	 */
	public void map(LongWritable key, Text vertexs, Context context)
			throws IOException, InterruptedException {

		StringTokenizer token = new StringTokenizer(vertexs.toString());
		outVertex.set(Integer.parseInt(token.nextToken())); // 获取顶点

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

		int oldLabel = getVertexLabel(outVertex.get());

		// 选择出顶点新的标签
		int newLabel = labelChoose.chooseLabel(neighberLabels,
				getVertexLabel(outVertex.get()));
		outLabel.set(newLabel);
		context.write(outVertex, outLabel);

		// 修改顶点的标签
		asynchronizedLabelChange(outVertex.get(), newLabel);

		// 如果标签发生了变化
		if (oldLabel != newLabel)
			lableChangedVertex.increment(1);
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

	private int getVertexLabel(int vertex) {
		if (!allLabels.containsKey(vertex))
			allLabels.put(vertex, vertex);
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
