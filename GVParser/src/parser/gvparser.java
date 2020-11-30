package parser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * This is a simple parser to convert text files to Graphviz(.GV) files and automatically generate the image files.
 * It takes text files (in a very simple format)  as inputs and generates the .GV and image files (.png,.svg etc).
 *
 * @author Md Nafee Al Islam
 * created on 08/16/2020
 * 
 */
public class gvparser {	
	
public static  HashMap<String, String> hazards;
public static  HashMap<String, String> UserHazards;
public static HashMap<String, String> requirements = new HashMap<String, String>();
public static ArrayList<String> mapping = new ArrayList<String>();
public final static String directory = "C://Seium/GVparsing/";
public final static String desired_image_file_type = "png";
public final static String desired_image_file_type2 = "svg";
public static  HashMap<String, String> context;
File file; 
File temp;
FileWriter fr;
BufferedWriter br;
	
	public static void main(String[] args) throws Throwable {
	
		File dir = new File(directory);
		  File[] directoryListing = dir.listFiles();
		  if (directoryListing != null) {
		    for (File child : directoryListing) {
		    	String filename = child.getName();
		    	if (filename.contains(".txt")) {
		    		makeGVFile(filename);
		    		createImageFile(filename);
		    	}	
		    }
		  } else {
		    	System.out.println("No text files found!");
		  		}
		  
		   HashMap<String, String> test = new HashMap<String, String>();
		   //test.put("a", "b");
		   //test.clear();
		   if(test.isEmpty()==false) {
			   System.out.println("Not NULL");
		   }
	}
	private static void createImageFile(String filename) {
		String filenameImg = filename.replace("txt", desired_image_file_type);
		String filenameImg2 = filename.replace("txt", desired_image_file_type2);
		String filenameGv = filename.replace("txt", "gv");		
		Runtime rt = Runtime.getRuntime();
		try {	
			Process pr = rt.exec("cmd.exe /c "+"cd "+directory+" && "+"dot -T"+desired_image_file_type+" "+ filenameGv + " -o "+filenameImg);
			pr = rt.exec("cmd.exe /c "+"cd "+directory+" && "+"dot -T"+desired_image_file_type2+" "+ filenameGv + " -o "+filenameImg2);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	private static void makeGVFile(String filename) throws Throwable {	
		Scanner scanner = new Scanner(new File(directory+filename));
		createMapping(scanner);
		gvparser gv = new gvparser();
		gv.WriteGV(filename);	
	}

	public String includeLineBreaks(String s) {
		int charsPerLine=16;
		char[] chars = s.toCharArray();
        int lastLinebreak = 0;
        boolean wantLinebreak = false;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            if (wantLinebreak && chars[i] == ' ') {
                sb.append('<');
                sb.append('B');
                sb.append('R');
                sb.append('/');
                sb.append('>');
                lastLinebreak = i;
                wantLinebreak = false;
            } else {
                sb.append(chars[i]);
            }
            if (i - lastLinebreak + 1 == charsPerLine)
                wantLinebreak = true;
        }
        return sb.toString();
	}
		
	public void WriteGV(String filename) throws IOException {
		String filenameGv = filename.replace(".txt", ".gv");		
		openFile(filenameGv);
		
		br.write("digraph g { graph [autosize=true, rankdir = \"TD\", ranksep=\"0.1\", pad=\"0\", nodesep=\"0.2\"];\r\n" + 
				"node [ fontsize = \"10\" shape = \"record\" ,width=0.2,height=0.1,margin=0.02,style=\"filled\",fillcolor=\"gray96\"];\r\n" + 
				"edge [arrowsize=\".5\"];\r\n" + 
				"");
		br.newLine();
		
        for (Map.Entry mapElement : hazards.entrySet()) { 
            String key = (String)mapElement.getKey();  
            String value = ((String)mapElement.getValue());  
            System.out.println(key + " : " + value); 
        }
		
        for (Map.Entry mapElement : hazards.entrySet()) { 
            String key = (String)mapElement.getKey();		
			br.write(key+"[\r\n" + 
					"label= <{<b>Hazard </b>|" + includeLineBreaks(hazards.get(key))  +"}>\r\n");
			
			if (key.equals("HX0")) {
				br.write("fillcolor=\"lightyellow\"\r\n" + 
						"];\r\n" + 
						"");
				br.newLine();
			}
			
			else {
				br.write("];\r\n" + 
						"");
				br.newLine();
			}
		}
        //UH
        for (Map.Entry mapElement : UserHazards.entrySet()) { 
            String key = (String)mapElement.getKey();  
            String value = ((String)mapElement.getValue());  
            System.out.println(key + " : " + value); 
        }
		
        for (Map.Entry mapElement : UserHazards.entrySet()) { 
            String key = (String)mapElement.getKey();		
			br.write(key+"[\r\n" + 
					"label= <{<b>Hazard </b>|" + includeLineBreaks(UserHazards.get(key))  +"}>\r\n");
			

				br.write("fillcolor=\"lightblue\"\r\n" + 
						"];\r\n" + 
						"");
				br.newLine();
			
			

		}
        //UH
		
        for (Map.Entry mapElement : requirements.entrySet()) { 
            String key = (String)mapElement.getKey();
			br.write(key+"[\r\n" + 
					"label= <{<b>Requirement </b>|" + includeLineBreaks(requirements.get(key))  +"}>\r\n");
				br.write("];\r\n" + 
						"");
				br.newLine();
			}
		
		if(context.isEmpty()==false)
			{
			//context edit
	        for (Map.Entry mapElement : context.entrySet()) { 
	            String key = (String)mapElement.getKey();
				br.write(key+"[\r\n" + 
						"label= <{<b>Context </b>|" + includeLineBreaks(context.get(key))  +"}>\r\n");
					br.write("];\r\n" + 
							"");
					br.newLine();
				}			
			//context edit			
			}
		
		for (int i=0;i<mapping.size();i++) {
			String [] mapped = mapping.get(i).split(" ",2);
			
				if (modifyNames(mapped[0]).charAt(0)=='C') {
					br.write(modifyNames(mapped[0])+"->"+modifyNames(mapped[1]));
					br.newLine();
					br.write("{rank = same;  "+modifyNames(mapped[1])+"; "+modifyNames(mapped[0])+" }");
					br.newLine();
				}else {
				br.write(modifyNames(mapped[0])+"->"+modifyNames(mapped[1])+"[dir=back]");
				br.newLine();
				}
		}
		
		br.write("}");
		closeFiles();		
	}

	public static String modifyNames(String str) {
		String string = null;
		
		if (str.charAt(0)=='h'){	
			string=str.replace("h", "HX");
		}
		else if (str.charAt(0)=='H'){		
			string=str.replace("H", "HX");
		}
		else if (str.charAt(0)=='u'){		
			string=str.replace("u", "UX");
		}
		else if (str.charAt(0)=='U'){		
			string=str.replace("U", "UX");
		}
		else if (str.charAt(0)=='r'){		
			string=str.replace("r", "RX");
		}
		else if (str.charAt(0)=='R'){		
			string=str.replace("R", "RX");
		}
		else if (str.charAt(0)=='c'){	
			string=str.replace("c", "CX");
		}
		else if (str.charAt(0)=='C'){	
			string=str.replace("C", "CX");
		}
		
		return string;
	}

	public void openFile(String filename) throws IOException {
		file = new File(directory+filename);
		fr = new FileWriter(file,false);
		br = new BufferedWriter(fr);			
	}
	
	public void closeFiles() throws IOException {
		br.close();
		fr.close();
	}

	private static void createMapping(Scanner scanner) throws Throwable {
		hazards = new HashMap<String, String>();
		UserHazards = new HashMap<String, String>();
		requirements = new HashMap<String, String>();
		if (mapping.size()!=0) {
			mapping.clear();
		}
		context=new HashMap<String, String>();
		
		while (scanner.hasNextLine()) {
		   String strLine = scanner.nextLine();

		
		   if (strLine.length()>0) {
			String [] arr = strLine.split(" ",2);
			
				if (arr[0].charAt(0)=='h' || arr[0].charAt(0)=='H'){	
					hazards.put(modifyNames(arr[0]) ,arr[1]);
				}
				else if (arr[0].charAt(0)=='u' || arr[0].charAt(0)=='U')
				{
					UserHazards.put(modifyNames(arr[0]) ,arr[1]);
				}
				else if (arr[0].charAt(0)=='r' || arr[0].charAt(0)=='R')
				{
					requirements.put(modifyNames(arr[0]) ,arr[1]);
				}
				else if (arr[0].charAt(0)=='c' || arr[0].charAt(0)=='C')
				{
					context.put(modifyNames(arr[0]) ,arr[1]);
				}
				else if (arr[0].equalsIgnoreCase("map"))
				{
					mapping.add(arr[1]);
				}
		   }
		}
	}
}
