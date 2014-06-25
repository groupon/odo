# Overview
Odo is a proxy server that can serve as a mock server or allow for manipulation of live data. Odo features dynamic configuration and plugin support for http request/response override behaviors.

Benefits
* Flexible: Multiple configurations can be run simultaneously
* Extensible: Plugin support for custom override behaviors
* Dynamic: Odo configuration can be changed through UI or through REST interface

# Getting Started
## Odo Concepts
* Path: Path defines an API endpoint and the actions (overrides) to perform. The path is specified by friendly name, path value, request type.
* Profile: A profile is a collection of paths.
* Client: A client is an instance of a profile. Clients share the same path definitions, but overrides are specific to a client. This allows multiple users to access a centralized Odo server with their own custom configuration.
* Override: An action to perform on a given endpoint. The actions could be to return a custom response, add a delay, return a specific response code, modify response data, etc.
* Plugin: Odo is extensible through plugins. You can provide your own override behaviors by creating an Odo plugin.

## Download Odo
To try out Odo without needing to download the source and package it, check out the [releases](https://github.com/groupon/odo/releases) for a prepackaged odo.war. Also included are a sample configuration and sample plugin. Import the sample (instructions below) to try out Odo with minimal steps.

### Prepackaged setup
1. Create a "plugins" directory at the odo.war location.
2. Place the plugin jar file in the plugins directory
3. Start Odo by running `java -Xmx1024m -jar odo.war`
4. Import the sample configuration by running `curl -X POST -F fileData=@backup.json http://localhost:8090/testproxy/api/backup`
5. View the Odo UI at `http://localhost:8090/testproxy`

## Package Odo
From the repo root, run

```
mvn clean install
mvn clean package
```

The `odo.war` will be created at `proxyui/target`.

## Run Odo
From the location where you have odo.war, run

```
java -Xmx1024m -jar odo.war
```

If you have Odo plugins, place them in a "plugins" directory under the Odo.war location.

## Using Odo
### Odo UI
Odo UI is available at http://localhost:8090/testproxy

#### UI Quickstart: Setting up a Profile
If you are starting from a fresh install, there are a few preliminary steps before actually configuring an override.

1. **Create a profile.** When you navigate to `http://localhost:8090/testproxy` you are presented with the profile list. Initially it will be empty. To create a new Profile, click the '+' icon below the list and give the new profile a name in the dialog that appears. Select the newly-created profile by clicking on the '>' button to the right of the the profile name. Finally, click the "Activate Profile" button at the top of the profile page.
2. **Add an API server.** An API server is needed to determine which requests to handle. When a request enters Odo, the request hostname is compared to the API Servers configured. If the request hostname is not in Odo's configured hostnames, Odo will simply pass the request through without processing it.

  To add a host, click the '+' button under the "API Servers" list and fill in the Source Hostname and Destination Hostname/IP. For a live server, the source would be the actual hostname (domain.com) and the destination will be the actual IP address of the host. For this example, add Source Hostname "localhost" and Destination "blackhole". When a custom response is enabled on a path that request is never actually sent to the destination, so the destination host will not matter in this case.
3. **Create a path.** Click on the '+' button under the Paths list to add a new path. A dialog prompting for path name, path value, and path type will show. Path name can be any name for you to identify it. Enter "Test path" for path name. Path value is a regular expression of the endpoint, so "/test" will match a response directed to "localhost/test". Enter "/test" for this value. Select "GET" for the path type.

#### UI Quickstart: Creating a Custom Response
1. **Add the Custom Response override.** Clicking on your newly added path's name will display a details view. The "Response" tab contains the configuration for your response overrides. A Response Override is for modifying the data received from an API server before sending that data back to the requestor. This is also where we enable a custom response to act as a mock server.

  From the "Add Override" dropdown list, select "Custom Response". This will add the "Custom Response" override to the list of enabled overrides and enable the "Response" column in the Paths list.
2. **Configure the override.** With your newly added override selected, the override parameters are displayed next to the "Overrides" list. For our custom response, we have "response" and "repeat count" parameters. The response parameter is the response content to return to the requestor. Enter "test content" here. The repeat count is a parameter that exists for all overrides. It is the number of times an override will be executed before it is disabled. -1 is infinite. Click "Apply" to update the override.
3. **Test it.** Verify the override is working by sending a request or navigating to http://localhost:8082/test. You will see that your response content "test content" is returned to you.

#### UI Quickstart: Sample configuration
Included in the examples directory is a sample configuration backup. The sample configuration uses the sample plugin. After running `mvn package` the sample plugin will be located at `examples/plugin/target/plugin-*.jar`. Create a directory called `plugins` at the location of the `odo.war` and copy the sample plugin jar there.

You can then import the backup.json data and view a sample Odo configuration.

```
curl -X POST -F fileData=@backup.json http://localhost:8090/testproxy/api/backup
```

Note that the API Servers contains a single entry. The source host is "localhost" and the destination host is "blackhole". This implies that requests sent to "localhost" will be considered for overrides. The destination host is "blackhole" simply because this is a host that does not resolve. For a path with a custom override enabled the destination host will not be resolved.

### Default Odo Ports
* 8090: API - access the Odo UI, Odo configuration REST endpoints (http://localhost:8090/testproxy/api/)
* 8082: HTTP proxy - handles HTTP traffic
* 8012: HTTPS proxy - handles HTTPS traffic
* 9090: Forwarding port. You can send HTTP and HTTPS requests to this port and Odo will forward them to the correct port according to the protocol
* 9092: Database

Each of these ports can be configured by setting an environment variable with the desired port value before startup.
* API: ODO\_API\_PORT
* HTTP: ODO\_HTTP\_PORT
* HTTPS: ODO\_HTTPS\_PORT
* Forwarding: ODO\_FWD\_PORT
* Database: ODO\_DB\_PORT

### Create a Configuration Backup
To export an existing configuration from Odo, simply gather the output from `http://localhost:8090/testproxy/api/backup` while Odo is running.

### Import a Configuration Backup
Where `backup.json` is the data gathered from `http://localhost:8090/testproxy/api/backup`,

```
curl -X POST -F fileData=@backup.json http://localhost:8090/testproxy/api/backup
```

### Exporting SSL Certificates
The following only applies if you are using the port 9090 forwarding proxy.

In some cases you will need to install the SSL certificates that Odo is signing with in order to use Odo as a proxy.  An example is that Windows Phone will only use untrusted SSL connections if the certificate in use is installed on the device.  The certificate for a domain can be exported/generated with the following URL (replace odohost with your Odo host and myurl.com with the target hostname):

```
http://odohost:8090/testproxy/cert/myurl.com
```

The certificates are regenerated after some time due to expiration.  Additionally since certificates are generated on the fly they will differ if you launch Odo from a different directory or a different machine.

### Samples
The examples directory contains samples to help get you started with Odo
* backup.json: A sample configuration that can be imported to demonstrate some basic features of Odo.
* api-usage: Example code demonstrating modifying an Odo configuration through the Java client.
* plugin: Example code demonstrating how you can extend Odo's functionality by adding your own override behaviors.

# Development Setup
This section will go over how to setup STS (Spring Tool Suite) for proxy development.  It assumes you have already checked the code out.
## STS Setup
1. Install the latest version of STS 3 (>=3.5 ) from SpringSource
2. Install JDK/OpenJDK 7 and set JAVA_HOME appropriately if you are on OS X
3. Import all maven projects individually into STS (it does not do well with aggregator POMs) - Each maven project is a subdirectory of the TestProxy directory (proxyui, proxylib, proxyserver, hostsedit, plugins, browsermob-proxy)
	1. File -> Import
	2. Select Maven -> Existing Maven Projects
	3. Open the pom.xml from the subdirectory
	4. Repeat for each project
4. Open HomeController.java from the proxyui project, right click in the main method and run or debug as Spring Boot
5. Setup the host editor instance (Optional for a development setup)
	1. In <repo root>/hostsedit run: mvn assembly:single
	2. Run: sudo java -jar <repo root>/hostsedit/target/hostsedit-jar-with-dependencies.jar

# Plugins
## Override Types
An override is simply a static function with an annotation specifying the type of override it is.  Currently available override types are:

- @ResponseOverride (com.groupon.odo.plugin.ResponseOverride) - Overrides response data from an HTTP request.  Currently only passes string data to the function.
- @RequestOverride (com.groupon.odo.plugin.RequestOverride) - Not currently defined - Only custom overrides are supported so far (query replacement)

## Maven Project For Plugins
1. Create a new maven project
2. Add the following dependency (you will have it if you compiled everything in the TestProxy directory with `mvn clean install`):

	```xml
	<dependency>
		<groupId>com.groupon</groupId>
		<artifactId>proxyplugin</artifactId>
		<version>1.0.0-beta.1</version>
	</dependency>
	```

3. Your plugin project can depend on any other maven jars that you need.  Building a jar with all dependencies is described later on.
4. Add an entry to the maven-assembly-plugin configuration section of pom.xml as follows.  Replace com.groupon.odo.sample with the appropriate package.

	```xml
	<archive>
		<manifestEntries>
			<Plugin-Package>com.groupon.odo.sample</Plugin-Package>
		</manifestEntries>
	</archive>
	```


### Override Class and Simple Override Method
1. Add a new class to your project to hold your override methods.  Generally you will want to group methods into classes by functional area (ex: a class to override all requests that return a list of deals)
2. Import the appropriate override type (ex: import com.groupon.odo.plugin.ResponseOverride)
3. Add a static method that returns a string with a single String parameter and the following annotation.  Name your function something that is meaningful:

	```java
	@ResponseOverride(
				httpCode=200,
				description="Replace a with b")
	public static String replaceLetter(String source) throws Exception {
		return source.replace('a', 'b');
	}
	```

4. Notice that the annotation allows for custom httpCodes and some descriptive text.  This should be set as necessary.

### Paramaterized Override Method
An override method can take multiple arguments.  Currently support argument types are:
* Integer
* String
* Boolean

The annotation supports an additional item which is "parameters".  This is an array of strings which will be shown in the proxy configuration UI to help out users.


An example method is:

```java
@ResponseOverride(
			httpCode=200,
			description="Replace string with integer",
			parameters={"letter", "number", "replace"
			})
public static String replaceLetter(String source,
                                   String letter,
                                   Integer number,
                                   Boolean replace) throws Exception {
	String returnVal = source;
	if (replace) {
		returnVal = returnVal.replace(letter, number.toString());
	}
	return returnVal;
}
```

To configure the arguments through the Proxy UI you simply double click on the enabled override in the enabled overrides list for a path.  A UI will appear allowing you to set parameters.

### Building a plugin jar with dependencies
1. Add build plugins to your maven project

	```xml
	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-jar-plugin</artifactId>
				<configuration>
					<archive>
						<manifest>
							<addClasspath>true</addClasspath>
						</manifest>
					</archive>
				</configuration>
			</plugin>
			<plugin>
				<artifactId>maven-assembly-plugin</artifactId>
				<configuration>
					<finalName>plugins</finalName>
					<descriptorRefs>
						<descriptorRef>jar-with-dependencies</descriptorRef>
					</descriptorRefs>
				</configuration>
			</plugin>
		</plugins>
	</build>
	```

2. Build project: `mvn clean compile`
3. Build single assembly: `mvn assembly:single`
4. Grab assembly and put it in your plugin path directory: `cp target/plugins-jar-with-dependencies.jar destination_path`

### Static Resources
#### Storing static resources in a plugin jar
1. Create a "resources" directory under src
2. Put your static files in that directory
3. Add an entry to the maven-assembly-plugin configuration section of pom.xml as follows.  Replace NameHere with a unique name

	```xml
	<archive>
		<manifestEntries>
			<Plugin-Name>NameHere</Plugin-Name>
		</manifestEntries>
	</archive>
	```

4. Add an entry to the build configuration section as follows:

	```xml
	<resources>
		<resource>
	    	<directory>src</directory>
	        <includes>
	        	<include>resources/**</include>
	        </includes>
	   </resource>
	</resources>
	```

5. Compile and assemble package normally

#### Accessing stored resources
1. Resources can be accessed through the API as `/testproxy/api/resource/NameHere/fileName`

    Example HTML embed: `<img src="http://localhost:8090/testproxy/api/resource/NameHere/logo-line.jpg"/>`

2. Resource can be accessed in a plugin function using `getResourceAsStream` on the current class (example classname is `MyClass`)

	```java
	InputStream resource = MyClass.class.getResourceAsStream("/resources/resource1.json");
	```

# Scripts
Groovy scripts can be added for annotating results in the history list.  Currently failed results color the list row in red.  Scripts can be added via the proxy ui Scripts link on the History page.  Groovy variables are as follows for history script calls:

```
arg0 - Request Type
arg1 - Request URL
arg2 - Request Parameters
arg3 - Request POST Data
arg4 - Request Headers
arg5 - Response Code
arg6 - Response Content Type
arg7 - Response Headers
```

The result of a script is a List with the following entries:

```
[0] - 0 or 1 indicating success or failure
[1]+ - A message to display in the history list.  Multiple messages can be specified by adding additional list entries.
```

# Proxy
## Parallel Clients for a Profile
Odo supports multiple clients for a specific profile via a header on all requests that specifies a client UUID to use(default is -1).  The header name is "Odo-Client-UUID" and the value should be the client uuid returned from the client creation call

Refer to Configuration Interfaces section for API details

## Configuration Interfaces
### General API supported by configuration interfaces
#### Terminology

* `{profileIdentifier}` - Name or ID of the profile to edit (Ex: My%20Profile)
* `{pathIdentifier}` - Name or ID of the path to edit (Ex: My%20Path)
* `{clientUUID}` - UUID of the profile client to update, -1 is the default client (I know this is not a UUID)

#### Create Profile Client

```
POST /testproxy/api/profile/{profileIdentifier}/clients
```

```json
{
	"client": {
		"id": 4,
		"isActive": false,
		"uuid": "5555-555-5555-5555",
		"profile": {
			…. profile information goes here
		}
	}
}
```

#### Delete Profile Client
Clients should be deleted when they are no longer going to be used (ex: in test cleanup code)

```
DELETE /testproxy/api/profile/{profileIdentifier}/clients/{clientUUID}
```

#### Enable/Disable A Profile/Client

```
POST /testproxy/api/profile/{profileIdentifier}/clients/{clientUUID}
POST BODY: active=true|false
```

#### Enable/Disable an Override Path

```
POST /testproxy/api/path/{pathIdentifier}
POST BODY for Response Path: profileIdentifier={profileIdentifier}&responseEnabled=true|false
POST BODY for Request Path: profileIdentifier={profileIdentifier}&requestEnabled=true|false
```

#### Reset all settings for an Override Path

```
POST /testproxy/api/path/{pathIdentifier}
POST BODY for Response Path: profileIdentifier={profileIdentifier}&resetResponse=true
POST BODY for Request Path: profileIdentifier={profileIdentifier}&resetRequest=true
```

#### Get ID for a Method
In order to enable/disable a method on a path you need the override ID.  The following request can be used to method information and find the ID
{methodName} - Fully qualified method name.  Ex: com.groupon.odo.override.Sleep

```
GET /testproxy/method/{methodName}
```

```json
{
  "method": {
    "id": 55,
    "httpCode": 200,
    "description": "Sleeps",
    "methodName": "Sleep",
    "className": "com.groupon.odo.override",
    "methodType": "interface com.groupon.odo.plugin.ResponseOverride",
    "methodArguments": [
    ],
    "methodArgumentNames": [
    ]
  }
}
```

#### Enable method on an override path

```
POST /testproxy/api/path/{pathIdentifier}
POST BODY: profileIdentifier={profileIdentifier}&addOverride={methodID}
```

##### Special Method IDs
Custom Response = -1
Custom Request = -2
Add Response Header = -3
Remove Response Header = -4
Add Request Header = -5
Remove Request Header = -6

#### Remove method from an override path

```
POST /testproxy/api/path/{pathIdentifier}
POST BODY: profileIdentifier={profileIdentifier}&removeOverride={methodID}
```

#### Set method arguments
Note: This is not always needed.. depends on the method you are using

The "ordinal" parameter is optional and is intended to indicate which enabled override you want to modify if multiple instances of the same override were added (ex: multiple custom responses).  The ordinal is based at 1.

```
POST /testproxy/api/path/{pathIdentifier}/{methodName}
POST BODY: profileIdentifier={profileIdentifier}&ordinal=1&arguments[]=argument1&arguments[]=argument2&arguments[]=argument3
```

#### Set custom response data
Custom response data is just the first argument to the custom response enabled override

```
POST /testproxy/api/path/{pathIdentifier}/-1
POST BODY: profileIdentifier={profileIdentifier}&ordinal=1&arguments[]=this%20is%20my%20custom%20response
```

#### Set custom request data

```
POST /testproxy/api/path/{pathIdentifier}
POST BODY for Response Path: profileIdentifier={profileIdentifier}&customResponse=<data>
POST BODY for Request Path: profileIdentifier={profileIdentifier}&customRequest=<data>
```

#### Set repeat count for an enabled override method
You can set the repeat count for a specific enabled method instead of the whole path.  This is the same call as setting an argument except using the parameter repeatNumber

```
POST /testproxy/api/path/{pathIdentifier}/-1
POST BODY: profileIdentifier={profileIdentifier}&ordinal=1&repeatNumber=5
```

### Java Configuration Interface
There is a Java interface available to configure settings in Odo while tests are running.

API Summary:

```
Namespace: com.groupon.odo.client.Client

Instantiation:
	Client cli = new Client("My Profile", true); // Create a new configuration client using Odo Parallel Clients
	Client cli = new Client("My Profile", false); // Create a new configuration client using the default profile client

Destruction:
	cli.destroy(); // Destroy the configuration client and delete the parallel client if necessary

Functions:
    cli.createPath("My Path", "/path/value", "GET") // Create a new path
	cli.getClientUUID(); // Get the UUID for the parallel client
	cli.toggleProfile(true or false); // Enable or disable the profile
	cli.toggleRequestOverride("My Path", true or false); // Enable or disable a specific request path
	cli.toggleResponseOverride("My Path", true or false); // Enable or disable a specific response path
	cli.resetProfile(); // Reset all path settings
	cli.resetRequestOverride("My Path"); // Reset all of the path settings for a specific request path
	cli.resetResponseOverride("My Path"); // Reset all of the path settings for a specific response path
	cli.setCustomRequest("My Path", "stuff=things"); // Set the custom request data for a specific path
	cli.setCustomResponse("My Path", "bunch of json"); // Add a custom response data for a specific path
	cli.setOverrideRepeatCount("My Path", "com.proxy.override.MyOverride", "1", "-1"); // Set the repeat count for an override on a Path. "1" is the ordinal - so the first override of this type on the path. "-1" is the new repeat count.
	cli.removeCustomRequest("My Path"); // remove the custom request data for a specific path
	cli.removeCustomResponse("My Path"); // remove the custom response data for a specific path
	cli.addMethodToResponseOverride("My Path", "com.proxy.override.MyOverride"); // enable the specific override method on the specified path
	cli.setMethodArguments("My Path", "com.proxy.override.MyOverride", 5, "Stuff"); // Set the arguments for a method that has already been added to a path
	cli.removeMethodFromResponseOverride("My Path", "com.proxy.override.MyOverride"); // remove the specific override method on the specified path
    cli.clearHistory(); //clear the proxy history for the active profile
    cli.filterHistory(String... filters); //retrieve the request history based on the specified filters
    cli.refreshHistory(); // Obtain the last (Default count) of history entries
    cli.refreshHistory(100, 10); // Obtain 100 history entries, starting from after the 10th most recent

Static Functions:
    Client.setCustomRequestForDefaultClient("My Profile", "My Path", "bunch of json");
    Client.setCustomResponseForDefaultClient("My Profile", "My Path", "bunch of json");
    Client.setCustomRequestForDefaultProfile("My Path", "bunch of json);
    Client.setCustomResponseForDefaultProfile("My Path", "bunch of json);

```

```
Namespace: com.groupon.odo.client.PathValueClient

PathValueClient exists for using Odo as a simple mock/stub server. It simplifies the steps of configuring a stub server through automation.

Instantiation:
    PathValueClient pvc = new PathValueClient("My Profile", true); // Create a new PathValueClient, creating a new client for the profile
    PathValueClient pvc = new PathValueClient("My Profile", false);  // Create a new PathValueClient, using the default client

Functions:
    pvc.getPathFromEndpoint("/path/value", "GET"); // Get Path information from the path's value
    pvc.removeCustomResponse("/path/value", "GET"); // Reset all of the path settings for a specific response path
    pvc.setCustomResponse("/path/value", "GET", "bunch of json"); // Sets the custom response for a path, or creates a new path if path value does not exist.

Static Functions:
    PathValueClient.setDefaultCustomResponse("My Path", "GET", "bunch of json"); // Sets the custom response for a path using a default profile, or creates a new path if path value does not exist.
    PathValueClient.removeDefaultCustomResponse("My Path", "GET"); // Reset all of the path settings for a specific response path of a default profile
```


## JSON Settings Backup/Import format

```json
{
	"groups": [
		{
			"name": "Group Name",
			"methods": [
				{
					"className": "com.blah.blah",
					"methodName": "doStuff"
				},
				{
					"className": "com.blah.blah",
					"methodName": "doStuff2"
				}
			]
		}
	],
	"profiles": [
		{
			"name": "Stuff",
			"paths": [
				{
					"pathName": "Name",
					"path": "blah\?",
					"groupNames": ["Group Name"],
					"contentType": "JSON",
					"requestType": 1,
					"global": false
				}
			],
			"servers": [
				{
					"source": "api.blah.com",
					"destination": "blahblah.com"
				}
			]
		}
	]
}
```

## Java Database Viewer
You may need to manually edit entries in database. We recommend using SQL Workbench.

1. Download and launch SQL Workbench http://www.sql-workbench.net/downloads.html
2. Download the H2 jar from : http://hsql.sourceforge.net/m2-repo/com/h2database/h2/1.3.175/h2-1.3.175.jar
3. On the Connect Window for Sql Workbench select "Manage Drivers"
4. Select the H2 driver and select the jar you downloaded in step 2 as the library.
5. Select the H2 driver you just created from the Driver dropdown box
6. In the URL field enter : jdbc:h2:tcp://localhost/h2proxydb/proxydb
7. In the Username field enter : SA
8. Click Ok. You should be connected to the running Database

## Integrating Browsermob Proxy
Browsermob Proxy 2.0-beta-9 is currently integrated via the "browsermob-proxy-odo" module.  The following is information about what files contain changes that would need to be ported to newer versions during an update.  Odo code changes have "BEGIN ODO CHANGES" and "END ODO CHANGES" around the changes

1. net.lightbody.bmp.proxy.BrowserMobProxyHandler
	1. handleConnect(..)
2. net.lightbody.bmp.proxy.http.BrowserMobHostNameResolver
	1. resolve(..)
3. net.lightbody.bmp.proxy.http.SimulatedSocketFactory
	1. connectSocket(..)
4. All included files have namespace changes to com.groupon.odo.bmp.*
5. All included files have appropriate imports update to reflect namespace changes

## Common problems

If you see an error like the following:

```
INFO] -------------------------------------------------------------
[ERROR] COMPILATION ERROR :
[INFO] -------------------------------------------------------------
[ERROR] /home/wizrrrd/projects/groupon/odo/browsermob-proxy/src/main/java/org/br
owsermob/proxy/jetty/jetty/servlet/ServletHandler.java:[1008,4] org.browsermob.p
roxy.jetty.jetty.servlet.ServletHandler.Context is not abstract and does not ove
rride abstract method declareRoles(java.lang.String...) in javax.servlet.Servlet
Context
[ERROR] /home/wizrrrd/projects/groupon/odo/browsermob-proxy/src/main/java/org/br
owsermob/proxy/jetty/jetty/servlet/ServletHttpRequest.java:[51,7] org.browsermob
.proxy.jetty.jetty.servlet.ServletHttpRequest is not abstract and does not overr
ide abstract method getPart(java.lang.String) in javax.servlet.http.HttpServletR
equest
[ERROR] /home/wizrrrd/projects/groupon/odo/browsermob-proxy/src/main/java/org/br
owsermob/proxy/jetty/jetty/servlet/ServletHttpResponse.java:[46,7] org.browsermo
b.proxy.jetty.jetty.servlet.ServletHttpResponse is not abstract and does not ove
rride abstract method getHeaderNames() in javax.servlet.http.HttpServletResponse`
```

Check your mvn version `mvn -v`. To properly compile you need mvn 3.


If Odo is taking a very long time to start, ensure that no antivirus software is running.
