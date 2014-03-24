package v3.info;

import org.apache.hadoop.mapreduce.Counter;

/**
 * 程序运行的监听器
 */
public class RunMonitor {
	/**
	 * 计数器group名称
	 */
	public static String COUNTER_GROUP = "info";

	/**
	 * spreadCounter的名称
	 */
	public static String SPREAD_NUM_COUNTER = "spreadnum";

	/**
	 * vertexNumCounter的名称
	 */
	public static String VERTEX_NUM_COUNTER = "vertexnum";

	/**
	 * sideNumCounter的名称
	 */
	public static String SIDE_NUM_COUNTER = "sidenum";
	/**
	 * 顶点数量的计数器
	 */
	private Counter vertexNumCounter;

	/**
	 * 边数量的计数器
	 */
	private Counter sideNumCounter;

	/**
	 * 记录一轮信息传播传播到的顶点数量
	 */
	private Counter spreadCounter;

	/**
	 * 累计收到信息的顶点计数
	 */
	private int receivedVertex;

	/**
	 * 当前信息传递轮次
	 */
	private int currentRound;

	public Counter getSpreadCounter() {
		return spreadCounter;
	}

	public void setSpreadCounter(Counter spreadCounter) {
		this.spreadCounter = spreadCounter;
		this.receivedVertex += spreadCounter.getValue();
	}

	/**
	 * 打印每轮次的信息
	 */
	public void printRoundInfo() {
		System.out.println();
		System.out.println("信息传播轮次           " + currentRound);
		System.out.println("本轮新的收到信息的顶点  " + spreadCounter.getValue());
		System.out.println("累计收到信息的顶点      " + receivedVertex);
		System.out.println();
	}

	/**
	 * 打印系统运行统计信息
	 */
	public void printRunInfo() {
		System.out.println();
		System.out.println("图顶点数 " + vertexNumCounter.getValue() + "\t图边数 "
				+ sideNumCounter.getValue() + "\t信息起始顶点 "
				+ PropertyManager.BEGIN_SPREAD_VERTEX);
		System.out.println("alpha " + PropertyManager.ALPHA_PARAM + "\tbeta "
				+ PropertyManager.BETA_PARAM);
		System.out.println("信息传播轮次       " + currentRound);
		System.out.println("累计收到信息的顶点  " + receivedVertex);
		System.out.println();
	}

	public int getCurrentRound() {
		return currentRound;
	}

	public void setCurrentRound(int currentRound) {
		this.currentRound = currentRound;
	}

	public Counter getVertexNumCounter() {
		return vertexNumCounter;
	}

	public void setVertexNumCounter(Counter vertexNumCounter) {
		this.vertexNumCounter = vertexNumCounter;
	}

	public Counter getSideNumCounter() {
		return sideNumCounter;
	}

	public void setSideNumCounter(Counter sideNumCounter) {
		this.sideNumCounter = sideNumCounter;
	}

}
