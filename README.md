# strategy
Repository for oopsla2015 artifacts

Requirements:

1) Java 1.7

2) panc.jar

3) programs.zip

Steps:

1) Your current directory should contain:
	- panc.jar
	- programs

2) Run following command:
	java -Xbootclasspath/p:./panc.jar -ea:com.sun.tools... -jar panc.jar -graphs programs/bencherl/bang.java
	
	This command compiles bang.java panini program using "-graphs" option 
	to produce cVector and mappings for each actor kind.

	Output for the above command is:
	Sender < false, nil, async, scatter, request-reply, io> : task
	Receiver < false, nil, async, leaf, request-reply, io> : monitor

	where, Sender and Receiver are two actor kinds in the bang.java panini program,
	< false, nil, async, scatter, request-reply, io> is the cVector,
	task is the mapping for Sender actor kind. 
