package kr.ac.kaist.ic.arSkelecton.sensorProc;

import android.util.Log;

import java.util.ArrayList;
import java.util.Vector;


/*
 * This class performs sliding window for real time sensor data
 * It accepts DataInstance with input(), and outputs the DataInstanceList through output() when window is ready 
 */
public class SlidingWindow {

	final static String TAG = "SlidingWindow";

	//DataInstanceList dataList;
	Vector<DataInstanceList> outputBuffer; // Should be thread-safe

	int windowSize, stepSize; // ms

	public SlidingWindow(int windowSize, int stepSize){
		this.windowSize = windowSize;
		this.stepSize = stepSize;
		
		//dataList = new DataInstanceList(); 
		outputBuffer = new Vector<DataInstanceList>();
	}

	/**
	 * 
	 * @param DataInstance di The raw data from sensors. 
	 */
	public void input(DataInstance di) {
		//Log.d(TAG, "input() : " + di.toPrettyString());
		//dataList.add(di);

		Log.d(TAG, "Input : " + di.toString());		

		long time = di.getUnixtime();
		 
		/*
		 * HOW 'long[] timeIds' works
		 *   Case 1. windowSize : 1000ms, stepSize : 500ms => A sample will be used twice
		 *   Case 2. windowSize : 1000ms, stepSize : 250ms => A sample will be used four times
		 *   NOTE : 'windowSize % stepSize' should be '0'
		 */

		int numSampleUsed = windowSize / stepSize;
		
		long timeRounded = time - (time % stepSize);
		long timeIdStart = timeRounded - stepSize * (numSampleUsed - 1); // Used as time id

		ArrayList<Long> timeIdList = new ArrayList<Long>();
		for(int i=0; i<numSampleUsed; i++){
			long timeId = timeIdStart + (i * stepSize);
			//Log.d(TAG, "This sample will be used window " + timeId);
			timeIdList.add(timeId);
		}
		
		/*
		 * 1. If window already exists
		 */
		for(int i=0; i<outputBuffer.size(); i++){
			DataInstanceList dataList = outputBuffer.get(i);
			long timeId = dataList.getTimeId();
			if(timeIdList.contains(timeId)){ // Found buffer that corresponds timeId
				try {
					dataList.add(di.clone());
				} catch (CloneNotSupportedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				timeIdList.remove(timeId);
			}
		} // FIX : If output buffer is not fetched for a long time, it will be very costly (not incremental)
		
		/*
		 * 2. If window does not exist
		 */
		for(long timeIdNew : timeIdList){
			DataInstanceList dataList = new DataInstanceList(timeIdNew);
			try {
				dataList.add(di.clone());
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			outputBuffer.add(dataList);
		}
		
	}

	/**
	 * @return It returns the oldest window. If nothing ready yet, it returns null
	 */
	public DataInstanceList output(){
		if(outputBuffer.size() > windowSize / stepSize){			
			DataInstanceList dl = outputBuffer.get(0);
			outputBuffer.remove(0);
			Log.v(TAG, "----------Output----------");
			
			for(int i=0; i<dl.size(); i++){
				DataInstance di = dl.get(i);
				Log.v(TAG, "Output : " + di.toString());
			}
			Log.v(TAG, "----------//Output----------");
			
			return dl;
		} else {
			return null;
		}
	}

    public boolean isBufferReady() {
        return (outputBuffer.size() > windowSize / stepSize);
    }

    public long getHeadTimeId() {
        if (outputBuffer.size() == 0) return 0;
        return outputBuffer.firstElement().getTimeId();
    }

    public void removeFirst() {
        if (outputBuffer.size() == 0) return;
        outputBuffer.remove(0);
    }

}
