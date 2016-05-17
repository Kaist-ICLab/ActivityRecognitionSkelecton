package kr.ac.kaist.ic.arSkelecton;

import android.hardware.SensorManager;

public class Constants {
	// Parameters
	public final static String[] CLASS_LABELS = {"walk", "run"}; // Should be predefined before collecting data
	public final static String[] ARFF_FILE_NAMES = {"walk.txt", "run.txt"}; // Will be used for model building after merging
	public final static String WORKING_DIR_NAME = "activityRecognition"; // Folder name in SD card
	public final static int SENSOR_DELAY = SensorManager.SENSOR_DELAY_FASTEST; // SensorManager.SENSOR_DELAY_FASTEST;
	
	public final static int DURATION_THREAD_SLEEP = 200; // ms (Should be smaller than step_size, otherwise bottle neck occurs)
	public final static int WINDOW_SIZE = 1000; // ms
	public final static int STEP_SIZE = 500; // ms
	
	// File name prefix	
	public final static String PREFIX_RAW_DATA = "1_raw_data_";
	public final static String PREFIX_FEATURES = "2_features_";
	public final static String PREFIX_MODEL = "3_model_"; // Not used for this application 
	public final static String PREFIX_RESULT = "4_result_";
	
	// Raw data
	public final static String HEADER_ACC_X = "acc_x";
	public final static String HEADER_ACC_Y = "acc_y";
	public final static String HEADER_ACC_Z = "acc_z";
	
	// Optional fields
	public final static String HEADER_CLASS_LABEL = "label";
	public final static String HEADER_UNIXTIME = "unix_time";
	
	// Features (Accelerometer)
	public final static String HEADER_ACC_X_MEAN = "acc_x_mean";
	public final static String HEADER_ACC_Y_MEAN = "acc_y_mean";
	public final static String HEADER_ACC_Z_MEAN = "acc_z_mean";
	
	public final static String HEADER_ACC_X_MAX = "acc_x_max";
	public final static String HEADER_ACC_Y_MAX = "acc_y_max";
	public final static String HEADER_ACC_Z_MAX = "acc_z_max";
	
	public final static String HEADER_ACC_X_MIN = "acc_x_min";
	public final static String HEADER_ACC_Y_MIN = "acc_y_min";
	public final static String HEADER_ACC_Z_MIN = "acc_z_min";
	
	public final static String HEADER_ACC_X_VARIANCE = "acc_x_variance";
	public final static String HEADER_ACC_Y_VARIANCE = "acc_y_variance";
	public final static String HEADER_ACC_Z_VARIANCE = "acc_z_variance";
	
	// Features (Gyroscope)
	public final static String HEADER_GYRO_X_MEAN = "gyro_x_mean";
	public final static String HEADER_GYRO_Y_MEAN = "gyro_y_mean";
	public final static String HEADER_GYRO_Z_MEAN = "gyro_z_mean";
	
	public final static String HEADER_GYRO_X_MAX = "gyro_x_max";
	public final static String HEADER_GYRO_Y_MAX = "gyro_y_max";
	public final static String HEADER_GYRO_Z_MAX = "gyro_z_max";
	
	public final static String HEADER_GYRO_X_MIN = "gyro_x_min";
	public final static String HEADER_GYRO_Y_MIN = "gyro_y_min";
	public final static String HEADER_GYRO_Z_MIN = "gyro_z_min";
	
	public final static String HEADER_GYRO_X_VARIANCE = "gyro_x_variance";
	public final static String HEADER_GYRO_Y_VARIANCE = "gyro_y_variance";
	public final static String HEADER_GYRO_Z_VARIANCE = "gyro_z_variance";
	
	// List of Features
	public final static String[] LIST_FEATURES = {
		HEADER_ACC_X_MEAN,
		HEADER_ACC_X_MAX,
		HEADER_ACC_X_MIN,
		HEADER_ACC_X_VARIANCE,
		HEADER_ACC_Y_MEAN,
		HEADER_ACC_Y_MAX,
		HEADER_ACC_Y_MIN,
		HEADER_ACC_Y_VARIANCE,
		HEADER_ACC_Z_MEAN,
		HEADER_ACC_Z_MAX,
		HEADER_ACC_Z_MIN,
		HEADER_ACC_Z_VARIANCE,
		HEADER_GYRO_X_MEAN,
		HEADER_GYRO_X_MAX,
		HEADER_GYRO_X_MIN,
		HEADER_GYRO_X_VARIANCE,
		HEADER_GYRO_Y_MEAN,
		HEADER_GYRO_Y_MAX,
		HEADER_GYRO_Y_MIN,
		HEADER_GYRO_Y_VARIANCE,
		HEADER_GYRO_Z_MEAN,
		HEADER_GYRO_Z_MAX,
		HEADER_GYRO_Z_MIN,
		HEADER_GYRO_Z_VARIANCE
	};
}
