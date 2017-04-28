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

        public boolean equals(Edge e)
        {
            return e.get_destination().equals(destination);
        }

        public String toString()
        {
            return destination + ", " + is_write;
        }
    }

    /*    
    public class LockOwner
    {
        private Semaphore s;
        private int pid;

        public LockOwner(int p)
        {
            pid = p;
            s = new Semaphore(1, true);
        }

        public void acquire(int p) throws InterruptedException
        {
            pid = p;
            s.acquire();
        }

        public void release() throws InterruptedException
        {
            s.release();
            pid = -1;
        }
    
        public int get_pid()
        {
            return pid;
        }

        public int set_pid(int p)
        {
            pid = p;
        }
    }
    */


    private HashMap<String, ArrayList<Edge>> graph; // outgoing edges
    // private HashMap<String, LockOwner[]> locks; // locks for every variable
    private HashMap<String, Semaphore> semaphores; // semaphore for each variable
    private Lock l;

    public Unlocker()
    {
        graph = new HashMap<String, ArrayList<Edge>>();
        graph.put("6", new ArrayList<Edge>());
        graph.put("7", new ArrayList<Edge>());
        graph.put("8", new ArrayList<Edge>());
        graph.put("9", new ArrayList<Edge>());

        // locks = new HashMap<String, LockOwner[]>();
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

        /*
        locks.put(vert, new LockOwner[3]);
        LockOwner[] r  = locks.get(vert);
        r[0] = new LockOwner(-1);
        r[1] = new LockOwner(-1);
        r[2] = new LockOwner(-1);
        */

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
    *  Removes a vertex from the graph
    */ 
    private void remove_vertex(String vert)
    {
        clear_vertex(vert);
        graph.remove(vert);
    }

    private void remove_edge(String vertA, String vertB)
    {
        ArrayList<Edge> temp = graph.get(vertA);
        temp.remove(new Edge(vertB, true));
    }

    private boolean contain_vert(String key)
    {
        return graph.containsKey(key);
    }

    private Edge contain_edge(String vertA, String vertB)
    {
        ArrayList<Edge> temp = graph.get(vertA);
        int index = temp.indexOf(new Edge(vertB, true));
        if(index != -1)
            return temp.get(index);
        else
            return null;
    }

    private void grab_all_three_semaphores(String pid, String key) throws InterruptedException
    {
        Semaphore s = semaphores.get(key);
        s.acquire(3);       
    }

    private void grab_a_semaphore(String pid, String key) throws InterruptedException
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
    * Check if acquired edge exists already
    * if it is write, do nothing and return
    * if it is read, remove edge
    * add wait for edge
    * if there is cycle return false so wait for will be aborted
    * else try to grab all 3 semaphores
    * remove wait for edge and replace with acquired edge
    * return true
    *
    */ 
    public boolean isWriteable(int pid, String key)
    {
        String pid_vert = Integer.toString(pid);
        l.lock();
        try
        {
            if(!contain_vert(key))
                add_vertex(key);

            Edge e = contain_edge(key, pid_vert);
            if(e != null)
            {
                if(e.is_write())
                    return true; // already have permission
                else
                {
                    remove_edge(key, pid_vert);
                    release_read_semaphore(key); // remove read semaphore
                }
            }

            add_edge(pid_vert, key, true); // wait for
            if(is_cycle(pid_vert))
            {
                return false; // edge will be removed in wait_for
            }
        }
        finally { l.unlock();}
        try{ grab_all_three_semaphores(pid_vert, key);} // need to unlock graph before grabbing semaphores
        catch (Exception e) {}
        l.lock();
        try
        {
            remove_edge(pid_vert, key);
            add_edge(key, pid_vert, true); // acquired
        }
        finally { l.unlock();}
        return true;
    }

    /*
    *
    * Check if key is in graph, if it isn't return false to abort transaction
    * Check if Write edge or read edge is in graph already
    * If it is, return true
    * else add wait for edge
    * if there is cycle return false so wait for edge will be aborted
    * else try to grab a semaphore
    * remove wait for edge and replace with acqured edge
    * return true
    *
    */
    public boolean isReadable(int pid, String key)
    {
        String pid_vert = Integer.toString(pid);
        l.lock();
        try
        {
            if(!contain_vert(key))
                return false;

            Edge e = contain_edge(key, pid_vert);
            if(e != null)
            {
                return true;
            }

            add_edge(pid_vert, key, false); // wait for
            if(is_cycle(pid_vert))
            {
                return false;
            }
        }
        finally { l.unlock();}
        try { grab_a_semaphore(pid_vert, key);} // need to unlock graph before grabbing semaphore
        catch (Exception e) {}
        l.lock();
        try
        {
            remove_edge(pid_vert, key);
            add_edge(key, pid_vert, false);
        }
        finally { l.unlock();}
        return true;
    }

    /*
    *
    * removes all edges coming into and leaving vert. LEAVES VERTEX IN THE GRAPH
    */
    public void clear_vertex(String vert)
    {
        l.lock();
        try
        {
            graph.put(vert, new ArrayList<Edge>());
            Edge e = new Edge(vert, true);
            Iterator it = graph.entrySet().iterator();
            while(it.hasNext())
            {
                Map.Entry pair = (Map.Entry)it.next();

                ArrayList<Edge> temp = (ArrayList<Edge>)pair.getValue();
                int index = temp.indexOf(e);
                if(index != -1)
                {
                    Edge curr = temp.get(index);
                    if(curr.is_write())
                    {
                        release_write_semaphore((String)pair.getKey());
                    }
                    else
                    {
                        release_read_semaphore((String)pair.getKey());
                    }
                    temp.remove(e);
                }
            }
        }
        finally { l.unlock();}
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


