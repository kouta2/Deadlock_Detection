/*
 *
 * Handles input
 */

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.net.InetAddress;
iimport java.util.Scanner;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.HashMap;

public class Main implements RPCFunctions
{
    static int PORT_NUM = 2002;
    static String SERVER_ID = "";
    static ConnectToOtherRPCs rpc_connect;
	static HashMap<String,String> kv;
	static HashMap<String, Integer> machines;

    public String set(String key, String value)
    {
       	kv.put(key,value);
		return null; 
    }

    public String get(String key)
    {
        return kv.get(key); 
    }

	private void init(){
		machines = new HashMap<String,Integer>;
		machines.put("A",1); 
		machines.put("B",2); 
		machines.put("C",3); 
		machines.put("D",4); 
		machines.put("E",5); 

		

	}


    public static void main(String[] args)
    {
     	
	
		init();//init stuff 

		try 
        { 
            int process_num = Integer.parseInt(InetAddress.getLocalHost().getHostName().substring(15, 17)); 
            String[] server_id = {"A", "B", "C", "D", "E"};
            SERVER_ID = server_id[process_num - 1];
        }
        catch (Exception e) {}

        if(!SERVER_ID.equals("")) // server
        {
            AcceptRPCConnections accept = new AcceptRPCConnections(PORT_NUM);
            accept.run();
        }
        else // client
        {
            Scanner scan = new Scanner(System.in);
            rpc_connect = new ConnectToOtherRPCs(PORT_NUM);
            while(true)
                Parser.handle_one_line(scan.nextLine());
        }

    }
}
