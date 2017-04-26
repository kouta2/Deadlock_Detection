Before compiling the code, create a folder called class_files inside the CS425MP2 directory. This folder only needs to be created once.
>> mkdir class_files

To compile, run the following bash script from inside the CS425MP2 directory
>> bash compile.sh

Now, you need to start a rmi registry to enable rpc calls. This command only needs to be executed once on a machine and needs to be ran inside the class_files directory. The rmi registry needs to be running while you run our main program.
>> cd class_files/
>> rmiregistry 2002 &

To check if rmiregistry is already running on a machine, type the following command and look for a running process called "rmiregistry 2002 &":
>> ps aux | grep rmiregistry

As long as rmiregistry is running, then type the following command inside the class_files directory:
>> java main

To kill a client, simply send a SIGINT (CTRL+C).

To kill a rmiregistry type:
>> ps aux | grep rmiregistry
look for the pid of the serving running and type:
>>kill <pid>

