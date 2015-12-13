# Smart Greenhouse Server repository

This repository holds the code for the web server used in the smart greenhouse. The code is divided into 2 main components i.e. a java web servlet and a java serial library.

The Serial library is based on open source JSSC libraries. The JSSC functionalities are extended by allowing the user to read lines and write lines to the serial port. The library is instantiaed by the web servlet.

The java web servlet can be run within any servlet container like Jetty or Tomcat. The servlet is configured with a web.xml such that it runs on http://ip:port/greenhouse/status. The ip:port combination is for the machine within which this servlet is deployed. The webservlet instantiates the serial library that starts a thread to continously monitor the serial port for incoming communication. Further, the servlet also sends new commands to the mbed using serial.write methods.



