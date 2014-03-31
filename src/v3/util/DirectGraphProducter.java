package v3.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class DirectGraphProducter {

	public static void main(String[] args) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader("weibo.xml"));
		BufferedWriter writer = new BufferedWriter(new FileWriter("dire_graph"));
		String str = null;

		int leftIndex = 0;
		int rightIndex = 0;
		long pid = 0, gid = 0;
		try {
			while ((str = reader.readLine()) != null) {
				if (str.startsWith("<person_id>")) {
					leftIndex = str.indexOf('>');
					rightIndex = str.lastIndexOf('<');
					pid = Long.parseLong(str.substring(leftIndex + 1,
							rightIndex));
				} else if (str.startsWith("<guanzhu_id>")) {
					leftIndex = str.indexOf('>');
					rightIndex = str.lastIndexOf('<');
					gid = Long.parseLong(str.substring(leftIndex + 1,
							rightIndex));

					writer.write(pid + "\t" + gid + "\n");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(str);
		}
		writer.close();

	}
}