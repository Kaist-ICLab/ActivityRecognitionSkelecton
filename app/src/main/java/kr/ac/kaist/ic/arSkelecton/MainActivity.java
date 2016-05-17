package kr.ac.kaist.ic.arSkelecton;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import kr.ac.kaist.ic.activityRecognitionSkelecton.R;
import kr.ac.kaist.ic.arSkelecton.classifierWrapper.ClassifierWrapper;
import kr.ac.kaist.ic.arSkelecton.classifierWrapper.J48Wrapper;
import kr.ac.kaist.ic.arSkelecton.sensorProc.DataInstance;
import kr.ac.kaist.ic.arSkelecton.sensorProc.DataInstanceList;
import kr.ac.kaist.ic.arSkelecton.sensorProc.FeatureGenerator;
import kr.ac.kaist.ic.arSkelecton.sensorProc.SlidingWindow;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author Jeungmin Oh
 * 
 * *** Overall Process ***
 * 1. SensorListener : Raw data acquisition and pass it to SlidingWindow
 * 2. SlidingWindow : Accepts raw data through input() and generate group of instances by window through output()
 * 3. MainRunnable (Thread) : Loops over sliding window's output continuously and performs generating features, saving ARFF files and classifying instances
 * 3-1. FeatureGenerator.process[SensorName]() : Process features from group of instances within a window
 * 3-2. FeatureGenerator.saveInstancesToArff() : It saves instances in the format of ARFF
 * 3-3. MainRunnable.setClassifier() : If classifier is set, it automatically classifies instances. (ARFF file necessary)
 * 
 * *** FileWriters ***
 * 1. SensorListener.saveRawDataToCSV() : Raw data writer
 * 2. MainRunnable.saveInstancesToArff() : Feature writer
 * 3. MainActivity.saveTestResultToFile() : Result writer
 */
public class MainActivity extends ActionBarActivity implements View.OnClickListener {

	final static String TAG = "MainActivity";

	// Raw Data Acquisition
	private SensorManager sm;
	private SensorListener sensorListener; // Stores raw data

	// Sliding window
	private SlidingWindow slidingWindowAcc, slidingWindowGyro; // For extracting samples by window
	private Thread mainThread; 
	private MainRunnable mainRunnable; // Stores calculated feature to ARFF file, and optionally classifies instances

	// Feature generation
	//private FeatureGenerator featureGenerator; // Only uses static methods

	// UI elements
	private Button btnStartCollectingData, btnFinishCollectingData;
	private Button btnStartTestingModel, btnFinishTestingModel;
	private EditText etClassLabelForModel, etOutputFileName;
	private TextView tvDataSource, tvLog;
	private ScrollView scrollViewForLog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		btnStartCollectingData = (Button) findViewById(R.id.btnStartCollectingData);
		btnFinishCollectingData = (Button) findViewById(R.id.btnFinishCollectingData);
		btnFinishCollectingData.setEnabled(false);

		btnStartTestingModel = (Button) findViewById(R.id.btnStartTestingModel);
		btnFinishTestingModel = (Button) findViewById(R.id.btnFinishTestingModel);
		btnFinishTestingModel.setEnabled(false);

		tvLog = (TextView) findViewById(R.id.tvLog);
		scrollViewForLog = (ScrollView) findViewById(R.id.scrollViewForLog);

		etClassLabelForModel = (EditText) findViewById(R.id.etClassLabel);
		{
			// Class label indicator
			StringBuilder sb = new StringBuilder();
			for(String classLabel : Constants.CLASS_LABELS){
				sb.append(classLabel + ",");
			}
			etClassLabelForModel.setHint(sb.deleteCharAt(sb.length()-1).toString());
		}

		etOutputFileName = (EditText) findViewById(R.id.etOutputFileName);

		tvDataSource = (TextView) findViewById(R.id.tvDataSource);
		{
			StringBuilder sb = new StringBuilder("[");
			for(String s : Constants.ARFF_FILE_NAMES){
				sb.append(s + ",");
			}		
			tvDataSource.setText(sb.deleteCharAt(sb.length()-1).append("]").toString());
		}

		// Attaching listeners
		btnStartCollectingData.setOnClickListener(this);
		btnFinishCollectingData.setOnClickListener(this);
		btnStartTestingModel.setOnClickListener(this);
		btnFinishTestingModel.setOnClickListener(this);

		// Sensor manager
		sm = (SensorManager) getSystemService(SENSOR_SERVICE);

		// Sliding window
		slidingWindowAcc = new SlidingWindow(Constants.WINDOW_SIZE, Constants.STEP_SIZE);
		slidingWindowGyro = new SlidingWindow(Constants.WINDOW_SIZE, Constants.STEP_SIZE);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

