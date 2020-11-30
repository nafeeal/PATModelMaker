package parser;

import java.io.File;
import java.io.IOException;



public class GVtoImage {
	public final static String directory = "C://Seium/GVparsing/white with blue gv/";
	public final static String desired_image_file_type = "png";
	
	public static void main(String[] args) throws Throwable {
		
		File dir = new File(directory);
		  File[] directoryListing = dir.listFiles();
		  if (directoryListing != null) {
		    for (File child : directoryListing) {
		    	String filename = child.getName();
		    	if (filename.contains(".gv")) {
		    		System.out.println(filename);
		    		createImageFile(filename);

		    	}	
		    }
		  } else {
		    	System.out.println("No text files found!");
		  		}
	}
	
	private static void createImageFile(String filename) {
		String filenameImg = filename.replace("gv", desired_image_file_type);
		String filenameGv = filename;		
		Runtime rt = Runtime.getRuntime();
		try {

			Process pr = rt.exec("cmd.exe /c "+"cd "+directory+" && "+"dot -T"+desired_image_file_type+" "+ filenameGv + " -o "+filenameImg);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	
	
	

}
