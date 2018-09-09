package com.myliqj.dir;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;

public class StreamGobbler extends Thread {
	InputStream in;
	String charsetName;
	String type;
	PrintStream out;
	StreamGobbler(InputStream inputStream, String charsetName, String type, PrintStream out) {
	    this.in = inputStream;
	    this.charsetName = charsetName;
	    this.type = type;
	    this.out = out;
	}
	@Override
	public void run() {
	    try {
	        InputStreamReader isr = new InputStreamReader(in, charsetName);
	        char[] cbuf = new char[256];
	        int len = -1;
	        while ( -1 != (len=isr.read(cbuf))){
	            out.print(Arrays.copyOf(cbuf, len));
	        }
	        isr.close();
	    	out.flush();
	    } catch (IOException ioe) {
	        ioe.printStackTrace();
	    } finally {
	    	try{
	    		in.close();
	    	}catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    }
	}
}
