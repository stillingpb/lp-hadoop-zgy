package v2.label;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class InitGraphMapper extends
		Mapper<LongWritable, Text, IntWritable, IntWritable> {
	public void setup(Context context) throws IOException, InterruptedException {
		// NOTHING
	}

	private IntWritable outVertex1 = new IntWritable();
	private IntWritable outVertex2 = new IntWritable();

	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		StringTokenizer token = new StringTokenizer(value.toString());
		int v1 = Integer.parseInt(token.nextToken());
		int v2 = Integer.parseInt(token.nextToken());

		outVertex1.set(v1);
		outVertex2.set(v2);
		context.write(outVertex1, outVertex2);
		context.write(outVertex2, outVertex1);
	}

	public void cleanup(Context context) throws IOException,
			InterruptedException {
		// NOTHING
	}

}
