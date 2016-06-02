package kr.ac.kaist.ic.arSkelecton.test;

import junit.framework.TestCase;

import kr.ac.kaist.ic.arSkelecton.sensorProc.DataInstance;
import kr.ac.kaist.ic.arSkelecton.sensorProc.DataInstanceList;
import kr.ac.kaist.ic.arSkelecton.sensorProc.FeatureGenerator;
import kr.ac.kaist.ic.arSkelecton.sensorProc.SlidingWindow;

/**
 * 
 * @author Jeungmin Oh
 * Test case for feature generator
 *
 */
public class MainActivityTest extends TestCase {
	// Ref : http://www.vogella.com/tutorials/JUnit/article.html
	
	final static String TAG = "MainActivityTest";

	public MainActivityTest(String name){
		super(name);

		{ // Test FeatureGenerator
			//testCalculateMean();
			//testCalculateMax();
			//testCalculateMin();
			//testCalculateVariance();
		}
	}

	public void testSlidingWindow() {
		SlidingWindow sw = new SlidingWindow(500, 250);

		long[] timeWindow = {
				101, 103, 104, 133, 148, 149, 200, 
				270, 278, 300, 350, 450, 
				650, 655, 
				769, 892, 999, 
				1000, 1232, 1233, 1239, 1240, 
				1400, 1444, 1449, 1455, 1500, 1510
		};

		/*
		 * Window #1 [0,500) : 101, 103, 104, 133, 148, 149, 200, 278, 300, 350, 450
		 * Window #2 [250,750) : 270, 278, 300, 350, 450, 650, 655
		 * Window #3 [500,1000) : 650, 655, 769, 892, 999
		 * Window #4 [750, 1250) : 769, 892, 999, 1000, 1232, 1233, 1239, 1240
		 * Window #5 [1000, 1500) : 1000, 1232, 1233, 1239, 1240, 1400, 1444, 1449, 1455
		 */

		for(long unixtime : timeWindow){
			sw.input(new DataInstance(unixtime, null)); // Create DataInstance without data
		}

		DataInstanceList window0 = sw.output();
		DataInstanceList window1 = sw.output();
		DataInstanceList window2 = sw.output();
		DataInstanceList window3 = sw.output();
		DataInstanceList window4 = sw.output();
		DataInstanceList window5 = sw.output();

		// Note that the initial window has minus time id (should it be fixed?)

		// Test for window's time id
		assertEquals(-250, window0.getTimeId());
		assertEquals(0, window1.getTimeId());
		assertEquals(250, window2.getTimeId());
		assertEquals(500, window3.getTimeId());
		assertEquals(750, window4.getTimeId());

		// Test for size and unixtime of first item in a window
		assertEquals(101, window0.get(0).getUnixtime()); // [101, 103, 104, 133, 148, 149, 200]
		assertEquals(7, window0.size());
		assertEquals(101, window1.get(0).getUnixtime()); // [101, 103, 104, 133, 148, 149, 200, 270, 278, 300, 350, 450] 
		assertEquals(12, window1.size());
		assertEquals(270, window2.get(0).getUnixtime()); // [270, 278, 300, 350, 450, 650, 655] 
		assertEquals(7, window2.size());
		assertEquals(650, window3.get(0).getUnixtime()); // [650, 655, 769, 892, 999]
		assertEquals(5, window3.size());
		assertEquals(769, window4.get(0).getUnixtime()); // [769, 892, 999, 1000, 1232, 1233, 1239, 1240]
		assertEquals(8, window4.size());
		assertEquals(1000, window5.get(0).getUnixtime()); // [1000, 1232, 1233, 1239, 1240, 1400, 1444, 1449, 1455]
		assertEquals(9, window5.size());

		// FIX : should be added more complex cases
	}

	public void testCalculateVariance() {		
		assertEquals(FeatureGenerator.calculateVariance(new float[]{20, 20, 20}), 0.0f);
		assertEquals(FeatureGenerator.calculateVariance(new float[]{1, 2, 3, 4, 5}), 2.0f);		
	}

	public void testCalculateMin() {
		assertEquals(FeatureGenerator.calculateMin(new float[]{1, -1}), -1.0f);
		assertEquals(FeatureGenerator.calculateMin(new float[]{0, 1, 2, 4, 3}), 0.0f);
	}

	public void testCalculateMax() {
		assertEquals(FeatureGenerator.calculateMax(new float[]{1, -1}), 1.0f);
		assertEquals(FeatureGenerator.calculateMax(new float[]{0, 1, 2, 4, 3}), 4.0f);

	}

	public void testCalculateMean() {
		assertEquals(FeatureGenerator.calculateMean(new float[]{20, 20, 20}), 20.0f);
		assertEquals(FeatureGenerator.calculateMean(new float[]{0, 1, 2, 4, 3}), 2.0f);

	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
