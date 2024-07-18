package file_handler;
import java.io.*;

/**
 * Class to handle basic file operations. Each Manager handles one file containing int data type.
 * @author itasio
 *
 */
public class FileManager {
	public final static int page_size=128;
	public final static int rec_size=32;
	private final static String infoType = "int";		
	private int numOfPages;
	private String fileName;
	private RandomAccessFile RAF;		// for the file associated with this manager
	
	public RandomAccessFile getRAF() {
		return RAF;
	}

	private void setRAF(RandomAccessFile raf) {
		RAF = raf;
	}

	public FileManager() {
		// empty constructor
	}
	
	public int getNumOfPages() {
		return this.numOfPages;
	}
	
	
	
	

	public String getFileName() {
		return fileName;
	}



	private void setFileName(String fileName) {
		this.fileName = fileName;
	}	
/**
 * creates a file, 
 * writes 1 page of info: data type stored, position of fp, num of pages written
 * @param name the name of the file to create
 * @return 1 in success or 0 on failure
 */
	public int CreateFile(String name){
		try{
        	this.setRAF(new RandomAccessFile (name, "rw"));
        	this.numOfPages = 0;			
        	setFileName(name);
        	updateInfoPage();
        	RAF.close();
            return 1;
		}
		catch(Exception e) {
			System.out.println("An error occurred in CreateFile");
			return 0;
		}	
	}

