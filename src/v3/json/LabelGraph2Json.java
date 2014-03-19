package v3.json;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 解析图为json串 图的格式： 每一行："id1制表符id2",例如: "1\t5"
 * 
 */
public class LabelGraph2Json implements Runnable {
	private static final String GRAPH_PATH = "graph"; // 输入图路径
	private static final String LABEL_PATH = "label"; // 输入标签路径
	private static final String JSON_PATH = "labelGraph.json"; // 输出路径
	private static final char GRAPH_DELIMITER = '\t'; // graph文件中的分割字符
	private static final char LABEL_DELIMITER = '\t'; // label文件中的分割字符

	private int indentNum = 0; // 控制缩进数目

	private Map<String, Node> nodes = new HashMap<String, Node>();
	private StringBuilder sb = new StringBuilder(); // 生成的json串

	@Override
	public void run() {

		parseGraph();
		parseLabel();

		// 生成json串
		sb.append("{\n");
		produceNodes();
		sb.append(",\n");
		produceEdges();
		sb.append('}');

		printGraph();
	}

	private void parseLabel() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(LABEL_PATH));
			String line;
			while ((line = br.readLine()) != null) {
				int delIndex = line.indexOf(LABEL_DELIMITER);
				String id = line.substring(0, delIndex);
				String label = line.substring(delIndex + 1);
				addLabel(id, label);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void printGraph() {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(JSON_PATH));
			bw.write(sb.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bw != null)
					bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 生成节点
	 */
	private void produceNodes() {
		addIndent();
		sb.append(getIndent() + "\"nodes\":{\n");
		addIndent();
		for (Entry<String, Node> entry : nodes.entrySet()) {
			String id = entry.getKey();
			Node node = entry.getValue();
			sb.append(getIndent() + "\"" + id + "\":{\n");
			addIndent();
			sb.append(getIndent() + "\"region\":\"" + node.label + "\"\n");
			reduceIndent();
			sb.append(getIndent() + "},\n");
		}
		sb.deleteCharAt(sb.length() - 2); // 删掉逗号
		reduceIndent();
		sb.append(getIndent() + '}');
		reduceIndent();
	}

	/**
	 * 生成边
	 */
	private void produceEdges() {
		addIndent();
		sb.append(getIndent() + "\"edges\":{\n");
		addIndent();
		for (Entry<String, Node> entry : nodes.entrySet()) {
			String id = entry.getKey();
			Node node = entry.getValue();
			sb.append(getIndent() + "\"" + id + "\":{\n");
			addIndent();
			for (String id2 : node.linkNodes) {
				sb.append(getIndent() + "\"" + id2 + "\":{},\n");
			}
			sb.deleteCharAt(sb.length() - 2); // 删掉逗号
			reduceIndent();
			sb.append(getIndent() + "},\n");
		}
		sb.deleteCharAt(sb.length() - 2); // 删掉逗号
		reduceIndent();
		sb.append(getIndent() + "}\n");
		reduceIndent();
	}

	private void parseGraph() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(GRAPH_PATH));
			String line;
			while ((line = br.readLine()) != null) {
				int delIndex = line.indexOf(GRAPH_DELIMITER);
				String id1 = line.substring(0, delIndex);
				String id2 = line.substring(delIndex + 1);
				addNode(id1, id2);
				addNode(id2, id1);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void addLabel(String id, String label) {
		nodes.get(id).label = label;
	}

	private void addNode(String id1, String id2) {
		if (nodes.containsKey(id1)) {
			nodes.get(id1).linkNodes.add(id2);
		} else {
			Node node = new Node(id1, id2);
			nodes.put(id1, node);
		}
	}

	private static class Node {
		String id; // 节点id
		List<String> linkNodes = new ArrayList<String>(); // 邻接节点
		String label; // 类别标签

		Node(String id1, String id2) {
			id = id1;
			linkNodes.add(id2);
		}
	}

	private void addIndent() {
		indentNum++;
	}

	private void reduceIndent() {
		indentNum--;
	}

	/**
	 * 获得缩进部分的字符串
	 * 
	 * @return
	 */
	private String getIndent() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < indentNum; i++)
			sb.append('\t');
		return sb.toString();
	}

	public static void main(String[] args) {
		new Thread(new LabelGraph2Json()).start();
	}
}
