import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class test {

	public static void main(String[] args) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader("weibo.xml"));
		BufferedWriter Writer = new BufferedWriter(new FileWriter("dire_graph"));
		String str;

		int leftIndex = 0;
		int rightIndex = 0;
		reader.readLine();
		while ((str = reader.readLine()) != null) {
			reader.readLine();
			str = reader.readLine();
			leftIndex = str.indexOf('>');
			rightIndex = str.lastIndexOf('<');
			int pid = Integer
					.parseInt(str.substring(leftIndex + 1, rightIndex));
			str = reader.readLine();
			leftIndex = str.indexOf('>');
			rightIndex = str.lastIndexOf('<');
			int gid = Integer
					.parseInt(str.substring(leftIndex + 1, rightIndex));
			reader.readLine();
		}
	}
}