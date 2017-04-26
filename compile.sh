#!/bin/bash
javac -cp ${PWD}/jgrapht-1.0.1/lib/jgrapht-core-1.0.1.jar -d class_files Main.java AcceptRPCConnections.java ConnectToOtherRPCs.java RPCFunctions.java Parser.java Transaction.java
# rmiregistry 2002 &
