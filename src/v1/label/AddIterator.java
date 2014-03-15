package v1.label;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class AddIterator {
	public static void main(String[] args) throws IOException,
			InterruptedException, ClassNotFoundException {
		int addedIterateTime;
		try{
			addedIterateTime =  Integer.parseInt(args[0]);
		}catch(Exception e){
			System.out.println("use> AddIterator num");
			return;
		}
		
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);
		
		fs.rename(new Path(LabelRun.COMMUNITY), new Path(LabelRun.TMP_LABEL));
		for (int i = 0; i < addedIterateTime; i++)
			LabelRun.runLabelPropagation(conf, fs);
		fs.rename(new Path(LabelRun.TMP_LABEL), new Path(LabelRun.COMMUNITY));
	}
}
