package edu.upenn.ese519.project.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.ese519.project.serial.SerialInterface;

public class WebServer extends HttpServlet {

	private static final long serialVersionUID = -7484271306737330384L;
	private static final long DAY_VALIDATION_PERIOD = 600000;//1800000;
	private static final String css = "<head>" + "<style>" + "table, th, td {"
			+ "    border: 1px solid black;" + "    border-collapse: collapse;"
			+ "}" + "th, td {" + "    padding: 5px;" + "}" + "</style>"
			+ "</head>";
	private String color = "#ffffff";
	private String fontColor = "black";
	private int resultCount;
	private SerialInterface serial;
	private Timer timer;
	private String tempSet = "";
	private String humiditySet = "";
	private String lightSet = "";
	private String soilMoisture1Set = "";
	private String soilMoisture2Set = "";
	private TimerTask dayTask = new TimerTask() {
		@Override
		public void run() {
			synchronized (serial) {
				Calendar c = Calendar.getInstance();
				int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
				if (timeOfDay >= 6 && timeOfDay < 17) {
					System.out.println("day");
					serial.write("$d=true;#");
					color = "#ffffff";
					fontColor = "black";
				} else {
					System.out.println("night");
					serial.write("$d=false;#");
					color = "#686868";
					fontColor = "white";
				}
			}
		}
	};

	public void init() {
		serial = new SerialInterface(getServletConfig()
				.getInitParameter("port"));
		serial.getReadThread().start();
		resultCount = 50;
		timer = new Timer();
		timer.schedule(dayTask, 0, DAY_VALIDATION_PERIOD);
	}

