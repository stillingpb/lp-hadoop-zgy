package v1.label;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

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

	private Map<Integer, Integer> allLabels = new HashMap<Integer, Integer>();// <side,label>

	public void setup(Context context) throws IOException, InterruptedException {
		String strPath = context.getConfiguration().get("lp.label.tmp");
		strPath = TMP_LABEL;
		Path labelPath = new Path(strPath);

		FileSystem fs = FileSystem.get(context.getConfiguration());
		FSDataInputStream inStream = fs.open(labelPath);
		String str = inStream.readLine();
		while (str != null) {
			StringTokenizer token = new StringTokenizer(str);
			int side = Integer.parseInt(token.nextToken());
			int label = Integer.parseInt(token.nextToken());
			allLabels.put(side, label);
			str = inStream.readLine();
		}
		inStream.close();
	}

	private IntWritable outSide = new IntWritable();
	private IntWritable outLabel = new IntWritable(); // <side,label>

	public void map(LongWritable key, Text sides, Context context)
			throws IOException, InterruptedException {

		StringTokenizer token = new StringTokenizer(sides.toString());
		outSide.set(Integer.parseInt(token.nextToken())); // 获取边

		Map<Integer, Integer> neighberLables = new HashMap<Integer, Integer>(); // <label,sum>
		neighberLables.put(outSide.get(), 1);
		while (token.hasMoreTokens()) {
			int neig = Integer.parseInt(token.nextToken());
			int label = allLabels.get(neig);
			if (neighberLables.containsKey(label)) {
				neighberLables.put(label, neighberLables.get(label) + 1);
			} else {
				neighberLables.put(label, 1);
			}
		}

		int maxLabelSum = 1;
		int maxLabel = allLabels.get(outSide.get());
		for (Entry<Integer, Integer> entry : neighberLables.entrySet()) {
			if (maxLabelSum < entry.getValue()) {
				maxLabelSum = entry.getValue();
				maxLabel = entry.getKey();
			} else if (maxLabelSum == entry.getValue()) {
				if (maxLabel < entry.getKey())
					maxLabel = entry.getKey();
			}
		}

		outLabel.set(maxLabel);
		context.write(outSide, outLabel);
	}

	/**
	 * Called once at the end of the task.
	 */
	public void cleanup(Context context) throws IOException,
			InterruptedException {
		// NOTHING
	}
}
