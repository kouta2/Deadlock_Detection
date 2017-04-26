import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RPCFunctions extends Remote 
{
    // String sayHello() throws RemoteException;

    // Add RPC function headers hear
    String get(String key) throws RemoteException;

    String set(String key, String value) throws RemoteException;
}

