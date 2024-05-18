java_spring (for java 1.8 +)
-------------------
[![last_version](https://img.shields.io/badge/last_version-v2..6.0-blue.svg?style=flat)](https://github.com/0129general/java_spring/tree/2..6.0)
[![Official site](https://img.shields.io/website-up-down-green-red/https/shields.io.svg?label=official%20site)](http://www.java_spring.com/)
[![Docker Pulls](https://img.shields.io/docker/pulls/java_springecomm/java_spring.svg)](https://hub.docker.com/r/java_springecomm/java_spring)
[![stackoverflow](https://img.shields.io/badge/java_spring-stackoverflow-orange.svg?style=flat)](http://stackoverflow.com/questions/tagged/java_spring)
-------------------

Java open source e-commerce software

- Shopping cart
- Catalogue
- Search
- Checkout
- Administration
- REST API

See the demo:
-------------------
http://demo.java_spring.com:8080


Get the code:
-------------------
Clone the repository:
     
	 $ git clone git://github.com/0129general/java_spring.git

If this is your first time using Github, review http://help.github.com to learn the basics.

You can also download the zip file containing the code from https://github.com/0129general/java_spring 

To build the application:
-------------------	
From the command line with Maven installed:

	$ cd java_spring
	$ mvn clean install
if Maven is not installed, use maven wrapper
       
	$ mvmw clean install
	

Run the application from Tomcat 
-------------------
copy sm-shop/target/ROOT.war to tomcat or any other application server deployment dir

Increase heap space to 1024 m

### Heap space configuration in Tomcat:


If you are using Tomcat, edit catalina.bat for windows users or catalina.sh for linux / Mac users

	in Windows
	set JAVA_OPTS="-Xms1024m -Xmx1024m -XX:MaxPermSize=256m" 
	
	in Linux / Mac
	export JAVA_OPTS="-Xms1024m -Xmx1024m -XX:MaxPermSize=256m" 

Run the application from Spring boot 
-------------------

       $ cd sm-shop
       $ mvn spring-boot:run
if Maven is not installed, use maven wrapper
       
	   $ mvmw spring-boot:run

Run the application from Spring boot in eclipse
-------------------

Right click on com.salesmanager.shop.application.ShopApplication

run as Java Application

### Access the application:
-------------------

Access the deployed web application at: http://localhost:8080/

Acces the admin section at: http://localhost:8080/admin

username : admin

password : password

The instructions above will let you run the application with default settings and configurations.
Please read the instructions on how to connect to MySQL, configure an email server and configure other subsystems


### Documentation:
-------------------

Documentation available from the wiki <http://0129general.github.io/java_spring/#>

ChatOps <https://java_spring.slack.com>  - Join our Slack channel https://java_spring-slackin.herokuapp.com/

More information is available on java_spring web site here <http://www.java_spring.com>

### Participation:
-------------------

If you have interest in giving feedback or for participating to java_spring project in any way
Feel to use the contact form <http://www.java_spring.com/contact.html> and share your email address
so we can send an invite to our Slack channel


