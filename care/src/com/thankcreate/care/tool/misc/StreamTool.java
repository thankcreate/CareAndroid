package com.thankcreate.care.tool.misc;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class StreamTool {
	
    final static int BUFFER_SIZE = 4096;  
    
    
	public static String inputStreamToString(InputStream in) throws Exception{  
        
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();  
        byte[] data = new byte[BUFFER_SIZE];  
        int count = -1;  
        while((count = in.read(data,0,BUFFER_SIZE)) != -1)  
            outStream.write(data, 0, count);  
          
        data = null;  
        return new String(outStream.toByteArray(),"ISO-8859-1");  
    }  
}
