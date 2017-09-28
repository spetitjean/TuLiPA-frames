/*
 * @(#)ConnectionManager.java created 12.09.2006
 * 
 * Copyright (c) 2006 Alexander Koller
 *  
 */

package de.saar.chorus.domgraph.utool.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import de.saar.basic.Logger;
import de.saar.chorus.domgraph.chart.Chart;
import de.saar.chorus.domgraph.chart.ChartSolver;
import de.saar.chorus.domgraph.chart.SolvedFormIterator;
import de.saar.chorus.domgraph.graph.DomGraph;
import de.saar.chorus.domgraph.graph.EdgeData;
import de.saar.chorus.domgraph.graph.EdgeType;
import de.saar.chorus.domgraph.graph.NodeData;
import de.saar.chorus.domgraph.graph.NodeLabels;
import de.saar.chorus.domgraph.graph.NodeType;
import de.saar.chorus.domgraph.utool.AbstractOptions;


/**
 * The Utool main program for accessing the Domgraph functionality
 * in server mode. Utool ("Underspecification Tool") is the
 * Swiss Army Knife of Underspecification (Java version). This
 * version will accept commands in XML format from a socket. It
 * is started by calling the command-line version with command "server".<p>
 * 
 * The operation of this class is described in more detail in the
 * end-user documentation of Utool.<p>
 * 
 * Technically, the class <code>ConnectionManager</code> is only
 * responsible for accepting a new socket connection. It then
 * starts a <code>ServerThread</code>, which does all the work
 * of XML parsing and domgraph computations.
 * 
 * @author Alexander Koller
 *
 */

public class ConnectionManager {
    private static Logger logger;
    
    public static enum State { RUNNING, STOPPED };
    private static State state = State.STOPPED;
    private static ServerSocket ssock = null;
    
    public static interface StateChangeListener {
    	public void stateChanged(State newState);
    }
    private static List<StateChangeListener> listeners =
    	new ArrayList<StateChangeListener>();
    
    private static List<ServerThread> threads = 
    	new ArrayList<ServerThread>();
    
    

    /**
     * Starts the Utool Server. This method opens a socket on the port
     * specified in the <code>cmdlineOptions</code>, and accepts connections
     * from clients on this port. It will then spawn a new thread for dealing
     * with this particular client and go back to accepting more connections.
     * This method doesn't return under normal circumstances. You will probably
     * want to run it in a new thread of its own, which also takes care of
     * catching the IOException that this method can throw.<p>
     * 
     * If an I/O error occurs in this method, it will throw the IOException
     * that was thrown by the method that encountered the problem. In this case,
     * the server and all client-specific threads will be shut down, as per
     * the <code>stopServer</code> method below.<p>
     * 
     * This method sets the server state to <code>RUNNING</code> while it
     * accepts connections. It will also notify all connected state change
     * listeners of the change to the <code>RUNNING</code> state.
     * 
     * @param cmdlineOptions - an AbstractOptions object that defines the
     * "port", "hasOptionLogging", "getLogWriter", and "hasOptionWarmup" options.
     * @throws IOException
     */
    public static void startServer(AbstractOptions cmdlineOptions) throws IOException { 
        int port;
        
        try {
        	synchronized (ConnectionManager.class) {
        		state = State.RUNNING;
        		notifyListeners();
        		
        		logger = new Logger(cmdlineOptions.hasOptionLogging(), cmdlineOptions.getLogWriter());
            	port = cmdlineOptions.getPort();


            	// warm up if requested
            	// TODO - make this interruptible; right now the server can't
            	// be shut down until the warmup is done.
            	if( cmdlineOptions.hasOptionWarmup() ) {
            		warmup();
            	}


            	// open server socket
            	ssock = new ServerSocket(port);
            	logger.log("Listening on port " + port + "...");
        	}
        } catch(IOException e) {
        	// if an I/O exception occurs, kill all threads and shut down
        	// the server
        	stopServer();
        	throw e;
        } 

        try {
        	while( true ) {
        		// accept one connection
        		logger.log("Waiting for connection ... ");
        		Socket sock = ssock.accept();
        		logger.log("accepted connection from " + sock);

        		synchronized (ConnectionManager.class) {
        			ServerThread thread = new ServerThread(sock, logger);
        			thread.start();
        			threads.add(thread);
        		}
        	}
        } catch(IOException e) {
        	// If an unexpected I/O exception occurs here, then shut down
            // the server and report it. However, if this was an exception
            // in the accept() call above which was caused by the stopServer()
            // method below (= the state is now STOPPED), then just ignore
            // the error.
            if( state == State.RUNNING ) {
                stopServer();
                throw e;
            }
        } 
    }
    
