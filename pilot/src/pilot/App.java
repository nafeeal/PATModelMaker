package pilot;

import java.util.HashMap;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.*;  


public class App {
	public static void main(String [] args) throws IOException {
		/*
		String [] Names = {"Tom","Seium","Nafee"};
		int CAge = 30;
		Scanner keyIn = new Scanner(System.in);
		Scanner num1 = new Scanner(System.in);
		System.out.print("Your name: ");
		String name = keyIn.nextLine();
		
		System.out.print("Your age: ");
		int num = keyIn.nextInt();
		
		
		System.out.print("Hi " + name + "! Your age is " + num);
		System.out.print(Names[0].charAt(2));
		try {
			int k = keyIn.nextInt();
		}catch(Exception e) {
			
			System.out.println("Invalid Input");
		}
		//Exceptions can be specified
		//Errors can be captured similarly
		// Throwable = Exceptions + Errors
		
		addn(3);
		int k=7;
		String g1="kalam";
		String g2="kalam";
		if(g1.equals(g2)) {
			System.out.println("\n5mara kha");
			
		}
		int l;
		for (l=0;l<10; l++) {System.out.println(l);}*/

		// Learning about Class and Object
		/*
		Student myStudent = new Student("Md",23,false);
		Student myStudent2 = new Student("Kamal",25,true);
		myStudent.setName("Nafee");
		System.out.println(myStudent.getisPassed());
		*/
		
		
		
		//MCQ Game
		/*
		String Q1="What is love?\n  a) True \n"
				+ "  b)Dhoka";
		String Q2="Apples are?\n  a)Orange\n  b)Red";
		Questions [] questions = {new Questions(Q1,"a"), new Questions(Q2,"b")};
		int g=askQ(questions);
		
		System.out.println(g +"/"+questions.length);
		
		*/
		
		// Learning interface -- Animals interface --Cat and Dog class
		/*
		
		Dog myDog= new Dog();
		myDog.speak();
		
		Animals [] myAnimals = {new Dog(), new Cat ()};
		
		myAnimals[0].speak();
		myAnimals[1].speak();
		*/
		
		//Learning ENUMS
		
		/*for(LoggingLevel state : LoggingLevel.values())
		{
			System.out.println(state);
			
			
		}
		*/
        HashMap<String, Integer> hm =  new HashMap<String, Integer>(); 

    // Adding mappings to HashMap 
       /* int i;
        for(i = 1;i<44; i++) {
        	String k="value "+i+":";
            hm.put(k, i); 

        }
        
        String [] nodes = {""};
        nodes = Arrays.copyOf(nodes, nodes.length+1);
        nodes[nodes.length-1]="C";
    	 

		
		System.out.println(nodes[nodes.length-1]);
		*/
        
        
        // Renaming
        
        File f1 = new File("C:\\temp\\oldname.txt");
        File f2 = new File("C:\\temp\\newname.txt");
        f1.createNewFile();
        f1.renameTo(f2);

       // System.out.println(b);
        
        
        ArrayList<String> al=new ArrayList<String>(); 
        al.add("Nafee");
        al.add("Good");
        
        String k=al.get(0);
        System.out.println(al.size());
        
        int po=6;
        String ll = "sss"+ Integer.toString(po);
        System.out.println(ll);
        
        double kk =Math.round(2.51-0.000000000000001);
        System.out.println(kk);
        
        
	}
	
	
	public static int askQ(Questions [] a) {
		int Score =0;
		
		for(int i=0;i<a.length;i++)
		{
			Scanner keyIn = new Scanner(System.in);
			System.out.println(a[i].que);
			String k= keyIn.nextLine();
			if (k.equals(a[i].ans)) {
				Score++;
				
			}
			
		}

		return Score;
		
	}
		

	
	
	public static int addn(int x) {
		int k=x*x;
		System.out.print(k);
		return k;
		
	}

}
