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
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat.Counter;

public class LabelMapper extends
		Mapper<LongWritable, Text, IntWritable, IntWritable> {

	private static String ROOT = "/lp";
	private static String GRAPH_PATH = ROOT + "/graph";
	private static String TMP = ROOT + "/tmp";
	private static String TMP_GRAPH = ROOT + "/graph.tmp";
	private static String TMP_GRAPH_SWAP = TMP + "/graph_swap";
	private static String TMP_LABEL = ROOT + "/label.tmp";
	private static String TMP_LABEL_SWAP = TMP + "/label_swap";
	private static String COMMUNITY = ROOT + "/community";

	private Map<Integer, Integer> allLabels = new HashMap<Integer, Integer>();// <vertex,label>

	private ILabelChoosen labelChoose; // 选择标签的算法很多，具体选哪种，有参数决定

	public void setup(Context context) throws IOException, InterruptedException {
		// 选择 标签选择时使用的算法
		String labelChooseMethod = context.getConfiguration().get(
				"lp.label.choose.method");
		// labelChooseMethod = "random";
		labelChooseMethod = "max";
		labelChoose = LabelChoosenFactory
				.createLabelChoosenFactory(labelChooseMethod);

		// 读取标签文件，将标签全部读到内存中
		String strPath = context.getConfiguration().get("lp.label.tmp");
		strPath = TMP_LABEL;
		Path labelPath = new Path(strPath);

		FileSystem fs = FileSystem.get(context.getConfiguration());
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

		// 选择出顶点新的标签
		int newLabel = labelChoose.chooseLabel(neighberLabels,
				getVertexLabel(outVertex.get()));
		outLabel.set(newLabel);
		context.write(outVertex, outLabel);

		org.apache.hadoop.mapreduce.Counter counter = context
				.getCounter(Counter.BYTES_READ);
		counter.increment(123);
	}

	private int getVertexLabel(int neig) {
		if (!allLabels.containsKey(neig))
			allLabels.put(neig, neig);
		return allLabels.get(neig);
	}

	/**
	 * Called once at the end of the task.
	 */
	public void cleanup(Context context) throws IOException,
			InterruptedException {
		// NOTHING
	}
}
