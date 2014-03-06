package v1.label;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class InitMapper extends
		Mapper<LongWritable, Text, IntWritable, IntWritable> {
	public void setup(Context context) throws IOException, InterruptedException {
		// NOTHING
	}

	private IntWritable outSide1 = new IntWritable();
	private IntWritable outSide2 = new IntWritable();

	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		StringTokenizer token = new StringTokenizer(value.toString());
		int side1 = Integer.parseInt(token.nextToken());
		int side2 = Integer.parseInt(token.nextToken());

		outSide1.set(side1);
		outSide2.set(side2);
		context.write(outSide1, outSide2);
		context.write(outSide2, outSide1);
	}

	public void cleanup(Context context) throws IOException,
			InterruptedException {
		// NOTHING
	}

}
