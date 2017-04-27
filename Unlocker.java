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

    public class Edge
    {
        private String destination;
        private boolean is_write;

        public Edge(String dest, boolean write)
        {
            destination = dest;
            is_write = write;
        }

        public boolean get_is_write()
        {
            return is_write;
        }

        public void upgrade()
        {
            is_write = true;
        }

        public String get_destination()
        {
            return destination;
        }

        public boolean equals(Edge e)
        {
            return e.get_destination().equals(destination);
        }

        public String toString()
        {
            return destination + ", " + is_write;
        }
    }

    private HashMap<String, ArrayList<Edge>> graph; // outgoing edges

    public Unlocker()
    {
        graph = new HashMap<String, ArrayList<Edge>>();
        graph.put("6", new ArrayList<Edge>());
        graph.put("7", new ArrayList<Edge>());
        graph.put("8", new ArrayList<Edge>());
        graph.put("9", new ArrayList<Edge>());
    }

    public HashMap<String, ArrayList<Edge>> get_graph()
    {
        return graph;
    }

    public void add_vertex(String vert)
    {
        graph.put(vert, new ArrayList<Edge>());
    }

    public void add_edge(String vertA, String vertB, boolean write)
    {
        graph.get(vertA).add(new Edge(vertB, write));
    }

    public ArrayList<Edge> get_neighbors(String vert)
    {
        return graph.get(vert);
    }

    public boolean is_cycle(String vert)
    {
        ArrayList<Edge> neighbors = get_neighbors(vert);
        HashSet<String> visited = new HashSet<String>();
        for(Edge e : neighbors)
        {
            String n = e.get_destination();
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
            ArrayList<Edge> neighbors = get_neighbors(curr);
            for(Edge e : neighbors)
            {
                String n = e.get_destination();
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
        graph.put(vert, new ArrayList<Edge>());
        Edge e = new Edge(vert, true);
        Iterator it = graph.entrySet().iterator();
        while(it.hasNext())
        {
            Map.Entry pair = (Map.Entry)it.next();

            ArrayList<Edge> temp = (ArrayList<Edge>)pair.getValue();
            temp.remove(e);
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
        u.add_edge("parker", "6", true);
        u.add_edge("7", "parker", false);
        System.out.println(u.is_cycle("6"));
        System.out.println(u.is_cycle("8"));
        u.add_edge("6", "7", false);
        System.out.println(u.get_graph().toString());
        System.out.println(u.is_cycle("6"));
        System.out.println(u.is_cycle("8"));
    }

}


