# About
The Communote remote user authentication plugin is a plugin for [Communote](https://github.com/Communote/communote-server) which adds support 
for authentication solutions that are running in a web server in front of Communote. This is typically used in single sign-on solutions such as 
CAS with an Apache web server or the Integrated Windows Authentication with an IIS server or a Kerberos enabled Apache web server.

In scenarios where the web server takes care of the authentication the authenticated user is usually provided by an environment variable named 
```REMOTE_USER```. With this plugin Communote can evaluate the ```REMOTE_USER``` value and start an authenticated session for the contained 
user without asking for credentials.

# Compatibility
The plugin can be used with a Communote standalone installation.

The following table shows which Communote server versions are supported by a specific version of the plugin. A server version which 
is not listed cannot be used with the plugin.

| Plugin Version  | Supported Server Version |
| ------------- | ------------- |
| 3.4  | 3.4.x, 3.5.x  |

# Installation
To install the plugin get a release from the [Releases](https://github.com/Communote/communote-plugin-remote-user-authentication/releases) 
section and deploy it to your Communote installation as described in the 
[Installation Documentation](http://communote.github.io/doc/install_extensions.html).

# Usage
After installing the plugin a new page named 'Remote User Pre-Authentication' will be available in the administration section of Communote 
under 'Extensions' which provides some configuration options. The first setting is used to enable the plugin. If this checkbox is not checked 
the ```REMOTE_USER``` will not be evaluated. Another option allows to define a regular expression for extracting the Communote user identifier 
from the ```REMOTE_USER``` value. The identifier can be the e-mail address, the Communote username or the login name of the user in a 
configured and enabled external repository like an LDAP directory. When leaving this setting empty the whole value will be taken otherwise the 
first capturing group of the regular expression is used. With another configuration option it is possible to restrict the search for the user 
to a specific repository (e.g. LDAP directory). If there is no user for the extracted identifier or the ```REMOTE_USER``` is unset Communote 
behaves as if the plugin is not installed and asks for credentials.

Apart from enabling the plugin and configuring your web server you also have to instruct the [Tomcat](http://tomcat.apache.org/) server 
into which Communote is deployed to pass the ```REMOTE_USER``` it receives from the web server to Communote. This can be done in the 
```server.xml``` file by modifying the ```Connector``` element over which the web server is communicating with Tomcat. This is only 
supported by the AJP protocol so you will have to use an [AJP Connector](https://tomcat.apache.org/tomcat-8.0-doc/config/ajp.html) and 
add the attribute ```tomcatAuthentication``` with value ```false``` to it. For security reasons you should also restrict the IP address 
the connector is listening on with the ```address``` attribute if possible. So if for example your Apache web server 
and the Tomcat are running on the same host the connector configuration can look like this:
 
```xml
<Connector port="8009" protocol="AJP/1.3" address="127.0.0.1" tomcatAuthentication="false" />
```

# Building
To build the plugin you have to clone or download the source and setup your development environment as described in our 
[Developer Documentation](http://communote.github.io/doc/dev_preparation.html). Afterwards you can just run ```mvn``` in the checkout 
directory. The JAR file will be created in the target subdirectory.

# License
The plugin is licensed under the [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0).
