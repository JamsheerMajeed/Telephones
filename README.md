#Neax7400ICS

##Description
This is an application to communicate with the NEC NEAX 7400 ICS PBX (Private Branch Exchange).  This is command line application and is a demonstration on how to get started communicating with the PBX.

##Requirements
To compile this application you need:

* Java
* Maven

To run the compiled application you need:

* Java
* Rxtx
* Your PBX connected to the Serial Port

##Compile
To compile this application type
`mvn clean package`
This creates a uber jar in the target folder

##Run
To run the application type
`java -jar target/tellApp-1.0.jar`
This is an executable jar so you do not have to specify the class name.

Once the application is running the application accepts inputs from the command line and the implemented inputs are

* Start : this command opens the serial port and listens for data
* Enq : This command writes ENQ to the PBX
* Status : This command sends a status enquiry message to the PBX
* Stop : This command closes the serial port
* Exit : This command exits the application.