	public void destroy() {
		serial.setRun(false);
		try {
			serial.getReadThread().interrupt();
			serial.getReadThread().join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		String pathInfo = request.getPathInfo();
		StringBuilder pageContent = new StringBuilder();
		if (pathInfo == null || pathInfo.equalsIgnoreCase("/")) // homepage
		{
			response.sendRedirect("/");
		} else if (pathInfo.equalsIgnoreCase("/thresholds")) {
			pageContent.append("received a request to change thresholds<br>");
			pageContent.append("temperature - "
					+ request.getParameter("temperature") + "<br>");
			pageContent.append("humidity - " + request.getParameter("humidity")
					+ "<br>");

			String humidity = request.getParameter("humidity");
			String temperature = request.getParameter("temperature");
			String light = request.getParameter("light");
			String soilMoisture1 = request.getParameter("soilmoisture1");
			String soilMoisture2 = request.getParameter("soilmoisture2");
			StringBuilder thresholdString = new StringBuilder();
			thresholdString.append("$");
			if (humidity != null && !humidity.isEmpty()) {
				thresholdString.append("h=" + humidity + ";");
			}
			if (temperature != null && !temperature.isEmpty()) {
				thresholdString.append("t=" + temperature + ";");
			}
			if (light != null && !light.isEmpty()) {
				thresholdString.append("l=" + light + ";");
			}
			if (soilMoisture1 != null && !soilMoisture1.isEmpty()) {
				thresholdString.append("sm1=" + soilMoisture1 + ";");
			}
			if (soilMoisture2 != null && !soilMoisture2.isEmpty()) {
				thresholdString.append("sm2=" + soilMoisture2 + ";");
			}
			thresholdString.append("#");
			pageContent.append("Pushing to control mbed - "
					+ thresholdString.toString() + "<br>");
			synchronized (serial) {
				serial.write(thresholdString.toString());
			}
		} else if (pathInfo.equalsIgnoreCase("/resultcount")) {
			String resultCountString = request.getParameter("result");
			resultCount = Integer.valueOf(resultCountString);
		} else if (pathInfo.equalsIgnoreCase("/preset1")) {
			String type = request.getParameter("preset");
			if (type.equals("onion")) {
				tempSet = "20";
				humiditySet = "30";
				soilMoisture1Set = "0.8";
				lightSet = "20";
			} else if (type.equals("lettuce")) {
				tempSet = "15";
				humiditySet = "30";
				soilMoisture1Set = "0.6";
				lightSet = "20";
			} else if (type.equals("beans")) {
				tempSet = "25";
				humiditySet = "50";
				soilMoisture1Set = "0.5";
				lightSet = "20";
			}
		} else if (pathInfo.equalsIgnoreCase("/preset2")) {
			String type = request.getParameter("preset");
			if (type.equals("onion")) {
				tempSet = "20";
				humiditySet = "30";
				soilMoisture2Set = "0.8";
				lightSet = "20";
			} else if (type.equals("lettuce")) {
				tempSet = "15";
				humiditySet = "30";
				soilMoisture2Set = "0.6";
				lightSet = "20";
			} else if (type.equals("beans")) {
				tempSet = "25";
				humiditySet = "50";
				soilMoisture2Set = "0.5";
				lightSet = "20";
			}
		}
		response.sendRedirect("/greenhouse/status");
		PrintWriter out = response.getWriter();
		out.print("<html>" + css + pageContent.toString() + "</html>");
		response.flushBuffer();
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws java.io.IOException {

		String pathInfo = request.getPathInfo();
		StringBuilder pageContent = new StringBuilder();
		if (pathInfo == null || pathInfo.equalsIgnoreCase("/")) // homepage
		{
			pageContent.append(getHomePage());
		} else if (pathInfo.equalsIgnoreCase("/status")) {
			pageContent.append(getStatusPage());
		} else {
			pageContent
					.append("<head><title>GreenHouse</title></head>"
							+ "<body>Unknown Url<br><br>"
							+ "Please use <a href=\"/greenhouse/status\">status page</a> "
							+ "to check the status of the greenhouse !<br>"
							+ "</body>");
		}
		PrintWriter out = response.getWriter();
		out.print("<html>" + css + pageContent.toString() + "</html>");
		response.flushBuffer();
	}

	private String getHomePage() {
		return "<head><title>greenhouse</title></head>" + "<body "
				+ "><h2>greenhouse Servlet home page </h2><br><br>"
				+ "Please use <a href=\"/greenhouse/status\">status page</a> "
				+ " to check the status of the greenhouse !<br>" + "</body>";
	}

	private String getStatusPage() {
		StringBuilder pageContent = new StringBuilder();

		pageContent.append("<head><title>Greenhouse</title></head>"
				+ "<body bgcolor=" + color + "><font color=" + fontColor
				+ "><h2>Greenhouse status page </h2><br>");
		pageContent.append("<table width = \"100%\"><tr><td>");
		pageContent.append("Set Control Parameters - <br><br>"
				+ "<form action=\"/greenhouse/thresholds\" method=\"post\">"
				+ "Temperature(degree C):<br>"
				+ "<input type=\"text\" name =\"temperature\" value = \""
				+ tempSet
				+ "\"><br>"
				+ "Humidity(Relative %):<br>"
				+ "<input type=\"text\" name =\"humidity\" value = \""
				+ humiditySet
				+ "\"><br>"
				+ "Soil Moisture 1(unit):<br>"
				+ "<input type=\"text\" name =\"soilmoisture1\" value = \""
				+ soilMoisture1Set
				+ "\"><br>"
				+ "Soil Moisture 2(unit):<br>"
				+ "<input type=\"text\" name =\"soilmoisture2\" value = \""
				+ soilMoisture2Set
				+ "\"><br>"
				+ "Light(lumens):<br>"
				+ "<input type=\"text\" name =\"light\" value = \""
				+ lightSet
				+ "\"><br>"
				+ "<input type=\"submit\" value =\"Submit\">"
				+ "</form><br><br>"
				+ "<form action=\"/greenhouse/resultcount\" method=\"post\">"
				+ "Number of Results per graph:<br>"
				+ "<input type=\"text\" name =\"result\"><br>"
				+ "<input type=\"submit\" value =\"Submit\">"
				+ "</form></font><br><br>");
		pageContent.append("</td>");
		pageContent
				.append("<td><br>Select Presets for pot 1 - <br><br>"
						+ "<form action=\"/greenhouse/preset1\" method=\"post\">"
						+ "<input type=\"radio\" name=\"preset\" value=\"onion\" checked>Onion"
						+ "  <br>"
						+ "  <input type=\"radio\" name=\"preset\" value=\"lettuce\">Lettuce"
						+ "  <br>"
						+ "  <input type=\"radio\" name=\"preset\" value=\"beans\">Beans"
						+ "  <br>"
						+ "<input type=\"submit\" value =\"Submit\">"
						+ "</form></td>");
		pageContent
				.append("<td><br>Select Presets for pot 2 - <br><br>"
						+ "<form action=\"/greenhouse/preset2\" method=\"post\">"
						+ "<input type=\"radio\" name=\"preset\" value=\"onion\" checked>Onion"
						+ "  <br>"
						+ "  <input type=\"radio\" name=\"preset\" value=\"lettuce\">Lettuce"
						+ "  <br>"
						+ "  <input type=\"radio\" name=\"preset\" value=\"beans\">Beans"
						+ "  <br>"
						+ "<input type=\"submit\" value =\"Submit\">"
						+ "</form></td>");
		pageContent.append("</tr></table>");

		// temperature data
		pageContent
				.append("<br><iframe width=\"400\" height=\"260\" style=\"border: 1px solid #cccccc;\" src=\"http://api.thingspeak.com/channels/64967/charts/1?width=400&height=260&results="
						+ resultCount
						+ "&dynamic=true\" ></iframe>"
						+ "<iframe width=\"400\" height=\"260\" style=\"border: 1px solid #cccccc;\" src=\"http://api.thingspeak.com/channels/68996/charts/1?width=400&height=260&results="
						+ resultCount
						+ "&dynamic=true\" ></iframe>"
						+ "<iframe width=\"400\" height=\"260\" style=\"border: 1px solid #cccccc;\" src=\"http://api.thingspeak.com/channels/68997/charts/1?width=400&height=260&results="
						+ resultCount + "&dynamic=true\" ></iframe>" + "<br>");
		// humidity data
		pageContent
				.append("<iframe width=\"400\" height=\"260\" style=\"border: 1px solid #cccccc;\" src=\"http://api.thingspeak.com/channels/64967/charts/2?width=400&height=260&results="
						+ resultCount
						+ "&dynamic=true\" ></iframe>"
						+ "<iframe width=\"400\" height=\"260\" style=\"border: 1px solid #cccccc;\" src=\"http://api.thingspeak.com/channels/68996/charts/2?width=400&height=260&results="
						+ resultCount
						+ "&dynamic=true\" ></iframe>"
						+ "<iframe width=\"400\" height=\"260\" style=\"border: 1px solid #cccccc;\" src=\"http://api.thingspeak.com/channels/68997/charts/2?width=400&height=260&results="
						+ resultCount + "&dynamic=true\" ></iframe>" + "<br>");
		// light data
		pageContent
				.append("<iframe width=\"400\" height=\"260\" style=\"border: 1px solid #cccccc;\" src=\"http://api.thingspeak.com/channels/64967/charts/3?width=400&height=260&results="
						+ resultCount
						+ "&dynamic=true\" ></iframe>"
						+ "<iframe width=\"400\" height=\"260\" style=\"border: 1px solid #cccccc;\" src=\"http://api.thingspeak.com/channels/68996/charts/3?width=400&height=260&results="
						+ resultCount
						+ "&dynamic=true\" ></iframe>"
						+ "<iframe width=\"400\" height=\"260\" style=\"border: 1px solid #cccccc;\" src=\"http://api.thingspeak.com/channels/68997/charts/3?width=400&height=260&results="
						+ resultCount
						+ "&dynamic=true\" ></iframe>"
						+ "<iframe width=\"400\" height=\"260\" style=\"border: 1px solid #cccccc;\" src=\"http://api.thingspeak.com/channels/68997/charts/4?width=400&height=260&results="
						+ resultCount + "&dynamic=true\" ></iframe>" + "<br>");

		// soil moisture 1 data
		pageContent
				.append("<iframe width=\"400\" height=\"260\" style=\"border: 1px solid #cccccc;\" src=\"http://api.thingspeak.com/channels/64967/charts/4?width=400&height=260&results="
						+ resultCount
						+ "&dynamic=true\" ></iframe>"
						+ "<iframe width=\"400\" height=\"260\" style=\"border: 1px solid #cccccc;\" src=\"http://api.thingspeak.com/channels/68996/charts/4?width=400&height=260&results="
						+ resultCount
						+ "&dynamic=true\" ></iframe>"
						+ "<iframe width=\"400\" height=\"260\" style=\"border: 1px solid #cccccc;\" src=\"http://api.thingspeak.com/channels/68997/charts/5?width=400&height=260&results="
						+ resultCount + "&dynamic=true\" ></iframe>" + "<br>");
		// soil moisture 2 data
		pageContent
				.append("<iframe width=\"400\" height=\"260\" style=\"border: 1px solid #cccccc;\" src=\"http://api.thingspeak.com/channels/64967/charts/5?width=400&height=260&results="
						+ resultCount
						+ "&dynamic=true\" ></iframe>"
						+ "<iframe width=\"400\" height=\"260\" style=\"border: 1px solid #cccccc;\" src=\"http://api.thingspeak.com/channels/68996/charts/5?width=400&height=260&results="
						+ resultCount
						+ "&dynamic=true\" ></iframe>"
						+ "<iframe width=\"400\" height=\"260\" style=\"border: 1px solid #cccccc;\" src=\"http://api.thingspeak.com/channels/68997/charts/6?width=400&height=260&results="
						+ resultCount + "&dynamic=true\" ></iframe>" + "<br>");

		// misc
		pageContent
				.append("<iframe width=\"400\" height=\"260\" style=\"border: 1px solid #cccccc;\" src=\"http://api.thingspeak.com/channels/64967/charts/6?width=400&height=260&results="
						+ resultCount
						+ "&dynamic=true\" ></iframe>"
						+ "<iframe width=\"400\" height=\"260\" style=\"border: 1px solid #cccccc;\" src=\"http://api.thingspeak.com/channels/64967/charts/7?width=400&height=260&results="
						+ resultCount
						+ "&dynamic=true\" ></iframe>"
						+ "<iframe width=\"400\" height=\"260\" style=\"border: 1px solid #cccccc;\" src=\"http://api.thingspeak.com/channels/68997/charts/7?width=400&height=260&results="
						+ resultCount + "&dynamic=true\" ></iframe>" + "<br>");
		pageContent.append("</body>");

		tempSet = "";
		humiditySet = "";
		soilMoisture1Set = "";
		soilMoisture2Set = "";
		lightSet = "";
		return pageContent.toString();
	}
}
