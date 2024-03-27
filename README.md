# TupleSpaces

Distributed Systems Project 2024

**Group A56**

*(choose one of the following levels and erase the other one)*  
**Difficulty level: I am Death incarnate!**


### Code Identification

In all source files (namely in the *groupId*s of the POMs), replace __GXX__ with your group identifier. The group
identifier consists of either A or T followed by the group number - always two digits. This change is important for 
code dependency management, to ensure your code runs using the correct components and not someone else's.

### Team Members

| Number | Name              | User                              | Email                                            |
|--------|-------------------|-----------------------------------|--------------------------------------------------|
| 102611 | João Gouveia      | <https://github.com/joaotgouveia> | <mailto:joaotalonegouveia@tecnico.ulisboa.pt>    |
| 102604 | Gonçalo Rua       | <https://github.com/goncrust>     | <mailto:goncalonrua@tecnico.ulisboa.pt>          |
| 102492 | Francisco Dias    | <https://github.com/lotrias17>    | <mailto:franciscodiasfonseca@tecnico.ulisboa.pt> |

## Getting Started

The overall system is made up of several modules. The different types of servers are located in _ServerX_ (where X denotes stage 1, 2 or 3). 
The clients is in _Client_.
The definition of messages and services is in _Contract_. The future naming server
is in _NamingServer_.

See the [Project Statement](https://github.com/tecnico-distsys/TupleSpaces) for a complete domain and system description.

### Prerequisites

The Project is configured with Java 17 (which is only compatible with Maven >= 3.8), but if you want to use Java 11 you
can too -- just downgrade the version in the POMs.

To confirm that you have them installed and which versions they are, run in the terminal:

```s
javac -version
mvn -version
```

### Installation


#### Setup a virtual environment in Linux:

Run to create the virtual environment:
```s
python -m venv .venv
```
Run to activate the virtual environment:
```s
source .venv/bin/activate
```
Run to install the package grpcio:
```s
python -m pip install grpcio
```
Run to install the package grpcio-tools:
```s
python -m pip install grpcio-tools
```

#### Setup a virtual environment in Windows:
Run to create the virtual environment:
```s
python -m venv .venv
```
Run to activate the virtual environment:
```s
.venv/Scripts/activate
```
Run to install the package grpcio:
```s
python -m pip install grpcio
```
Run to install the package grpcio-tools:
```s
python -m pip install grpcio-tools
```

#### Run all processes in the same machine 

In the Contract directory:

```s
mvn clean install exec:exec
```

In the NameServer directory:

```s
python server.py [-debug]
```

In the Sequencer directory:

```s
mvn clean compile
mvn exec:java -Dexec.args="<portSeq>"
```

To run three servers at the same time, run, in different terminals, in the ServerR3 directory, these commands, each with different port and qualifier (qualifiers should specifically be A, B, C):

```s
mvn clean compile
mvn exec:java -Dexec.args="<port> <qualifier> [-debug]"
```

In the Client directory (Seq stands for Sequencer):

```s
mvn clean compile
mvn exec:java -Dexec.args="<hostNS> <portNS> <portSeq> [-debug]"
```

## Built With

* [Maven](https://maven.apache.org/) - Build and dependency management tool;
* [gRPC](https://grpc.io/) - RPC framework.
