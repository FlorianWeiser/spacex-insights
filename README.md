# SpaceX Insights
A small Java Server Pages project to display interesting SpaceX information in a web dashboard.

# Clone repository
Clone this repository with
```
git clone https://github.com/FlorianWeiser/spacex-insights.git
```

# Run on a Tomcat server
To run this project, you need to have an [Apache Tomcat server](https://tomcat.apache.org/download-90.cgi) installed.

Next, copy the SpaceXInsights.WAR file (from the export directory) in the webapps directory of Tomcat and start the server.

You should then be able to access the web dashboard, e.g. under the url http://localhost:8080/SpaceXInsights/.

# Run in console mode
The SpaceX Insights information can also be viewed from console without setting up a Tomcat server.

Navigate to the export directory and execute the command ```java -jar SpaceXInsights.jar``` to test this feature.

# Modify project (in Eclipse)
This section explains, how to SpaceXInsights Dashboard can be edited in Eclipse. The project, however, isn't limited to this IDE.

To modify the project in the Eclipse IDE for Java EE Developers, import it with ```Import > Existing Maven Projects```.

After making changes, a new .WAR file can be created with ```Export > Web > War File```. Alternatively, it is possible to [install a Tomcat server in Eclipse](https://www.eclipse.org/webtools/jst/components/ws/1.5/tutorials/InstallTomcat/InstallTomcat.html) and run the project there.
