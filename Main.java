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
import java.util.Map; 

public class Main implements RPCFunctions
{
    static int PORT_NUM = 2002;
    static String SERVER_ID = "";
    static int PROCESS_ID;
    static ConnectToOtherRPCs rpc_connect;
	static HashMap<String,String> kv;
	static HashMap<String, Integer> machines;
	static Unlocker unlocker; 


    public String s_set(String key, String value)
    {
       	kv.put(key,value);
        //System.err.println("key is: " + key + " and value is: " + value);
	    return null; 
    }

    public String s_get(String key)
    {
        //System.err.println("in s_get with key: " + key);
    	String val = kv.get(key); 
		if (val == null)
			return "NOT FOUND"; 
	    return val; 
    }


    public String c_set(int pid, String key, String value)
    {
        // check if set is valid and return

        // System.err.println("in c_set with key is: " + key + " and value is: " + value);

		boolean allowed = true;  	
		//TODO: Graph edge checking
		
        allowed = unlocker.isWriteable(pid, key);

		if (!allowed){
			return null; 
		}

        return "OK";
    }

    public String c_get(int pid, String key, boolean local, String result)
    {
        // check if get is valid
        // return result
    	
        //System.err.println("in c_get with key: " + key);

		boolean allowed = true;  
    	//TODO: Graph edge checking    
    	
        allowed = unlocker.isReadable(pid, key);
		
		if (!allowed){
			return null; 	
		}

		if (local){
			return result; 
		}

		int key_owner = machines.get(key.split("\\.")[0]); 	
		RPCFunctions r = rpc_connect.get_connection(key_owner); 
		
		result = null;  
		try{
			result = r.s_get(key);
		}catch(Exception e){}
		
		return result; 
    }

    public String c_commit(int pid, HashMap<String, String> updates)
    {
        // send updates to proper servers
        // clean up graph

        // System.err.println("In c_commit with updates : " + updates.toString());
		
		for (Map.Entry<String,String> entry : updates.entrySet())
        {

			String key = entry.getKey(); 
			String value = entry.getValue(); 
			int key_owner = machines.get(key.split("\\.")[0]); 	

			RPCFunctions r = rpc_connect.get_connection(key_owner); 
			try{
				r.s_set(key, value); 
			}catch(Exception e){}
		}

		unlocker.clear_vertex(Integer.toString(pid)); 
        return null;
    }

    public String c_abort(int pid)
    {
        // TODO: We needed to consider keys that were added from this transaction that never existed before so that they should be removed from graph
        //System.err.println("in c_abort from pid: " + pid);
		unlocker.clear_vertex(Integer.toString(pid)); 
        return null;
   }

	private static void init()
    {
		//init machines data structure for convenience
		machines = new HashMap<String,Integer>();
		machines.put("A",1); 
		machines.put("B",2); 
		machines.put("C",3); 
		machines.put("D",4); 
		machines.put("E",5); 

        rpc_connect = new ConnectToOtherRPCs(PORT_NUM);
	}


    public static void main(String[] args)
    {
		init(); 

	    try 
        { 
            PROCESS_ID = Integer.parseInt(InetAddress.getLocalHost().getHostName().substring(15, 17)); 
            if(PROCESS_ID == 10)
            { 
		        SERVER_ID = "coordinator!";
		        unlocker = new Unlocker(); 
	        }
            else
            {
                String[] server_id = {"A", "B", "C", "D", "E"};
                SERVER_ID = server_id[PROCESS_ID - 1];
            }
        }
        catch (Exception e) {}

        if(!SERVER_ID.equals("")) // server
        {
			kv = new HashMap<String, String>(); //init the key value store
            AcceptRPCConnections accept = new AcceptRPCConnections(PORT_NUM);
            accept.run(SERVER_ID);
        }
        else // client
        {
            //System.err.println("Client " + PROCESS_ID + " ready");
            Scanner scan = new Scanner(System.in);
            while(true)
                Parser.handle_one_line(scan.nextLine());
        }
    }
}
