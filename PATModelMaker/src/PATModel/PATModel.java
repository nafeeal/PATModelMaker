package PATModel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
//import org.json.JSONObject;
//import org.json.JSONArray;
//import org.json.simple.parser.JSONParser;
/**
This code takes various mission specification, models them in PAT and creates lts files to be model checked by PAT.
 *
 * @author Md Nafee Al Islam
 * created on 08/16/2020
 * 
 */

public class PATModel {

	public static ArrayList<transition> transitionList = new ArrayList<transition>();
	public static ArrayList<String> defaultStates = new ArrayList<String> (Arrays.asList("OnGround-UnArmed", "OnGround-Armed","Launching","InAir-waiting-for-mission","MissionComplete"));
	public static ArrayList<String> allStates = new ArrayList<String> (Arrays.asList("OnGround-UnArmed", "OnGround-Armed","Launching","InAir-waiting-for-mission","MissionComplete","Searching","RequestingToken","Tracking","MakingDelivery","WaitingForNextInstruction","FlyingToLocation","LocationReached","Surveying","Sampling","WaitingForNextSamplingTask"));

	public final static String directory = "C://Seium/PhD/PAT/Process Analysis Toolkit/Process Analysis Toolkit 3.5.1/";
	public final static String fileName = "ModelLTS.lts";
	static File file; 
	File temp;
	static FileWriter fr;
	static BufferedWriter br;
	
	
	public static void main(String[] args) throws Throwable {
		//ArrayList<String> selectedStates=allStates; //will be used later
		makeListOfAllTransitions();
		JSONObject jsonMsg = makeJSONMessage();
		makeLTSfile(jsonMsg);
		modelCheck();
	}

