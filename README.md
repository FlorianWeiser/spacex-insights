# SpaceX Insights
A small Java Server Pages project to display interesting SpaceX information in a web dashboard.

Clone this repository with
```
git clone https://github.com/FlorianWeiser/spacex-insights.git
```

# Run project
To run this project, you need to have an [Apache Tomcat server](https://tomcat.apache.org/download-90.cgi) installed.

Next, copy the SpaceXInsights.WAR file in the webapps directory of Tomcat and start the server.

You should then be able to access the web dashboard, e.g. under the url http://localhost:8080/SpaceXInsights/.

# Modify project (in Eclipse)
To modify the project in Eclipse, you can import it with ```Import > Existing Maven Projects```.

After making changes, you can create a new .WAR file with ```Export > Web > War File```. Alternatively, you can [install a Tomcat server in Eclipse](https://www.eclipse.org/webtools/jst/components/ws/1.5/tutorials/InstallTomcat/InstallTomcat.html) and run the project there.
