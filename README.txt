/* Name: Jianshuo Qiu
 * UNI: jq2253
 */

——————————————————————————————
How to compile and run the files:

General:
1. Put gbnnode.java, gbnSender.java, gbnReceiver.java, gbnpublicMethods.java, newTimerTask.java, dvnode.java, dvpublicMethods.java, dvReceiver.java, cnGBN.java, cnnode.java, cnpublicMethods.java, cnReceiver.java, Link.java and Makefile into the same folder.

2. Open the Terminal on the computer.

3. In the Terminal, go to the folder where these files are, by typing “cd (folder’s directory)” and hitting enter.

4. To compile any java file, type “make” and hit enter. You will have the compiled class files with the same file name as the java files.

To run gbnnode.class:
We need one sender and one receiver in order to run the features of Go-Back-N. Open two terminals and make sure two terminals are in the directory where gbnnode.class, gbnSender.class, gbnReceiver.class, gbnpublicMethods.class, and newTimerTask.class are. In one terminal, type “java gbnnode <port1> <port2> <window-size> [ -d <value-of-n> j -p <value-of-p>]”, and then hit enter and then “node> ” will be prompted in this terminal. Then, in the other terminal, type “java gbnnode <port2> <port1> <window-size> [ -d <value-of-n> j -p <value-of-p>]” and hit enter, and “node> ”will be prompted also. Now we can send message from one terminal to the other: Select one of the terminal and type “send <YourMessage>” and hit enter. Then, program will send <YourMessage> one character by one character from one port to the other port. Please open “test.txt” to see more behavior of this program.

To run dvnode.class:
We need several nodes (max number of nodes: 16) to run the features of DV algorithm. If you want to run N nodes, then open N terminals, all in the directory where dvnode.class, dvpublicMethods.class, and dvReceiver.class are. In each terminal, type “java dvnode <local-port> <neighbor1-port> <loss-rate-1> <neighbor2-port> <loss-rate-2> ...” and hit enter. As you type in the last terminal, remember to add “last” at the end of the command line arguments so that the program knows you have input all nodes. Then, different nodes will send message to each other according to DV algorithm. More behaviors of this program are shown in “test.txt”.

To run cnnode.class:
We need several nodes (max number of nodes: 16) to run the features of as specified in the assignment instruction. If you want to run N nodes, then open N terminals, all in the directory where cnGBN.class, cnnode.class, cnpublicMethods.class, cnReceiver.class, and Link.java are. In each terminal, type “cnnode <local-port> receive <neighbor1-port> <loss-rate-1> <neighbor2-port> <loss-rate-2> ... <neighborM-port> <loss-rate-M> send <neighbor(M+1)-port> <neighbor(M+2)-port> ... <neighborN-port>” and hit enter. As you type in the last terminal, remember to add “last” at the end of the command line arguments so that the program knows you have input all nodes. Then, nodes will send messages to each other according to the algorithm specified in the assignment instruction. More behaviors of this program are shown in “test.txt”.

——————————————————————————————
Project documentation and features:

GBN:
1. gbnnode.java: it initiates all variables in this program and initiates the sender Thread and receiver Thread of one “node”. By taking in user’s input message, the sender Thread will send the input message one character by one character to the other “node”’s receiver Thread.

2. gbnSender.java: runs the sender Thread of the program. 3. gbnReceiver.java: runs the receiver Thread of the program. They both follow the GBN protocol.

3. gbnpublicMethods.java: provides public methods to help other java file to finish different tasks.

4. newTimerTask.java: overrides Java’s TimeTask and is responsible for doing the timer.

DV:
1. dvnode.java: initiates all variables in this program and initiates the receiver Thread of the program. It is also responsible for updating, printing, and sending counting table of the node.

2. dvReceiver.java: runs the receiver Thread of the program. It is responsible for receiving the message from other nodes, and digest the information in the message.

3. dvpublicMethods.java: provides public methods to help other java file to finish different tasks.

CN:
1. cnnode.java: initiates all variables in this program and initiates the receiver Thread of the program. It is also responsible for updating, printing, and sending counting table of the node.

2. cnReceiver.java: runs the receiver Thread of the program. It is responsible for receiving the message from other nodes, and digest the information in the message.

3. cnGBN.java: because this program implements GBN protocol, we use dvGBN.java to realize features of GBN.

4. Link.java: creates instances of Link objects. Each Link objects has its id, loss rate value and weight.

5. cnpublicMethods.java: provides public methods to help other java file to finish different tasks.


——————————————————————————————
Algorithms and data structures:

GBN:
1. buffer and window: According to GBN protocol, we need one buffer and one window at the sender side. In my implementation, buffer’s size is twice of window size so that it is easier to solve the problem of sending termination.

2. timer: there is a timer for the oldest unACKed message. Once this message is ACKed, the times is cancel() and a new timer is created for next oldest unACKed message.

3. I implement the Go-Back-N protocol exactly the same as specified in textbook and lecture.

DV:
1. a 2D array “table” to represent the counting table. The index of the array represents the ID of each port.

2. a HashMap “ptoiMap” to store <Key = port, Value = ID> so that I assign each port an ID in each node.

3. an array “itop” to store port numbers and the index represents ID.

4. I implement the DV algorithm exactly the same as specified in the textbook and lecture.

CN:
1. “table” , “itop” and “ptoiMap” are the same as above in DV. However, there is an additional HashMap “linkmap” to store <Key = link ID, value = link object>, because I assign each adjacent edge/link an ID in each node.

2. As specified in the assignment instruction, I combined GBN protocol and DV algorithm in this program.


——————————————————————————————
Bugs:
Currently no bugs found.



