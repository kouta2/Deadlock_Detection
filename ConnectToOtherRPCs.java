import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Arrays;

/*
 * attempts to connect to other processes to use their RPCs
 *
 */
public class ConnectToOtherRPCs
{
    
    int port_num = 2002;
    String ip_addrs [] = {"172.22.146.231", "172.22.146.233", "172.22.146.235", "172.22.146.237", "172.22.146.239", "172.22.146.241", "172.22.146.243", "172.22.146.245", "172.22.146.247", "172.22.146.249"}; // list of ip addresses used for accepting clients and connecting to clients
    public ConnectToOtherRPCs(int p_num)
    {
        port_num = p_num;
    }

    public RPCFunctions get_connection(int process)
    {
        try
        {
            Registry client_sock = LocateRegistry.getRegistry(ip_addrs[process - 1], port_num);
            RPCFunctions stub = (RPCFunctions) client_sock.lookup("RPCFunctions");
            return stub;
        }
        catch (Exception e)
        {
            // e.printStackTrace();
            return null;
        }
    }
}
