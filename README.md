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

### Samples
The examples directory contains samples to help get you started with Odo
* backup.json: A sample configuration that can be imported to demonstrate some basic features of Odo.
* api-usage: Example code demonstrating modifying an Odo configuration through the Java client.
* plugin: Example code demonstrating how you can extend Odo's functionality by adding your own override behaviors.

## Odo Details

For more detailed information on Odo and developing with Odo, visit the [Odo Wiki](https://github.com/groupon/odo/wiki) 