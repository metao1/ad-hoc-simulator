# What is the Java Server Discovery?
Server Discovery is a Java Network Simulator for ad-hoc and distributed networks. 
With Server Discovery Simulator you can create easily create a network mesh of ad-hoc nodes and send message to each node just by using a GUI!

When you start adding a node in the plane, (a node represents a minimal entity in a network mesh). Each node starts broadcasting a broadcasting message in the open air repeatedly when created. Every node in the simulator has a range defined that describes reachability of it, like a physical antenna. These are all can be configured in the simulator easily. In order for each node to be reachable by other nodes new routing table is created dynamically as nodes move, remove or created in the environment. Meanwhile the whole nodes in the simulator synchronously broadcasts at the same time with the latest shortest path table. This table helps like a map of how to reach to each node in the system. Regular text messages then can be passed to other nodes crossing each node until reach to destination.

Watch video for more information.

![](https://github.com/metao1/server-discovery/raw/master/video.gif)
[![Watch the video](screenshot.png)](https://www.youtube.com/watch?v=GeJ2hzihFaM)
