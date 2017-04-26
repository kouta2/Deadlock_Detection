import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;

public interface RPCFunctions extends Remote 
{
    // String sayHello() throws RemoteException;

    // Add RPC function headers hear
    String s_get(String key) throws RemoteException;

    String s_set(String key, String value) throws RemoteException;

    String c_set(String cmd, String key, String value) throws RemoteException;

    String c_get(String cmd, String key, boolean local, String result) throws RemoteException;

    String c_commit(String client, HashMap<String, String> updates) throws RemoteException;

    String c_abort(String client) throws RemoteException;
}

