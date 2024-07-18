package file_searcher;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;

import file_handler.FileManager;
import nodes.Node;

public class BinarySearchDataFile extends Binary_Search{
	public BinarySearchDataFile() {
		
	}
	public int searchOrderedFile(FileManager fm, int keySearch) throws IOException{
		int totDiskAcc = 0;
		if(fm.OpenFile() == -1) {
			throw new IOException();
		}
//		fm.OpenFile();
//		int pages = (int) fm.length() / FileManager.page_size - 1;
		int pages = fm.getNumOfPages();
		int prevPageForRead = 0;
		int curPageForRead = pages/2;
		int dis = Math.abs(curPageForRead - prevPageForRead);
		int minValPage = Integer.MIN_VALUE;
		int maxValPage = Integer.MAX_VALUE;
		int pagesSearched = 0;
		//First page is info page 
		if(pages <=  1) {
			System.out.println("This file contains no data");
			return Integer.MIN_VALUE;

		}
		
		boolean found = false;
		while(!found && pagesSearched != pages) { //till key is found or we search whole file 
			Vector<Node> vec = new Vector<Node>();				//binary search in page files
			byte [] ReadDataPage = fm.ReadBlock(curPageForRead);			//read middle page
			ByteArrayInputStream bis= new ByteArrayInputStream(ReadDataPage);
			DataInputStream ois= new DataInputStream(bis);
			totDiskAcc += 1;
			pagesSearched += 1;
			for(int i =0; i<4;i++) {			//create nodes of each page
				int key = ois.readInt();
				int [] data = new int[7];
				for(int j = 0; j < data.length; j++) {
					 data[j] = ois.readInt();
				}
				Node node = new Node(key, data);
				vec.add(node);
			}
			minValPage = vec.get(0).getKey();
			maxValPage = vec.lastElement().getKey();
			dis = Math.abs(curPageForRead - prevPageForRead);
			if(keySearch > maxValPage) {
				prevPageForRead = curPageForRead;
				curPageForRead = (int)Math.ceil(curPageForRead + ((float) dis / 2));
				//pageForRead = (int)Math.ceil((pageForRead + pages) / 2);
			}else if(keySearch < minValPage){
				prevPageForRead = curPageForRead;
				curPageForRead = (int) (curPageForRead - ((float)dis / 2)); 
				//pageForRead = pageForRead / 2;
			}
			int leftIndex  = 0;
			int rightIndex = vec.size()-1;
			found = super.doSearch(leftIndex, rightIndex, keySearch, vec); //binary search for each page
			//now if found == false, key is not in this page
			if (found){
				System.out.println("after "+totDiskAcc+" disk accesses");
			}
			
		}
		if(fm.CloseFile() == 0) {
			throw new IOException();
		}
		return totDiskAcc;

	}
}
