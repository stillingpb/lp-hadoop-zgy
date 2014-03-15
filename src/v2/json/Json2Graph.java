package v2.json;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

public class Json2Graph implements Runnable {
	private static String IN_PATH = "graph_in.json";
	private static String OUT_PATH = "graph_out";
	private BufferedReader br;
	private BufferedWriter bw;

	private static String EDGE_SPLIT = "edges"; // 标识json串开始读取边的位置
	private static Set<String> edges = new HashSet<String>();// 存储边:<v1,v2>,存储形式为"v1\tv2\n"

	@Override
	public void run() {
		String str;
		// 寻找到记录边的那一行
		while ((str = nextLine()) != null)
			if (str.indexOf(EDGE_SPLIT) != -1)
				break;

		// 处理边
		while (true) {
			boolean stop = parseEdgesOfVertex();
			if (stop)
				break;
		}

		// 输出边，关闭打开的文件
		outputEdges();
		close();
	}

	/**
	 * 输出边
	 */
	private void outputEdges() {
		try {
			bw = new BufferedWriter(new FileWriter(OUT_PATH));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			for (String edge : edges)
				bw.write(edge);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 处理一个顶点的边
	 * 
	 * @return stop 是否还可以继续读取数据
	 */
	private boolean parseEdgesOfVertex() {
		String str = nextLine();
		int beginIndex = str.indexOf('"');
		if (beginIndex == -1)
			return true;
		int endIndex = str.indexOf('"', beginIndex + 1);
		String v1 = str.substring(beginIndex + 1, endIndex);

		while (true) {
			str = nextLine();
			beginIndex = str.indexOf('"');
			if (beginIndex == -1)
				break;
			endIndex = str.indexOf('"', beginIndex + 1);
			String v2 = str.substring(beginIndex + 1, endIndex);
			if (v1.compareTo(v2) > 0)
				edges.add(v1 + "\t" + v2 + "\n");
			else
				edges.add(v2 + "\t" + v1 + "\n");
		}
		return false;
	}

	private void close() {
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String nextLine() {
		if (br == null) {
			try {
				br = new BufferedReader(new FileReader(IN_PATH));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}
		}
		try {
			return br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) {
		new Thread(new Json2Graph()).start();
	}
}
