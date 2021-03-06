import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

/*
 * handles the client connections to us to use our RPC calls
 * 
 */
public class AcceptRPCConnections
{
    int port_num;

    public AcceptRPCConnections(int p_num)
    {
        port_num = p_num;
    }    

    public void run(String id)
    {
        try
        {
            Main m = new Main();
            RPCFunctions stub = (RPCFunctions) UnicastRemoteObject.exportObject(m, 0);
            Registry registry = LocateRegistry.getRegistry(port_num);
            try
            {
                registry.bind("RPCFunctions", stub);
            }
            catch (Exception e)
            {
                registry.rebind("RPCFunctions", stub);
            }
            if(id.equals("coordinator!"))
                System.err.println("Coordinator ready");
            else
                System.err.println("Server " + id + " ready");
        }
        catch (Exception e)
        {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
