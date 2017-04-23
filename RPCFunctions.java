import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RPCFunctions extends Remote 
{
    // String sayHello() throws RemoteException;

    // Add RPC function headers hear
    String get(String key) throws RemoteException;

    String set(String key, String value) throws RemoteException;

    // String owners(String key) throws RemoteException;
    
    boolean heartbeat() throws RemoteException;

    void notify_failure(int failed_id) throws RemoteException;

    void notify_connection(int connected_id) throws RemoteException;
}

