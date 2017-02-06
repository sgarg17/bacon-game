import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import net.datastructures.*;
/**
 * Class that allows searching of maps and computing distances from the root
 *
 * @author Shelley Garg, Dartmouth CS 10, Winter 2015
 */
public class BaconGame {
	//undirected graph with actors as vertices and movies as edges
	public static AdjacencyListGraphMap<String, String> baconGraph = new AdjacencyListGraphMap<String,String>();
	
	//actor that all paths will be measured from
	public String rootActor;
	
	//maps to help build the baconGraph
	public static Map<Integer, String> actorMap = new TreeMap<Integer,String>();
	public static Map<Integer, String> movieMap =  new TreeMap<Integer,String>();
	static Map<Integer, ArrayList<Integer>> movieActorMap =  new TreeMap<Integer,ArrayList<Integer>>();
	
	//directed graph that holds the connections to the root actor
	DirectedAdjListMap<String,String> shortestPaths;
	
	//maps distance from root to all actors at that distance
	Map<Integer, ArrayList<String>> distances;
	
	ArrayList<String> visitedList;
	
	public BaconGame(String rootActor) {
		this.rootActor = rootActor;
		
	}
	
	/** 
	 * Reads in from a file creates a map with integer IDs to strings
	 * @param the map to fill and the file from which data is pulled
	 * @throws IOException 
	 *
	 */
	
	static public void buildBasicMap(Map<Integer,String> map, String in_file) throws IOException{
		BufferedReader input = null;
		try{
			input = new BufferedReader(new FileReader(in_file));
			String line;
			
			while ((line = input.readLine()) != null){
				String[] tokens = line.split("\\|");
				map.put(Integer.parseInt(tokens[0]), tokens[1]);
			}
			
		}catch(FileNotFoundException e){
			System.err.println(e.getMessage());
			System.exit(1);
			
		}catch(IOException e){
			System.err.println(e.getMessage());
			System.exit(1);
			
		}finally{
			if (input != null) {
				input.close();
			}
		}
	}
	
	/** 
	 * Reads in from a file and uses data to create map of movie IDs to actor IDs
	 * @param the map to fill and the file from which data is pulled
	 * @throws IOException 
	 */
	public static void buildArrayMap(Map<Integer, ArrayList<Integer>> arrayMap, String in_file) throws IOException{
		BufferedReader input = null;
		try{
			input = new BufferedReader(new FileReader(in_file));
		
			String line;
		
			while ((line = input.readLine()) != null){
				String[] tokens = line.split("\\|");
				int movieID = Integer.parseInt(tokens[0]);
				int actorID = Integer.parseInt(tokens[1]);
			
				//access the actor list of the movie and add the new actor
				if (movieActorMap.containsKey(movieID)){
			    	movieActorMap.get(movieID).add(actorID);
			    }
				
			    //add a new movie and new array list of actors
			    else{
			    	ArrayList<Integer> actors = new ArrayList<Integer>();
			    	actors.add(actorID);
			    	movieActorMap.put(movieID, actors);
			    }
			}
		}catch(FileNotFoundException e){
			System.err.println(e.getMessage());
			System.exit(1);
			
		}catch(IOException e){
			System.err.println(e.getMessage());
			System.exit(1);
			
		}finally{
			if (input != null) {
				input.close();
			}
		}
		
	
	}
	
	/** 
	 * builds the necessary maps and synthesizes map information into a graph which hold actors as vertices and movies as edges
	 * @throws IOException 
	 *
	 */
	static public void buildGraph() throws IOException {
		//build all necessary maps
		buildBasicMap(actorMap, "Files/actorsTest.txt");
		buildBasicMap(movieMap, "Files/moviesTest.txt");
		buildArrayMap(movieActorMap, "Files/movie-actorsTest.txt");
		
		//insert all the vertices
		 for(Integer actorID : actorMap.keySet()){
		    baconGraph.insertVertex(actorMap.get(actorID));
		 }
		
		 //insert all the edges by looping through each movie and then each actor within the movie to build edges
		 for (Integer movieID : movieActorMap.keySet()){
			 for (int i = 0; i < movieActorMap.get(movieID).size(); i++){
				 for(int j = i+1; j < movieActorMap.get(movieID).size(); j++){
					baconGraph.insertEdge(actorMap.get(movieActorMap.get(movieID).get(i)), actorMap.get(movieActorMap.get(movieID).get(j)), movieMap.get(movieID));
				 }
			 } 
		 }
		 
	}
	
