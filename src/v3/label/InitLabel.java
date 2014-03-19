package v3.label;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * 初始化标签
 *
 */
@Deprecated
public class InitLabel {
	private FSDataInputStream inStream;
	private FSDataOutputStream outStream;

	private Set<String> vertexs = new HashSet<String>();// 顶点的集合

	public InitLabel(FileSystem fs, String graphPath, String labelPath) {
		try {
			inStream = fs.open(new Path(graphPath));
			outStream = fs.create(new Path(labelPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Deprecated
	public void start() {
		// 将图中的节点读入map
		String str;
		try {
			while ((str = inStream.readLine()) != null) {
				StringTokenizer token = new StringTokenizer(str);
				while (token.hasMoreTokens())
					vertexs.add(token.nextToken());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		outputInitLabelFile();
		close();
	}

	/**
	 * 将顶点的初始化标签输出到文件中
	 */
	private void outputInitLabelFile() {
		try {
			for (String v : vertexs) {
				System.out.print(v + "\t" + v + "\n");
				outStream.writeBytes(v + "\t" + v + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 关闭流
	 */
	private void close() {
		try {
			inStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			outStream.flush();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			outStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
