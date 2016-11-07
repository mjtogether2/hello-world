package utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtil {

	public static String StreamToString(InputStream inputStream) throws IOException{
		int length=-1;
		byte[] bs =new byte[1024];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		while((length=inputStream.read(bs))!=-1){
			baos.write(bs, 0, length);
		}
		String result = new String(baos.toByteArray(),"utf-8");
		return result;
	}
}
