package v2.label;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class InitGraphReducer extends
		Reducer<IntWritable, IntWritable, IntWritable, Text> {
	public void setup(Context context) throws IOException, InterruptedException {
		// NOTHING
	}

	private Text outSides = new Text();

	public void reduce(IntWritable key, Iterable<IntWritable> values,
			Context context) throws IOException, InterruptedException {
		StringBuilder sb = new StringBuilder();
		for (IntWritable value : values) {
			sb.append(value.toString()).append('\t');
		}
		outSides.set(sb.toString());
		context.write(key, outSides);
	}

	public void cleanup(Context context) throws IOException,
			InterruptedException {
		// NOTHING
	}
}