		// Cleaning
		//finishBuildingModel();
	}

	public class SensorListener implements SensorEventListener {

		final static String TAG = "SensorEvent";
		private String label = null;		

		private DataInstanceList dlAcc, dlGyro; // For raw data save purpose	

		public SensorListener(String label){
			// When testing a model, classLabel will be ignored
			this.label = label;

			dlAcc = new DataInstanceList();
			dlGyro = new DataInstanceList();
		}

		public void setLabel(String label){
			this.label = label;
		}

		public String getLabel(){
			return label;
		}

		public void saveRawDataToCSV() {
			String fileNameAcc = Constants.PREFIX_RAW_DATA + System.currentTimeMillis() + "_" + label + "_acc.txt";
			dlAcc.saveToCsvFile(fileNameAcc);

			String fileNameGyro = Constants.PREFIX_RAW_DATA + System.currentTimeMillis() + "_" + label + "_gyro.txt";
			dlGyro.saveToCsvFile(fileNameGyro);
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			// TODO Auto-generated method stub
			if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
				float[] values = event.values;
				float x = values[0];
				float y = values[1];
				float z = values[2];

				DataInstance diAcc = new DataInstance(System.currentTimeMillis(), values);
				diAcc.setLabel(label); // Optional field
				dlAcc.add(diAcc); // Save for raw data backup
				slidingWindowAcc.input(diAcc);

				Log.d(TAG, "Acc : " + x + "," + y + "," + z);
			}
			else if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){	
				float[] values = event.values;
				float x = values[0];
				float y = values[1];
				float z = values[2];

				DataInstance diGyro = new DataInstance(System.currentTimeMillis(), values);
				diGyro.setLabel(label); // Optional field
				dlGyro.add(diGyro); // Save for raw data backup
				slidingWindowGyro.input(diGyro);

				Log.d(TAG, "Gyro : " + x + "," + y + "," + z);

			}
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub

		}
	}

	public class MainRunnable implements Runnable {

		private final static String TAG = "MainThread";

		// Data collection (Always)
		private Instances instancesForDataCollection;
		private String classLabel; // Optional value, only necessary for data collection

		// Classification (Optional)
		//private Instances instancesForClassification = null;
		private ClassifierWrapper classifier = null;
		private ArrayList<String> classificationResultList;	

		public MainRunnable(String label){
			this.classLabel = label;
		}		

		public String getResultTest(){
			StringBuilder sb = new StringBuilder();
			sb.append("********** Test Result **********\n");

			for(String result : classificationResultList){
				sb.append(result + "\n");
			}

			return sb.toString();

		}
		/*
		public String getResultCrossValidation(){
			StringBuilder sb = new StringBuilder();

			// Cross validation
			Evaluation evaluation;
			try {
				Instances instances = classifier.getInstances();
				evaluation = new Evaluation(instances);
				evaluation.crossValidateModel(classifier, instances, 10, new Random(1));
				Log.i(TAG, evaluation.toSummaryString());

				sb.append("********** Cross Validation Summary **********\n");
				sb.append(evaluation.toSummaryString(true) + "\n");

				return sb.toString();
			} catch(Exception e){

			}

			return null;
		}
		 */
		@Override
		public void run() {
			try {
				// Reusable buffer
				DataInstanceList dlAcc, dlGyro;

				while(!Thread.currentThread().isInterrupted()){
					while(true){ 
						// Fetching a slices of sliding window
						dlAcc = slidingWindowAcc.output();
						dlGyro = slidingWindowGyro.output();	

						if(dlAcc != null && dlGyro != null){
							if(dlAcc.getTimeId() == dlGyro.getTimeId()){
								Log.i(TAG, "Sensors are synced!");

								// Calculate features (without class label)
								HashMap<String, Float> featureMapAcc = 
										FeatureGenerator.processAcc(dlAcc);
								HashMap<String, Float> featureMapGyro = 
										FeatureGenerator.processGyro(dlGyro);

								// Generating header
								if(instancesForDataCollection == null){
									String[] featureHeader = Constants.LIST_FEATURES;
									instancesForDataCollection = 
											FeatureGenerator.createEmptyInstances(featureHeader, true); // makeClassLabel);
								}

								// Aggregate features in single Weka instance
								int attributeSize = featureMapAcc.size() + featureMapAcc.size() + 1;
								Instance instance = new Instance(attributeSize); // including class classLabel

								// Filling features for accelerometer
								for(String feature : featureMapAcc.keySet()){
									float value = featureMapAcc.get(feature);
									Attribute attr = instancesForDataCollection.attribute(feature);
									instance.setValue(attr, value);
								}

								// Filling features for gyroscope
								for(String feature : featureMapGyro.keySet()){
									float value = featureMapGyro.get(feature);
									Attribute attr = instancesForDataCollection.attribute(feature);
									instance.setValue(attr, value);
								}

								// Adding class attribute
								Attribute attrClass = instancesForDataCollection.attribute(Constants.HEADER_CLASS_LABEL);
								instancesForDataCollection.setClass(attrClass);
								if(classLabel != null)
									instance.setValue(attrClass, classLabel); // Only for labeled data when collecting data

								// Add generated Instance
								instancesForDataCollection.add(instance); // Final calculated feature set
								Log.i(TAG, "Instance added : " + instancesForDataCollection.numInstances());

								// Classify a instance if classifier is ready by setClassifier()
								if(classifier != null){
									try {
										// Store the result in ArrayList
										if(classificationResultList == null){
											classificationResultList = new ArrayList<String>();
										}

										// Use header information of data collection										
										instance.setDataset(instancesForDataCollection);
										//Log.e(TAG, "ClassifierWrapper : " + classifier);

										final String resultClass = classifier.predict(instance);

										classificationResultList.add(/*instance + "," + */resultClass);								

										Log.i(TAG, "Classified as : " + resultClass);
										runOnUiThread(new Runnable() {

											@Override
											public void run() {
												String formattedDate = "";
												{
													Calendar cal = Calendar.getInstance();
													cal.setTimeInMillis(System.currentTimeMillis());
													SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
													formattedDate = sdf.format(cal.getTime());
												}
												String result = formattedDate + " : " + resultClass;
												tvLog.append(result.toString());
												tvLog.append("\n");
												scrollViewForLog.fullScroll(View.FOCUS_DOWN);
											}
										});
									} catch (Exception e) {
										e.printStackTrace();
										Log.e(TAG, "Classification error : " + e.getMessage());

										classificationResultList.add("Classification error");								
									}
								} 

							} else {
								Log.e(TAG, "Sample are not synced!"); // Issue : What if not synced (Very rare case) => Ignored
							}
						}

						if(dlAcc == null && dlGyro == null){
							Log.d(TAG, "Thread sleeps for " + Constants.DURATION_THREAD_SLEEP + "ms");
							Thread.sleep(Constants.DURATION_THREAD_SLEEP);
						}
					} 

				}

			} catch (InterruptedException e) {
				// Expected exception by onPause()
				e.printStackTrace(); 
			}

		} // End of run()

		public Instances getInstances(){
			return instancesForDataCollection;
		}

		public String getLabel(){
			return classLabel;
		}

		public void saveInstancesToArff(Instances instances, String fileName){

			try {
				ArffSaver saver = new ArffSaver();

				saver.setInstances(instances);
				String dirPath = Environment.getExternalStorageDirectory() + "/" + Constants.WORKING_DIR_NAME;
				String filePath = dirPath + "/" + fileName;

				File dirFile = new File(dirPath);
				if(!dirFile.exists()){
					dirFile.mkdirs();
				}			

				saver.setFile(new File(filePath));
				saver.writeBatch();
				Log.i(TAG, "Arff saved : " + filePath);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "saveInstancesToArff() error : " + e.getMessage());
			}
		}

		public void setClassifier(String[] arffFileNames) {
			// Load training data
			Instances instancesForTraining = 
					ClassifierWrapper.loadInstancesFromArffFile(arffFileNames[0]);
			for(int i=1; i<arffFileNames.length; i++){ // From second Instances
				Instances instances = 
						ClassifierWrapper.loadInstancesFromArffFile(arffFileNames[i]);	
				for(int j=0; j<instances.numInstances(); j++){
					Instance instance = instances.instance(j);
					instancesForTraining.add(instance);
				}			
			}

			// Build a classifier and set it to global classifier variable
			{
				ClassifierWrapper classifierTmp = new J48Wrapper();
				//ClassifierWrapper classifierTmp = new LibLinearWrapper();
				classifierTmp.train(instancesForTraining);
				classifier = classifierTmp;
			} 
		}
	}

	/*
	 * Highlight the button specified in argument, if argument is null, the buttons turn to initial condition
	 */
	public void highlightButton(Button btn) {

		if(btn == null){ // Initial states
			btnStartCollectingData.setEnabled(true);
			btnStartTestingModel.setEnabled(true);
			btnFinishCollectingData.setEnabled(false);
			btnFinishTestingModel.setEnabled(false);
		} else { // Highlight
			btnStartCollectingData.setEnabled(false);
			btnStartTestingModel.setEnabled(false);
			btnFinishCollectingData.setEnabled(false);
			btnFinishTestingModel.setEnabled(false);
			btn.setEnabled(true);
		}
	}

	public void startDataCollection(String label){
		highlightButton(btnFinishCollectingData);		

		sensorListener = new SensorListener(label);
		sm.registerListener(sensorListener, 
				sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 
				Constants.SENSOR_DELAY);
		sm.registerListener(sensorListener, 
				sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE), 
				Constants.SENSOR_DELAY);

		mainRunnable = new MainRunnable(label);
		mainThread = new Thread(mainRunnable);
		mainThread.start();
	}

	public void finishDataCollection(){
		highlightButton(null);

		// Save raw data
		sensorListener.saveRawDataToCSV();

		// Save calculated feature sets
		String label = mainRunnable.getLabel();
		mainRunnable.saveInstancesToArff(mainRunnable.getInstances(), Constants.PREFIX_FEATURES + System.currentTimeMillis() + "_" + label + ".txt");

		// Cleaning
		btnStartCollectingData.setEnabled(true);
		btnFinishCollectingData.setEnabled(false);

		if(sm != null){
			sm.unregisterListener(sensorListener);
			sensorListener = null;
		}

		if(!mainThread.isInterrupted()){
			mainThread.interrupt();
		}
	}

	public void startModelTest(String[] arffFileNames, String outputFileName){
		highlightButton(btnFinishTestingModel);

		sensorListener = new SensorListener(null); // null means testing mode
		sm.registerListener(sensorListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		sm.registerListener(sensorListener, sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);

		mainRunnable = new MainRunnable(null); // null means testing mode
		mainRunnable.setClassifier(arffFileNames);

		mainThread = new Thread(mainRunnable);
		mainThread.start();

		tvLog.setText("");
	}

	public void saveTestResultToFile(String outputFileName, String content){
		// Writing summary if output is set
		if(outputFileName != null){
			// Set output file for writing result
			String filePath = Environment.getExternalStorageDirectory() + "/" + Constants.WORKING_DIR_NAME + "/" + Constants.PREFIX_RESULT + System.currentTimeMillis() + "_" + outputFileName + ".txt";
			FileWriter fw = null;
			try {
				fw = new FileWriter(filePath);
				Log.i(TAG, "Output file writer is open!");

				fw.write(content);
				fw.flush();
				fw.close();
			} catch (IOException e) { 
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "setOutputFileName() error : " + e.getMessage());
			} 
		}
	}

	public void finishModelTest(String outputFileName){
		highlightButton(null);	

		// Save raw data
		sensorListener.saveRawDataToCSV();

		// Save calculated feature sets
		String label = mainRunnable.getLabel();
		mainRunnable.saveInstancesToArff(mainRunnable.getInstances(), Constants.PREFIX_FEATURES + System.currentTimeMillis() + "_" + label + ".txt");


		//String resultCrossValidation = mainRunnable.getResultCrossValidation(); 
		String resultTest = mainRunnable.getResultTest();
		saveTestResultToFile(outputFileName, /*resultCrossValidation + */"\n" + resultTest);

		// Cleaning 
		btnStartCollectingData.setEnabled(true);
		btnFinishCollectingData.setEnabled(false);

		if(sm != null){
			sm.unregisterListener(sensorListener);
			sensorListener = null;
		}

		if(!mainThread.isInterrupted()){
			mainThread.interrupt();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == btnStartCollectingData){
			Log.i(TAG, "startBuildingModel()");

			String classLabelInput = etClassLabelForModel.getText().toString();
			if(classLabelInput == null || classLabelInput.equals("")){
				Toast.makeText(this, "Class label is required for collecting data", Toast.LENGTH_SHORT).show();
			} else {
				boolean isValidClassLabel = false;
				for(String classLabel : Constants.CLASS_LABELS){
					if(classLabel.equals(classLabelInput)){
						isValidClassLabel = true;						
						break;
					}
				}

				if(isValidClassLabel){
					etClassLabelForModel.setEnabled(false);
					startDataCollection(classLabelInput);
				} else {
					Toast.makeText(this, "Class label should be one of the predefined classes", Toast.LENGTH_SHORT).show();
				}
			}



		}
		else if (v == btnFinishCollectingData){
			Log.i(TAG, "finishBuildingModel()");
			etClassLabelForModel.setEnabled(true);
			finishDataCollection();

		}
		else if(v == btnStartTestingModel){
			Log.i(TAG, "startTestingModel()");			

			String outputFileName = etOutputFileName.getText().toString();
			if(outputFileName == null || outputFileName.equals("")){
				Toast.makeText(this, "Output file name is required for testing a model", Toast.LENGTH_SHORT).show();
			} else {
				startModelTest(Constants.ARFF_FILE_NAMES, outputFileName);
				etOutputFileName.setEnabled(false);
			}
		}
		else if (v == btnFinishTestingModel){
			Log.i(TAG, "finishTestingModel()");
			String outputFileName = etOutputFileName.getText().toString();
			finishModelTest(outputFileName);
			etOutputFileName.setEnabled(true);
		}
	}

}
