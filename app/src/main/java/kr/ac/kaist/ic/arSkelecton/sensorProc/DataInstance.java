package kr.ac.kaist.ic.arSkelecton.sensorProc;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DataInstance implements Cloneable{

	private long unixtime;	
	private String classLabel; 
	private float values[];

	public DataInstance(){
		this.unixtime = System.currentTimeMillis();
	}

	public DataInstance(long unixtime, float values[]){
		this.unixtime = unixtime;
		this.values = values.clone();
	}
/*
	public void setValues(){
		this.values = values;
	}
*/
	public float[] getValues(){
		return values;
	}

	public void setLabel(String label){
		this.classLabel = label;
	}

	public String getLabel(){
		return classLabel;
	}

	public long getUnixtime (){
		return unixtime;
	}

	public float getMagnitude(){
		float[] values = getValues();		
		return (float) Math.sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2]);
	}

	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(unixtime + "," + classLabel);

		if(values != null){
			for(int i=0; i<values.length; i++){
				sb.append(","+values[i]);
			}
		} else {
			sb.append("no values");
		}
		return sb.toString();
	}

	public String toPrettyString(){
		StringBuilder sb = new StringBuilder();
		{
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(unixtime);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String formattedDate = sdf.format(cal.getTime());		
			sb.append(formattedDate);
		}
		sb.append("." + (unixtime % 1000));	
		for(int i=0; i<values.length; i++){
			sb.append(", " + values[i]);
		}
		sb.append(", " + classLabel);

		return sb.toString();
	}

	public DataInstance clone() throws CloneNotSupportedException {
		DataInstance di = (DataInstance) super.clone();

		di.unixtime = this.unixtime;
		if(classLabel != null)
			di.classLabel = this.classLabel;
		if(values != null)
			di.values = this.values.clone();

		return di;
	}

}




