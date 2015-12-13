package edu.upenn.ese519.project.serial;

import java.util.HashMap;

public class SensorData {

	private static final String T_KEY = "field1";	//temperature
	private static final String H_KEY = "field2";	//humidity
	private static final String SM_KEY = "field3";	//soil moiture
	private static final String L_KEY = "field4";	//luminosity 
	private static final String F_KEY = "field5";	//flow
	private HashMap<String, String> sensorMap;
	
	public SensorData(String serialString)
	{
		sensorMap = new HashMap<String, String>();
		String[] sensorReadings = serialString.split(";");
		for(String sensorReading : sensorReadings)
		{
			String type = sensorReading.split("=")[0].trim();
			String value = sensorReading.split("=")[1].trim();
			sensorMap.put(type, value);
		}
	}
}
