package kr.ac.kaist.ic.arSkelecton.classifierWrapper;

import java.io.BufferedReader;
import java.io.FileReader;

import kr.ac.kaist.ic.arSkelecton.Constants;
import weka.core.Instance;
import weka.core.Instances;
import android.os.Environment;
import android.util.Log;

public abstract class ClassifierWrapper {
	
	protected final static String TAG = "ClassifierWrapper";	
	protected Instances instancesForTraining = null;	

	public Instances getInstances(){
		return instancesForTraining;
	}

	public abstract void train(Instances instances);
	
	public abstract String predict(Instance instance);
	
	public static Instances loadInstancesFromArffFile(String fileName){
		String dirPath = 
				Environment.getExternalStorageDirectory() 
				+ "/" 
				+ Constants.WORKING_DIR_NAME;
		String filePath = dirPath + "/" + fileName;

		try { 
			BufferedReader reader =
					new BufferedReader(new FileReader(filePath));	
			Instances data = new Instances(reader);
			if (data.classIndex() == -1)
				data.setClassIndex(data.numAttributes() - 1);
			return data;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "loadInstancesFromArffFile() error : " + e.getMessage());
		}

		return null;
	}
	
}
