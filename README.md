# What is the ad-hoc network simulator?
Ad-hoc is a wireless distributed and point-to-point messaging network. With this simulator you can create this network and learn how it is engineered.
A wireless ad hoc network or mobile ad hoc network is a decentralized type of wireless network.
It is used when a server is not required and communication is flatten within the whole network area. In ad hoc network, each device plays a role of server and also client.  
An ad hoc routing protocol is a convention, or standard, that controls how nodes decide routing of packets in distributed network. 
The reason why it called mobile is that each node or device, can move freely in the region that connection allowed.
In ad hoc networks, nodes are not familiar with the topology of their networks, in other words, they are topology agnostic.
Network comprises of many nodes all behaving as a server or client at the same time. This connection called peer-2-peer.


### Usecases
When a node only need connection to its peers. p2p file-sharing, Tor network, in military, car's network in autonamous driving and so on.
Packets in network connection does not have to travel all the way to server, so it only communicatates with it's neighbors. This way the connection is distributed,
and also secured. (each node can add layers of encryption).

### Advantages:
- Secured e2e encryption: only the sender and reciever would be able to read the message. Nodes in between the connection does not have private key of the encryption message, so they only carry the message from source to destination. 
- Power efficient: Depending on the messaging distribution and behaviour and routing, messages are only received via involved nodes in network. Despite the   Ethernet protocol where all of the nodes received all the packets in network; This helps to reducing battery and power for processing only a portion of messages.
- Secured: Since each node added a new encryption layer over each message when receiving, it is not possible to spoof messages.
- Distributed: Since the connection is not centralized, the server is off the duty, and messages can only transmit among those needed sub networks.
- Protocol agnostic: Protocol-agnostic is independent of communication protocols. It negotiates a protocol with its peer and begins communication. Ad hoc defined so that each node in the network does not need to know the protocols they are dealing with. In other words, nodes are not familiar with the topology of their networks, so they are topology agnostic. This makes nodes so easy to implement, maintain and develop.

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
