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

public class Main implements RPCFunctions
{
    static int PORT_NUM = 2002;
    static String SERVER_ID = "";
    static ConnectToOtherRPCs rpc_connect;

    public String set(String key, String value)
    {
        // TODO: localy update data structure and return proper info
        return null;
    }

    public String get(String key)
    {
        // TODO: locally grab data and return proper info
        return null;
    }

    public static void main(String[] args)
    {
        try 
        { 
            int process_num = Integer.parseInt(InetAddress.getLocalHost().getHostName().substring(15, 17)); 
            String[] server_id = {"A", "B", "C", "D", "E"};
            SERVER_ID = server_id[process_num - 1];
        }
        catch (Exception e) {}

        if(SERVER_ID == "") // server
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
