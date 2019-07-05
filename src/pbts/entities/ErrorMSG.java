package pbts.entities;

import pbts.enums.*;
public class ErrorMSG {

	public String msg;
	public ErrorType err;
	public ErrorMSG(ErrorType err, String msg){
		this.err = err;
		this.msg = msg;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
