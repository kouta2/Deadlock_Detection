/*
 *
 * handles locking of data
 */

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Iterator;
import java.util.HashSet;
import java.util.*;

public class Unlocker
{

    private HashMap<String, ArrayList<String>> graph; // outgoing edges

    public Unlocker()
    {
        graph = new HashMap<String, ArrayList<String>>();
        graph.put("6", new ArrayList<String>());
        graph.put("7", new ArrayList<String>());
        graph.put("8", new ArrayList<String>());
    }

    public HashMap<String, ArrayList<String>> get_graph()
    {
        return graph;
    }

    public void add_vertex(String vert)
    {
        graph.put(vert, new ArrayList<String>());
    }

    public void add_edge(String vertA, String vertB)
    {
        graph.get(vertA).add(vertB);
    }

    public ArrayList<String> get_neighbors(String vert)
    {
        return graph.get(vert);
    }

    public boolean is_cycle(String vert)
    {
        ArrayList<String> neighbors = get_neighbors(vert);
        HashSet<String> visited = new HashSet<String>();
        for(String n : neighbors)
        {
            if(!visited.contains(n))
                if(is_cycle_helper(n, visited, vert))
                    return true;
        }
        return false;
    }

    private boolean is_cycle_helper(String curr, HashSet<String> visited, String vert)
    {
        visited.add(curr);
        if(curr.equals(vert))
            return true;
        else
        {
            ArrayList<String> neighbors = get_neighbors(curr);
            for(String n : neighbors)
            {
                if(!visited.contains(n))
                    if(is_cycle_helper(n, visited, vert))
                        return true;
            }
            return false;
        }
    }

    /*
    *
    * removes all edges coming into and leaving vert. LEAVES VERTEX IN THE GRAPH
    */
    public void clear_vertex(String vert)
    {
        graph.put(vert, new ArrayList<String>());

        Iterator it = graph.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry pair = (Map.Entry)it.next();

            ArrayList<String> temp = (ArrayList<String>)pair.getValue();
            temp.remove(vert);
        }
    }

    /*
    *
    *  Removes a vertex from the graph
    */ 
    public void remove_vertex(String vert)
    {
        clear_vertex(vert);
        graph.remove(vert);
    }

    public static void main(String[] args)
    {
        Unlocker u = new Unlocker();
        u.add_vertex("parker");
        u.add_edge("parker", "6");
        u.add_edge("7", "parker");
        System.out.println(u.is_cycle("6"));
        System.out.println(u.is_cycle("8"));
        u.add_edge("6", "7");
        System.out.println(u.get_graph().toString());
        System.out.println(u.is_cycle("6"));
        System.out.println(u.is_cycle("8"));
    }

}


