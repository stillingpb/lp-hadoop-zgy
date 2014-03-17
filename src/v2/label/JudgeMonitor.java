package v2.label;

import org.apache.hadoop.mapreduce.Counter;

/**
 * 决裁lp算法的迭代行为，是否停止
 * 
 * 
 */
public class JudgeMonitor {

	/**
	 * 标签传递算法停止的阈值
	 */
	private static final double STOP_THREAD = 0.99;

	/**
	 * 计数器group
	 */
	public static final String COUNTER_GROUP = "lp";

	/**
	 * vertexNum计数器的名称
	 */
	public static final String COUNTER_VERTEX_NUM = "vertex_num";

	/**
	 * graphEdgeNum计数器的名称
	 */
	public static final String COUNTER_EDGES = "graph_edges";

	/**
	 * lableChangedVertex计数器的名称
	 */
	public static final String COUNTER_VERTEX_LABEL_CHANGED = "vertex_label_changed";

	/**
	 * 统计图中顶点数量的计数器
	 */
	private Counter vertexNum;

	/**
	 * 图的边数
	 */
	private Counter graphEdgeNum;

	/**
	 * 统计当前标签传递中，有多少顶点的标签发生变化
	 */
	private Counter lableChangedVertex;

	/**
	 * 记录算法的迭代次数
	 */
	private int reverseTimes = 0;

	/**
	 * 判断lp迭代是否应该停止
	 * 
	 * @param lableChangedVertex
	 *            统计标签发生变化的顶点的计数器
	 * @return true 继续迭代 false 停止迭代
	 */
	public boolean judgeReverseStop() {
		long changed = lableChangedVertex.getValue();
		long sum = vertexNum.getValue();

		boolean continueReverse = 1.0 * changed / sum >= 1 - STOP_THREAD;
		String ifContinueReverse;
		if (continueReverse)
			ifContinueReverse = "continue lp Reverse";
		else
			ifContinueReverse = "stop lp Reverse";
		System.out.println("\nreverse Times > " + this.reverseTimes
				+ "\nstop thread > " + STOP_THREAD + "\t\tall label > " + sum
				+ "\t\tlabel changed > " + changed + "\n" + ifContinueReverse
				+ "------------------------\n");
		return continueReverse;
	}

	/**
	 * 增加迭代次数
	 * 
	 * @param added
	 */
	public void incrementReverseNum(int added) {
		this.reverseTimes += added;
	}

	/**
	 * 输出运行结果
	 */
	public void printResult() {
		System.out.println("处理结果：\n系统共处理节点 > " + this.vertexNum.getValue()
				+ "\n共处理边 > " + this.graphEdgeNum.getValue() + "\n迭代次数 > "
				+ this.reverseTimes + "\n");
	}

	public Counter getVertexNum() {
		return vertexNum;
	}

	public void setVertexNum(Counter vertexNum) {
		this.vertexNum = vertexNum;
	}

	public Counter getLableChangedVertex() {
		return lableChangedVertex;
	}

	public void setLableChangedVertex(Counter lableChangedVertex) {
		this.lableChangedVertex = lableChangedVertex;
	}

	public Counter getGraphEdgeNum() {
		return graphEdgeNum;
	}

	public void setGraphEdgeNum(Counter graphEdgeNum) {
		this.graphEdgeNum = graphEdgeNum;
	}
}