	/**
	 * Updates the info page of the specified file. Namely {@link #infoType}, {@link #page_size} and {@link #numOfPages}
	 * 
	 */
	public void updateInfoPage() {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
	    	DataOutputStream out = new DataOutputStream(bos);
	    	
	     	byte [] dst = new byte [116];
	    	byte [] src = infoType.getBytes();				//convert string to byte array
	    	System.arraycopy(src, 0, dst, 0, src.length);	//and copy to dst[116]
	    	
	   		out.write(dst, 0, dst.length);					//116 bytes	
	    	out.writeLong(page_size);			//8 bytes
	    	out.writeInt(this.numOfPages);						//4 bytes ->now out is 128 bytes
	    
	    	out.close();
	    	byte[] buffer = bos.toByteArray(); 
	    	bos.close();
	    	
			RAF.seek(0);
	        RAF.write(buffer);
			//System.out.println("Number of pages written : "+this.numOfPages);

		
		} catch (IOException e) {
			System.out.println("An error occurred in updateInfoPage");

		}								
    	
	}
	
	
	
	
	/**
	 * Opens the file associated with the FileManager class for rw, file-pointer set to 0
	 * 
	 * @return the number of disc pages of this file in success or -1 in case of failure
	 */
	public int OpenFile(){
		try{
//			RandomAccessFile MyFile = new RandomAccessFile (name, "rw");
			RAF.seek(0);
			return this.numOfPages;
		}
		catch(Exception e) {
			System.out.println("An error occurred in OpenFile");
			return -1;
		}
	}
	
	
	/**
	 * Reads {@link #page_size} bytes in the page specified from the file associated with the FileManager
	 * @param page the page of the file to be read
	 * @return a byte array with size: {@link #page_size} or null in case of failure
	 */
	public byte[] ReadBlock(int page){
		try {
			RAF.seek((long) page_size *page);
			byte[] ReadDataPage = new byte[page_size];
            RAF.read(ReadDataPage);
            return ReadDataPage;
			
		} catch (IOException e) {
			System.out.println("An error occurred in ReadBlock");
			return null;
		}
	}
	
	
	/**
	 * Reads the next {@link #page_size} bytes relative to the current position of the file pointer
	 * from the file associated with the FileManager
	 * @return a byte array with size: {@link #page_size} or null in case of failure
	 */
	public byte[] ReadNextBlock(){
		byte[] ReadDataPage = new byte[page_size];
        try {
			RAF.read(ReadDataPage);
			return ReadDataPage;
        } catch (IOException e) {
			System.out.println("An error occurred in ReadNextBlock");
			return null;

		}					
		
	}	
	
	/**
	 * Reads the previous {@link #page_size} bytes relative to the current position of the file pointer
	 * from the file associated with the FileManager
	 * @return a byte array with size: {@link #page_size} or null in case of failure
	 */
	@SuppressWarnings("unused")
    public byte[] ReadPrevBlock(){
		byte[] ReadDataPage = new byte[page_size];
        try {
        	RAF.seek(RAF.getFilePointer()-128);
			RAF.read(ReadDataPage);
			return ReadDataPage;
        } catch (IOException e) {
			System.out.println("An error occurred in ReadPrevBlock");
			return null;

		}		
	}	
	

	//page 0 is considered info page
	
	/**
	 * Writes a byte array in the file associated with the FileManager.
	 * The specified page must be within limits of the total pages of the file.
	 * <br>
	 * e.g. You cannot write on page 20 if the file has 18 pages. But you can write on pages 1-19.
	 * Page 19 will be appended in file. Page 0 is excluded since it is info page, not writable
	 * @param buffer the byte array to write
	 * @param page the page of the file to be written
	 * @return 1 in success or 0 on failure
	 */
	public int WriteBlock(byte[] buffer, int page ){
		try {
			if (page > (this.numOfPages+1)) {
				System.out.println("Total pages so far "+this.numOfPages);
				System.out.println("Next available write in page"+(this.numOfPages));
				return 0;
			}else if(page == (this.numOfPages+1)) {
				this.numOfPages += 1;		
			}
			RAF.seek((long) page_size *page);
			RAF.write(buffer);
			this.updateInfoPage();
			return 1;						//TODO make sure page 0 is not overwritten as it is info page
		} catch (IOException e) {
			System.out.println("An error occurred in WriteBlock");
			return 0;
		}
		

	}
	
	
	/**
	 * Writes a byte array in the next page relative to 
	 * the current position of the file pointer in the file associated with the FileManager
	 * @param buffer the byte array to write
	 * @return 1 in success or 0 on failure
	 */
	public int WriteNextBlock(byte[] buffer){
		try {
			if (RAF.getFilePointer() == RAF.length()) {
				this.numOfPages += 1;
			}
			RAF.write(buffer);
			//this.updateInfoPage(MyFile);	//TODO why in comments ?
			return 1;
		} catch (IOException e) {
			System.out.println("An error occurred in WriteNextBlock");
			return 0;
		}
		

	}

	/**
	 * Append a byte array to the file associated with the FileManager
	 * @param buffer the byte array to write
	 * @return 1 in success or 0 on failure
	 */
	public int AppendBlock(byte[] buffer){
		try {
			RAF.seek((long) (this.numOfPages + 1) * page_size);	//go to end of file
			RAF.write(buffer);
			this.numOfPages += 1;
			this.updateInfoPage();
			return 1;
		} catch (IOException e) {
			System.out.println("An error occurred in AppendBlock");
			return 0;
		}

	}

	/**
	 * Deletes the specified page from the file associated with the FileManager. 
	 * The specified page must be within limits of the total pages of the file.
	 * @param page the page of file to delete
	 * @return 1 in success or 0 on failure
	 */
    @SuppressWarnings("unused")
    public int DeleteBlock(int page){
		try {
			if (page > this.numOfPages) {
				System.out.println("Total pages so far "+this.numOfPages);
				System.out.println("Max available delete for page"+(this.numOfPages));
				return 0;
			}else {
				RAF.seek(RAF.length() - 128);			//go to the start of last page
				byte[] ReadDataPage = this.ReadBlock(this.numOfPages);			
				this.WriteBlock(ReadDataPage, page); //write last page to page about to be deleted
				this.numOfPages -= 1;
				this.updateInfoPage();
				return 1;						//TODO make sure page 0 is not deleted
		}
			} catch (IOException e) {
				System.out.println("An error occurred in DeleteBlock");
				return 0;
			}
	}
	
	/**
	 * Updates the info page and closes the specified file
	 * @return 1 in success or 0 on failure
	 */
	public int CloseFile(){
		try {		//TODO when is it called, and from who
			updateInfoPage();
			RAF.close();
			return 1;
		} catch (IOException e) {
			System.out.println("An error occurred in CloseFile "+e.getMessage());
			return 0;
		}
	}

}