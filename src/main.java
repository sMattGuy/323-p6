import java.util.NoSuchElementException;
import java.util.Scanner;
import java.io.*;


public class main{
	public static void main(String args[]) throws FileNotFoundException{
		if(args.length != 3){
			System.out.println("You must include 3 arguments!");
			System.out.println("inFile, outFile1, outFile2");
			return;
		}
		File inFile = new File(args[0]);
		if(!inFile.isFile() && !inFile.canRead()){
			System.out.println("Error reading inFile! (Make sure its typed correctly!)");
			return;
		}
		PrintStream output1 = new PrintStream(new FileOutputStream(args[1], true));
		PrintStream output2 = new PrintStream(new FileOutputStream(args[2], true));
		
		//step 0
		//define Qtree
		QuadTree qTree = new QuadTree();
		//step 1
		qTree.loadImage(inFile);
		//print power2
		System.setOut(output2);
		System.out.println("power2 result: "+qTree.power2);
		//step 2 output image array
		System.out.println("Printing ImgArray...");
		for(int i=0;i<qTree.power2;i++){
			for(int j=0;j<qTree.power2;j++){
				System.out.print(qTree.imgArray[i][j]+" ");
			}
			System.out.print("\n");
		}
		//step 3 build root
		qTree.root = qTree.buildQuadTree(0,0,qTree.power2,output2);
		//step 4 print preorder
		System.setOut(output1);
		System.out.println("Printing Preorder Traversal...");
		qTree.preOrder(qTree.root, output1);
		//step 5 print postorder
		System.out.println("Printing Postorder Traversal...");
		qTree.postOrder(qTree.root, output1);
		//step 6 close all files
		output1.close();
		output2.close();
	}
}

class QtNode{
	int color;
	int upperR;
	int upperC;
	int size;
	
	QtNode NWkid;
	QtNode NEkid;
	QtNode SWkid;
	QtNode SEkid;
	
	public QtNode(int color, int upperR, int upperC, int size, QtNode NWkid, QtNode NEkid, QtNode SWkid, QtNode SEkid){
		this.color = color;
		this.upperR = upperR;
		this.upperC = upperC;
		this.size = size;
		
		this.NWkid = NWkid;
		this.NEkid = NEkid;
		this.SWkid = SWkid;
		this.SEkid = SEkid;
	}
	
	public void printQtNode(PrintStream output){
		System.setOut(output);
		System.out.print("color: "+this.color);
		System.out.print("; upperR: "+this.upperR);
		System.out.print("; upperC: "+this.upperC);
		if(this.NWkid == null)
			System.out.print("; NWkids color: NULL");
		else
			System.out.print("; NWkids color: "+this.NWkid.color);
		if(this.NEkid == null)
			System.out.print("; NEkids color: NULL");
		else
			System.out.print("; NEkids color: "+this.NEkid.color);
		if(this.SWkid == null)
			System.out.print("; SWkids color: NULL");
		else
			System.out.print("; SWkids color: "+this.SWkid.color);
		if(this.SEkid == null)
			System.out.print("; SEkids color: NULL\n");
		else
			System.out.print("; SEkids color: "+this.NEkid.color+"\n");
	}
}

class QuadTree{
	int numRows;
	int numCols;
	int minVal;
	int maxVal;
	int power2;
	
	int[][] imgArray;
	
	QtNode root;
	
	public QuadTree(){
		this.numRows = -1;
		this.numCols = -1;
		this.minVal = -1;
		this.maxVal = -1;
		this.power2 = -1;
		
		QtNode root = null;
	}
	void computePower2(){
		int size = Math.max(this.numRows, this.numCols);
		this.power2 = 2;
		while(size > this.power2){
			this.power2 *= 2;
		}
	}
	void loadImage(File inFile) throws FileNotFoundException{
		Scanner scan = new Scanner(inFile);
		this.numRows = scan.nextInt();
		this.numCols = scan.nextInt();
		this.minVal = scan.nextInt();
		this.maxVal = scan.nextInt();
		
		this.computePower2();
		this.zero2DArray();
		
		for(int i=0;i<this.numRows;i++){
			for(int j=0;j<this.numCols;j++){
				int data = scan.nextInt();
				this.imgArray[i][j] = data;
			}
		}
	}
	void zero2DArray(){
		this.imgArray = new int[this.power2][this.power2];
		for(int i=0;i<this.power2;i++){
			for(int j=0;j<this.power2;j++){
				this.imgArray[i][j] = 0;
			}
		}
	}
	
	QtNode buildQuadTree(int upR, int upC, int size, PrintStream output){
		QtNode newQtNode = new QtNode(-1, upR, upC, size, null,null,null,null);
		newQtNode.printQtNode(output);
		if(size == 1){
			newQtNode.color = this.imgArray[upR][upC];
		}
		else{
			int halfSize = size/2;
			newQtNode.NWkid = buildQuadTree(upR, upC, halfSize,output);
			newQtNode.NEkid = buildQuadTree(upR, upC+halfSize, halfSize,output);
			newQtNode.SWkid = buildQuadTree(upR+halfSize, upC, halfSize,output);
			newQtNode.SEkid = buildQuadTree(upR+halfSize, upC+halfSize, halfSize,output);
			int sumColor = this.sumKidsColor(newQtNode);
			if(sumColor == 0){
				newQtNode.color = 0;
				this.setLeaf(newQtNode);
			}
			else{
				if(sumColor == 4){
					newQtNode.color = 1;
					this.setLeaf(newQtNode);
				}
				else{
					newQtNode.color = 5;
				}
			}
		}
		return newQtNode;
	}
	int sumKidsColor(QtNode node){
		int result = node.NWkid.color + node.NEkid.color + node.SWkid.color + node.SEkid.color;
		return result;
	}
	void setLeaf(QtNode node){
		node.NEkid = null;
		node.NWkid = null;
		node.SEkid = null;
		node.SWkid = null;
	}
	boolean isLeaf(QtNode node){
		if(node.color == 1 || node.color == 0){
			return true;
		}
		return false;
	}
	void preOrder(QtNode node, PrintStream output){
		if(this.isLeaf(node)){
			node.printQtNode(output);
		}
		else{
			node.printQtNode(output);
			this.preOrder(node.NWkid, output);
			this.preOrder(node.NEkid, output);
			this.preOrder(node.SWkid, output);
			this.preOrder(node.SEkid, output);
		}
	}
	void postOrder(QtNode node, PrintStream output){
		if(this.isLeaf(node)){
			node.printQtNode(output);
		}
		else{
			this.preOrder(node.NWkid, output);
			this.preOrder(node.NEkid, output);
			this.preOrder(node.SWkid, output);
			this.preOrder(node.SEkid, output);
			node.printQtNode(output);
		}
	}
}