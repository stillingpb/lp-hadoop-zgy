package v3.info;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Reducer;

public class InitGraphReducer extends
		Reducer<LongWritable, LongWritable, LongWritable, Text> {

	public void setup(Context context) throws IOException, InterruptedException {
		vertexNumCounter = context.getCounter(RunMonitor.COUNTER_GROUP,
				RunMonitor.VERTEX_NUM_COUNTER);
		sideNumCounter = context.getCounter(RunMonitor.COUNTER_GROUP,
				RunMonitor.SIDE_NUM_COUNTER);
	}

	private Counter vertexNumCounter;
	private Counter sideNumCounter;

	private Text outText = new Text();// 格式 "v1\tv2.."

	public void reduce(LongWritable key, Iterable<LongWritable> vertexs,
			Context context) throws IOException, InterruptedException {
		StringBuilder sb = new StringBuilder();
		for (LongWritable vertex : vertexs)
			if (vertex.get() != -1) { // 如果是有效边
				sb.append(vertex.get()).append('\t');
				sideNumCounter.increment(1);
			}
		outText.set(sb.toString());
		context.write(key, outText);

		vertexNumCounter.increment(1);
	}

	public void cleanup(Context context) throws IOException,
			InterruptedException {
		// NOTHING
	}
}
