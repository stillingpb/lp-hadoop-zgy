package v3.info;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class InitInfoSpreadReducer extends
		Reducer<LongWritable, LongWritable, LongWritable, Text> {

	public void setup(Context context) throws IOException, InterruptedException {
		beginVertex = context.getConfiguration().getLong(
				"info.init.beginvertex", 1);
	}

	private long beginVertex;
	/**
	 * 格式 "顶点出度\t到达时间"
	 */
	private Text outText = new Text();// 格式 out_v spread_round

	public void reduce(LongWritable key, Iterable<LongWritable> vertexs,
			Context context) throws IOException, InterruptedException {
		int outV = 0;
		for (LongWritable vertex : vertexs)
			if (vertex.get() != -1) // 如果是有效边
				outV++;
		if (beginVertex == key.get())
			outText.set(outV + "\t" + "0");
		else
			outText.set(outV + "\t" + "-1");
		context.write(key, outText);
	}

	public void cleanup(Context context) throws IOException,
			InterruptedException {
		// NOTHING
	}
}
