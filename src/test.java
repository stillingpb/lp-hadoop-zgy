import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class test {

	public static class TestMapper extends
			Mapper<LongWritable, Text, IntWritable, IntWritable> {

		public void setup(Context context) throws IOException,
				InterruptedException {
		}

		public void map(LongWritable key, Text vertexs, Context context)
				throws IOException, InterruptedException {
			Counter c = context.getCounter("test", "map");
			c.increment(1);

			context.write(new IntWritable((int) key.get()), new IntWritable(1));
		}

		/**
		 * Called once at the end of the task.
		 */
		public void cleanup(Context context) throws IOException,
				InterruptedException {
			// NOTHING
		}
	}

	public static class TestReducer extends
			Reducer<IntWritable, IntWritable, IntWritable, IntWritable> {

		public void setup(Context context) throws IOException,
				InterruptedException {
		}

		public void reduce(IntWritable key, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {
			Counter c = context.getCounter("test", "reduce");

			c.increment(2);

			for (IntWritable value : values)
				context.write(key, value);
		}

		/**
		 * Called once at the end of the task.
		 */
		public void cleanup(Context context) throws IOException,
				InterruptedException {
			// NOTHING
		}
	}

	public static void main(String[] args) throws Exception {
		Configuration conf = new Configuration();
		Job job = new Job(conf, "test");
		job.setJarByClass(test.class);

		job.setMapperClass(TestMapper.class);
		job.setReducerClass(TestReducer.class);

		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(IntWritable.class);

		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(Text.class);

		FileInputFormat.addInputPath(job, new Path("/lp/graph"));
		FileOutputFormat.setOutputPath(job, new Path("/out"));

		job.waitForCompletion(true);

		Counter mapC = job.getCounters().findCounter("test", "map");
		Counter reduceC = job.getCounters().findCounter("test", "reduce");

		System.out.println(mapC.getValue());
		System.out.println(reduceC.getValue());
	}
}