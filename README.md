# What is the ad-hoc simulator?
It is used when a server is not required and communication is flatten within the whole network. 
An ad hoc routing protocol is a convention, or standard, that controls how nodes decide which way to route packets between computing devices in a mobile ad hoc network. In ad hoc networks, nodes are not familiar with the topology of their networks.
Network comprised of several nodes all behaving as a server or client. The connection then called peer-2-peer.
Usecases? When you only trust your peers. p2p file-sharing, Tor network. In military, car's netowkr in autonamous driving and so on.
ad-hoc is a wireless distributed and point-to-point messaging network. With this simulator you can create this network and learn how it's engineered. 

### Advantages:
- Secured e2e encryption: only the sender and reciever would be able to read the message; 
- Power efficient: Depending on the messaging distribution and behaviour and routing, messages are only received via involved nodes in network. Despite the   Ethernet protocol where all of the nodes received all the packets in network; This helps to reducing battery and power for processing only a portion of messages.
- Secured: Since each node added a new encryption layer over each message when receiving, it is not possible to spoof messages.

### Disadvantages:
- Undetermined throughput: Depending on the nodes involved in each specific route, throughput differs. Processing of messages when passing through each node in a specific node is highly depended on that node processing power and if there is a message ingestion on that node. So there is a stochastic distribution in throughput depending on network traffic.        
- Trade-off highly-coupling / loosely-couplede connection: If one node is down or not reachable a new route is created. The route is always optimized to the best route with less weight. But if there is no route finding as a recovery phase, the connection to that node is dropped. So the connection to the destination is highly coupled to the inner-nodes, or it can be losely coupled if there are many available nodes in between so a new route is always created in recovery.


## How the ad-hoc simulator works:
### Working with it is very simple.
It has a Graphical User Interface:

- You can add nodes, and start simulation. 
- Each node starts broadcasting a broadcasting message in the open air repeatedly when created (device can be physical not the focus here). 
- Node has a range defined that describes reachability. (antenna gain or nework boundry)
- Start/Pause/Stop simulation
- Saves simulation in file and import it later!

Mechanism:
I've implemented one famous protocol [Destination Sequence Distance Vector (DSDV)](https://en.wikipedia.org/wiki/Destination-Sequenced_Distance_Vector_routing). 
These are couple of other protocols. All can be configured in the simulator easily. 

Watch video for more information.

![](https://github.com/metao1/server-discovery/raw/master/video.gif)
[![Watch the video](screenshot.png)](https://www.youtube.com/watch?v=GeJ2hzihFaM)
