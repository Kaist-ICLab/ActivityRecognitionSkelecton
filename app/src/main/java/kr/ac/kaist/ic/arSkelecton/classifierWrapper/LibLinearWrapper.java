package kr.ac.kaist.ic.arSkelecton.classifierWrapper;

import android.util.Log;

import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;
import de.bwaldvogel.liblinear.Linear;
import de.bwaldvogel.liblinear.Model;
import de.bwaldvogel.liblinear.Parameter;
import de.bwaldvogel.liblinear.Problem;
import de.bwaldvogel.liblinear.SolverType;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

public class LibLinearWrapper extends ClassifierWrapper {

	private Model model;

	@Override
	public void train(Instances instances) {
		int sizeOfTrainingSet = instances.numInstances();
		int sizeOfFeaturesWithoutClass = instances.numAttributes() - 1;

		double[] classLabels = new double[sizeOfTrainingSet]; // Class attribute
		FeatureNode[][] convertedInstances = new FeatureNode[sizeOfTrainingSet][]; // Feature attributes

		for(int i=0; i<sizeOfTrainingSet; i++){
			Instance instance = instances.instance(i);			

			FeatureNode[] featureSet = new FeatureNode[sizeOfFeaturesWithoutClass];
			for(int f=0; f<instance.numAttributes(); f++){
				Attribute attr = instance.attribute(f);
				double value = instance.value(attr);
				if(attr == instance.classAttribute()){
					classLabels[i] = value;// Set class attribute
					//Log.d(TAG, "Class : " + value);
				} else {
					featureSet[f] = new FeatureNode(f+1, value); // Set feature attributes (index of feature node should start from 1, not 0)
					//Log.d(TAG, "Value : " + value);
				}
			}	
			convertedInstances[i] = featureSet;
		}

		// Model data
		Problem problem = new Problem();

		// number of training examples
		problem.l = sizeOfTrainingSet; 

		// number of features
		problem.n = sizeOfFeaturesWithoutClass; 

		// feature nodes
		problem.x = convertedInstances; 

		// target values
		problem.y = classLabels; 

		// Model parameters
		SolverType solver = SolverType.L2R_LR; // -s 0
		double C = 1.0;    // cost of constraints violation
		double eps = 0.01; // stopping criteria

		Parameter parameter = new Parameter(solver, C, eps);

		// Building a model
		Log.i(TAG, "Problem : " + problem);
		Log.i(TAG, "Parameter : " + parameter);
		model = Linear.train(problem, parameter);

		// Cross validation result
		{
			double[] crossValidationResult = new double[sizeOfTrainingSet];
			Linear.crossValidation(problem, parameter, 10, crossValidationResult);
			/*for(int i=0; i<crossValidationResult.length; i++){
				Log.i(TAG, "RESULT : " + crossValidationResult[i]);
			}*/
		}
		/*
		// Store the model as file		
		try {
			File modelFile = new File("model");
			model.save(modelFile);
			model = Model.load(modelFile);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// load model or use it directly		
		 */
	}

	@Override
	public String predict(Instance instance) {
		
		FeatureNode[] featureSet = new FeatureNode[instance.numAttributes() - 1];
		for(int f=0; f<instance.numAttributes(); f++){
			Attribute attr = instance.attribute(f);
			double value = instance.value(attr);
			if(attr == instance.classAttribute()){
				; // Predicting unknown class label
			} else {
				featureSet[f] = new FeatureNode(f+1, value); // Set feature attributes (index of feature node should start from 1, not 0)
				//Log.d(TAG, "Value : " + value);
			}
		}	
		
		
		//Feature[] convertedInstance = { new FeatureNode(1, 4), new FeatureNode(2, 2) };
		Feature[] convertedInstance = new Feature[instance.numAttributes() - 1];
		for(int i=0; i<featureSet.length; i++){
			convertedInstance[i] = featureSet[i];
		}
		
		double result = Linear.predict(model, convertedInstance);		
		String resultClass = instancesForTraining.classAttribute().value((int)result);	

		Log.i(TAG, "Classified result : " + resultClass);
		
		return resultClass;
	}

}
