package v3.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UnDirectGraphProducter {
	static class Side implements Comparable<Side> {
		long pid;
		long gid;

		Side(long pid, long gid) {
			this.pid = pid;
			this.gid = gid;
		}

		@Override
		public int compareTo(Side dir) {
			if (pid != dir.pid)
				return pid > dir.pid ? 1 : -1;
			if (gid != dir.gid)
				return gid > dir.gid ? 1 : -1;
			return 0;
		}

		public String toString() {
			return pid + "\t" + gid + "\n";
		}
	}

	public static void main(String[] args) throws Exception {
		BufferedReader reader = new BufferedReader(new FileReader("dire_graph"));
		BufferedWriter writer = new BufferedWriter(new FileWriter(
				"undire_graph"));
		String str = null;

		List<Side> originList = new ArrayList<Side>();
		List<Side> exList = new ArrayList<Side>();
		while ((str = reader.readLine()) != null) {
			int index = str.indexOf('\t');
			long pid = Long.parseLong(str.substring(0, index));
			long gid = Long.parseLong(str.substring(index + 1, str.length()));
			Side side = new Side(pid, gid);
			originList.add(side);
			if (pid > gid) // make sure left < right
				side = new Side(gid, pid);
			exList.add(side);
		}
		reader.close();

		Object[] exSides = exList.toArray();
		Arrays.sort(exSides);

		Object[] originSides = originList.toArray();
		Arrays.sort(originSides);
		
		Side pre = (Side) exSides[0];
		if (Arrays.binarySearch(originSides, new Side(pre.gid, pre.pid)) >= 0) {
			writer.write(pre.pid + "\t" + pre.gid + "\n");
		}
		for (int i = 1, len = exSides.length; i < len; i++) {
			Side cur = (Side) exSides[i];
			if (pre.compareTo(cur) == 0)
				continue;
			if (Arrays.binarySearch(originSides, cur) >= 0
					&& Arrays.binarySearch(originSides, new Side(cur.gid,
							cur.pid)) >= 0)
				writer.write(cur.toString());
			pre = cur;
		}

		writer.close();
	}
}