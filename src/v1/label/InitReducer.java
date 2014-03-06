package v1.label;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class InitReducer extends
		Reducer<IntWritable, IntWritable, IntWritable, Text> {
	public void setup(Context context) throws IOException, InterruptedException {
		// NOTHING
	}

	private Text outSides = new Text();

	public void reduce(IntWritable key, Iterable<IntWritable> values,
			Context context) throws IOException, InterruptedException {
		String str = "";
		for (IntWritable value : values) {
			str += value.toString() + "\t";
		}
		outSides.set(str);
		context.write(key, outSides);
	}

	public void cleanup(Context context) throws IOException,
			InterruptedException {
		// NOTHING
	}
}