	/** 
	 * traverses the undirected graph to find the shortest paths back to the root actor and holds path information in a new directed map
	 * @param 
	 */
	public void buildTree(){
		//access the vertex referenced by the string passed in
		Vertex<String> v = baconGraph.getVertex(rootActor);
		
		//create a map to hold the shortest path between 
		shortestPaths = new DirectedAdjListMap<String, String>();
		
		//create a queue of nodes to be visited
		ArrayList<Vertex<String>> tbVisitedQ = new ArrayList<Vertex<String>>();
		
		//add root to queue and map
		shortestPaths.insertVertex(v.element());
		tbVisitedQ.add(v);
		
		//until queue is empty
		while (tbVisitedQ.size() > 0){
			//remove the first vertex in list and collect its incident edges
			Vertex<String> currVertex = tbVisitedQ.remove(0);
			Iterable<Edge<String>>incEdges = baconGraph.incidentEdges(currVertex);
			
			//looping through the incident edges, check if each edge is already in the tree(already has been visited)
			for (Edge<String> e: incEdges) {
				//get the opposite vertex on the incident edge
				Vertex<String> v1 = baconGraph.opposite(currVertex, e);
				
				// if the opposite vertex has not been visited(is not in the tree yet)
				if (!shortestPaths.vertexInGraph(v1.element())){
					//add the vertex to the tree and the queue of vertices to be visited
					shortestPaths.insertVertex(v1.element());
					shortestPaths.insertDirectedEdge(v1.element(), currVertex.element(), e.element());
					tbVisitedQ.add(v1);
				}
			}
		}
	}
	
	public void traverseTree(){
		distances = new TreeMap<Integer, ArrayList<String>>()
;		visitedList = new ArrayList<String>();
		traverseTreeHelper(shortestPaths.getVertex(rootActor), 0);
		
	}
	
	public void traverseTreeHelper(Vertex<String> currVertex, int distance){
		//implicit base case if visited, do nothing
		if(!visitedList.contains(currVertex.element())){
			visitedList.add(currVertex.element());
			//access the actor list of the movie and add the new actor
			if (distances.containsKey(distance)){
		    	distances.get(distance).add(currVertex.element());
		    }
			
		    //add a new movie and new array list of actors
		    else{
		    	ArrayList<String> actors = new ArrayList<String>();
		    	actors.add(currVertex.element());
		    	distances.put(distance, actors);
		    }
			Iterable<Edge<String>>incEdges = baconGraph.incidentEdges(currVertex);
			for (Edge<String> e: incEdges) {
				//get the opposite vertex on the incident edge
				Vertex<String> nextVertex = baconGraph.opposite(currVertex, e);
				traverseTreeHelper(nextVertex, distance+1);
			}
		}
	}
	/** 
	 * traverses the undirected graph to find the shortest paths back to the root actor and holds path information in a new directed map
	 * @param 
	 */
	public ArrayList<Edge<String>> pathToRoot(String startVertexName){
		//create the shortest path directed map/tree
		buildTree();
		
		//access the start vertex
		Vertex<String> startVertex = shortestPaths.getVertex(startVertexName);
		
		//create new array list to hold path from start vertex to root
		ArrayList<Edge<String>> baconNumberPath = new ArrayList<Edge<String>>();
		
		// if the vertex is connected to the root
		if(shortestPaths.getVertex(startVertexName) != null){
			Vertex<String> current = startVertex; //iterating vertex set to start vertex
			
			//continue searching until the root vertex is found
			while (!current.equals(shortestPaths.getVertex(rootActor))){
				//access incident edges out (of which there should only be one) and add to path 
				ArrayList<Edge<String>> incEdgesOut = (ArrayList<Edge<String>>) shortestPaths.incidentEdgesOut(current.element());

				Edge<String> e = incEdgesOut.get(0);
				baconNumberPath.add(e);
				
				//get the source vertex or the opposite along the incident edge in and update current
				Vertex<String> source = shortestPaths.opposite(current, e);
				current = source;
			}
		}
		return baconNumberPath;
	}
	
