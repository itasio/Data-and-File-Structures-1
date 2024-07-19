package file_handler;

import java.io.*;
import java.util.*;

import file_searcher.BinarySearchDataFile;
import file_searcher.BinarySearchIndexFile;
import file_searcher.SearchSerialDataFile;
import file_searcher.SearchSerialIndexedFile;
import nodes.IndexNode;
import nodes.IndexNodeComparator;
import nodes.Node;
import nodes.NodeComparator;

public class Main {
	private static final String dataFile = "dataFile";
	private static final String dataFileSorted = "dataSortedFile";
	private static final String indexFile = "indexFile";
	private static final String indexFileSorted = "indexSortedFile";
	
	private static final int numKeys = 10000;
	private static final int minKey = 1;
	private static final int maxKey = 1000000;
	private static final int maxValue = 200;
	private static final int numOfSearches = 20;
	
	public static void main(String[] args){
		
		int [] totalKeys = intArrayGenerator(minKey, maxKey+1, numKeys);
		
		//searchKeys[] ->positions of totalKeys[] to searched
		int [] searchKeys = intArrayGenerator(0, totalKeys.length, numOfSearches);
					
		FileManager f1 = new FileManager();
		FileManager f2 = new FileManager();
		FileManager f3 = new FileManager();
		FileManager f4 = new FileManager();

		try {
		// create dataFile and indexed file
		createFiles(f1, f2, f3, f4, totalKeys);

		searchFiles(f1, f2, f3, f4, totalKeys, searchKeys);
		} catch (IOException e) {
			System.err.println("An error occurred: "+e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
		}
	}

	/**
	 * Searches in the four files
	 * @param f1 the file manager associated with the data file.
	 * @param f2 the file manager associated with the index file.
	 * @param f3 the file manager associated with the sorted data file.
	 * @param f4 the file manager associated with the sorted index file.
	 * @param totalKeys the total keys contained in the files.
	 * @param searchKeys the keys to search in the files.
	 * @throws IOException if an I/O error occurs.
	 */
	private static void searchFiles(FileManager f1, FileManager f2, FileManager f3, FileManager f4, int[] totalKeys, int[] searchKeys) throws IOException {
		System.out.println("============ Now serial searching data file =======================");
		SearchSerialDataFile ssdf = new SearchSerialDataFile();
		ssdf.searchSerialDataFile(f1, searchKeys, totalKeys);
		
		System.out.println("============ Now serial searching index file ======================");
		SearchSerialIndexedFile ssif = new SearchSerialIndexedFile();
		ssif.searchIndexedFile(f1, f2, searchKeys, totalKeys);

		
		System.out.println("============ Now binary search sorted index file ==================");
		BinarySearchIndexFile bsif = new BinarySearchIndexFile();
		int  diskAccessSortedIndexFile = 0;
        for (int searchKey : searchKeys) {
            diskAccessSortedIndexFile += bsif.searchOrderedFile(f4, totalKeys[searchKey]);
            System.out.println();
        }
		System.out.println();
		System.out.println("Average disk accesses of D type file organization: "+((diskAccessSortedIndexFile/searchKeys.length)+ 1));
		//+1 because for every key, I add 1 disk access to get the info from the info file

		System.out.println();
		System.out.print("============ Now binary search sorted data file =====================");
		BinarySearchDataFile bsdf = new BinarySearchDataFile();
		int  diskAccessSortedDataFile = 0;
        for (int searchKey : searchKeys) {
            System.out.println();
            diskAccessSortedDataFile += bsdf.searchOrderedFile(f3, totalKeys[searchKey]);
        }
		System.out.println();
		System.out.println("Average disk accesses of C type file organization: "+diskAccessSortedDataFile/searchKeys.length);
		
		
	}

	/**
	 * Creates the four files required
	 * @param f1 the file manager associated with the data file.
	 * @param f2 the file manager associated with the index file.
	 * @param f3 the file manager associated with the sorted data file.
	 * @param f4 the file manager associated with the sorted index file.
	 * @param totalKeys the keys to add in the files.
	 * @throws IOException if an I/O error occurs.
	 */
	private static void createFiles(FileManager f1, FileManager f2,  FileManager f3,  FileManager f4, int [] totalKeys) throws IOException {
		if(f1.CreateFile(indexFile) == 0 || f2.CreateFile(dataFile) == 0){
			throw new IOException();
		}

		if(f1.OpenFile() == -1 || f2.OpenFile() == -1){
			throw new IOException();
		}

		byte [] block;
		Random random = new Random(); 
		
   		ByteArrayOutputStream bos1 = new ByteArrayOutputStream() ;
    	DataOutputStream output1 = new DataOutputStream(bos1);
    	
    	ByteArrayOutputStream bos2 = new ByteArrayOutputStream() ;
    	DataOutputStream output2 = new DataOutputStream(bos2);
    	
    	//number of values for each key (each record) i.e. 7 integers
    	int numOfValues = (FileManager.rec_size - Integer.BYTES) / Integer.BYTES;	
		for(int i=1; i <= numKeys; i++) {
			output2.writeInt(totalKeys[i-1]);							//for indexFile
			output2.writeInt(f1.getNumOfPages()+1);
			
			output1.writeInt(totalKeys[i-1]);								
			for (int j = 0; j < numOfValues; j++) {
				output1.writeInt(random.nextInt(maxValue));
			}
						
			if((i % 4) == 0) {
				block = bos1.toByteArray();
				f1.AppendBlock(block);			//write page to end of dataFile
				bos1.reset();
			}
			if((i % 16) == 0) {
				block = bos2.toByteArray();
				f2.WriteNextBlock(block);		//write key,page to index file
				bos2.reset();
			}
		}
        //noinspection ConstantValue
        if (numKeys % 16 != 0) {
			block = bos2.toByteArray();				//last page of keys
			f2.WriteNextBlock(block);		//write key,page to index file
		}
		bos1.close();
		output1.close();
		bos2.close();
		output2.close();


		System.out.println("============ Now sorting data file ================================");

		Vector<Node> listNodes = createNodesFromDataFile(f1);        //unordered

		Collections.sort(listNodes, new NodeComparator());

		System.out.println("============ Now creating sorted data file ========================");
		System.out.println();


		createSortedDataFile(f3, listNodes);
		
		System.out.println("============ Now sorting index file ===============================");
		Vector<IndexNode> indexListNodes = createIndexedNodesFromIndFile(f2);

		Collections.sort(indexListNodes, new IndexNodeComparator());
		

		System.out.println("============ Now creating sorted index file =======================");
		System.out.println();

		createSortedIndexedFile(f4, indexListNodes);

		if(f1.CloseFile() == 0 || f2.CloseFile() == 0)
			throw new IOException();
	}

	/**
	 * Creates index file from a vector of index nodes
	 * @param f4 the file manager associated with the data file.
	 * @param indexListNodes the vector from which the file will be created.
	 * @throws IOException if an I/O error occurs
	 */
	public static void createSortedIndexedFile(FileManager f4, Vector<IndexNode> indexListNodes) throws IOException{
		if(f4.CreateFile(indexFileSorted) == 0) {
			throw new IOException();
		}
		if(f4.OpenFile() == -1) {
			throw new IOException();
		}
		byte [] block;
		ByteArrayOutputStream bos1 = new ByteArrayOutputStream() ;
    	DataOutputStream output1 = new DataOutputStream(bos1);
    	for(int i=1; i<=indexListNodes.size(); i++) {
    		output1.writeInt(indexListNodes.get(i-1).getKey());	
    		output1.writeInt(indexListNodes.get(i-1).getPage());
    		if((i % 16) == 0){			//16 recs per page
    			block = bos1.toByteArray();
				if(f4.WriteNextBlock(block) == 0) { //write key,page to index file
					throw new IOException();
				}
				bos1.reset();
    		}
    	}
    	if (indexListNodes.size() % 16 != 0) {
    		block = bos1.toByteArray();
			if(f4.WriteNextBlock(block) == 0){ //write key,page to index file
				throw new IOException();
			}
    	}
		if(f4.CloseFile() == 0)
			throw new IOException();
	}

	/**
	 * Creates data file from a vector of nodes.
	 * @param f3  the file manager associated with the data file
	 * @param listNodes the nodes from which the file will be created
	 * @throws IOException if an I/O error occurs.
	 */
	public static void createSortedDataFile(FileManager f3, Vector<Node> listNodes) throws IOException{
		if(f3.CreateFile(dataFileSorted) == 0) {
			System.err.println("An error occurred in creating sorted data file");
			throw new IOException();
		}
		if(f3.OpenFile() == -1) {
			System.err.println("An error occurred in opening data file");
			throw new IOException();
		}
		f3.getRAF().seek(FileManager.page_size);
		byte [] block;
		ByteArrayOutputStream bos1 = new ByteArrayOutputStream() ;		
    	DataOutputStream output1 = new DataOutputStream(bos1);
    	for(int i = 1; i <= listNodes.size(); i++) {			//for every Node
    		output1.writeInt(listNodes.get(i-1).getKey());	//key of node
    		
    		for(int j = 0; j < listNodes.get(i-1).getData().length; j++) {		//for length of data[]
    			output1.writeInt(listNodes.get(i-1).getElementData(j));
    		}
			if(((i % 4) == 0) && (i != 0)) {
				block = bos1.toByteArray();
				if(f3.AppendBlock(block) == 0){	//write page to end of fileA
					throw new IOException();
				}
				bos1.reset();
			}
    	}
    	block = bos1.toByteArray();
    	
    	if(f3.WriteNextBlock(block) == 0){
			throw new IOException();
		}
    	bos1.close();
		output1.close();
		if(f3.CloseFile() == 0)
			throw new IOException();
	}
	
	
	/**
	 * Reads the specified file from the position appointed until the end 
	 * and prints its content is stdout as integers
	 * @param pos the position in bytes from the start of the file
	 */
	@SuppressWarnings("unused")
    public static void readWholeFile(FileManager fm, long pos) throws IOException{
		byte[] ReadDataPage;
		if(fm.OpenFile() == -1) {
			throw new IOException();
		}
		fm.getRAF().seek(pos);
		System.out.println("============ Read results file "+fm.getFileName()+"========================");
		
		while(fm.getRAF().getFilePointer() < fm.getRAF().length()) {		//read whole file
			ReadDataPage = fm.ReadNextBlock();
			if(ReadDataPage == null) {
				throw new IOException();
			}
			ByteArrayInputStream bis= new ByteArrayInputStream(ReadDataPage);
			DataInputStream ois= new DataInputStream(bis);
			int elements = ReadDataPage.length / Integer.BYTES; //the array consists of int data
			for(int i = 0; i < elements; i++) {
				System.out.print(ois.readInt()+" ");
			}
			System.out.println();

		}
		if(fm.CloseFile() == 0)
			throw new IOException();

	}
	
	
	/**
	 * Creates an array with distinct numbers between startInt (inclusive)  and endInt (exclusive)
	 * @param startInt the lower limit of the numbers created
	 * @param endInt the upper limit of the numbers created
	 * @param numOfElements the number of numbers created i.e. the size of the array
	 * @return int []
	 */
	public static int[] intArrayGenerator(int startInt, int endInt, int numOfElements) {
		java.util.Random randomGenerator = new java.util.Random();
        return randomGenerator.ints(startInt, endInt).distinct().limit(numOfElements).toArray();
	}

	/*
	 * 1 access = 1 page file read
	 */


	/**
	 * Creates a vector of index nodes from the information of the index file.
	 * The index file must already be open.
	 * @param f2 the file manager associated with the data file
	 * @return a Vector of  index nodes
	 * @throws IOException if an I/O error occurs
	 */
	public static Vector<IndexNode> createIndexedNodesFromIndFile(FileManager f2) throws IOException {
		int totDiskAcc = 0;
		byte[] ReadDataPage;

		f2.getRAF().seek(0);
		int key;
		int page;
		
		Vector<IndexNode> vec = new Vector<IndexNode>();
		
		while(f2.getRAF().getFilePointer() < f2.getRAF().length()) {		//read whole file
			ReadDataPage = f2.ReadNextBlock();
			if(ReadDataPage == null)
				throw new IOException();
			ByteArrayInputStream bis= new ByteArrayInputStream(ReadDataPage);
			DataInputStream ois= new DataInputStream(bis);
			totDiskAcc += 1;
			for(int i = 0; i < 16; i++) {		//16 index nodes per page
				key = ois.readInt();
				page = ois.readInt();
				IndexNode inode = new IndexNode(key, page);
				vec.add(inode);
			}
		}

		System.out.println("Disk accesses for sorting D case: "+totDiskAcc);
		return vec;
	}


	/**
	 * Creates a vector of nodes from the information of the data file.
	 * The data file must already be open.
	 * @param f1 the file manager associated with the data file
	 * @return a Vector of nodes
	 * @throws IOException if an I/O error occurs
	 */
	public static Vector<Node> createNodesFromDataFile(FileManager f1) throws IOException {
		int totDiskAcc = 0;
		byte[] ReadDataPage;

		f1.getRAF().seek(FileManager.page_size);
		int key;
		
		Vector<Node> vec = new Vector<Node>();
		
		while(f1.getRAF().getFilePointer() < f1.getRAF().length()) {		//read whole file
			ReadDataPage = f1.ReadNextBlock();
			if(ReadDataPage == null)
				throw new IOException();
			ByteArrayInputStream bis= new ByteArrayInputStream(ReadDataPage);
			DataInputStream ois= new DataInputStream(bis);
			totDiskAcc += 1;
			for(int i = 0; i < 4; i++) {
					key = ois.readInt();
					int [] data = new int[7];
					for(int j = 0; j < data.length; j++) {
						 data[j] = ois.readInt();
					}
					Node node = new Node(key, data);
					vec.add(node);
			}
		}

		System.out.println("Disk accesses for sorting C case: "+totDiskAcc);
		return vec;
	}
	
}