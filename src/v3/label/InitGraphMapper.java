package v3.label;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Mapper;

public class InitGraphMapper extends
		Mapper<LongWritable, Text, LongWritable, LongWritable> {
	public void setup(Context context) throws IOException, InterruptedException {
		graphEdgeNum = context.getCounter(JudgeMonitor.COUNTER_GROUP,
				JudgeMonitor.COUNTER_EDGES);
	}

	private Counter graphEdgeNum;

	private LongWritable outVertex1 = new LongWritable();
	private LongWritable outVertex2 = new LongWritable();

	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		StringTokenizer token = new StringTokenizer(value.toString());
		long v1 = Long.parseLong(token.nextToken());
		long v2 = Long.parseLong(token.nextToken());

		outVertex1.set(v1);
		outVertex2.set(v2);
		context.write(outVertex1, outVertex2);
		context.write(outVertex2, outVertex1);

		graphEdgeNum.increment(1);
	}

	public void cleanup(Context context) throws IOException,
			InterruptedException {
		// NOTHING
	}

}
