# Documentation
SpaceXInsights is a web dashboard which displays interesting information about SpaceX.
This documentation contains a short description of each Java class (or other files) and their methods.

## index.jsp
Defines the content loaded by the Tomcat Server.  
The index.jsp file works like a html file, but allows using Java code in ```<% // Java Code %>``` tags.  
In this case, it instantiates a Main class, receives all processed SpaceX API data from it and prints them to the website.
Furthermore, the index.jsp file loads a style.css file, which defines the looks of the web dashboard.

## DataLoader
This class loads the required Json objects from the SpaceX API and buffers them.

* **public DataLoader()**  
DataLoader constructor.  
Sends parallel GET requests for every API information needed and stores them in a buffer.

* **public JsonArray get(JsonData jd)**  
Returns a buffered JsonArray which belongs to a JsonData object.  
@param jd	JsonData enum whose buffered JsonArray shall be returned  
@return		The associated JsonArray

## JsonData
This enum defines which values can be loaded from the SpaceX API.
The enum constants are:
* CORES("cores")
* CREW("crew")
* LAUNCHES_PAST("launches/past")
* PAYLOADS("payloads")
* ROCKETS("rockets")

The constructor string represents the url to load the associated data from the SpaceX API.

## Main
Application Main class.
Includes the main method and methods to process JsonData.

* **public static void main(String... args)**  
Main method of the SpaceXInsights console based program.  
Prints total payloads, number of persons sent to space, launches by month and core status probability.
This method is not executed when running the application on a Tomcat Server.  
@param args	program arguments. If the first argument equals "--timing", additional timing information is printed.

* **public HashMap<String, Integer> getRocketPayloads()**  
Returns the total payload sent to space by each rocket type.  
@return	A map with the rocket types as keys and the total payload sent to space by them as values

* **public int personsInSpace()**  
Returns the total number of persons sent to space.  
Launch dates must be in the past and astronauts with multiple space flights are only counted once.  
@return	the SpaceX astronaut count

* **public Map<String, Long> getLaunchesPerMonth()**  
Returns the number of launches grouped by the month in which they took place.  
@return	a map with all twelve months as keys (e.g. the month January is encoded as "01") and the number of launches in this month as associated value

* **public Map<String, Double> getCoreStatusProbability()**  
Returns which status are the most common among SpaceX cores.  
@return	a mapping of all possible core status and how likely they are (as a double in percent)
