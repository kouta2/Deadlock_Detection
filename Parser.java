import java.util.HashMap;


/*
 *
 * Parses stdin
 */
public class Parser
{


	static HashMap<String, String> local; 


	private static void re_init();  


    public static void handle_one_line(String cmd){
    
		RPCFunctions r = Main.rpc_connect.get_connection(10);
		
		
   
		if (cmd.toUpperCase().equals("BEGIN")){
			r.c_abort(Main.PROCESS_ID); //This machine is resetting its transaction
			re_init(); 
			System.out.println("OK");
		}

		if (cmd.toUpperCase().equals("ABORT")){
			//clear out local key value store
			r.c_abort(Main.PROCESS_ID); 
			re_init(); 
			System.out.println("ABORT");
		}

		if (cmd.toUpperCase().equals("COMMIT")){
			//TODO: implement actual committing
			//clear key value store
			r.c_commit(local);
			re_init();  
			System.out.println("COMMIT OK");
		}
		
		String[] args = cmd.split(" ");
		
		String arg = args[0];
		String[] obj = args[1].split(".");
		int machine = Main.machines.get(obj[0]);
		String key = args[1];

	 
		String result;
	
		if (arg.toUpperCase().equals("GET")){
			result = local.get(key); 
			local = result != null; 
			r.c_get(machine,key,local,result); 
			


		}

		if (arg.toUpperCase().equals("SET")){
			String value = args[2]; 
			result = r.s_set(key, value);
		}
		

		System.out.println(result); 




    }

}
