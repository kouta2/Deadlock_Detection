/*
 *
 * handles locking of data
 */

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Iterator;
import java.util.HashSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.Semaphore;
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

        public boolean is_write()
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

        @Override
        public boolean equals(Object o)
        {
            if(o == this)
                return true;

            if(!(o instanceof Edge))
                return false;

            Edge e = (Edge)o;
            return e.destination.equals(this.destination);
        }

        public String toString()
        {
            return destination + ", " + is_write;
        }
    }

    private HashMap<String, ArrayList<Edge>> graph; // outgoing edges
    private HashMap<String, Semaphore> semaphores; // semaphore for each variable
    private Lock l;

    public Unlocker()
    {
        graph = new HashMap<String, ArrayList<Edge>>();
        graph.put("6", new ArrayList<Edge>());
        graph.put("7", new ArrayList<Edge>());
        graph.put("8", new ArrayList<Edge>());
        graph.put("9", new ArrayList<Edge>());

        semaphores = new HashMap<String, Semaphore>();

        l = new ReentrantLock();
    }

    public HashMap<String, ArrayList<Edge>> get_graph()
    {
        return graph;
    }

    private void add_vertex(String vert)
    {
        graph.put(vert, new ArrayList<Edge>());
        semaphores.put(vert, new Semaphore(3, true));
    }

    private void add_edge(String vertA, String vertB, boolean write)
    {
        graph.get(vertA).add(new Edge(vertB, write));
    }

    private ArrayList<Edge> get_neighbors(String vert)
    {
        return graph.get(vert);
    }

    private boolean is_cycle(String vert)
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
        {
            return true;
        }
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
    *  Removes a vertex from the graph
    */ 
    private void remove_vertex(String vert)
    {
        clear_vertex(vert);
        graph.remove(vert);
    }

    private void remove_edge(String vertA, String vertB)
    {
        /*
        System.err.println("IN REMOVE_EDGES!!!");
        ArrayList<Edge> temp = graph.get(vertA);
        System.err.println(temp.toString());
        
        temp.remove(vertB);
        System.err.println(temp.toString());
        */
        System.err.println("IN REMOVE_EDGES!!!");
        ArrayList<Edge> temp = graph.get(vertA);
        System.err.println(temp.toString());
        ArrayList<Edge> new_vals = new ArrayList<Edge>();
        for(Edge e : temp)
        {
            if(!e.get_destination().equals(vertB))
                new_vals.add(e);
        }
        System.err.println(new_vals.toString());
        graph.put(vertA, new_vals);
        
    }

    private boolean contain_vert(String key)
    {
        return graph.containsKey(key);
    }

    private Edge contain_edge(String vertA, String vertB)
    {
        ArrayList<Edge> temp = graph.get(vertA);
        Edge cmp = new Edge(vertB, true);
        for(Edge e : temp)
        {
            if(e.equals(cmp))
                return e;
        }
        return null;
    }

    private void grab_all_three_semaphores(String key) throws InterruptedException
    {
        Semaphore s = semaphores.get(key);
        s.acquire(3);       
    }

    private void grab_a_semaphore(String key) throws InterruptedException
    {
        Semaphore s = semaphores.get(key);
        s.acquire(1);
    }

    private void release_write_semaphore(String key)
    {
        Semaphore s = semaphores.get(key);
        s.release(3);
    }

    private void release_read_semaphore(String key)
    {
        Semaphore s = semaphores.get(key);
        s.release(1);
    }

    /*
    *
    * Check if key is in graph, if it isn't add it
    * Check if key has any edges leaving it
    * if only edge is yours, make yourself a write edge
    * else if other edges exist, add wait for edge and run cycle detection
    * else add acquired write edge
    *
    */ 
    public boolean isWriteable(int pid, String key)
    {
        System.err.println("in isWriteable with pid = " + pid + " key = " + key);
        String pid_vert = Integer.toString(pid);
        l.lock();
        try
        {
            if(!contain_vert(key))
                add_vertex(key);

            ArrayList<Edge> neighbors = get_neighbors(key);
            for(Edge e : neighbors)
            {
                if(e.get_destination().equals(pid_vert))
                {
                    if(e.is_write())
                        return true;
                    else
                    {
                        System.err.println("ARE WE REMOVING READ EDGE IN ISWRITEABLE???");
                        System.err.println(graph.toString());
                        remove_edge(key, pid_vert);
                        release_read_semaphore(key);
                        System.err.println(graph.toString());
                        break;
                    }
                }
            }

            if(!neighbors.isEmpty())
            {
                add_edge(pid_vert, key, true); // wait for edge
                if(is_cycle(pid_vert))
                {
                    remove_edge(pid_vert, key);
                    return false;
                }
            }
            
            l.unlock();
            System.err.println("Grabbing semaphores in isWriteable");
            grab_all_three_semaphores(key);
            System.err.println("Grabbed all 3 semaphores in isWriteable");
            l.lock();
            remove_edge(pid_vert, key); // remove wait for edge
            add_edge(key, pid_vert, true); // acquired edge
            return true;
            
        }
        catch (Exception e) {}
        finally { l.unlock();}
        return false;
    }

    /*
    *
    * Check if key is in graph, if it isn't return false to abort transaction
    * Check edges
    * If any edge is us, return true
    * else if we have no edges and there is a write edge, add wait for edge and run cycle detection
    * add acquired read edge
    *
    */
    public boolean isReadable(int pid, String key)
    {
        System.err.println("in isReadable with pid = " + pid + " key = " + key);
        String pid_vert = Integer.toString(pid);
        l.lock();
        try
        {
            if(!contain_vert(key))
                return false;

            ArrayList<Edge> neighbors = get_neighbors(key);
            Edge cmp = new Edge(pid_vert, true);
            for(Edge e : neighbors)
            {
                if(e.equals(cmp))
                    return true;
            }

            boolean has_writer = false;
            for(Edge e : neighbors)
            {
                if(e.is_write())
                {
                    has_writer = true;
                    break;
                }
            }
            if(has_writer)
            {
                add_edge(pid_vert, key, false); // wait for reading edge
                if(is_cycle(pid_vert))
                {
                    remove_edge(pid_vert, key);
                    return false;
                }
            }
            l.unlock();
            System.err.println("Grabbing a semaphore in isReadable");
            grab_a_semaphore(key);
            System.err.println("Grabbed a semaphore in isReadable");
            l.lock();
            remove_edge(pid_vert, key); // remove wait for edge
            add_edge(key, pid_vert, false); // acquired edge
            return true;
        }
        catch (Exception e) {}
        finally { l.unlock();}
        return false;
    }

    /*
    *
    * removes all edges coming into and leaving vert. LEAVES VERTEX IN THE GRAPH
    */
    public void clear_vertex(String vert)
    {
        System.err.println("in clear_vertex with vertex = " + vert);
        System.err.println("graph before is: " + graph);
        l.lock();
        try
        {
            graph.put(vert, new ArrayList<Edge>());
            Edge e = new Edge(vert, true);
            Iterator it = graph.entrySet().iterator();
            while(it.hasNext())
            {
                Map.Entry pair = (Map.Entry)it.next();
                String key = (String)pair.getKey();
                ArrayList<Edge> temp = (ArrayList<Edge>)pair.getValue();

                Edge curr = contain_edge(key, vert);

                if(curr != null)
                {
                    if(curr.is_write())
                    {
                        System.err.println("Release write semaphore");
                        release_write_semaphore(key);
                    }
                    else
                    {
                        System.err.println("Release read semaphore");
                        release_read_semaphore(key);
                    }
                    temp.remove(curr);
                }
            }
        }
        finally { l.unlock();}
        System.err.println("graph after is: " + graph + "\n");
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