    /**
     * Stops a running Utool Server. This will terminate the server
     * thread and all client-specific threads, and will shut down
     * all associated sockets. (This may result in clients complaining
     * about lost sockets.) <p>
     * 
     * This method sets the server state to <code>STOPPED</code>. 
     * It will also notify all connected state change
     * listeners of the change to the <code>STOPPED</code> state.
     * 
     */
    @SuppressWarnings("deprecation")
	public static void stopServer() {
    	synchronized (ConnectionManager.class) {
    		if( state == State.RUNNING ) {
                state = State.STOPPED;
                
    			if( ssock != null ) {
    				// We have to be this brutal, as the blocking accept()
    				// call in startServer() won't let us interrupt it.
    				// At this point, accept() will throw an I/O exception,
    				// and stopServer() will be called a second time; but
    				// as we have the lock in this thread, we will change the
    				// state to STOPPED before the original server thread
    				// gets here, and so it will just do nothing.
    				try {
						ssock.close();
						
		    			ssock = null;
					} catch (IOException e) {
						// At this point, we really don't care.
					}
    			}
    			
    			for( ServerThread thread : threads ) {
    				if( thread.getState() != Thread.State.TERMINATED ) {
                        try {
                            thread.closeSocket();
                        } catch(IOException e) {
                            // At this point, we really don't care.
                        }
                        
                        
    					// We can get away with using the stop method here, because
    					// there are no objects that are visible from more than one
    					// server thread, and thus thread-safety is not such a big
    					// concern here. It might still be nice to replace this
    					// by a call to interrupt, but then we might have to
    					// distribute wait() calls throughout the rest of the
    					// Domgraph code to have something that _can_ be interrupted
    					// in a fine-grained way, and would that be much better? - AK
    					thread.stop();
    				}
    			}

    			threads.clear();

    			notifyListeners();
    		}
		}
    }
    
    /**
     * Returns the current state of the server (RUNNING or STOPPED).
     * 
     * @return the state
     */
    public static State getState() {
    	return state;
    }
    
    /**
     * Adds a state change listener to this server. The listener's
     * <code>stateChanged</code> method will be called each time the
     * server's state changes.
     * 
     * @param listener a listener
     */
    public static void addListener(StateChangeListener listener) {
    	listeners.add(listener);
    }
    
    public static void removeListener(StateChangeListener listener) {
    	listeners.remove(listener);
    }
    
    
    private static void notifyListeners() {
    	for( StateChangeListener listener : listeners ) {
    		listener.stateChanged(state);
    	}
	}

    /**
     * Warms up the server after it has been started. This exercises the
     * most time-critical methods in the solver, in order to make sure the
     * JVM compiles them to native code and your later commands are executed
     * more efficiently.<p>
     * 
     * At the moment, the warmup command enumerates all solved forms of the
     * pure chain of length 12, two times. This seems to be sufficient on
     * Java 1.6 Beta on MacOS in server mode.
     * 
     */
    private static void warmup() {
        final int PASSES = 2;
        DomGraph graph = new DomGraph();
        NodeLabels labels = new NodeLabels();
        makeWarmupGraph(graph, labels);
        
        System.err.println("Warming up the server (" + PASSES + " passes) ... ");
        
        for( int i = 0; i < PASSES; i++ ) {
            Chart chart = new Chart();
            System.err.println("  - pass " + (i+1));
            
            ChartSolver.solve(graph, chart);
            SolvedFormIterator it = new SolvedFormIterator(chart,graph);
            while( it.hasNext() ) {
                it.next();
            }
        }
        
        
        System.err.println("Utool is now warmed up.");
    }


