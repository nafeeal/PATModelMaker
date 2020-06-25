package pilot;

public class Student {
	
	private String Name;
	private int Age;
	private boolean isPassed;
	private static int studentCount =0;
	
	public Student(String Name, int Age, boolean isPassed){
		this.Name=Name;
		this.Age=Age;
		this.isPassed = isPassed;
		studentCount++;
		System.out.println("Stuents Count: "+ studentCount);
		
		
}
	
	public void setName(String Name) {
		
		this.Name=Name;
		
	}
	public void setAge(int Age) {
		
		this.Age=Age;
		
	}
	public void setisPassed(boolean isPassed) {
		
		this.isPassed=isPassed;
		
	}
	
	public String getName() {
		return this.Name;
	}
	public int getAge() {
		return this.Age;
	}
	
	public boolean getisPassed() {
		return this.isPassed;
	}
	
	
	
	public boolean isOld() {
		
		if(this.Age>24)
			{return true;}
		else 
			{return false;}
		
		
	}
	
}
