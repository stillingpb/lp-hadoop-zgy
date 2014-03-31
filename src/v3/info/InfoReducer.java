package v3.info;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class InfoReducer extends
		Reducer<LongWritable, InfoParam, LongWritable, Text> {

	public void setup(Context context) throws IOException, InterruptedException {
//		Configuration conf = context.getConfiguration();
//		currentRound = conf.getInt("info.spread.currentround", 1);
	}

	// private int currentRound;
	/**
	 * 格式 "顶点出度\t信息到达轮次"
	 */
	private Text outText = new Text();

	public void reduce(LongWritable key, Iterable<InfoParam> params,
			Context context) throws IOException, InterruptedException {
		boolean firstTime = true;
		for (InfoParam p : params) {
			int spreadRound = p.getSpreadRound();
			if (spreadRound != -1) {
				outText.set(p.getOutV() + "\t" + p.getSpreadRound());
				context.write(key, outText);
				return;
			} else if (firstTime) {
				outText.set(p.getOutV() + "\t" + "-1");
				firstTime = false;
			}
		}
		context.write(key, outText);
	}

	public void cleanup(Context context) throws IOException,
			InterruptedException {
		// NOTHING
	}
}
