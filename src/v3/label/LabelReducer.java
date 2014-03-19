package v3.label;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * do nothing
 * 
 */
public class LabelReducer extends Reducer<IntWritable, Text, IntWritable, Text> {

	public void reduce(IntWritable key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {
		for (Text value : values)
			context.write(key, value);
	}
}