	public static void main(String [] args) {
		//declare new object
		BaconGame bG;
		//make graph
		try {
			BaconGame.buildGraph();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		//scanner object
		Scanner user_input = new Scanner(System.in);
		
		//get root for graph
		System.out.print("Enter the name of a root actor: ");
		String rootactor;
		rootactor = user_input.nextLine(); 
		
		while(baconGraph.getVertex(rootactor) == null){
			System.out.println("That actor is not in the database. Try again.");
			System.out.print("Enter the name of a root actor: ");
			rootactor = user_input.nextLine();
		}
		//build graph
		bG = new BaconGame(rootactor);
		bG.buildTree();
		bG.traverseTree();

		Character command;
		
        do {
            System.out.print("Command (q, n, m, A, a, g, ?): ");
            command = user_input.nextLine().charAt(0);

            switch (command) {
            case 'q': // Quit
                System.out.println("Thanks for playing!");
                break;

            case 'n': //create new graph
                System.out.print("Enter new root: ");
                rootactor = user_input.nextLine();
                while(baconGraph.getVertex(rootactor) == null){
        			System.out.println("That actor is not in the database. Try again.");
        			System.out.print("Enter the name of a root actor: ");
        			rootactor = user_input.nextLine();
        		}
                bG = new BaconGame(rootactor);
                bG.buildTree();
                bG.traverseTree();
                break;
            
            case 'm': //get max number of degrees
            	bG.traverseTree();
            	int max = 0;
            	for(int key: bG.distances.keySet()){
                	max++;
                }
            	max = max - 1; //accounting for root
            	System.out.println("The maximum degrees of separation from " + bG.rootActor +  " are " + max + ".");
                break;

            case 'A': // actorsAtDistance
                System.out.print("Enter degrees of separation(integer): ");
                int degrees = Integer.parseInt((user_input.nextLine()));
               
                //bG.buildTree();
                //bG.traverseTree();
                
                if(bG.distances.containsKey(degrees)){
                	for (String actor: bG.distances.get(degrees)){
                		System.out.println(actor);
                	}
                }
                else{
                	System.out.println("No actors at this degree of separation!");
                }
                break;

            case 'a': //average distance
            	bG.buildTree();
            	bG.traverseTree();
            	
                int total = 0;
                int num = 0;
               
                for(int key: bG.distances.keySet()){
                	for(String actor: bG.distances.get(key)){
                		num++;
                		total+=key;
                	}
                }
                float average = (float)total/(float)(num-1);
                System.out.println("The average degrees of separation from " + bG.rootActor +  " are " + average + ".");
                break;

            case 'g': //get Degree
                System.out.print("Enter name: ");
                String actor_name = user_input.nextLine();
                if (BaconGame.baconGraph.getVertex(actor_name) == null){
    				System.out.println("That actor is not in the database. Try again.");
    			}
    			//if entered actor is root actor, degrees of separation = 0
    			else if(actor_name.equals(bG.rootActor)){
    				System.out.println(actor_name + "'s " + bG.rootActor + " number is 0!");
    				
    			}
    			//if in graph output degrees of separation and path of separation
    			else {
    				ArrayList<Edge<String>> path = bG.pathToRoot(actor_name);
    				
    				if (path.size()>0){
    					System.out.println(actor_name + "'s " + bG.rootActor + " number is " + path.size() + ".");
    					
    					for(Edge<String> edge: path){
    						System.out.println(bG.shortestPaths.endVertices(edge)[0] + " appeared in " + edge.element() + " with " + bG.shortestPaths.endVertices(edge)[1] + ".");
    						
    					}
    				}
    				//if path size is zero, actor is not connected to root
    				else{
    					System.out.println(actor_name + "'s Kevin Bacon number is infinity.");
    				}
    			}
                break;

            case '?': // Print all the commands
                System.out.println("Commands are\n  q: quit\n"  
                                + "  n: newGraph \n  m: maxDistance\n  A: actorsAtDistance\n  a: averageDistance\n  g: getDegree\n  ?: print this command list\n");
                break;

            default:
                System.out.println("Huh?");
            }
        } while (command != 'q');
        user_input.close();
	}
}

