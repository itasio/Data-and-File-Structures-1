package file_searcher;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Vector;

import file_handler.FileManager;
import nodes.IndexNode;

public class BinarySearchIndexFile extends Binary_Search{
	public BinarySearchIndexFile() {
		
	}

	public int searchOrderedFile(FileManager fm, int keySearch) throws IOException{
			int totDiskAcc = 0;
			if(fm.OpenFile() == -1) {
				throw new IOException();
			}
//			int pages = (int) file.length() / FileManager.page_size;
			int pages = fm.getNumOfPages();
			int prevPageForRead = 0;
			int curPageForRead = pages/2;
			int dis = Math.abs(curPageForRead - prevPageForRead);
			int minValPage = Integer.MIN_VALUE;
			int maxValPage = Integer.MAX_VALUE;
			int pagesSearched = 0;
			
			boolean found = false;
			while(!found && pagesSearched != pages) { //till key is found or we search whole file 
				Vector<IndexNode> vec = new Vector<IndexNode>();				//binary search in page files
				byte [] ReadDataPage = fm.ReadBlock(curPageForRead);			//read middle page
				ByteArrayInputStream bis= new ByteArrayInputStream(ReadDataPage);
				DataInputStream ois= new DataInputStream(bis);
				totDiskAcc += 1;
				pagesSearched += 1;
				for(int i = 0; i < 16; i++) {		//16 index nodes per page
					int key = ois.readInt();
					int page = ois.readInt();
					IndexNode inode = new IndexNode(key, page);
					vec.add(inode);
				}
				minValPage = vec.get(0).getKey();
				maxValPage = vec.lastElement().getKey();
				dis = Math.abs(curPageForRead - prevPageForRead);
				if(keySearch > maxValPage) {
					prevPageForRead = curPageForRead;
					curPageForRead = (int)Math.ceil(curPageForRead + ((float) dis / 2));
					//curPageForRead = (int)Math.ceil((curPageForRead + pages) / 2);
				}else if(keySearch < minValPage){
					prevPageForRead = curPageForRead;
					curPageForRead = (int) (curPageForRead - ((float)dis / 2)); 
					//curPageForRead = curPageForRead / 2;
				}
				int leftIndex  = 0;
				int rightIndex = vec.size()-1;
				found = super.doSearch(leftIndex, rightIndex, keySearch, vec); // binary search for each page
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
