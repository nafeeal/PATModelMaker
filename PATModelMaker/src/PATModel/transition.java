package PATModel;

public class transition {
	String fromState;
	String toState;
	String label;
	
	public transition(String fromState, String label, String toState) {
		this.fromState = fromState;
		this.toState = toState;
		this.label= label;	
		
	}
	
	public void print() {

		System.out.println(this.fromState +"  ----"+this.label+"---->> "+this.toState);
		
		
	}

}
