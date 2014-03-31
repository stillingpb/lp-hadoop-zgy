package v3.info;

import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class InitGraphMapper extends
		Mapper<LongWritable, Text, LongWritable, LongWritable> {
	public void setup(Context context) throws IOException, InterruptedException {
		// NOTHING
	}

	LongWritable v1 = new LongWritable();
	LongWritable v2 = new LongWritable();
	LongWritable invalidV = new LongWritable(-1); // 无效的顶点

	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		StringTokenizer token = new StringTokenizer(value.toString());
		v1.set(Long.parseLong(token.nextToken()));
		v2.set(Long.parseLong(token.nextToken()));
		context.write(v1, v2);
		context.write(v2, invalidV);
	}

	public void cleanup(Context context) throws IOException,
			InterruptedException {
		// NOTHING
	}

}
