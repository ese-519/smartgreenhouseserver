package edu.upenn.ese519.project.serial;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

public class SerialInterface {

	private static final String SENSOR_KEY = "WAGLJILGAXVPAGWK";
	private static final String ACTUATOR_KEY = "9ZB0M2WQM3PQOGVQ";
	private static final String THRESHOLD_KEY = "I5CQN7U17TZ7NU4B";
	private SerialPort serialPort;
	private boolean run = true;
	private Thread readThread = new Thread(new Runnable() {
		@Override
		public void run() {
			String bufferString = "";
			StringBuilder result = new StringBuilder();
			byte[] buffer;
			try {
				while (run) {
					try {
						synchronized (this) {
							buffer = serialPort.readBytes();
							bufferString = new String(buffer);
							for (int i = 0; i < bufferString.length(); i++) {
								if (bufferString.charAt(i) == '\n') {
									System.out.println(result.toString());
									updateThingSpeak(result.toString());
									result.setLength(0);
								} else {
									result.append(bufferString.charAt(i));
								}
							}
						}
					} catch (NullPointerException e) {
						Thread.sleep(200);
					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (SerialPortException ex) {
				System.out.println(ex);
			} finally {
				try {
					serialPort.closePort();
				} catch (SerialPortException e) {
					e.printStackTrace();
				}
			}
		}

		private void updateThingSpeak(String serialString) throws IOException {
			String type = serialString.split(":")[0];
			if (type.equals("Sensor") || type.equals("Actuator")
					|| type.equals("Threshold")) {
				String values = serialString.split(":")[1].trim();
				String key = null;
				if (type.equals("Sensor"))
					key = SENSOR_KEY;
				else if (type.equals("Actuator"))
					key = ACTUATOR_KEY;
				else
					key = THRESHOLD_KEY;
				String urlString = "http://api.thingspeak.com/update?key="
						+ key + "&" + values;
				sendUpdate(urlString);
			} else if (type.equals("Error")) {
				System.out.println(serialString);
			} else {
				System.out.println("Unknown serial string - " + serialString);
			}
		}

		private void sendUpdate(String urlString) throws IOException {
			URL obj = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'GET' request to URL : " + urlString);
			System.out.println("Response Code : " + responseCode);
			InputStream inputStream = con.getInputStream();
			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream);
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = bufferedReader.readLine()) != null) {
				response.append(inputLine);
			}
			bufferedReader.close();
			inputStreamReader.close();
			inputStream.close();
			// print result
			System.out.println(response.toString());
		}
	});

	public SerialInterface(String portName) {
		this.serialPort = new SerialPort(portName);
		try {
			serialPort.openPort();
			serialPort.setParams(9600, 8, 1, 0);
		} catch (SerialPortException e) {
			e.printStackTrace();
		}
	}

	public void write(String data) {
		try {
			// serialPort.writeString(data);
			serialPort.writeBytes(data.getBytes());
		} catch (SerialPortException e) {
			e.printStackTrace();
		}

	}

	public SerialPort getSerialPort() {
		return serialPort;
	}

	public void setSerialPort(SerialPort serialPort) {
		this.serialPort = serialPort;
	}

	public Thread getReadThread() {
		return readThread;
	}

	public void setReadThread(Thread readThread) {
		this.readThread = readThread;
	}

	public boolean isRun() {
		return run;
	}

	public void setRun(boolean run) {
		this.run = run;
	}

	@Override
	public String toString() {
		return "SerialInterface [serialPort=" + serialPort + ", run=" + run
				+ ", readThread=" + readThread + "]";
	}

	public static void main(String[] args) throws SerialPortException,
			InterruptedException {
		String[] portNames = SerialPortList.getPortNames();
		for (int i = 0; i < portNames.length; i++) {
			System.out.println(portNames[i]);
		}
		SerialInterface serial = new SerialInterface("COM4");
		System.out.println("start");
		serial.readThread.start();
		//String data = "$t=30;h=34;sm=60;l=8;#";
		String data = "$d=true;#";

		while (true) {
			//serial.write(data);
			Thread.sleep(5000);
		}
	}
}