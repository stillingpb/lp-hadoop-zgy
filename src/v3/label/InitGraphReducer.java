package v3.label;

import java.io.IOException;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Reducer;

public class InitGraphReducer extends
		Reducer<LongWritable, LongWritable, LongWritable, Text> {
	private Counter vertexNum;

	public void setup(Context context) throws IOException, InterruptedException {
		vertexNum = context.getCounter(JudgeMonitor.COUNTER_GROUP,
				JudgeMonitor.COUNTER_VERTEX_NUM);
	}

	private Text outSides = new Text();

	public void reduce(LongWritable key, Iterable<LongWritable> values,
			Context context) throws IOException, InterruptedException {
		StringBuilder sb = new StringBuilder();
		for (LongWritable value : values) {
			sb.append(value.toString()).append('\t');
		}
		outSides.set(sb.toString());
		context.write(key, outSides);

		// 统计图中顶点数量
		vertexNum.increment(1);
	}

	public void cleanup(Context context) throws IOException,
			InterruptedException {
		// NOTHING
	}
}
