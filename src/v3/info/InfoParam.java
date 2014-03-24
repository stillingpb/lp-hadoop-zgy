package v3.info;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.Writable;

/**
 * 节点的参数类
 * 
 */
public class InfoParam implements Writable, Comparable<InfoParam> {
	private int vertex; // 节点
	private int outV; // 出度
	private int spreadRound; // 传递轮次 未传递到，标识为-1

	public InfoParam() {
	}

	public InfoParam(int vertex, int outV, int spreadRound) {
		this.vertex = vertex;
		this.outV = outV;
		this.spreadRound = spreadRound;
	}

	@Override
	public void readFields(DataInput in) throws IOException {
		vertex = in.readInt();
		outV = in.readInt();
		spreadRound = in.readInt();
	}

	@Override
	public void write(DataOutput out) throws IOException {
		out.writeInt(vertex);
		out.writeInt(outV);
		out.writeInt(spreadRound);
	}

	public int getVertex() {
		return vertex;
	}

	public void setVertex(int vertex) {
		this.vertex = vertex;
	}

	public int getOutV() {
		return outV;
	}

	public void setOutV(int outV) {
		this.outV = outV;
	}

	public int getSpreadRound() {
		return spreadRound;
	}

	public void setSpreadRound(int spreadRound) {
		this.spreadRound = spreadRound;
	}

	@Override
	public int compareTo(InfoParam arg0) {
		return this.outV - arg0.outV;
	}
}