all : Device EventLog Generator Ethernet

Device: Device.java
	javac Device.java

EventLog: EventLog.java
	javac EventLog.java

Generator: Generator.java
	javac Generator.java

Ethernet: Ethernet.java
	javac Ethernet.java

Run: all
	java Ethernet 123 2 4 4

clean:
	rm *.class
