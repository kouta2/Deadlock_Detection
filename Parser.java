import java.util.HashMap;


/*
 *
 * Parses stdin
 */
public class Parser
{


	static HashMap<String, String> local; 


	private static void re_init(){
		local = new HashMap<String,String>(); 
	}  


    public static void handle_one_line(String cmd){
    
		RPCFunctions r = Main.rpc_connect.get_connection(10);
		
		if (cmd.toUpperCase().equals("BEGIN")){
			re_init(); 
			System.out.println("OK");
		}

		if (cmd.toUpperCase().equals("ABORT")){
			try{
				r.c_abort(Main.PROCESS_ID); 
			}catch (Exception e){}; 
			re_init(); 
			System.out.println("ABORT");
		}

		if (cmd.toUpperCase().equals("COMMIT")){

			try{
				r.c_commit(Main.PROCESS_ID, local);
			}catch(Exception e){} 

			re_init();  
			System.out.println("COMMIT OK");
		}
		
		String[] args = cmd.split(" ");
		
		String arg = args[0];
		String[] obj = args[1].split("\\.");
		int machine = Main.machines.get(obj[0]);
		String key = args[1];

	 
		String result = null; 
	
		if (arg.toUpperCase().equals("GET")){
			result = local.get(key); 
			boolean local_flag = (result != null); 
			
			try{
				result = r.c_get(Main.PROCESS_ID,key,local_flag,result); 
			}catch (Exception e){} 
			
			if (result == null){	
				System.out.println("ABORT"); 
				
				try{
					r.c_abort(Main.PROCESS_ID); 
				}catch(Exception e){} 
				re_init(); 
			}else if (result.equals("NOT FOUND")){
				try{
					r.c_abort(Main.PROCESS_ID); 
				}catch (Exception e){}
				re_init(); 
				System.out.println(result); 
			}else{
				System.out.println(result); 
			}
			
		}

		if (arg.toUpperCase().equals("SET")){
			String value = args[2]; 
			try{
				result = r.c_set(Main.PROCESS_ID, key, value);
			}catch (Exception e){}
			if (result == null){
				try{
					r.c_abort(Main.PROCESS_ID); 
				}catch (Exception e){}
				re_init(); 
				System.out.println("ABORT"); 	
			}else{
				local.put(key, value); 
				System.out.println(result); 
			}
		}
	}
}
