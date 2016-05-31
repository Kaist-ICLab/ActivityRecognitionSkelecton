package kr.ac.kaist.ic.arSkelecton;

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

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import kr.ac.kaist.ic.activityRecognitionSkelecton.R;

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
public class MainActivity extends ActionBarActivity {

	final static String TAG = "MainActivity";

	// Feature generation
	//private FeatureGenerator featureGenerator; // Only uses static methods

	// UI elements
	private Button btnStartCollectingData, btnFinishCollectingData;
	private Button btnStartTestingModel, btnFinishTestingModel;
	private EditText etClassLabelForModel, etOutputFileName;
	private TextView tvDataSource, tvLog;
	private ScrollView scrollViewForLog;

    private SensorDataHandler sensorDataHandler;
    private DataClassifier sensorDataClassifier;

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
        btnStartCollectingData.setOnClickListener(btnStartCollectingDataOnClick);
        btnFinishCollectingData.setOnClickListener(btnFinishCollectingDataOnClick);
        btnStartTestingModel.setOnClickListener(btnStartTestingModelOnClick);
        btnFinishTestingModel.setOnClickListener(btnFinishTestingModelOnClick);


        this.sensorDataHandler = new SensorDataHandler(this);
        this.sensorDataClassifier = new DataClassifier();
        this.sensorDataHandler.dataAdaptor = this.sensorDataClassifier;

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

    //UI Event Handler
    private View.OnClickListener btnStartCollectingDataOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "startBuildingModel()");

            String classLabelInput = etClassLabelForModel.getText().toString();
            if(classLabelInput == null || classLabelInput.equals("")){
                Toast.makeText(MainActivity.this, "Class label is required for collecting data", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(MainActivity.this, "Class label should be one of the predefined classes", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    private View.OnClickListener btnFinishCollectingDataOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "finishBuildingModel()");
            etClassLabelForModel.setEnabled(true);
            finishDataCollection();
        }
    };

    private View.OnClickListener btnStartTestingModelOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "startTestingModel()");

            String outputFileName = etOutputFileName.getText().toString();
            if (outputFileName == null || outputFileName.equals("")) {
                Toast.makeText(MainActivity.this, "Output file name is required for testing a model", Toast.LENGTH_SHORT).show();
            } else {
                startModelTest(Constants.ARFF_FILE_NAMES, outputFileName);
                etOutputFileName.setEnabled(false);
            }
        }
    };

    private View.OnClickListener btnFinishTestingModelOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i(TAG, "finishTestingModel()");
            String outputFileName = etOutputFileName.getText().toString();
            finishModelTest(outputFileName);
            etOutputFileName.setEnabled(true);
        }
    };

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
        sensorDataClassifier.clear();
        sensorDataHandler.setClassLabel(label);
        sensorDataHandler.start();
	}

	public void finishDataCollection(){
		highlightButton(null);

        sensorDataHandler.stop();
		// Save raw data
        sensorDataHandler.saveRawDataToCSV();

		// Save calculated feature sets
        String label = sensorDataHandler.getClassLabel();
        sensorDataClassifier.saveInstancesToArff(sensorDataClassifier.getInstances(), Constants.PREFIX_FEATURES + System.currentTimeMillis() + "_" + label + ".txt");

		// Cleaning
		btnStartCollectingData.setEnabled(true);
		btnFinishCollectingData.setEnabled(false);
	}

	public void startModelTest(String[] arffFileNames, String outputFileName) {
        try {
            highlightButton(btnFinishTestingModel);

            sensorDataHandler.setClassLabel(null);
            sensorDataClassifier.setClassifier(arffFileNames);

            sensorDataHandler.start();

            tvLog.setText("");
        } catch (FileNotFoundException e) {
            sensorDataHandler.stop();
            highlightButton(null);
            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
	}

	public void saveTestResultToFile(String outputFileName, String content) {
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

        sensorDataHandler.stop();

		// Save raw data
        sensorDataHandler.saveRawDataToCSV();

		// Save calculated feature sets
        String label = sensorDataHandler.getClassLabel();
        sensorDataClassifier.saveInstancesToArff(sensorDataClassifier.getInstances(), Constants.PREFIX_FEATURES + System.currentTimeMillis() + "_" + label + ".txt");


		//String resultCrossValidation = mainRunnable.getResultCrossValidation(); 
        String resultTest = sensorDataClassifier.getResultTest();
        saveTestResultToFile(outputFileName, /*resultCrossValidation + */"\n" + resultTest);

		// Cleaning 
		btnStartCollectingData.setEnabled(true);
		btnFinishCollectingData.setEnabled(false);
	}

}