    /**
     * Generates the chain of length 12.
     * 
     * @param graph
     * @param labels
     */
    private static void makeWarmupGraph(DomGraph graph, NodeLabels labels) {
        graph.clear();
        labels.clear();

        graph.addNode("y0", new NodeData(NodeType.LABELLED));
        labels.addLabel("y0", "a0");
        graph.addNode("x1", new NodeData(NodeType.LABELLED));
        labels.addLabel("x1", "f1");
        graph.addNode("xl1", new NodeData(NodeType.UNLABELLED));
        graph.addNode("xr1", new NodeData(NodeType.UNLABELLED));
        graph.addNode("y1", new NodeData(NodeType.LABELLED));
        labels.addLabel("y1", "a1");
        graph.addNode("x2", new NodeData(NodeType.LABELLED));
        labels.addLabel("x2", "f2");
        graph.addNode("xl2", new NodeData(NodeType.UNLABELLED));
        graph.addNode("xr2", new NodeData(NodeType.UNLABELLED));
        graph.addNode("y2", new NodeData(NodeType.LABELLED));
        labels.addLabel("y2", "a2");
        graph.addNode("x3", new NodeData(NodeType.LABELLED));
        labels.addLabel("x3", "f3");
        graph.addNode("xl3", new NodeData(NodeType.UNLABELLED));
        graph.addNode("xr3", new NodeData(NodeType.UNLABELLED));
        graph.addNode("y3", new NodeData(NodeType.LABELLED));
        labels.addLabel("y3", "a3");
        graph.addNode("x4", new NodeData(NodeType.LABELLED));
        labels.addLabel("x4", "f4");
        graph.addNode("xl4", new NodeData(NodeType.UNLABELLED));
        graph.addNode("xr4", new NodeData(NodeType.UNLABELLED));
        graph.addNode("y4", new NodeData(NodeType.LABELLED));
        labels.addLabel("y4", "a4");
        graph.addNode("x5", new NodeData(NodeType.LABELLED));
        labels.addLabel("x5", "f5");
        graph.addNode("xl5", new NodeData(NodeType.UNLABELLED));
        graph.addNode("xr5", new NodeData(NodeType.UNLABELLED));
        graph.addNode("y5", new NodeData(NodeType.LABELLED));
        labels.addLabel("y5", "a5");
        graph.addNode("x6", new NodeData(NodeType.LABELLED));
        labels.addLabel("x6", "f6");
        graph.addNode("xl6", new NodeData(NodeType.UNLABELLED));
        graph.addNode("xr6", new NodeData(NodeType.UNLABELLED));
        graph.addNode("y6", new NodeData(NodeType.LABELLED));
        labels.addLabel("y6", "a6");
        graph.addNode("x7", new NodeData(NodeType.LABELLED));
        labels.addLabel("x7", "f7");
        graph.addNode("xl7", new NodeData(NodeType.UNLABELLED));
        graph.addNode("xr7", new NodeData(NodeType.UNLABELLED));
        graph.addNode("y7", new NodeData(NodeType.LABELLED));
        labels.addLabel("y7", "a7");
        graph.addNode("x8", new NodeData(NodeType.LABELLED));
        labels.addLabel("x8", "f8");
        graph.addNode("xl8", new NodeData(NodeType.UNLABELLED));
        graph.addNode("xr8", new NodeData(NodeType.UNLABELLED));
        graph.addNode("y8", new NodeData(NodeType.LABELLED));
        labels.addLabel("y8", "a8");
        graph.addNode("x9", new NodeData(NodeType.LABELLED));
        labels.addLabel("x9", "f9");
        graph.addNode("xl9", new NodeData(NodeType.UNLABELLED));
        graph.addNode("xr9", new NodeData(NodeType.UNLABELLED));
        graph.addNode("y9", new NodeData(NodeType.LABELLED));
        labels.addLabel("y9", "a9");
        graph.addNode("x10", new NodeData(NodeType.LABELLED));
        labels.addLabel("x10", "f10");
        graph.addNode("xl10", new NodeData(NodeType.UNLABELLED));
        graph.addNode("xr10", new NodeData(NodeType.UNLABELLED));
        graph.addNode("y10", new NodeData(NodeType.LABELLED));
        labels.addLabel("y10", "a10");
        graph.addNode("x11", new NodeData(NodeType.LABELLED));
        labels.addLabel("x11", "f11");
        graph.addNode("xl11", new NodeData(NodeType.UNLABELLED));
        graph.addNode("xr11", new NodeData(NodeType.UNLABELLED));
        graph.addNode("y11", new NodeData(NodeType.LABELLED));
        labels.addLabel("y11", "a11");
        graph.addNode("x12", new NodeData(NodeType.LABELLED));
        labels.addLabel("x12", "f12");
        graph.addNode("xl12", new NodeData(NodeType.UNLABELLED));
        graph.addNode("xr12", new NodeData(NodeType.UNLABELLED));
        graph.addNode("y12", new NodeData(NodeType.LABELLED));
        labels.addLabel("y12", "a12");

        graph.addEdge("x1", "xl1", new EdgeData(EdgeType.TREE));
        graph.addEdge("x1", "xr1", new EdgeData(EdgeType.TREE));
        graph.addEdge("xl1", "y0", new EdgeData(EdgeType.DOMINANCE));
        graph.addEdge("xr1", "y1", new EdgeData(EdgeType.DOMINANCE));
        graph.addEdge("x2", "xl2", new EdgeData(EdgeType.TREE));
        graph.addEdge("x2", "xr2", new EdgeData(EdgeType.TREE));
        graph.addEdge("xl2", "y1", new EdgeData(EdgeType.DOMINANCE));
        graph.addEdge("xr2", "y2", new EdgeData(EdgeType.DOMINANCE));
        graph.addEdge("x3", "xl3", new EdgeData(EdgeType.TREE));
        graph.addEdge("x3", "xr3", new EdgeData(EdgeType.TREE));
        graph.addEdge("xl3", "y2", new EdgeData(EdgeType.DOMINANCE));
        graph.addEdge("xr3", "y3", new EdgeData(EdgeType.DOMINANCE));
        graph.addEdge("x4", "xl4", new EdgeData(EdgeType.TREE));
        graph.addEdge("x4", "xr4", new EdgeData(EdgeType.TREE));
        graph.addEdge("xl4", "y3", new EdgeData(EdgeType.DOMINANCE));
        graph.addEdge("xr4", "y4", new EdgeData(EdgeType.DOMINANCE));
        graph.addEdge("x5", "xl5", new EdgeData(EdgeType.TREE));
        graph.addEdge("x5", "xr5", new EdgeData(EdgeType.TREE));
        graph.addEdge("xl5", "y4", new EdgeData(EdgeType.DOMINANCE));
        graph.addEdge("xr5", "y5", new EdgeData(EdgeType.DOMINANCE));
        graph.addEdge("x6", "xl6", new EdgeData(EdgeType.TREE));
        graph.addEdge("x6", "xr6", new EdgeData(EdgeType.TREE));
        graph.addEdge("xl6", "y5", new EdgeData(EdgeType.DOMINANCE));
        graph.addEdge("xr6", "y6", new EdgeData(EdgeType.DOMINANCE));
        graph.addEdge("x7", "xl7", new EdgeData(EdgeType.TREE));
        graph.addEdge("x7", "xr7", new EdgeData(EdgeType.TREE));
        graph.addEdge("xl7", "y6", new EdgeData(EdgeType.DOMINANCE));
        graph.addEdge("xr7", "y7", new EdgeData(EdgeType.DOMINANCE));
        graph.addEdge("x8", "xl8", new EdgeData(EdgeType.TREE));
        graph.addEdge("x8", "xr8", new EdgeData(EdgeType.TREE));
        graph.addEdge("xl8", "y7", new EdgeData(EdgeType.DOMINANCE));
        graph.addEdge("xr8", "y8", new EdgeData(EdgeType.DOMINANCE));
        graph.addEdge("x9", "xl9", new EdgeData(EdgeType.TREE));
        graph.addEdge("x9", "xr9", new EdgeData(EdgeType.TREE));
        graph.addEdge("xl9", "y8", new EdgeData(EdgeType.DOMINANCE));
        graph.addEdge("xr9", "y9", new EdgeData(EdgeType.DOMINANCE));
        graph.addEdge("x10", "xl10", new EdgeData(EdgeType.TREE));
        graph.addEdge("x10", "xr10", new EdgeData(EdgeType.TREE));
        graph.addEdge("xl10", "y9", new EdgeData(EdgeType.DOMINANCE));
        graph.addEdge("xr10", "y10", new EdgeData(EdgeType.DOMINANCE));
        graph.addEdge("x11", "xl11", new EdgeData(EdgeType.TREE));
        graph.addEdge("x11", "xr11", new EdgeData(EdgeType.TREE));
        graph.addEdge("xl11", "y10", new EdgeData(EdgeType.DOMINANCE));
        graph.addEdge("xr11", "y11", new EdgeData(EdgeType.DOMINANCE));
        graph.addEdge("x12", "xl12", new EdgeData(EdgeType.TREE));
        graph.addEdge("x12", "xr12", new EdgeData(EdgeType.TREE));
        graph.addEdge("xl12", "y11", new EdgeData(EdgeType.DOMINANCE));
        graph.addEdge("xr12", "y12", new EdgeData(EdgeType.DOMINANCE));
    }
    

}
