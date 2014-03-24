package v3.info;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class InitInfoSpreadMapper extends
		Mapper<LongWritable, Text, IntWritable, IntWritable> {
	public void setup(Context context) throws IOException, InterruptedException {
		// NOTHING
	}

	IntWritable v1 = new IntWritable();
	IntWritable v2 = new IntWritable();
	IntWritable invalidV = new IntWritable(-1); //无效顶点

	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		StringTokenizer token = new StringTokenizer(value.toString());
		v1.set(Integer.valueOf(token.nextToken()));
		v2.set(Integer.valueOf(token.nextToken()));
		context.write(v1, v2);
		context.write(v2, invalidV);
	}

	public void cleanup(Context context) throws IOException,
			InterruptedException {
		// NOTHING
	}

}
