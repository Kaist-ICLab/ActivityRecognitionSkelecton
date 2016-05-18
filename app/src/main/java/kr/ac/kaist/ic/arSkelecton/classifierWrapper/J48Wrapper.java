package kr.ac.kaist.ic.arSkelecton.classifierWrapper;

import android.util.Log;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;

public class J48Wrapper extends ClassifierWrapper {
	
	Classifier classifier = null;
	
	@Override
	public void train(Instances instances) {		
		try {
			classifier = new J48();
			classifier.buildClassifier(instances);
			instancesForTraining = instances;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(TAG, "Train() error : " + e.getMessage());
			classifier = null;
		}
	}

	@Override
	public String predict(Instance instance) {
		String resultClass = null;
		try {
			double result = classifier.classifyInstance(instance);
			resultClass = instancesForTraining.classAttribute().value((int)result);					
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			e.printStackTrace();
		}	
		return resultClass;
	}


}
