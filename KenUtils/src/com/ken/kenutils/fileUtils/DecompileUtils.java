package com.ken.kenutils.fileUtils;

import java.io.IOException;

import com.strobel.decompiler.Decompiler;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;

public class DecompileUtils {

	public String decompile(String source) throws IOException{
			
		final DecompilerSettings settings = DecompilerSettings.javaDefaults();
		PlainTextOutput pText = new PlainTextOutput();
    	Decompiler.decompile(source, pText, settings);
	    return pText.toString();	
	}
	
	public static String decompileTest(String source) throws IOException{
		
		final DecompilerSettings settings = DecompilerSettings.javaDefaults();
		PlainTextOutput pText = new PlainTextOutput();
    	Decompiler.decompile(source, pText, settings);
	    return pText.toString();	
	}
	
}
