import java.util.HashMap


/*
 *
 * Parses stdin
 */
public class Parser
{


	


    public static void handle_one_line(String cmd){
    
		RPCFunctions r = Main.rpc_connect.get_connection(10);
		
		
   
		if (cmd.toUpperCase().equals("BEGIN")){
			//TODO: implement locking/whatever
			System.out.println("OK");
		}

		if (cmd.toUpperCase().equals("ABORT")){
			//TODO: implement rollback
			System.out.println("ABORT");
		}

		if (cmd.toUpperCase().equals("COMMIT")){
			//TODO: implement actual committing
			System.out.println("COMMIT OK");
		}
		
		String[] args = cmd.split(" ");
		
		String arg = args[0];
		String[] obj = args[1].split(".");
		int machine = Main.machines.get(obj[0]);
		String key = obj[1];

		
	 
		RPCFunctions r = Main.rpc_connect.get_connection(machine); 
		String result;
	
		if (arg.toUpperCase().equals("GET")){
			result = r.set(key); 		
		}

		if (arg.toUpperCase().equals("SET")){
			String value = args[2]; 
			result = r.set(key, value);
		}
		

		System.out.println(result); 




    }

}
