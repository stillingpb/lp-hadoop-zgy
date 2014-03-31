package v3.info;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Counter;
import org.apache.hadoop.mapreduce.Mapper;

public class InfoMapper extends
		Mapper<LongWritable, Text, LongWritable, InfoParam> {

	public void setup(Context context) throws IOException, InterruptedException {
		Configuration conf = context.getConfiguration();
		currentRound = conf.getInt("info.spread.currentround", 1);
		alpha = conf.getFloat("info.spread.alpha", 0.3f);
		beta = conf.getEnum("info.spread.beta", BetaParam.TOP_OUT);

		spreadCounter = context.getCounter(RunMonitor.COUNTER_GROUP,
				RunMonitor.SPREAD_NUM_COUNTER);

		String infoPath = conf.get("info.spread.infofile");
		FileSystem fs = FileSystem.get(conf);
		FSDataInputStream in = fs.open(new Path(infoPath));
		String line;
		while ((line = in.readLine()) != null) {
			StringTokenizer token = new StringTokenizer(line);
			long vertex = Long.parseLong(token.nextToken());
			int outV = Integer.parseInt(token.nextToken());
			int spreadRound = Integer.parseInt(token.nextToken());
			InfoParam infoPara = new InfoParam(vertex, outV, spreadRound);
			infoData.put(vertex, infoPara);
		}
		in.close();
	}

	private Map<Long, InfoParam> infoData = new HashMap<Long, InfoParam>();

	private float alpha;
	private BetaParam beta;
	private int currentRound;

	private Counter spreadCounter;

	private LongWritable outVertex1 = new LongWritable();
	private LongWritable outVertex2 = new LongWritable();

	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {
		StringTokenizer token = new StringTokenizer(value.toString());
		long v1 = Long.parseLong(token.nextToken());
		InfoParam param1 = infoData.get(v1);
		outVertex1.set(v1);
		context.write(outVertex1, param1);

		// 如果当前顶点不是上一轮收到信息的顶点，就不对它的出度顶点做处理
		if (currentRound - 1 != param1.getSpreadRound())
			return;

		// 选出出度顶点，传播信息给它们
		List<Long> neigV = new ArrayList<Long>();
		while (token.hasMoreTokens()) {
			long v2 = Long.parseLong(token.nextToken());
			neigV.add(v2);
		}

		// 选择不同的信息传播策略
		if (beta == BetaParam.TOP_OUT) {
			topStrategyMap(context, neigV);
		} else if (beta == BetaParam.TAIL_OUT) {
			tailStrategyMap(context, neigV);
		} else if (beta == BetaParam.RANDOM_OUT) {
			RandomStrategyMap(context, neigV);
		}
	}

	/**
	 * 选取出度最大的一些顶点进行信息传递
	 * 
	 * @param context
	 * @param neigV
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void topStrategyMap(Context context, List<Long> neigV)
			throws IOException, InterruptedException {
		int len = neigV.size();
		int num = (int) (alpha * len);
		if (len == 0 || num == 0)
			return;

		InfoParam[] list = new InfoParam[len];
		int i = 0;
		for (Long v : neigV) {
			InfoParam param = infoData.get(v);
			list[i++] = param;
		}
		Arrays.sort(list);// 按顶点的出度升序排序
		for (int j = 0; j < num; j++) {
			InfoParam param = list[len - j - 1];
			outVertex2.set(param.getVertex());
			outputSpreadVertex(context, outVertex2, param);
		}
	}

	/**
	 * 选取出度最小的一些顶点进行信息传递
	 * 
	 * @param context
	 * @param neigV
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void tailStrategyMap(Context context, List<Long> neigV)
			throws IOException, InterruptedException {
		int len = neigV.size();
		int num = (int) (alpha * len);
		if (len == 0 || num == 0)
			return;

		InfoParam[] list = new InfoParam[len];
		for (int i = 0; i < len; i++) {
			long v = neigV.get(i);
			InfoParam param = infoData.get(v);
			list[i] = param;
		}
		Arrays.sort(list);// 按顶点的出度升序排序
		for (int j = 0; j < num; j++) {
			InfoParam param = list[j];
			outVertex2.set(param.getVertex());
			outputSpreadVertex(context, outVertex2, param);
		}
	}

	/**
	 * 随机选取一些顶点进行信息传递
	 * 
	 * @param context
	 * @param neigV
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void RandomStrategyMap(Context context, List<Long> neigV)
			throws IOException, InterruptedException {
		int len = neigV.size();
		int num = (int) (alpha * len);
		if (len == 0 || num == 0)
			return;

		int count = 0;
		Set<Integer> visited = new HashSet<Integer>(len);
		Random rand = new Random();
		do {
			int index = rand.nextInt(num);
			if (!visited.contains(index)) {
				count++;
				visited.add(index);
				InfoParam param = infoData.get(neigV.get(index));
				outVertex2.set(param.getVertex());
				outputSpreadVertex(context, outVertex2, param);
			}
		} while (count < num);
	}

	private void outputSpreadVertex(Context context, LongWritable v,
			InfoParam param) throws IOException, InterruptedException {
		// 信息已经在之前传递到了该顶点
		if (param.getSpreadRound() != -1)
			return;
		param.setSpreadRound(currentRound);
		context.write(v, param);

		spreadCounter.increment(1);
	}

	public void cleanup(Context context) throws IOException,
			InterruptedException {
		// NOTHING
	}

}
