import java.io.*;
import java.lang.*;
import java.util.*;

public class familyTree{
	//Class node representing a name and its parent 
	private class Node{
		public String name;
		public String parent;
		public Node(String mem, String par){
			name = mem;
			parent = par;
		}
	}

	//Generation class that holds a list of nodes for each generation.
	private class Generation{
		public ArrayList<Node> nodes;

		public Generation(){
			nodes =  new ArrayList<Node>();
		}

		public Generation(String name, String parent){
			nodes =  new ArrayList<Node>();
			nodes.add(0, new Node(name, parent));
		}

		public Generation(ArrayList<Node> children){
			nodes = new ArrayList<Node>();
			nodes.addAll(children);
		}
		public void addNode(String name, String parent){
			nodes.add(new Node(name, parent));
		}

		public boolean areChildren(ArrayList<Node> parent){ //checks whether the current generation are the children of the previous generation in the tree
			for(Node child : nodes){
				for(Node par : parent) {
					if(child.parent.equals(par.name)){
						return true;
					}
				}
			}
			return false;
		}
	}

	//Priority queue class that holds any nodes whose parents/children are not yet in the family tree
	private class Queue{
		private ArrayList<Node> queue;
		private ArrayList<String> parents;

		public Queue(){  
			queue = new ArrayList<Node>();
			parents = new ArrayList<String>();
		}

		public void enqueue(String name, String parent){ //adds new node into the priority queue
			queue.add(new Node(name, parent));
			parents.add(new String(parent));
		}

		public boolean containsParent(String parent){
			if(parents.contains(parent)) return true;
			else return false;
		}

		public ArrayList<Node> dequeue(String parent){ //removes node(s) whose parent matches the input string and adds it to the family tree
			ArrayList<Node> children = new ArrayList<Node>();
			for(Node node : this.queue){
				if(node.parent.equals(parent)){ 
					children.add(node);		
				}
			}
			parents.remove(parent);
			return children; 
		}

		public boolean empty(){
			if(queue.size() > 0) return false;
			else return true;
		}

	}

	private ArrayList<Generation> genTree;  //overall tree that holds the list of generations
	private Queue queue;	//queue which temporarily holds nodes which have not yet been added to the overall family tree

	public familyTree() {
		genTree = new ArrayList<Generation>();
		queue = new Queue();
	}

	private void addChildren(int i, String parent){ //adds children nodes whose parent matches the input string
		if(genTree.size() > i+1 && genTree.get(i+1).areChildren(genTree.get(i).nodes)) {
			genTree.get(i+1).nodes.addAll(queue.dequeue(parent));
		} else if (genTree.size() > i+1 && !genTree.get(i+1).areChildren(genTree.get(i).nodes)){
			Generation gen = new Generation(queue.dequeue(parent));
			genTree.add(i+1, gen);
		} else {
			Generation gen = new Generation(queue.dequeue(parent));
			genTree.add(gen);
		}
	}

	private BufferedReader br;
	
	/* fillTree represents the core method that organises the family tree structure into successive generations.
		
		Initially, it reads in the first node and fills it into the tree, then for every other node read from the file, it performs the following two:
		
		1) it checks whether the read node is the main parent in the family tree (i.e has a NULL parent). If so, it adds it to the root of the tree, then 
			moves on to read the next node in the file. 
			Else, it performs step two. 
		2) it checks whether any of the current nodes in the tree is a parent/child of the read node. If so, it adds it in its respective generation 
			position in the tree. (This procedure optimises the speed in which nodes are added into the tree instead of just filling every node (except 
			for the root node) into a queue)
			If not, it stores the node in the priority queue and reads the next node from the file.
		
		When all nodes have been read from the file, every node that is currently in the tree (starting from the root node, moving breadth first) checks 
		whether it has any children stored in the queue. If so, all its children are removed from the queue and added to the tree IN AN ORDERLY MANNER. 

		A final check is done to confirm that there are no nodes remaining in the queue. The tree is now filled. 
	*/
	public void fillTree(String filename){		
		String line;
		try{
			br = new BufferedReader(new FileReader(filename));
			br.readLine();
			while((line = br.readLine()) != null){
				String[] names = line.split(",");
				//fill the first read node into the tree regardless of whether it is the root or not. (This procedure speeds up the fillTree process)
				boolean found = false;
				if(genTree.size() == 0 || names[0].equals("NULL")){ 
					Generation gen = new Generation(names[1], names[0]);
					genTree.add(0, gen);
					found = true;
				} else {
					for(int i = 0; !found && i < genTree.size(); i++){
						for(Node node : genTree.get(i).nodes){  //check if the read node is a parent/child of any node in the tree (check generation after generation)
							if(names[1].equals(node.parent) && i == 0){ //check if it is a parent
								Generation gen = new Generation(names[1], names[0]);
								genTree.add(0, gen);
								found = true;
								break;
							} else if (names[1].equals(node.parent) && i > 0 && genTree.get(i).areChildren(genTree.get(i-1).nodes)) {
								genTree.get(i-1).addNode(names[1], names[0]);
								found = true;
								break;
							} else if (names[1].equals(node.parent) && i > 0 && !genTree.get(i).areChildren(genTree.get(i-1).nodes)) {
								Generation gen = new Generation(names[1], names[0]);
								genTree.add(i-1, gen);
								found = true;
								break;
							} else if (names[0].equals(node.name) && i+1 == genTree.size()){  //now check if it is a child of any node
								Generation gen = new Generation(names[1], names[0]);
								genTree.add(gen);
								found = true;
								break;
							}	else if (names[0].equals(node.name) && i+1 < genTree.size() && genTree.get(i+1).areChildren(genTree.get(i).nodes)) {
								genTree.get(i+1).addNode(names[1], names[0]);
								found = true;
								break;
							} else if (names[0].equals(node.name) && i+1 < genTree.size() && genTree.get(i+1).areChildren(genTree.get(i).nodes)) {
								Generation gen = new Generation(names[1], names[0]);
								genTree.add(i-1, gen);
								found = true;
								break;
							}
						}
					}
				}
				if(!found){
					queue.enqueue(names[1], names[0]);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//This part of the function searches through every node in the pre-filled tree and checks whether it has any children stored in the p.queue 
		for(int i = 0; i < genTree.size() && !queue.empty(); i++){
			for(Node node : genTree.get(i).nodes){
				if(queue.containsParent(node.name)){
					this.addChildren(i, node.name);
				}
				if(queue.empty()) break;
			}
		}
	}
	
	public void search(int depth){
		int i = 0;
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<Node> nodes = genTree.get(depth).nodes;
		for(Node node : nodes){
			names.add(node.name);
		}
		Object[] names2 =  names.toArray();
		Arrays.sort(names2);
		for(Object name : names2) System.out.println(name);
	}

	public static void main (String[] args){
		if(args.length != 2) System.err.println("Enter Input Correctly");
		else{
			familyTree tree = new familyTree();
			String filename = "/Users/alfiememo/Documents/Projects/FamilyTree/" + args[0];
			tree.fillTree(filename);
			int depth = Integer.parseInt(args[1]);
			tree.search(depth);
		}
	} 
	   
}
