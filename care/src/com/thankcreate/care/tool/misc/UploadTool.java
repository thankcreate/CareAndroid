package com.thankcreate.care.tool.misc;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;

import android.os.Bundle;

public class UploadTool {
	
	private static String parseContentType(String fileName) {
		String contentType = "image/jpg";
		fileName = fileName.toLowerCase();
		if (fileName.endsWith(".jpg"))
			contentType = "image/jpg";
		else if (fileName.endsWith(".png"))
			contentType = "image/png";
		else if (fileName.endsWith(".jpeg"))
			contentType = "image/jpeg";
		else if (fileName.endsWith(".gif"))
			contentType = "image/gif";
		else if (fileName.endsWith(".bmp"))
			contentType = "image/bmp";
		else
			throw new RuntimeException("不支持的文件类型'" + fileName + "'(或没有文件扩展名)");
		return contentType;
	}
	
	
	public static HttpURLConnection doubanSendFormdata(String reqUrl, String token, Bundle parameters,
            String filename, byte[] data) {
        HttpURLConnection urlConn = null;
        try {
            URL url = new URL(reqUrl);
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.setConnectTimeout(5000);// （单位：毫秒）jdk
            urlConn.setReadTimeout(5000);// （单位：毫秒）jdk 1.5换成这个,读操作超时
            urlConn.setDoOutput(true);
            urlConn.setRequestProperty("Authorization", "Bearer " + token);
            urlConn.setRequestProperty("connection", "keep-alive");

            String boundary = "-----------------------------114975832116442893661388290519"; // 分隔符
            urlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            boundary = "--" + boundary;
            StringBuffer params = new StringBuffer();
            if (parameters != null) {
                for (Iterator<String> iter = parameters.keySet().iterator(); iter.hasNext();) {
                    String name = iter.next();
                    String value = parameters.getString(name);
                    params.append(boundary + "\r\n");
                    params.append("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
                    // params.append(URLEncoder.encode(value, "UTF-8"));
                    params.append(value);
                    params.append("\r\n");
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append(boundary);
            sb.append("\r\n");
            String fileParamName = "image";
            sb.append("Content-Disposition: form-data; name=\"" + fileParamName + "\"; filename=\""
                    + filename + "\"\r\n");
            String contentType = parseContentType(filename);
            sb.append("Content-Type: " + contentType + "\r\n\r\n");
            byte[] fileDiv = sb.toString().getBytes();
            byte[] endData = ("\r\n" + boundary + "--\r\n").getBytes();
            byte[] ps = params.toString().getBytes();

            OutputStream os = urlConn.getOutputStream();
            os.write(ps);
            os.write(fileDiv);
            os.write(data);
            os.write(endData);

            os.flush();
            os.close();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return urlConn;
    }
}
