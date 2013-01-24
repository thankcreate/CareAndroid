package com.thankcreate.care.tool.misc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.thankcreate.care.App;
import com.thankcreate.care.AppConstants;


public class BlessHelper {
	

	private final String imagePathFileURL = "http://42.96.147.167/thankcreate/doc/image_path.txt";
	private final List<String> listImagePath = new ArrayList<String>();
	
	public void refresh()
	{
		fetchListImagePath(new FetchCompleteListener() {			
			@Override
			public void fetchComplete() {				
				fetchImages();
			}
		});
	}
	
	public List<Bitmap> getBlessImages()
	{
		List<Bitmap> resList = new ArrayList<Bitmap>();
		
		File myDir = App.getAppContext().getFilesDir();		
		File blessingDir = new File(myDir, AppConstants.BLESSING_BACKGROUND_DIR);
		if(!blessingDir.exists())
		{
			try {
				blessingDir.mkdirs(); 
            } catch (Exception e) { 
            	return resList;
            } 
		}
		
		File alreadyExistsFile[] = blessingDir.listFiles();		
		for(int i = 0; i < alreadyExistsFile.length; i++)
		{
			Bitmap bitmap = BitmapFactory.decodeFile(alreadyExistsFile[i].getAbsolutePath());
			resList.add(bitmap);
		}
		return resList;
	}
	
	private void fetchImages()
	{
		if(listImagePath == null || listImagePath.size() == 0)
			return;
		// 先检查本地是否已经下载过这些图了，直接通过文件名来检测
		File myDir = App.getAppContext().getFilesDir();		
		File blessingDir = new File(myDir, AppConstants.BLESSING_BACKGROUND_DIR);
		if(!blessingDir.exists())
		{
			try {
				blessingDir.mkdirs(); 
            } catch (Exception e) { 
            	return;
            } 
		}
		String alreadyExistsFile[] = blessingDir.list();
		for (String fullURL : listImagePath) {
			try {				
				String fileName = StringTool.getFileName(fullURL);
				if(!isOneOf(fileName, alreadyExistsFile))
				{
					fetchImage(fullURL);
				}
			} catch (Exception e) {				
				continue;
			}
		}
	}	
	
	
	
	/**
	 * 如果url是空的话，直接返回true，这样从后续逻辑上就不会去做图片下载了
	 * @param url
	 * @param urlArray
	 * @return
	 */
	private boolean isOneOf(String url, String urlArray[])
	{
		if(urlArray == null || urlArray.length == 0)
			return false;
		if(url == null)
			return true;
		for(int i = 0; i < urlArray.length; i++)
		{
			if(url.equalsIgnoreCase(urlArray[i]))
			{
				return true;
			}
		}
		return false;
	}
	
	private void fetchImage(String fullURL)
	{
		if(fullURL == null || fullURL.isEmpty())
			return;
		File myDir = App.getAppContext().getFilesDir();		
		File blessingDir = new File(myDir, AppConstants.BLESSING_BACKGROUND_DIR);
		if(!blessingDir.exists())
		{
			try {
				blessingDir.mkdirs(); 
            } catch (Exception e) { 
            	return;
            } 
		}
		try {
			DefaultHttpClient httpClient = new DefaultHttpClient();
	        HttpGet request = new HttpGet(fullURL);
	        HttpResponse response = httpClient.execute(request);
	        InputStream is = response.getEntity().getContent();
	        Bitmap bmp =BitmapFactory.decodeStream(is);
	        			
			String fileName = StringTool.getFileName(fullURL);	        
	        File imageFile = new File(blessingDir ,fileName);
	        FileOutputStream fout = new FileOutputStream(imageFile);
	        bmp.compress(Bitmap.CompressFormat.JPEG, 100, fout);
		    fout.flush();
		    fout.close();
		} catch (Exception e) {
			return;
		}
	}

	private void fetchListImagePath(final FetchCompleteListener listener) {
		new Thread(new Runnable() {
			@Override
			public void run() {

				try {
					DefaultHttpClient httpClient = new DefaultHttpClient();
					HttpGet request = new HttpGet(imagePathFileURL);
					HttpResponse response = httpClient.execute(request);
					InputStream is = response.getEntity().getContent();
					String plainPath = StreamTool.inputStreamToString(is);
					String pathArray[] = plainPath.split(";");
					for (int i = 0; i < pathArray.length; i++) {
						listImagePath.add(pathArray[i]);
					}
				} catch (Exception e) {
					listImagePath.clear();
				}
				finally
				{
					listener.fetchComplete();
				}
			}
		}).start();
	}
	
	
	public interface FetchCompleteListener
	{	
		public void fetchComplete();
	}
}
