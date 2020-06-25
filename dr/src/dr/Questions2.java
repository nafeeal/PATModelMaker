package dr;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

// FIX THE OUTPUT FILE ON ROW 36.

public class Questions2 {
	
	Map<String,String> questions;
	Map<String,String> nodes;
	Scanner s;
	File file; 
	FileWriter fr;
	BufferedWriter br;
	
	// Critical Response variables
    String fireType = null;
    String terrain=null;	
    boolean extraDelivery= false;  // for missions that ALSO deliver supplies
    String mission;
    String surveyType;
    String waypoints;
    boolean onboardAnalysis = false;
    String sampleType=null;
    Boolean independentRescueTeams = null;
    Boolean tracking = null;
	
	public Questions2() throws IOException {
		questions = new HashMap<String,String>();
		nodes = new HashMap<String,String>();
		s=new Scanner(System.in);
		file = new File("C:\\temp\\test2.gv");
		fr = new FileWriter(file,false);
		br = new BufferedWriter(fr);
		writeTopGV();
		buildQuestions();

	}
	
	public void writeBottomGV() throws IOException {
		br.write("}");
		br.newLine();
	}
	
	public void writeTopGV() throws IOException {
		br.write("digraph G {"); 
		br.newLine();
		br.write("graph [ranksep=\"0.4\", pad=\".5\", nodesep=\"0.3\"];"); 
		br.newLine();
		br.write("edge [fontsize=12,fontname=\"times:italic\"];"); 
		br.newLine();
		br.write("node [fontsize=16,fontname=\"times\"shape=record,style=filled ,fillcolor = grey94]//\"/pastel18/2\"]");
		br.newLine();
	}
	
	public void writeNode(String name, String label) throws IOException {
		if(name.contains("START_MISSION")|| name.contains("END_MISSION"))
			br.write(name + " [ label=\"" + label + "\",shape=ellipse]");
		else
		    br.write(name + " [ label=\"" + label + "\"]");
		br.newLine();
	}
	
	public void writeLink(String node1, String node2, String label) throws IOException {
		br.write(node1 + "->" + node2 + "[label=\"" +label + "\"]");
		br.newLine();
	}
	
	public void writeLink(String node1, String node2, String label, Boolean visible) throws IOException {
		br.write(node1 + "->" + node2 + "[style=invis,label=\"" +label + "\"]");
		br.newLine();
	}
	
	
	public void closeFiles() throws IOException {
		br.close();
		fr.close();
	}
	
	public void buildQuestions() {
		questions.put("Q1","What type of mission? FIRE, SURVEY, SEARCH, SAMPLE, DELIVERY");
		questions.put("Q2","Will you define a flight REGION or a series of WAYPOINTS?");
		questions.put("Q3","Are you fighting a structural Fire? ( YES, NO )");			
		questions.put("Q4","What type of environment are working in? WATER, LAND, ICE, SNOW?");		
		questions.put("Q5","What are you surveying? TRAFFIC, FLOOD, OTHER");
		questions.put("Q6","Do you have independent rescue teams? ( YES, NO )");
		questions.put("Q7","Should drones track the victim? ( YES, NO )");
		questions.put("Q8","Will your mission deliver rescue equipment to the victim? ( YES, NO) ");
		questions.put("Q9","Do drones have onboard sample analysis capabilities? ( YES, NO )");
		questions.put("Q10","What are you sampling? (WATER, AIR, RADIO)");
	}
	
