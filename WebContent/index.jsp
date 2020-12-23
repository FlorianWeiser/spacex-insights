<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"
    import="main.Main,java.util.Map,java.util.Locale,java.time.Month,java.time.format.TextStyle"%>
<!DOCTYPE html>
<html>
<head>
<link href="style.css" rel="stylesheet" type="text/css">
<meta charset="ISO-8859-1">
<title>SpaceX Insights</title>
</head>
<body>
	<!-- image free for commercial use, no attribution required - https://pixabay.com/photos/earth-lights-environment-globe-1149733/ -->
	<div id=bg></div>
	<div id="header">
		<h1>SpaceX Insights</h1>
	</div>
	<div id=content>
		<div class="split left">
			<div class="infobox">
				<h2>Total Payloads</h2>
				<p class="description">Total payload sent to space by each rocket type</p>
				<div class="separator"></div>
				<%
					Main main = new Main();
					System.out.println("site reloaded");
					Map<String, Integer> rocketPayloads = main.getRocketPayloads();
					for(String rocket : rocketPayloads.keySet()){
						out.println("<div class=\"numberbox\">");
						out.println("<p class=\"number\">" + rocketPayloads.get(rocket) + " kg</p>");
						out.println("<p class=\"numbertext\">" + rocket + "</p>");
						out.println("</div>");
					}
				%>
			</div>
			<div class="infobox">
				<h2>Core Status Probability</h2>
				<div class="separator"></div>
				<%
					Map<String, Double> sp = main.getCoreStatusProbability();
					for(String status : sp.keySet()) {
						out.println("<div class=\"numberbox\">");
						out.println("<p class=\"number\">" + String.format("%.2f", 100 * sp.get(status)) + "%</p>");
						out.println("<p class=\"numbertext\">" + status + "</p>");
						out.println("</div>");
					}
				%>
			</div>
		</div>
		<div class="split right">
			<div class="infobox">
				<h2>Persons Sent To Space</h2>
				<div class="separator"></div>
				<%
					out.println("<p class=\"number\">" + main.personsInSpace() + "</p>");
				%>
			</div>
			<div class="infobox">
				<h2>Launches By Month</h2>
				<div class="separator"></div>
				<%
					Map<String, Long> lpm = main.getLaunchesPerMonth();
					for(int i = 1; i <= 12; i++) {
						String month = Month.of(i).getDisplayName(TextStyle.FULL, Locale.US);
						out.println("<p>" + month + ": " + lpm.get(String.format("%02d", i)) + "</p>");
					}
				%>
			</div>
		</div>
	</div>
</body>
</html>