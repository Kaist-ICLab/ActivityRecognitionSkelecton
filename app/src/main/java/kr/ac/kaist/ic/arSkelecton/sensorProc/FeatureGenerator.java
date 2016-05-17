package kr.ac.kaist.ic.arSkelecton.sensorProc;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import kr.ac.kaist.ic.arSkelecton.Constants;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;
import android.util.Log;

public class FeatureGenerator {
	// Mean, Max, Min, Magnitude, Energy, FFT, 

	final static String TAG = "FeatureGenerator";

	public FeatureGenerator() {
		;
	}	
/*
	public static double[] calculateFft(double[] dataArray){
		 [TOSN'10Reddy]
		 * DFT (Discrete Fourier Transform) energy coefficients between 1-10Hz based on magnitude of
		 * the force vector of the accelerometer were evaluated along with the speed of
		 * the GPS receiver
		 

		int sampleSize = dataArray.length;
		int N = 1;

		// Find the FFT input size (2^N)
		for(;; N*=2){
			if(N >= sampleSize)
				break;
		}		

		// Cut off some samples instead of zero padding
		if((sampleSize & (sampleSize - 1)) != 0){
			N = N/2; 
		}

		// Samples within a window
		double[] reals = new double[N];
		double[] imags = new double[N];

		// Zero-padding
		for(int i=0; i<N; i++){
			if(i<dataArray.length){
				reals[i] = dataArray[i];
				imags[i] = 0.0;
			}
			else{
				reals[i] = 0.0;
				imags[i] = 0.0;
			}
		}

		FFT fft = new FFT(N);
		fft.fft(reals, imags);

		double[] mags = new double[N];
		for(int i=0; i<N; i++){
			mags[i] = Math.sqrt(reals[i] * reals[i] + imags[i] * imags[i]) / mags.length; // Should be divided by the length due to FFT
		}

		return mags;
	};	
	*/

	public static float calculateMean(float[] data){
		float sum = 0;
		for(float d : data){
			sum += d;
		}
		return sum / data.length;		
	}

	public static float calculateMean(DataInstance di){
		return calculateMean(di.getValues());
	}

	public static float calculateMax(float[] data){
		float max = data[0]; // Temporary value
		for(float d : data){
			max = (d >= max) ? d : max;
		}
		return max;		
	}

	public static float calculateMax(DataInstance di){
		return calculateMax(di.getValues());
	}

	public static float calculateMin(float[] data){
		float min = data[0]; // Temporary value
		for(float d : data){
			min = (d <= min) ? d : min;
		}
		return min;
	}

	public static float calculateMin(DataInstance di){
		return calculateMin(di.getValues());
	}

	public static float calculateVariance(float[] data){

		//////////////////////////////
		// TO BE IMPLEMENTED FOR KSE624 ASSIGNMENT
		//////////////////////////////

		return 0;
	}

	public static float calculateVariance(DataInstance di){
		return calculateMin(di.getValues());
	}

	public static HashMap<String, Float> processAcc(DataInstanceList dl) {	

		float[] xAggregated = new float[dl.size()];
		float[] yAggregated = new float[dl.size()];
		float[] zAggregated = new float[dl.size()];
		double[] magAggregated = new double[dl.size()]; // Data for fft should be double type

		// Ready for calculation
		for(int i=0; i<dl.size(); i++){
			float x = ((DataInstance)dl.get(i)).getValues()[0];
			float y = ((DataInstance)dl.get(i)).getValues()[1];
			float z = ((DataInstance)dl.get(i)).getValues()[2];

			xAggregated[i] = x;
			//Log.i(TAG, "x : " + xAggregated[i]);
			yAggregated[i] = y;
			zAggregated[i] = z;
			magAggregated[i] = Math.sqrt(x*x + y*y + z*z);
		}

		float x_mean = calculateMean(xAggregated);
		float y_mean = calculateMean(yAggregated);
		float z_mean = calculateMean(zAggregated);

		float x_max = calculateMax(xAggregated);
		float y_max = calculateMax(yAggregated);
		float z_max = calculateMax(zAggregated);

		float x_min = calculateMin(xAggregated);
		float y_min = calculateMin(yAggregated);
		float z_min = calculateMin(zAggregated);

		float x_var = calculateVariance(xAggregated);
		float y_var = calculateVariance(yAggregated);
		float z_var = calculateVariance(zAggregated);
		
		// Output variables
		HashMap<String, Float> featureMap = new HashMap<String, Float>();
		featureMap.put(Constants.HEADER_ACC_X_MEAN, x_mean);
		featureMap.put(Constants.HEADER_ACC_Y_MEAN, y_mean);
		featureMap.put(Constants.HEADER_ACC_Z_MEAN, z_mean);
		featureMap.put(Constants.HEADER_ACC_X_MAX, x_max);
		featureMap.put(Constants.HEADER_ACC_Y_MAX, y_max);
		featureMap.put(Constants.HEADER_ACC_Z_MAX, z_max);
		featureMap.put(Constants.HEADER_ACC_X_MIN, x_min);
		featureMap.put(Constants.HEADER_ACC_Y_MIN, y_min);
		featureMap.put(Constants.HEADER_ACC_Z_MIN, z_min);
		featureMap.put(Constants.HEADER_ACC_X_VARIANCE, x_var);
		featureMap.put(Constants.HEADER_ACC_Y_VARIANCE, y_var);
		featureMap.put(Constants.HEADER_ACC_Z_VARIANCE, z_var);

		//Log.d(TAG, "Accelerometer features : " + x_mean + " / " + x_max + " / " + x_min + " / " + acc_x_var);
		//Log.i(TAG, "Frequency data size  : " + freqs.length);


		return featureMap;

	}

	public static HashMap<String, Float> processGyro(DataInstanceList dl) {


		float[] xAggregated = new float[dl.size()];
		float[] yAggregated = new float[dl.size()];
		float[] zAggregated = new float[dl.size()];

		// Ready for calculation
		for(int i=0; i<dl.size(); i++){
			float x = ((DataInstance)dl.get(i)).getValues()[0];
			float y = ((DataInstance)dl.get(i)).getValues()[1];
			float z = ((DataInstance)dl.get(i)).getValues()[2];

			xAggregated[i] = x;
			//Log.i(TAG, "x : " + xAggregated[i]);
			yAggregated[i] = y;
			zAggregated[i] = z;
		}


		// Output variables
		HashMap<String, Float> featureMap = new HashMap<String, Float>();
		//featureMap.put(Constants.HEADER_GYRO_X_MEAN, x_mean);

		//////////////////////////////
		// TO BE IMPLEMENTED FOR KSE624 ASSIGNMENT
		//////////////////////////////

		//Log.d(TAG, "Gyroscope features : " + x_mean + " / " + x_max + " / " + x_min + " / " + acc_x_var);

		return featureMap;
	}

	public static Instances createEmptyInstances(String[] headers, boolean isLabelRequired){		

		FastVector attrs = new FastVector();

		for(String header : headers){
			attrs.addElement(new Attribute(header));
		}

		if(isLabelRequired){
			FastVector fv = new FastVector();
			for(String classLabel : Constants.CLASS_LABELS){
				fv.addElement(classLabel);
			}
			attrs.addElement(new Attribute(Constants.HEADER_CLASS_LABEL, (FastVector) fv));

		}

		String formattedDate = "";
		{
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(System.currentTimeMillis());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			formattedDate = sdf.format(cal.getTime());
		}

		Instances data = new Instances(formattedDate, attrs, 10000);

		return data;
	}
}
