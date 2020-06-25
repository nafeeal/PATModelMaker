package pilot;

public class thistest {
	
	thistest(){
		
		this("marakha");
		System.out.println("Inside Constructor without parameter");

		
		
	}

public thistest(String string) {
		// TODO Auto-generated constructor stub
	System.out.println(string);

		
	}

public static void main(String[] args) {
		thistest obj = new thistest();
	}
	
	

}