	private static void modelCheck() {
		Runtime rt = Runtime.getRuntime();
		try {	
			Process pr = rt.exec("cmd.exe /c "+"cd "+directory+" && "+"PAT3.Console.exe -lts "+fileName+" result.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}

	private static void makeLTSfile(JSONObject jsonMsg) throws IOException {
		JSONArray missions = (JSONArray) jsonMsg.get("missions");
		openFile(fileName);
		
		//Initial Description
		br.write("<LTS>\r\n" + 
				"  <Declaration>\n");
		br.newLine();

		for (int i=0;i<missions.size();i++) {
			JSONObject spec = (JSONObject) missions.get(i);
			String MissionId = (String) spec.get("MissionId");
			br.write("var takeoff"+MissionId+":{0..1} = 0;\r\n" + 
					"var armed"+MissionId+":{0..1} = 0;\r\n" + 
					"var onGround"+MissionId+":{0..1} = 1;\r\n" + 
					"var isMissionCompleted"+MissionId+" = false;\r\n" + 
					"var isRTL"+MissionId+" =false;\r\n" + 
					"var isTracking"+MissionId+" = false;\r\n" + 
					"var isDelivering"+MissionId+" = false;");
			br.newLine();
			br.newLine();

		}
		
		br.write("var targetAlert=false;\r\n" + 
				"var hasTrackingToken:{0.."+(missions.size())+"}="+missions.size()+";\r\n" + 
				"var hasDeliveryToken: {0.."+(missions.size()) +"}="+missions.size()+";");
		br.newLine();
		br.newLine();
		br.write("System = ");
		
		for (int i=0;i<missions.size();i++) {
			JSONObject spec = (JSONObject) missions.get(i);
			String MissionId = (String) spec.get("MissionId");
			br.write(MissionId+"() ");
			if(i != (missions.size()-1)) {br.write("|| ");}
		}
		
		br.write(";");		
		br.newLine();
		br.newLine();
		br.newLine();
		br.newLine();
		
	
		br.write("//Reachability Assertions \n");
		br.newLine();
		
		
		//to gather all goals to check with single command
		ArrayList <String> goalList = new ArrayList<String>();

		
		//To make sure that multiple drones do not track at the same time
		br.write("//To check if multiple drones track at the same time \n");
		br.newLine();
		for (int i=0;i<(missions.size()-1);i++) {
			for (int j=i+1;j<missions.size();j++)
			{
				JSONObject spec = (JSONObject) missions.get(i);
				JSONObject spec2 = (JSONObject) missions.get(j);
				String MissionId1 = (String) spec.get("MissionId");
				String MissionId2 = (String) spec2.get("MissionId");
				br.write("#define goalTracking"+MissionId1.charAt(MissionId1.length()-1)+"_"+MissionId2.charAt(MissionId2.length()-1)+" (isTracking"+MissionId1+"==true &amp;&amp; isTracking"+MissionId2+"==true);");
				br.newLine();
				goalList.add("goalTracking"+MissionId1.charAt(MissionId1.length()-1)+"_"+MissionId2.charAt(MissionId2.length()-1));
			}
		}
		br.newLine();
		br.newLine();
		
		//To make sure that multiple drones do not deliver at the same time
		br.write("//To check if multiple drones deliver at the same time \n");
		br.newLine();

		for (int i=0;i<(missions.size()-1);i++) {
			for (int j=i+1;j<missions.size();j++)
			{
				JSONObject spec = (JSONObject) missions.get(i);
				JSONObject spec2 = (JSONObject) missions.get(j);
				String MissionId1 = (String) spec.get("MissionId");
				String MissionId2 = (String) spec2.get("MissionId");
				br.write("#define goalDelivery"+MissionId1.charAt(MissionId1.length()-1)+"_"+MissionId2.charAt(MissionId2.length()-1)+" (isDelivering"+MissionId1+"==true &amp;&amp; isDelivering"+MissionId2+"==true);");
				br.newLine();
				goalList.add("goalDelivery"+MissionId1.charAt(MissionId1.length()-1)+"_"+MissionId2.charAt(MissionId2.length()-1));
			}
		}

		br.newLine();
		br.newLine();
		
		//To check if a single drone can have tracking and delivery at the same time
		br.write("//To check if a single drone can have tracking and delivery at the same time \n");	
		br.newLine();

		for (int i=0;i<missions.size();i++) {
			JSONObject spec = (JSONObject) missions.get(i);
			String MissionId = (String) spec.get("MissionId");
			br.write("#define goal"+MissionId+" (isDelivering"+MissionId+"==true &amp;&amp; isTracking"+MissionId+"==true);\r\n");
			goalList.add("goal"+MissionId);
		}

		br.newLine();

		//combining all goals		
		br.write("#define goalAll !(");
		for (int i=0;i<goalList.size();i++) {
			br.write(goalList.get(i));
			if (i!=(goalList.size()-1)) {br.write(" || ");}
		}
		br.write(");");
		br.newLine();
		br.newLine();
		br.write("#assert System reaches goalAll;");
		br.newLine();
		br.newLine();
				
		br.write("//Some basic LTL formulas\r\n" + 
				"// weak untill P W Q == []P || (P U Q)\r\n" + 
				"// Precedence, S precedes P == 	&lt;&gt;P -&gt; (!P U (S &amp; !P))\r\n" + 
				"// Response, [](P -&gt; &lt;&gt;S)\r\n"
				+ "// implies, P-&gt;Q == !P || Q\n");
		
		br.newLine();
		
		//LTL Assertions for each Drone
		// note for some syntaxes
		// <> == &lt;&gt;
		// & == &amp;
		for (int i=0;i<missions.size();i++) {
			JSONObject spec = (JSONObject) missions.get(i);
			String MissionId = (String) spec.get("MissionId");
			JSONArray states = (JSONArray) spec.get("states");
			br.write("//LTL Assertions "+MissionId+"\n");
			br.newLine();
			
			br.write("#assert "+MissionId+"() |= (!Launch U Arm );\n");	
			br.write("#assert "+MissionId+"() |= &lt;&gt; FlyToRemoteLocation -&gt; (!FlyToRemoteLocation U (LaunchCompleteNotification &amp;&amp; !FlyToRemoteLocation));\r\n");			
			br.write("#assert "+MissionId+"() |=  &lt;&gt; SearchCommand -&gt; (!SearchCommand U (LaunchCompleteNotification &amp;&amp; !SearchCommand));\r\n");			
			br.write("#assert "+MissionId+"() |=  [](TrackingConfirmed -&gt; &lt;&gt;(TrackingCompleted || TrackingFailed));\r\n");			
			br.write("#assert "+MissionId+"() |= [](DeliveryConfirmed -&gt; &lt;&gt;(DeliveryCompleted || DeliveryFailed));\r\n");		
			br.write("#assert "+MissionId+"() |= []((TargetDetected || TargetDetectedAlert) -&gt;  &lt;&gt;(TrackingConfirmed ||  DeliveryConfirmed || TokenDenied));\r\n");	
			br.write("#assert "+MissionId+"() |= []!Survey || (!Survey U LocationReachedNotification);\r\n");	
			br.write("#assert "+MissionId+"() |= []!Sample || (!Sample U LocationReachedNotification);\r\n");	
			br.write("#assert "+MissionId+"() |= [](FlyToRemoteLocation -&gt; &lt;&gt; LocationReachedNotification);\r\n");
			br.write("#assert "+MissionId+"() |= [](LocationReachedNotification -&gt; &lt;&gt;(Survey || Sample || DeliveryConfirmed || SearchCommand) );\r\n");
			
			br.newLine();
			
// UNCOMMENT THIS IF YOU NEED SEPARATE LTL FORMULAS FOR EACH DRONE BASED ON THEIR SELECTED STATES		
//			if (states.contains("FlyingToLocation")) 
//				{br.write("#assert "+MissionId+"() |= &lt;&gt; FlyToRemoteLocation -&gt; (!FlyToRemoteLocation U (LaunchCompleteNotification &amp;&amp; !FlyToRemoteLocation));\r\n");}
//			
//			if (states.contains("Searching")) 
//				{br.write("#assert "+MissionId+"() |=  &lt;&gt; SearchCommand -&gt; (!SearchCommand U (LaunchCompleteNotification &amp;&amp; !SearchCommand));\r\n");}
//			
//			if (states.contains("Tracking")) 
//			{br.write("#assert "+MissionId+"() |=  [](TrackingConfirmed -&gt; &lt;&gt;(TrackingCompleted || TrackingFailed));\r\n");}
//			
//			if (states.contains("MakingDelivery")) 
//			{br.write("#assert "+MissionId+"() |= [](DeliveryConfirmed -&gt; &lt;&gt;(DeliveryCompleted || DeliveryFailed));\r\n");}
//			
//			if (states.contains("Tracking") && states.contains("MakingDelivery")==false) 
//			{br.write("#assert "+MissionId+"() |= []((TargetDetected || TargetDetectedAlert) -&gt;  &lt;&gt;(TrackingConfirmed || TokenDenied));\r\n");}
//			
//			else if (states.contains("Tracking")== false && states.contains("MakingDelivery")) 
//			{br.write("#assert "+MissionId+"() |= []((TargetDetected || TargetDetectedAlert) -&gt;  &lt;&gt;(DeliveryConfirmed || TokenDenied));\r\n");}
//			
//			else if (states.contains("Tracking") && states.contains("MakingDelivery")) 
//			{br.write("#assert "+MissionId+"() |= []((TargetDetected || TargetDetectedAlert) -&gt;  &lt;&gt;(TrackingConfirmed ||  DeliveryConfirmed || TokenDenied));\r\n");}
//			
//			if (states.contains("Surveying")) 
//			{br.write("#assert "+MissionId+"() |= []!Survey || (!Survey U LocationReachedNotification);\r\n");}
//			
//			if (states.contains("Sampling")) 
//			{br.write("#assert "+MissionId+"() |= []!Sample || (!Sample U LocationReachedNotification);\r\n");}		
//			
			br.newLine();
			br.newLine();
			br.newLine();			
		}
		br.newLine();
		br.newLine();
		br.write("</Declaration>");
		br.newLine();
		
		///PROCESS DESCRIPTION STARTS HERE
		br.write("  <Processes>");
		br.newLine();

		for (int i=0;i<missions.size();i++) {
			JSONObject spec = (JSONObject) missions.get(i);
			String MissionId = (String) spec.get("MissionId");
			JSONArray states = (JSONArray) spec.get("states");
			br.write("    <Process Name=\""+MissionId+"\" Parameter=\"\" Zoom=\"1\" StateCounter=\""+states.size()+"\">\r\n" + 
					"");
			br.newLine();
			///All default states
			br.write("     <States>\r\n" + 
					"        <State Name=\"OnGround-Armed\" Init=\"False\">\r\n" + 
					"          <Position X=\"2.7\" Y=\"0.9\" Width=\"0.2\" />\r\n" + 
					"          <Label>\r\n" + 
					"            <Position X=\"2.3\" Y=\"1.2\" Width=\"1.2\" />\r\n" + 
					"          </Label>\r\n" + 
					"        </State>\r\n" + 
					"        <State Name=\"OnGround-UnArmed\" Init=\"True\">\r\n" + 
					"          <Position X=\"0.9\" Y=\"0.9\" Width=\"0.2\" />\r\n" + 
					"          <Label>\r\n" + 
					"            <Position X=\"0.3\" Y=\"1.2\" Width=\"1.4\" />\r\n" + 
					"          </Label>\r\n" + 
					"        </State>\r\n" + 
					"        <State Name=\"Launching\" Init=\"False\">\r\n" + 
					"          <Position X=\"4\" Y=\"0.9\" Width=\"0.2\" />\r\n" + 
					"          <Label>\r\n" + 
					"            <Position X=\"3.7\" Y=\"1.1\" Width=\"0.7\" />\r\n" + 
					"          </Label>\r\n" + 
					"        </State>\r\n" + 
					"        <State Name=\"InAir-waiting-for-mission\" Init=\"False\">\r\n" + 
					"          <Position X=\"5.4\" Y=\"0.9\" Width=\"0.2\" />\r\n" + 
					"          <Label>\r\n" + 
					"            <Position X=\"4.9\" Y=\"1.1\" Width=\"1.6\" />\r\n" + 
					"          </Label>\r\n" + 
					"        </State>\r\n" +  
					"        <State Name=\"MissionComplete\" Init=\"False\">\r\n" + 
					"          <Position X=\"13.2\" Y=\"3.7\" Width=\"0.2\" />\r\n" + 
					"          <Label>\r\n" + 
					"            <Position X=\"12.8\" Y=\"4\" Width=\"1.193327\" />\r\n" + 
					"          </Label>\r\n" + 
					"        </State>\r\n"
					+ "        <State Name=\"u1\" Init=\"False\">\r\n" + 
					"          <Position X=\"14.3\" Y=\"0.5\" Width=\"0.2\" />\r\n" + 
					"          <Label>\r\n" + 
					"            <Position X=\"14.3\" Y=\"0.8\" Width=\"0.2\" />\r\n" + 
					"          </Label>\r\n" + 
					"        </State>\r\n" + 
					"        <State Name=\"u2\" Init=\"False\">\r\n" + 
					"          <Position X=\"15.6\" Y=\"0.5\" Width=\"0.2\" />\r\n" + 
					"          <Label>\r\n" + 
					"            <Position X=\"15.6\" Y=\"0.8\" Width=\"0.2\" />\r\n" + 
					"          </Label>\r\n" + 
					"        </State>");
			br.newLine();
			
			////Variable States
			
			if (states.contains("FlyingToLocation"))
			{
				br.write("        <State Name=\"FlyingToLocation\" Init=\"False\">\r\n" + 
						"          <Position X=\"5.3\" Y=\"3.6\" Width=\"0.2\" />\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"4.7\" Y=\"3.8\" Width=\"1.1\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </State>");
				br.newLine();
			}

			if (states.contains("Sampling"))
			{
				br.write("        <State Name=\"Sampling\" Init=\"False\">\r\n" + 
						"          <Position X=\"7\" Y=\"4.7\" Width=\"0.2\" />\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"6.8\" Y=\"4.9\" Width=\"0.6\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </State>");
				br.newLine();
			}
			
			if (states.contains("WaitingForNextSamplingTask"))
			{
				br.write("        <State Name=\"WaitingForNextSamplingTask\" Init=\"False\">\r\n" + 
						"          <Position X=\"6.4\" Y=\"5.8\" Width=\"0.2\" />\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"5.5\" Y=\"6\" Width=\"2\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </State>");
				br.newLine();
			}
			
			if (states.contains("Surveying"))
			{
				br.write("        <State Name=\"Surveying\" Init=\"False\">\r\n" + 
						"          <Position X=\"8.8\" Y=\"3.6\" Width=\"0.2\" />\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"8.6\" Y=\"3.8\" Width=\"0.7\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </State>");
				br.newLine();
			}
			
			if (states.contains("RequestingToken"))
			{
				br.write("        <State Name=\"RequestingToken\" Init=\"False\">\r\n" + 
						"          <Position X=\"8.7\" Y=\"0.9\" Width=\"0.2\" />\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"8.3\" Y=\"1.1\" Width=\"1.1\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </State>");
				br.newLine();
			}
			
			if (states.contains("LocationReached"))
			{
				br.write("        <State Name=\"LocationReached\" Init=\"False\">\r\n" + 
						"          <Position X=\"7.6\" Y=\"3.6\" Width=\"0.2\" />\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"7.2\" Y=\"3.8\" Width=\"1.2\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </State>");
				br.newLine();
			}
			if (states.contains("MakingDelivery"))
			{
				br.write("        <State Name=\"MakingDelivery\" Init=\"False\">\r\n" + 
						"          <Position X=\"9.4\" Y=\"1.9\" Width=\"0.2\" />\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"9\" Y=\"2.1\" Width=\"1\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </State>");
				br.newLine();
			}
			
			if (states.contains("Searching"))
			{
				br.write("       <State Name=\"Searching\" Init=\"False\">\r\n" + 
						"          <Position X=\"6.9\" Y=\"0.9\" Width=\"0.2\" />\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"6.7\" Y=\"1.2\" Width=\"0.7\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </State>");
				br.newLine();
			}
			
			if (states.contains("Tracking"))
			{
				br.write("        <State Name=\"Tracking\" Init=\"False\">\r\n" + 
						"          <Position X=\"10.1\" Y=\"0.9\" Width=\"0.2\" />\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"9.97\" Y=\"1.27\" Width=\"0.6\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </State>");
				br.newLine();
			}
			
			if (states.contains("WaitingForNextInstruction"))
			{
				br.write("        <State Name=\"WaitingForNextInstruction\" Init=\"False\">\r\n" + 
						"          <Position X=\"10.8\" Y=\"2.6\" Width=\"0.2\" />\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"10\" Y=\"2.8\" Width=\"1.7\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </State>");
				br.newLine();
			}
			
			br.write("      </States>");
			br.newLine();
			br.newLine();
			
			///Links
			
			br.write("      <Links>\r\n");
			////default links
			br.write("        <Link>\r\n" + 
					"          <From>OnGround-UnArmed</From>\r\n" + 
					"          <To>OnGround-Armed</To>\r\n" + 
					"          <Select>\r\n" + 
					"          </Select>\r\n" + 
					"          <Event>Arm</Event>\r\n" + 
					"          <ClockGuard>\r\n" + 
					"          </ClockGuard>\r\n" + 
					"          <Guard>\r\n" + 
					"          </Guard>\r\n" + 
					"          <Program>\r\n" + 
					"          </Program>\r\n" + 
					"          <ClockReset>\r\n" + 
					"          </ClockReset>\r\n" + 
					"          <Label>\r\n" + 
					"            <Position X=\"1.6\" Y=\"0.8\" Width=\"0.3\" />\r\n" + 
					"          </Label>\r\n" + 
					"        </Link>\r\n" + 
					"        <Link>\r\n" + 
					"          <From>OnGround-Armed</From>\r\n" + 
					"          <To>Launching</To>\r\n" + 
					"          <Select>\r\n" + 
					"          </Select>\r\n" + 
					"          <Event>Launch</Event>\r\n" + 
					"          <ClockGuard>\r\n" + 
					"          </ClockGuard>\r\n" + 
					"          <Guard>\r\n" + 
					"          </Guard>\r\n" + 
					"          <Program>\r\n" + 
					"          </Program>\r\n" + 
					"          <ClockReset>\r\n" + 
					"          </ClockReset>\r\n" + 
					"          <Label>\r\n" + 
					"            <Position X=\"3.2\" Y=\"0.8\" Width=\"0.5\" />\r\n" + 
					"          </Label>\r\n" + 
					"        </Link>\r\n" + 
					"        <Link>\r\n" + 
					"          <From>MissionComplete</From>\r\n" + 
					"          <To>OnGround-Armed</To>\r\n" + 
					"          <Select>\r\n" + 
					"          </Select>\r\n" + 
					"          <Event>ReturnHome</Event>\r\n" + 
					"          <ClockGuard>\r\n" + 
					"          </ClockGuard>\r\n" + 
					"          <Guard>\r\n" + 
					"          </Guard>\r\n" + 
					"          <Program>\r\n" + 
					"          </Program>\r\n" + 
					"          <ClockReset>\r\n" + 
					"          </ClockReset>\r\n" + 
					"          <Nail>\r\n" + 
					"            <Position X=\"13.2\" Y=\"7.3\" Width=\"0.1\" />\r\n" + 
					"          </Nail>\r\n" + 
					"          <Nail>\r\n" + 
					"            <Position X=\"2.6\" Y=\"7.5\" Width=\"0.1\" />\r\n" + 
					"          </Nail>\r\n" + 
					"          <Label>\r\n" + 
					"            <Position X=\"7.8\" Y=\"7.5\" Width=\"0.9\" />\r\n" + 
					"          </Label>\r\n" + 
					"        </Link>\r\n" + 
					"        <Link>\r\n" + 
					"          <From>OnGround-Armed</From>\r\n" + 
					"          <To>OnGround-UnArmed</To>\r\n" + 
					"          <Select>\r\n" + 
					"          </Select>\r\n" + 
					"          <Event>Disarm</Event>\r\n" + 
					"          <ClockGuard>\r\n" + 
					"          </ClockGuard>\r\n" + 
					"          <Guard>\r\n" + 
					"          </Guard>\r\n" + 
					"          <Program>isRTL"+MissionId+" = true;</Program>\r\n" + 
					"          <ClockReset>\r\n" + 
					"          </ClockReset>\r\n" + 
					"          <Nail>\r\n" + 
					"            <Position X=\"1.9\" Y=\"2.1\" Width=\"0.1\" />\r\n" + 
					"          </Nail>\r\n" + 
					"          <Label>\r\n" + 
					"            <Position X=\"1.2\" Y=\"2.3\" Width=\"1.7\" />\r\n" + 
					"          </Label>\r\n" + 
					"        </Link>\r\n" + 
					"        <Link>\r\n" + 
					"          <From>Launching</From>\r\n" + 
					"          <To>InAir-waiting-for-mission</To>\r\n" + 
					"          <Select>\r\n" + 
					"          </Select>\r\n" + 
					"          <Event>LaunchCompleteNotification</Event>\r\n" + 
					"          <ClockGuard>\r\n" + 
					"          </ClockGuard>\r\n" + 
					"          <Guard>\r\n" + 
					"          </Guard>\r\n" + 
					"          <Program>\r\n" + 
					"          </Program>\r\n" + 
					"          <ClockReset>\r\n" + 
					"          </ClockReset>\r\n" + 
					"          <Label>\r\n" + 
					"            <Position X=\"3.9\" Y=\"0.7\" Width=\"1.9\" />\r\n" + 
					"          </Label>\r\n" + 
					"        </Link>");
			br.newLine();
			
			br.write("<Link>\r\n" + 
					"          <From>MissionComplete</From>\r\n" + 
					"          <To>InAir-waiting-for-mission</To>\r\n" + 
					"          <Select>\r\n" + 
					"          </Select>\r\n" + 
					"          <Event>NewMission</Event>\r\n" + 
					"          <ClockGuard>\r\n" + 
					"          </ClockGuard>\r\n" + 
					"          <Guard>\r\n" + 
					"          </Guard>\r\n" + 
					"          <Program>\r\n" + 
					"          </Program>\r\n" + 
					"          <ClockReset>\r\n" + 
					"          </ClockReset>\r\n" + 
					"          <Nail>\r\n" + 
					"            <Position X=\"13\" Y=\"7.1\" Width=\"0.1\" />\r\n" + 
					"          </Nail>\r\n" + 
					"          <Nail>\r\n" + 
					"            <Position X=\"3\" Y=\"7.2\" Width=\"0.1\" />\r\n" + 
					"          </Nail>\r\n" + 
					"          <Nail>\r\n" + 
					"            <Position X=\"3.1\" Y=\"1.7\" Width=\"0.1\" />\r\n" + 
					"          </Nail>\r\n" + 
					"          <Label>\r\n" + 
					"            <Position X=\"7.5\" Y=\"7.2\" Width=\"0.8\" />\r\n" + 
					"          </Label>\r\n" + 
					"        </Link>");
			br.newLine();
			//Adding all universal links
			for (int k=0;k<transitionList.size();k++) {
				br.write("<Link>\r\n" + 
						"          <From>u1</From>\r\n" + 
						"          <To>u2</To>\r\n" + 
						"          <Select>\r\n" + 
						"          </Select>\r\n" + 
						"          <Event>"+transitionList.get(k).label+"</Event>\r\n" + 
						"          <ClockGuard>\r\n" + 
						"          </ClockGuard>\r\n" + 
						"          <Guard>\r\n" + 
						"          </Guard>\r\n" + 
						"          <Program>\r\n" + 
						"          </Program>\r\n" + 
						"          <ClockReset>\r\n" + 
						"          </ClockReset>\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"15.2\" Y=\"0.4\" Width=\"0.25\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </Link>");
				br.newLine();			
			}
			
			
			////variable links
			
			if (states.contains("Searching")) {
				br.write("        <Link>\r\n" + 
						"          <From>InAir-waiting-for-mission</From>\r\n" + 
						"          <To>Searching</To>\r\n" + 
						"          <Select>\r\n" + 
						"          </Select>\r\n" + 
						"          <Event>SearchCommand</Event>\r\n" + 
						"          <ClockGuard>\r\n" + 
						"          </ClockGuard>\r\n" + 
						"          <Guard>\r\n" + 
						"          </Guard>\r\n" + 
						"          <Program>\r\n" + 
						"          </Program>\r\n" + 
						"          <ClockReset>\r\n" + 
						"          </ClockReset>\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"5.7\" Y=\"0.8\" Width=\"1.2\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </Link>");
				br.newLine();
			}
			
			if (states.contains("Searching") && states.contains("RequestingToken")) {
				br.write("        <Link>\r\n" + 
						"          <From>Searching</From>\r\n" + 
						"          <To>RequestingToken</To>\r\n" + 
						"          <Select>\r\n" + 
						"          </Select>\r\n" + 
						"          <Event>TargetDetected</Event>\r\n" + 
						"          <ClockGuard>\r\n" + 
						"          </ClockGuard>\r\n" + 
						"          <Guard>targetAlert== false</Guard>\r\n" + 
						"          <Program>targetAlert=true;</Program>\r\n" + 
						"          <ClockReset>\r\n" + 
						"          </ClockReset>\r\n" + 
						"          <Nail>\r\n" + 
						"            <Position X=\"7.9\" Y=\"0.4\" Width=\"0.1\" />\r\n" + 
						"          </Nail>\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"6.2\" Y=\"0.5\" Width=\"3.2\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </Link>\r\n" + 
						"        <Link>\r\n" + 
						"          <From>Searching</From>\r\n" + 
						"          <To>RequestingToken</To>\r\n" + 
						"          <Select>\r\n" + 
						"          </Select>\r\n" + 
						"          <Event>TargetDetectedAlert</Event>\r\n" + 
						"          <ClockGuard>\r\n" + 
						"          </ClockGuard>\r\n" + 
						"          <Guard>targetAlert==true</Guard>\r\n" + 
						"          <Program>\r\n" + 
						"          </Program>\r\n" + 
						"          <ClockReset>\r\n" + 
						"          </ClockReset>\r\n" + 
						"          <Nail>\r\n" + 
						"            <Position X=\"7.9\" Y=\"1.5\" Width=\"0.1\" />\r\n" + 
						"          </Nail>\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"6.4\" Y=\"1.6\" Width=\"2.5\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </Link>\r\n" + 
						"        <Link>\r\n" + 
						"          <From>RequestingToken</From>\r\n" + 
						"          <To>Searching</To>\r\n" + 
						"          <Select>\r\n" + 
						"          </Select>\r\n" + 
						"          <Event>TokenDenied</Event>\r\n" + 
						"          <ClockGuard>\r\n" + 
						"          </ClockGuard>\r\n" + 
						"          <Guard>\r\n" + 
						"          </Guard>\r\n" + 
						"          <Program>\r\n" + 
						"          </Program>\r\n" + 
						"          <ClockReset>\r\n" + 
						"          </ClockReset>\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"7.5\" Y=\"1\" Width=\"0.9\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </Link>");
				br.newLine();
			}
			if (states.contains("RequestingToken") && states.contains("Tracking")) {
				br.write("        <Link>\r\n" + 
						"          <From>RequestingToken</From>\r\n" + 
						"          <To>Tracking</To>\r\n" + 
						"          <Select>\r\n" + 
						"          </Select>\r\n" + 
						"          <Event>TrackingConfirmed</Event>\r\n" + 
						"          <ClockGuard>\r\n" + 
						"          </ClockGuard>\r\n" + 
						"          <Guard>hasTrackingToken =="+missions.size()+"</Guard>\r\n" + 
						"          <Program>hasTrackingToken="+MissionId.charAt(MissionId.length()-1)+";\r\n" + 
						"isTracking"+MissionId+" = true;</Program>\r\n" + 
						"          <ClockReset>\r\n" + 
						"          </ClockReset>\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"8.7\" Y=\"0.7\" Width=\"3.7\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </Link>");
				br.newLine();
			}
			if (states.contains("Tracking")) {
				br.write("        <Link>\r\n" + 
						"          <From>Tracking</From>\r\n" + 
						"          <To>MissionComplete</To>\r\n" + 
						"          <Select>\r\n" + 
						"          </Select>\r\n" + 
						"          <Event>TrackingCompleted</Event>\r\n" + 
						"          <ClockGuard>\r\n" + 
						"          </ClockGuard>\r\n" + 
						"          <Guard>\r\n" + 
						"          </Guard>\r\n" + 
						"          <Program>hasTrackingToken="+missions.size()+";\r\n" + 
						"targetAlert=false;\r\n" + 
						"isTracking"+MissionId+" = false;</Program>\r\n" + 
						"          <ClockReset>\r\n" + 
						"          </ClockReset>\r\n" + 
						"          <Nail>\r\n" + 
						"            <Position X=\"13\" Y=\"0.9\" Width=\"0.1\" />\r\n" + 
						"          </Nail>\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"10.3\" Y=\"1\" Width=\"3.6\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </Link>");
				br.newLine();
			}			
			
			if (states.contains("Searching")) {
				br.write("        <Link>\r\n" + 
						"          <From>Searching</From>\r\n" + 
						"          <To>MissionComplete</To>\r\n" + 
						"          <Select>\r\n" + 
						"          </Select>\r\n" + 
						"          <Event>NoVictimConfirmation</Event>\r\n" + 
						"          <ClockGuard>\r\n" + 
						"          </ClockGuard>\r\n" + 
						"          <Guard>\r\n" + 
						"          </Guard>\r\n" + 
						"          <Program>\r\n" + 
						"          </Program>\r\n" + 
						"          <ClockReset>\r\n" + 
						"          </ClockReset>\r\n" + 
						"          <Nail>\r\n" + 
						"            <Position X=\"7.3\" Y=\"0.2\" Width=\"0.1\" />\r\n" + 
						"          </Nail>\r\n" + 
						"          <Nail>\r\n" + 
						"            <Position X=\"13.2\" Y=\"0.2\" Width=\"0.1\" />\r\n" + 
						"          </Nail>\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"10\" Y=\"0.3\" Width=\"1.4\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </Link>");
				br.newLine();
				
			}			
			if (states.contains("FlyingToLocation")) {
				br.write("        <Link>\r\n" + 
						"          <From>InAir-waiting-for-mission</From>\r\n" + 
						"          <To>FlyingToLocation</To>\r\n" + 
						"          <Select>\r\n" + 
						"          </Select>\r\n" + 
						"          <Event>FlyToRemoteLocation</Event>\r\n" + 
						"          <ClockGuard>\r\n" + 
						"          </ClockGuard>\r\n" + 
						"          <Guard>\r\n" + 
						"          </Guard>\r\n" + 
						"          <Program>\r\n" + 
						"          </Program>\r\n" + 
						"          <ClockReset>\r\n" + 
						"          </ClockReset>\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"4.3\" Y=\"1.7\" Width=\"1.6\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </Link>");
				br.newLine();
			}			
			if (states.contains("FlyingToLocation") && states.contains("LocationReached")) {
				br.write("        <Link>\r\n" + 
						"          <From>FlyingToLocation</From>\r\n" + 
						"          <To>LocationReached</To>\r\n" + 
						"          <Select>\r\n" + 
						"          </Select>\r\n" + 
						"          <Event>LocationReachedNotification</Event>\r\n" + 
						"          <ClockGuard>\r\n" + 
						"          </ClockGuard>\r\n" + 
						"          <Guard>\r\n" + 
						"          </Guard>\r\n" + 
						"          <Program>\r\n" + 
						"          </Program>\r\n" + 
						"          <ClockReset>\r\n" + 
						"          </ClockReset>\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"5.6\" Y=\"3.5\" Width=\"1.9\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </Link>");
				br.newLine();
			}			
			if (states.contains("LocationReached") && states.contains("Searching")) {
				br.write("        <Link>\r\n" + 
						"          <From>LocationReached</From>\r\n" + 
						"          <To>Searching</To>\r\n" + 
						"          <Select>\r\n" + 
						"          </Select>\r\n" + 
						"          <Event>SearchCommand</Event>\r\n" + 
						"          <ClockGuard>\r\n" + 
						"          </ClockGuard>\r\n" + 
						"          <Guard>\r\n" + 
						"          </Guard>\r\n" + 
						"          <Program>\r\n" + 
						"          </Program>\r\n" + 
						"          <ClockReset>\r\n" + 
						"          </ClockReset>\r\n" + 
						"          <Nail>\r\n" + 
						"            <Position X=\"5.9\" Y=\"3.2\" Width=\"0.1\" />\r\n" + 
						"          </Nail>\r\n" + 
						"          <Nail>\r\n" + 
						"            <Position X=\"5.9\" Y=\"1.4\" Width=\"0.1\" />\r\n" + 
						"          </Nail>\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"5.6\" Y=\"2.5\" Width=\"1.2\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </Link>\r\n" );
				br.newLine();
			}			
			if (states.contains("LocationReached") && states.contains("Sampling")) {
				br.write("        <Link>\r\n" + 
						"          <From>LocationReached</From>\r\n" + 
						"          <To>Sampling</To>\r\n" + 
						"          <Select>\r\n" + 
						"          </Select>\r\n" + 
						"          <Event>Sample</Event>\r\n" + 
						"          <ClockGuard>\r\n" + 
						"          </ClockGuard>\r\n" + 
						"          <Guard>\r\n" + 
						"          </Guard>\r\n" + 
						"          <Program>\r\n" + 
						"          </Program>\r\n" + 
						"          <ClockReset>\r\n" + 
						"          </ClockReset>\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"7.2\" Y=\"4.2\" Width=\"0.5\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </Link>");
				br.newLine();
			}			
			if (states.contains("WaitingForNextSamplingTask") && states.contains("FlyingToLocation")) {
				br.write("        <Link>\r\n" + 
						"          <From>WaitingForNextSamplingTask</From>\r\n" + 
						"          <To>FlyingToLocation</To>\r\n" + 
						"          <Select>\r\n" + 
						"          </Select>\r\n" + 
						"          <Event>FlyToRemoteLocation</Event>\r\n" + 
						"          <ClockGuard>\r\n" + 
						"          </ClockGuard>\r\n" + 
						"          <Guard>\r\n" + 
						"          </Guard>\r\n" + 
						"          <Program>\r\n" + 
						"          </Program>\r\n" + 
						"          <ClockReset>\r\n" + 
						"          </ClockReset>\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"5.2\" Y=\"4.3\" Width=\"1.5\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </Link>");
				br.newLine();
			}			
			if (states.contains("Sampling") && states.contains("WaitingForNextSamplingTask")) {
				br.write("        <Link>\r\n" + 
						"          <From>Sampling</From>\r\n" + 
						"          <To>WaitingForNextSamplingTask</To>\r\n" + 
						"          <Select>\r\n" + 
						"          </Select>\r\n" + 
						"          <Event>SamplingCompleted</Event>\r\n" + 
						"          <ClockGuard>\r\n" + 
						"          </ClockGuard>\r\n" + 
						"          <Guard>\r\n" + 
						"          </Guard>\r\n" + 
						"          <Program>\r\n" + 
						"          </Program>\r\n" + 
						"          <ClockReset>\r\n" + 
						"          </ClockReset>\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"6.4\" Y=\"5.3\" Width=\"1.3\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </Link>");
				br.newLine();
			}			
			if (states.contains("WaitingForNextSamplingTask")) {
				br.write("        <Link>\r\n" + 
						"          <From>WaitingForNextSamplingTask</From>\r\n" + 
						"          <To>MissionComplete</To>\r\n" + 
						"          <Select>\r\n" + 
						"          </Select>\r\n" + 
						"          <Event>NoSamplingTaskRemaining</Event>\r\n" + 
						"          <ClockGuard>\r\n" + 
						"          </ClockGuard>\r\n" + 
						"          <Guard>\r\n" + 
						"          </Guard>\r\n" + 
						"          <Program>\r\n" + 
						"          </Program>\r\n" + 
						"          <ClockReset>\r\n" + 
						"          </ClockReset>\r\n" + 
						"          <Nail>\r\n" + 
						"            <Position X=\"6.4\" Y=\"6.9\" Width=\"0.1\" />\r\n" + 
						"          </Nail>\r\n" + 
						"          <Nail>\r\n" + 
						"            <Position X=\"12.8\" Y=\"6.8\" Width=\"0.1\" />\r\n" + 
						"          </Nail>\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"9.1\" Y=\"6.9\" Width=\"1.8\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </Link>");
				br.newLine();
			}			
			if (states.contains("LocationReached") && states.contains("Surveying")) {
				br.write("        <Link>\r\n" + 
						"          <From>LocationReached</From>\r\n" + 
						"          <To>Surveying</To>\r\n" + 
						"          <Select>\r\n" + 
						"          </Select>\r\n" + 
						"          <Event>Survey</Event>\r\n" + 
						"          <ClockGuard>\r\n" + 
						"          </ClockGuard>\r\n" + 
						"          <Guard>\r\n" + 
						"          </Guard>\r\n" + 
						"          <Program>\r\n" + 
						"          </Program>\r\n" + 
						"          <ClockReset>\r\n" + 
						"          </ClockReset>\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"8.1\" Y=\"3.5\" Width=\"0.5\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </Link>");
				br.newLine();
			}			
			if (states.contains("Surveying")) {
				br.write("        <Link>\r\n" + 
						"          <From>Surveying</From>\r\n" + 
						"          <To>MissionComplete</To>\r\n" + 
						"          <Select>\r\n" + 
						"          </Select>\r\n" + 
						"          <Event>SurveyComplete</Event>\r\n" + 
						"          <ClockGuard>\r\n" + 
						"          </ClockGuard>\r\n" + 
						"          <Guard>\r\n" + 
						"          </Guard>\r\n" + 
						"          <Program>\r\n" + 
						"          </Program>\r\n" + 
						"          <ClockReset>\r\n" + 
						"          </ClockReset>\r\n" + 
						"          <Nail>\r\n" + 
						"            <Position X=\"7.6\" Y=\"6.6\" Width=\"0.1\" />\r\n" + 
						"          </Nail>\r\n" + 
						"          <Nail>\r\n" + 
						"            <Position X=\"12.6\" Y=\"6.5\" Width=\"0.1\" />\r\n" + 
						"          </Nail>\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"10.6\" Y=\"6.6\" Width=\"1.1\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </Link>");
				br.newLine();
			}			
			if (states.contains("RequestingToken") && states.contains("MakingDelivery")) {
				br.write("        <Link>\r\n" + 
						"          <From>RequestingToken</From>\r\n" + 
						"          <To>MakingDelivery</To>\r\n" + 
						"          <Select>\r\n" + 
						"          </Select>\r\n" + 
						"          <Event>DeliveryConfirmed</Event>\r\n" + 
						"          <ClockGuard>\r\n" + 
						"          </ClockGuard>\r\n" + 
						"          <Guard>hasDeliveryToken=="+missions.size()+"</Guard>\r\n" + 
						"          <Program>hasDeliveryToken="+MissionId.charAt(MissionId.length()-1)+";\r\n" + 
						"isDelivering"+MissionId+"=true;\r\n" + 
						"</Program>\r\n" + 
						"          <ClockReset>\r\n" + 
						"          </ClockReset>\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"7.6\" Y=\"1.4\" Width=\"3.6\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </Link>");
				br.newLine();
			}			
			if (states.contains("MakingDelivery")) {
				br.write("        <Link>\r\n" + 
						"          <From>MakingDelivery</From>\r\n" + 
						"          <To>MissionComplete</To>\r\n" + 
						"          <Select>\r\n" + 
						"          </Select>\r\n" + 
						"          <Event>DeliveryCompleted</Event>\r\n" + 
						"          <ClockGuard>\r\n" + 
						"          </ClockGuard>\r\n" + 
						"          <Guard>\r\n" + 
						"          </Guard>\r\n" + 
						"          <Program>hasDeliveryToken="+missions.size()+";\r\n" + 
						"targetAlert=false;\r\n" + 
						"isDelivering"+MissionId+" = false;</Program>\r\n" + 
						"          <ClockReset>\r\n" + 
						"          </ClockReset>\r\n" + 
						"          <Nail>\r\n" + 
						"            <Position X=\"12.8\" Y=\"1.9\" Width=\"0.1\" />\r\n" + 
						"          </Nail>\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"11.5\" Y=\"1.8\" Width=\"3.6\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </Link>");
				br.newLine();
			}			
			if (states.contains("LocationReached") && states.contains("MakingDelivery")) {
				br.write("        <Link>\r\n" + 
						"          <From>LocationReached</From>\r\n" + 
						"          <To>MakingDelivery</To>\r\n" + 
						"          <Select>\r\n" + 
						"          </Select>\r\n" + 
						"          <Event>DeliveryConfirmed</Event>\r\n" + 
						"          <ClockGuard>\r\n" + 
						"          </ClockGuard>\r\n" + 
						"          <Guard>hasDeliveryToken=="+missions.size()+"</Guard>\r\n" + 
						"          <Program>hasDeliveryToken="+MissionId.charAt(MissionId.length()-1)+";\r\n" + 
						"isDelivering"+MissionId+"=true;</Program>\r\n" + 
						"          <ClockReset>\r\n" + 
						"          </ClockReset>\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"6.8\" Y=\"3\" Width=\"3.6\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </Link>");
				br.newLine();
			}			
			if (states.contains("MakingDelivery") && states.contains("WaitingForNextInstruction")) {
				br.write("        <Link>\r\n" + 
						"          <From>MakingDelivery</From>\r\n" + 
						"          <To>WaitingForNextInstruction</To>\r\n" + 
						"          <Select>\r\n" + 
						"          </Select>\r\n" + 
						"          <Event>DeliveryFailed</Event>\r\n" + 
						"          <ClockGuard>\r\n" + 
						"          </ClockGuard>\r\n" + 
						"          <Guard>\r\n" + 
						"          </Guard>\r\n" + 
						"          <Program>hasDeliveryToken="+missions.size()+";\r\n" + 
						"targetAlert=false;\r\n" + 
						"isDelivering"+MissionId+" = false;</Program>\r\n" + 
						"          <ClockReset>\r\n" + 
						"          </ClockReset>\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"9.7\" Y=\"2.3\" Width=\"3.4\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </Link>");
				br.newLine();
			}			
			if (states.contains("WaitingForNextInstruction")) {
				br.write("        <Link>\r\n" + 
						"          <From>WaitingForNextInstruction</From>\r\n" + 
						"          <To>MissionComplete</To>\r\n" + 
						"          <Select>\r\n" + 
						"          </Select>\r\n" + 
						"          <Event>Abort</Event>\r\n" + 
						"          <ClockGuard>\r\n" + 
						"          </ClockGuard>\r\n" + 
						"          <Guard>\r\n" + 
						"          </Guard>\r\n" + 
						"          <Program>\r\n" + 
						"          </Program>\r\n" + 
						"          <ClockReset>\r\n" + 
						"          </ClockReset>\r\n" + 
						"          <Nail>\r\n" + 
						"            <Position X=\"12.8\" Y=\"2.7\" Width=\"0.1\" />\r\n" + 
						"          </Nail>\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"11.9\" Y=\"2.5\" Width=\"0.4\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </Link>");
				br.newLine();
			}			
			if (states.contains("WaitingForNextInstruction") && states.contains("Searching")) {
				br.write("        <Link>\r\n" + 
						"          <From>WaitingForNextInstruction</From>\r\n" + 
						"          <To>Searching</To>\r\n" + 
						"          <Select>\r\n" + 
						"          </Select>\r\n" + 
						"          <Event>SearchCommand</Event>\r\n" + 
						"          <ClockGuard>\r\n" + 
						"          </ClockGuard>\r\n" + 
						"          <Guard>\r\n" + 
						"          </Guard>\r\n" + 
						"          <Program>\r\n" + 
						"          </Program>\r\n" + 
						"          <ClockReset>\r\n" + 
						"          </ClockReset>\r\n" + 
						"          <Nail>\r\n" + 
						"            <Position X=\"7\" Y=\"2.6\" Width=\"0.1\" />\r\n" + 
						"          </Nail>\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"7.8\" Y=\"2.5\" Width=\"1.2\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </Link>");
				br.newLine();
			}			if (states.contains("Tracking") && states.contains("WaitingForNextInstruction")) {
				br.write("        <Link>\r\n" + 
						"          <From>Tracking</From>\r\n" + 
						"          <To>WaitingForNextInstruction</To>\r\n" + 
						"          <Select>\r\n" + 
						"          </Select>\r\n" + 
						"          <Event>TrackingFailed</Event>\r\n" + 
						"          <ClockGuard>\r\n" + 
						"          </ClockGuard>\r\n" + 
						"          <Guard>\r\n" + 
						"          </Guard>\r\n" + 
						"          <Program>hasTrackingToken="+missions.size()+";\r\n" + 
						"targetAlert=false;\r\n" + 
						"isTracking"+MissionId+" = false;</Program>\r\n" + 
						"          <ClockReset>\r\n" + 
						"          </ClockReset>\r\n" + 
						"          <Label>\r\n" + 
						"            <Position X=\"10.2\" Y=\"1.7\" Width=\"3.5\" />\r\n" + 
						"          </Label>\r\n" + 
						"        </Link>");
				br.newLine();
			}
			br.write("      </Links>\r\n" + 
					"    </Process>");
		}	
		br.write("  </Processes>\r\n" + 
				"</LTS>");
	
		// PROCESS and LINKS ENDS HERE
		for (int i=0;i<missions.size();i++) {
			JSONObject spec = (JSONObject) missions.get(i);
			String MissionId = (String) spec.get("MissionId");
			JSONArray states = (JSONArray) spec.get("states");
			System.out.println(states);
			System.out.println(states.size());
			for (int j=0;j<states.size();j++) {				
			}			
		}
		closeFiles();
	}
	
	public static void openFile(String filename) throws IOException {
		file = new File(directory+filename);
		fr = new FileWriter(file,false);
		br = new BufferedWriter(fr);			
	}
	
	public static void closeFiles() throws IOException {
		br.close();
		fr.close();
	}

	private static JSONObject makeJSONMessage() {
		
		String message;
		JSONObject json = new JSONObject();
		JSONArray mission = new JSONArray();
		
		JSONObject d0 = new JSONObject();
		d0.put("MissionId","Mission0");
		JSONArray state = new JSONArray();	
		for (int i=0;i<allStates.size();i++) {
			state.add(allStates.get(i));
		}
		
		d0.put("states",state);		
		mission.add(d0);
		
		//For a survey drone
		JSONObject d1 = new JSONObject();
		d1.put("MissionId","Mission1");
		state = new JSONArray();	
		state.add("FlyingToLocation");
		state.add("LocationReached");
		state.add("Surveying");
		d1.put("states",state);		
		mission.add(d1);	
		
		
		//For a search-tracking
		JSONObject d2 = new JSONObject();
		d2.put("MissionId","Mission2");
		state = new JSONArray();	
		state.add("Searching");
		state.add("RequestingToken");
		state.add("Tracking");
		state.add("WaitingForNextInstruction");
		d2.put("states",state);
		mission.add(d2);
		json.put("missions",mission);
		
		//For a long distance deivery drone
		JSONObject d3 = new JSONObject();
		d3.put("MissionId","Mission3");
		state = new JSONArray();	
		state.add("FlyingToLocation");
		state.add("LocationReached");
		//state.add("MakingDelivery");
		d3.put("states",state);		
		mission.add(d3);
		
		message = json.toString();
		return json;
		
		
	}
	
	
	//This methods creates all the possible "Transition" objects and gathers them in the ArrayList named transitionList
	//this ArayList is not being used right but it might be useful later. 
	private static void makeListOfAllTransitions() {
		
		transitionList.add(new transition("OnGround-UnArmed","Arm","OnGround-Armed"));
		transitionList.add(new transition("OnGround-Armed","Disarm","OnGround-UnArmed"));
		transitionList.add(new transition("OnGround-Armed","Launch","Launching"));
		transitionList.add(new transition("Launching","LaunchCompleteNotification","InAir-waiting-for-mission"));
		transitionList.add(new transition("InAir-waiting-for-mission","SearchCommand","Searching"));
		transitionList.add(new transition("InAir-waiting-for-mission","FlyToRemoteLocation","FlyingToLocation"));
		transitionList.add(new transition("Searching","TargetDetected","RequestingToken"));
		transitionList.add(new transition("Searching","TargetDetectedAlert","RequestingToken"));
		transitionList.add(new transition("Searching","NoVictimConfirmation","MissionComplete"));
		transitionList.add(new transition("RequestingToken","TrackingConfirmed","Tracking"));
		transitionList.add(new transition("RequestingToken","DeliveryConfirmed","MakingDelivery"));
		transitionList.add(new transition("RequestingToken","TokenDenied","Searching"));
		transitionList.add(new transition("Tracking","TrackingCompleted","MissionComplete"));
		transitionList.add(new transition("Tracking","TrackingFailed","WaitingForNextInstruction"));
		transitionList.add(new transition("MakingDelivery","DeliveryCompleted","MissionComplete"));
		transitionList.add(new transition("MakingDelivery","DeliveryFailed","WaitingForNextInstruction"));
		transitionList.add(new transition("WaitingForNextInstruction","Abort","MissionComplete"));
		transitionList.add(new transition("WaitingForNextInstruction","SearchCommand","Searching"));
		transitionList.add(new transition("FlyingToLocation","LocationReachedNotification","LocationReached"));
		transitionList.add(new transition("LocationReached","Survey","Surveying"));
		transitionList.add(new transition("LocationReached","Sample","Sampling"));
		transitionList.add(new transition("LocationReached","DeliveryConfirmed","MakingDelivery"));
		transitionList.add(new transition("Surveying","SurveyComplete","MissionComplete"));
		transitionList.add(new transition("Sampling","SamplingCompleted","WaitingForNextSamplingTask"));
		transitionList.add(new transition("WaitingForNextSamplingTask","FlyToRemoteLocation","FlyingToLocation"));
		transitionList.add(new transition("WaitingForNextSamplingTask","NoSamplingTaskRemaining","MissionComplete"));
		transitionList.add(new transition("MissionComplete","NewMission","InAir-waiting-for-mission"));
		transitionList.add(new transition("MissionComplete","ReturnHome","OnGround-Armed"));

	}
	
	
	

}