	public void buildNodes() {
		nodes.put("Mission-SEARCH","Start Search Mission");
	}
	
	
	public String ask(String question) {
		System.out.println(questions.get(question));
		return s.nextLine();		
	}
	
	
	public void generateGV() throws IOException {
		
	}
	public void askQuestions() throws IOException {
		
		// Mission node
		mission = ask("Q1");
		
		switch (mission) {
        case "FIRE":  
        	System.out.println("FIRE SELECTED");
        	fireType = ask("Q3").equals("YES")? "STRUCTURAL":"TERRAIN";
        	if (fireType.contentEquals("TERRAIN")) {
        		terrain="LAND"; // unlikely that it is on water or snow etc!
        	} else {
        		terrain = "Structural fire";
        	}        
        	System.out.println("Mission: " + mission + " - " + fireType + " Terrain: " + terrain);
        	break;

        case "SURVEY":  
        	terrain=ask("Q4");
        	surveyType = ask("Q5");
        	waypoints = ask ("Q2");
           	System.out.println("Mission: " + mission + " Terrain: " + terrain  + " SurveyType: " + surveyType + " Waypoints/Region: " + waypoints);
        	break;
        	
        case "SAMPLE":
        	terrain="";
        	waypoints = ask("Q2");
        	sampleType = ask("Q10");
        	onboardAnalysis = (ask("Q9").equals("YES"))?true:false;        	
           	System.out.println("Mission: " + mission + " " + sampleType + "Onboard analysis? " +  onboardAnalysis + " Waypoints/Region: " + waypoints);
        	break;
        	
        case "DELIVERY": 
        	terrain=ask("Q4");
        	System.out.println("DELIVERY SELECTED");
        	break;
        	
        case "SEARCH": 
        	terrain=ask("Q4");
        	independentRescueTeams = (ask("Q6").contentEquals("YES"))?true:false;
        	tracking = (ask("Q7").contentEquals("YES"))?true:false;
        	String extraDeliveryResponse = ask("Q8");
        	extraDelivery = extraDeliveryResponse.contentEquals("YES")?true:false;
        	System.out.println("SEARCH SELECTED");
        	break;
        	
        default:
        	System.out.println("NOTHING SELECTED");        	
		}
		
		
		writeNode("START_MISSION", "Start " + mission.toLowerCase());
		
		
		// THIS IS ALL ABOUT WAYPOINTS vs. AREAS
		if("SURVEY,SAMPLING".contains(mission)) {
	        if (waypoints.contentEquals("WAYPOINTS"))
	        	writeNode("REGION","Define target waypoints");
	        else
	        	writeNode("REGION","Define Area and generate flight routes");
		} else if (mission.contentEquals("DELIVERY")){
        	writeNode("REGION","Define target waypoints");
		} else if (!terrain.equals("Structural fire")) {
			writeNode("REGION","Define Area and Generate flight routes");
		} else
			writeNode("REGION","Define building coordinates");
				
		
	    writeNode("END_MISSION","End mission");	        
        writeLink("START_MISSION","REGION","");
               	
        // Launch node
        writeNode("LAUNCH","Connect and launch drones");
        writeLink("REGION","LAUNCH","");
        
        // Fly to waypoint
        writeNode("FLY_TO_WAYPOINT","Fly to target waypoint");
        writeLink("LAUNCH","FLY_TO_WAYPOINT","");
                
        if("FIRE,SURVEY,SEARCH".contains(mission)) {
        	if(!terrain.contentEquals("Structural fire"))
        	   writeNode("SCENE_RECONSTRUCTION","Scene Reconstruction activated for " + terrain );
        	else 
        		writeNode("SCENE_RECONSTRUCTION","Scene Reconstruction activated for a structural fire");

        	writeLink("FLY_TO_WAYPOINT","SCENE_RECONSTRUCTION","");
        }
        
    
        /////////////////////////////////////////////////////////////////////////////////////
        //Surveillance Strategies
        /////////////////////////////////////////////////////////////////////////////////////
       
        if("FIRE,SURVEY".contains(mission)) {
            if(mission.contentEquals("FIRE"))
            	writeNode("PLAN_SURVEY","Dynamically plan fire surveillance");
            else {
             	if(!surveyType.contentEquals("OTHER"))
            		writeNode("PLAN_SURVEY","Dynamically plan " + surveyType + " surveillance");
            	else
            		writeNode("PLAN_SURVEY","Dynamically plan survey");
            }
 
            writeNode("PERFORM_SURVEY","Perform surveillance");  
            writeNode("GENERATE_2DMODEL","Generate 2D \nscene visualization");
            
            if (terrain.contentEquals("Structural Fire")) 
               writeNode("GENERATE_HEATMAP","Generate 3D Heat Map");
            else 
                writeNode("GENERATE_HEATMAP","Generate 2D Heat Map");

            
            writeLink("PERFORM_SURVEY","GENERATE_HEATMAP","Thermal Camera\navailable");
            writeLink("GENERATE_HEATMAP","PLAN_SURVEY",""); 
            writeLink("SCENE_RECONSTRUCTION","PLAN_SURVEY","");
            writeLink("PLAN_SURVEY","PERFORM_SURVEY","");
            writeLink("PERFORM_SURVEY","GENERATE_2DMODEL","RGB Camera\navailable");
        
            writeLink("GENERATE_2DMODEL","PLAN_SURVEY","");

        	writeNode("DRONES_RECALLED","Drones recalled by Incident commander");
            writeLink("PERFORM_SURVEY","DRONES_RECALLED","");
            writeLink("DRONES_RECALLED","RETURN_HOME","");
        }
        

        /////////////////////////////////////////////////////////////////////////////////////
        //SEARCH_AND_RESCUE
        /////////////////////////////////////////////////////////////////////////////////////

        if("SEARCH".contains(mission)) {
        	writeNode("SEARCH","Search");
        	writeNode("VICTIM_FOUND","Victim or Target Found");
        	writeLink("SCENE_RECONSTRUCTION","SEARCH","");
        	writeLink("SEARCH","VICTIM_FOUND","");
        	writeLink("VICTIM_FOUND","LAUNCH","Flotation device \n delivery requested");
        	
           	writeNode("VICTIM_RESCUED","Victim rescued by human responders");
        	
        	if (independentRescueTeams) {
        		writeNode("REQUEST_AID","Request human aid\n via mobile unit");
        		writeLink("VICTIM_FOUND","REQUEST_AID","");
        		writeLink("REQUEST_AID","VICTIM_RESCUED","");
        	}
     
        	if (tracking) {
        		writeNode("TRACK_VICTIM","Track Victim");
        		writeLink("VICTIM_FOUND","TRACK_VICTIM","");
        		writeLink("TRACK_VICTIM","VICTIM_RESCUED","");
        	}
        	
        	if (!tracking && !independentRescueTeams)
        		writeLink("VICTIM_FOUND","VICTIM_RESCUED","");
        	
        
        	writeNode("DRONES_RECALLED","Drones recalled by Incident commander");
        	writeLink("VICTIM_RESCUED","DRONES_RECALLED",""); 
        	writeLink("DRONES_RECALLED","RETURN_HOME","");
      
        }
        
        /////////////////////////////////////////////
        // Deliveries
        /////////////////////////////////////////////
        if("DELIVERY".contains(mission) || extraDelivery==true) {
        	writeNode("FIND_DROPLOCATION","Identify a safe drop spot");
        	writeNode("DROP_SUPPLIES","Deliver supplies");
        	writeNode("FIND_RTL_LOCATION","Identify a home-based within flying range");
        	
        	if("DELIVERY".contains(mission))
        		writeLink("FLY_TO_WAYPOINT","FIND_DROPLOCATION","");
        	else
        		writeLink("FLY_TO_WAYPOINT","FIND_DROPLOCATION","Only deliver \nsupplies \nupon request");
        	writeLink("FIND_DROPLOCATION","DROP_SUPPLIES","");
        	writeLink("DROP_SUPPLIES","FIND_RTL_LOCATION","Confirmed");
        	writeLink("FIND_DROPLOCATION","FIND_RTL_LOCATION","Cancelled");
        	writeLink("FIND_RTL_LOCATION","RETURN_HOME","");        	
        }
        
        /////////////////////////////////////////////
        // SAMPLING
        /////////////////////////////////////////////
 
        if("SAMPLE".contains(mission)) {
        
        	
        	writeNode("POSITION_FOR_SAMPLING","Position drone for accurate " + sampleType + " sampling");
        	writeNode("COLLECT_SAMPLE","Collect sample");
        	writeNode("COLLECTION_COMPLETE","Collection complete");
        	writeLink("FLY_TO_WAYPOINT","POSITION_FOR_SAMPLING",sampleType + " \nsampling");
        	writeLink("POSITION_FOR_SAMPLING","COLLECT_SAMPLE","");
        	writeLink("COLLECT_SAMPLE","COLLECTION_COMPLETE","No more \n collection points");
        	        	
        	if(onboardAnalysis) {        	
        		writeNode("ONBOARD_ANALYSIS","Perform onboard analysis \n of samples");
        		writeNode("DYNAMIC_SAMPLING_PLAN","Dynamically update \nsampling plan");
        		writeLink("COLLECT_SAMPLE","ONBOARD_ANALYSIS","Onboard \nanalysis \ncapabilities");
        		writeLink("ONBOARD_ANALYSIS","DYNAMIC_SAMPLING_PLAN","");
        		writeLink("DYNAMIC_SAMPLING_PLAN","COLLECTION_COMPLETE","No more \ncollection points");
        		writeLink("DYNAMIC_SAMPLING_PLAN","FLY_TO_WAYPOINT","Waypoints \nmodified");
        	}
        	
        	writeLink("COLLECTION_COMPLETE","RETURN_HOME","");	
        }
        
        writeNode("RETURN_HOME","Return Home");
        
        writeLink("RETURN_HOME","END_MISSION","");
		writeBottomGV();
		closeFiles();
		System.out.println("MISSION GENERATED IN GRAPHVIS");
	}

	public static void main(String[] args) {
		Questions2 questions;
		try {
			questions = new Questions2();
			questions.askQuestions();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
