/*
 *
 * Handles input
 */

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.HashMap;

public class Main implements RPCFunctions
{
    static int PORT_NUM = 2002;
    static String SERVER_ID = "";
    static int PROCESS_ID;
    static ConnectToOtherRPCs rpc_connect;
	static HashMap<String,String> kv;
	static HashMap<String, Integer> machines;

    public String s_set(String key, String value)
    {
       	kv.put(key,value);
		return null; 
    }

    public String s_get(String key)
    {
        return kv.get(key); 
    }


    public String c_set(int pid, String cmd, String key, String value)
    {
        // check if set is valid and return
        return null;
    }

    public String c_get(int pid, String cmd, String key, boolean local, String result)
    {
        // check if get is valid
        // return result
        return null;
    }

    public String c_commit(int pid, HashMap<String, String> updates)
    {
        // send updates to proper servers
        // clean up graph

        return null;
    }

    public String c_abort(int pid)
    {
        // clean up graph

        return null;
    }

	private static void init(){
		machines = new HashMap<String,Integer>();
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
            PROCESS_ID = Integer.parseInt(InetAddress.getLocalHost().getHostName().substring(15, 17)); 
            if(PROCESS_ID == 10)
                SERVER_ID = "coordinator!";
            else
            {
                String[] server_id = {"A", "B", "C", "D", "E"};
                SERVER_ID = server_id[PROCESS_ID - 1];
            }
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
