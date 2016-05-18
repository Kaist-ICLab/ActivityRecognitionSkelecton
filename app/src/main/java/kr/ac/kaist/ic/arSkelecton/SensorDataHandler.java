package kr.ac.kaist.ic.arSkelecton;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import kr.ac.kaist.ic.arSkelecton.classifierWrapper.ClassifierWrapper;
import kr.ac.kaist.ic.arSkelecton.sensorProc.DataInstance;
import kr.ac.kaist.ic.arSkelecton.sensorProc.DataInstanceList;
import kr.ac.kaist.ic.arSkelecton.sensorProc.FeatureGenerator;
import kr.ac.kaist.ic.arSkelecton.sensorProc.SlidingWindow;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;


/**
 * Created by mtjddnr on 2016. 5. 18..
 */
public class SensorDataHandler {

    final static String TAG = "SensorDataHandler";

    private Context context;
    private SensorManager sensorManager;

    private boolean running;
    private HandlerThread sensorThread;
    private Handler sensorHandler;

    private String classLabel; // Optional value, only necessary for data collection
    private DataInstanceList dlAcc, dlGyro = new DataInstanceList(); // For raw data save purpose
    private SlidingWindow slidingWindowAcc, slidingWindowGyro; // For extracting samples by window

    public SensorDataHandler(Context context) {
        this.context = context;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);


        this.dlAcc = new DataInstanceList();
        this.dlGyro = new DataInstanceList();

        // Sliding window
        slidingWindowAcc = new SlidingWindow(Constants.WINDOW_SIZE, Constants.STEP_SIZE);
        slidingWindowGyro = new SlidingWindow(Constants.WINDOW_SIZE, Constants.STEP_SIZE);

        //Background Thread
        sensorThread = new HandlerThread("Sensor thread", Thread.MAX_PRIORITY);
        sensorThread.start();
        sensorHandler = new Handler(sensorThread.getLooper());
    }

    public String getClassLabel() {
        return this.classLabel;
    }
    public void setClassLabel(final String classLabel) {
        sensorHandler.post(new Runnable() {
            @Override
            public void run() {
                SensorDataHandler.this.classLabel = classLabel;
            }
        });
    }

    public void clearData() {
        sensorHandler.post(new Runnable() {
            @Override
            public void run() {
                dlAcc = new DataInstanceList();
                dlGyro = new DataInstanceList();
            }
        });
    }

    public void start() {
        if (running) return;
        running = true;

        sensorManager.registerListener(eventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), Constants.SENSOR_DELAY, sensorHandler);
        sensorManager.registerListener(eventListener, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), Constants.SENSOR_DELAY, sensorHandler);
    }

    public void stop() {
        if (!running) return;
        running = false;
        sensorManager.unregisterListener(eventListener);
    }

    public void saveRawDataToCSV() {
        sensorHandler.post(new Runnable() {
            @Override
            public void run() {
                String fileNameAcc = Constants.PREFIX_RAW_DATA + System.currentTimeMillis() + "_" + classLabel + "_acc.txt";
                dlAcc.saveToCsvFile(fileNameAcc);

                String fileNameGyro = Constants.PREFIX_RAW_DATA + System.currentTimeMillis() + "_" + classLabel + "_gyro.txt";
                dlGyro.saveToCsvFile(fileNameGyro);
            }
        });
    }

    private SensorEventListener eventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            assert (Looper.getMainLooper().getThread() != Thread.currentThread()) : "Should not run on main thread";

            if (running == false) return;
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER: {
                    float[] values = event.values;
                    float x = values[0];
                    float y = values[1];
                    float z = values[2];

                    DataInstance diAcc = new DataInstance(System.currentTimeMillis(), values);
                    diAcc.setLabel(classLabel); // Optional field
                    dlAcc.add(diAcc); // Save for raw data backup
                    slidingWindowAcc.input(diAcc);

                    //Log.d(TAG, "Acc : " + x + "," + y + "," + z);
                    break;
                }
                case Sensor.TYPE_GYROSCOPE: {
                    float[] values = event.values;
                    float x = values[0];
                    float y = values[1];
                    float z = values[2];

                    DataInstance diGyro = new DataInstance(System.currentTimeMillis(), values);
                    diGyro.setLabel(classLabel); // Optional field
                    dlGyro.add(diGyro); // Save for raw data backup
                    slidingWindowGyro.input(diGyro);

                    //Log.d(TAG, "Gyro : " + x + "," + y + "," + z);
                    break;
                }
            }
            processWindowBuffer();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public interface DataAdaptor {
        public void slidingWindowData(String classLabel, DataInstanceList dlAcc, DataInstanceList dlGyro);
    }

    public DataAdaptor dataAdaptor;

    private void processWindowBuffer() {
        // Fetching a slices of sliding window
        DataInstanceList dlAcc = slidingWindowAcc.output();
        DataInstanceList dlGyro = slidingWindowGyro.output();

        if (dlAcc == null || dlGyro == null) return;

        if (dlAcc.getTimeId() != dlGyro.getTimeId()) {
            Log.e(TAG, "Sample are not synced!"); // Issue : What if not synced (Very rare case) => Ignored
            return;
        }

        if (dataAdaptor == null) return;
        dataAdaptor.slidingWindowData(this.classLabel, dlAcc, dlGyro);
    }
}
