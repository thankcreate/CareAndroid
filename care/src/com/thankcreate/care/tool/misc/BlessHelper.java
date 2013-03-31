package com.thankcreate.care.tool.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import android.R.bool;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.thankcreate.care.App;
import com.thankcreate.care.AppConstants;
import com.thankcreate.care.R;
import com.thankcreate.care.tool.converter.BlessConverter;
import com.thankcreate.care.tool.ui.ToastHelper;
import com.thankcreate.care.viewmodel.BlessItemViewModel;
import com.thankcreate.care.viewmodel.ChatItemViewModel;


public class BlessHelper {
	

	
	
	private List<String> listImagePath = new ArrayList<String>();
	private int defaultBkgIdArray[] = {R.drawable.bkg_blessing_1, R.drawable.bkg_blessing_2, R.drawable.bkg_blessing_3};
	private final int MAX_BKG_COUNT = 3;
	private final int MIN_BLESS_ITEM_COUNT = 3; 
	
	// 注意，此处defaultName和defaultContent的长度比须到少是MIN_BLESS_ITEM_COUNT
	private final String defaultName[] = {"tankery", "豪子", "一个孤独的散步者"};
	private final String defaultContent[] = {"生命中最悲哀的事莫过于放弃追逐你所爱的人，看着她远离。无论你追逐多久，你还是要让他走。",
			"人生为棋，我愿为卒，行动虽慢，可谁曾见我后退一步",
			"不要期待得到爱，慢慢地等待你的爱在她的心中生根发芽，即使不会，你也当满足，因为你心中已有一片绿洲。"};
	
	public void cacheBlessImages()
	{
		fetchListImagePath(new FetchCompleteListener() {			
			@Override
			public void fetchComplete() {	
				try {
					fetchImages();	
				} catch (Exception e) {
					e.printStackTrace();
				}				
			}
		});
	}
	
	public List<Drawable> getBlessImages(Context context)
	{
		List<Drawable> resList = new ArrayList<Drawable>();
		
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
		// 如果当前image本地存储文件夹里多于MAX_BKG_COUNT张，则只取MAX_BKG_COUNT张
		// 绝对多数情况下，应该是小于或等于MAX_BKG_COUNT张的
		int localCount = alreadyExistsFile.length > MAX_BKG_COUNT ? MAX_BKG_COUNT : alreadyExistsFile.length; 
		for(int i = 0; i < localCount; i++)
		{
			try {
				Bitmap bitmap = BitmapFactory.decodeFile(alreadyExistsFile[i].getAbsolutePath());
				if(bitmap != null) 
				{
					BitmapDrawable bd = new BitmapDrawable(context.getResources(), bitmap);
					resList.add(bd);	
				}	
			} catch (Exception e) {
				continue;
			}
		}
		
		// 看看当前加载的张数是否小于MAX_BKG_COUNT张，如果少了，直接从包内的默认bkg中加载
		int remainCount = MAX_BKG_COUNT - resList.size();
		if(remainCount > 0)
		{
			for(int i = 0 ; i < remainCount; i++)
			{
				try {
					Drawable defaultDrawable = context.getResources().getDrawable(defaultBkgIdArray[i]);
					resList.add(defaultDrawable);
				} catch (Exception e) {
					continue;
				}
			}
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
		String alreadyExistsFileStr[] = blessingDir.list();
		for (String fullURL : listImagePath) {
			try {				
				String fileName = StringTool.getFileName(fullURL);
				if(!isOneOf(fileName, alreadyExistsFileStr))
				{
					fetchImage(fullURL);
				}
			} catch (Exception e) {				
				continue;
			}
		}
		

		// 本地缓存的图中，删掉listImagePath中不存在的内容
		// 这是因为每次都是直接拿本地那个目录中的所有图
		// 如果不删，旧图会一直堆积
		List<String> listImageName = new ArrayList<String>();
		for (String path: listImagePath) {
			listImageName.add(StringTool.getFileName(path));
		}
		String[] listImageNameArray = {};
		listImageNameArray = listImageName.toArray(listImageNameArray);
		File allFile[] = blessingDir.listFiles();
		
		if(allFile == null || allFile.length == 0)
			return;
		for (File file : allFile) {
			String fileName = StringTool.getFileName(file.getAbsolutePath());
			if(!isOneOf(fileName ,listImageNameArray))
			{
				file.delete();
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
		if(fullURL == null || StringTool.isNullOrEmpty(fullURL))
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
	        if(bmp == null)
	        	return;
	        			
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
					HttpGet request = new HttpGet(AppConstants.BLESS_IMAGES_PATH_DOC_URL);
					HttpResponse response = httpClient.execute(request);
					InputStream is = response.getEntity().getContent();
					String plainPath = StreamTool.inputStreamToString(is);
					String pathArray[] = plainPath.split(";");
					for (int i = 0; i < pathArray.length; i++) {
						listImagePath.add(pathArray[i].trim());
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
	
	public interface FetchBlessItemListener
	{
		public void fetchComplete(List<BlessItemViewModel> resList);
	}
	
	public void cacheBlessPassedItem()
	{
		fetchBlessItem(10, true, new FetchBlessItemListener() {			
			@Override
			public void fetchComplete(List<BlessItemViewModel> resList) {
				if(resList == null || resList.size() == 0)
					return;
				
				// 存到缓存
				try {
					File myDir = App.getAppContext().getFilesDir();
					File cacheFile = new File(myDir, AppConstants.CACHE_BLESS_ITEM);
					FileOutputStream fos = new FileOutputStream(cacheFile);
					ObjectOutputStream oos = new ObjectOutputStream(fos);
					oos.writeObject(resList);
					oos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public List<BlessItemViewModel> getCachedBlessPassedItem()
	{
		List<BlessItemViewModel> listModel = null;
		try {
			File myDir = App.getAppContext().getFilesDir();
			File cacheFile = new File(myDir, AppConstants.CACHE_BLESS_ITEM);
			FileInputStream fis = new FileInputStream(cacheFile);
			ObjectInputStream ois = new ObjectInputStream(fis);
			listModel = (ArrayList<BlessItemViewModel>) ois.readObject();
			ois.close();
		} catch (Exception e) {
			listModel = null;
			e.printStackTrace();
		}
		
		if(listModel == null)
			listModel = new ArrayList<BlessItemViewModel>();
		// 如果连最低项数都达不到，插入写死的几条
		int remain = MIN_BLESS_ITEM_COUNT - listModel.size();
		if(remain > 0)
		{
			for(int i = 0; i < remain; ++i)
			{
				BlessItemViewModel item = new BlessItemViewModel();
				item.title = defaultName[i];
				item.content = defaultContent[i];
				listModel.add(item);
			}
		}
		return listModel;
	}
	
	public void fetchBlessItem(final int count, final boolean isPassed, final FetchBlessItemListener listener)
	{
		if(listener == null)
			return;
		final List<BlessItemViewModel> resList = new ArrayList<BlessItemViewModel>();
		 
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					DefaultHttpClient httpClient = new DefaultHttpClient();
					
					
					String paramCount;
					if(count == 0)
						paramCount = "20";
					else {
						paramCount = String.valueOf(count);
					}
					
					String paramNeedPassed = isPassed ? "1" : "0";					
					
					String finalString  = String.format("%s?count=%s&needPassed=%s", 
							AppConstants.BLESS_GET_URL, paramCount, paramNeedPassed);
					HttpGet request = new HttpGet(finalString);
					
					
					HttpResponse response = httpClient.execute(request);
					InputStream is = response.getEntity().getContent();
					String plainJsonStr = StreamTool.inputStreamToString(is);
					JSONArray itemsArray = new JSONArray(plainJsonStr);
					if(itemsArray != null && itemsArray.length() != 0 )
					{
						for(int i = 0 ; i < itemsArray.length(); i++)
						{
							JSONObject ob = itemsArray.getJSONObject(i);
							BlessItemViewModel item = BlessConverter.convertToViewModel(ob);
							if(item != null)
							{
								resList.add(item);
							}
						}
					}
				} 
				catch (Exception e) 
				{
					ToastHelper.show(">_< 获取祝福墙时失败，请确保网络连接正常。");
				}
				listener.fetchComplete(resList);
			}
		}).start();
	}
	
	public interface PostBlessItemListener
	{
		public void postComplete();
	}
	
	
	public void postBlessItem(String name, String content, final PostBlessItemListener listener)
	{
		Log.i("timestamp", String.valueOf(System.currentTimeMillis()));
		if(listener == null)
			return;
		if(StringTool.isNullOrEmpty(name))
		{
			name = "匿名";
		}
		if(content == null)
			content = "";
		
		final String finalName = name;
		final String finalContent = content;
		
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					DefaultHttpClient httpClient = new DefaultHttpClient();					
					HttpPost request = new HttpPost(AppConstants.BLESS_POST_URL);	
					
					List<NameValuePair> parms= new ArrayList<NameValuePair>();
					parms.add(new BasicNameValuePair("name", finalName));
					parms.add(new BasicNameValuePair("content", finalContent));
					request.setEntity(new UrlEncodedFormEntity(parms, HTTP.UTF_8));
					
					HttpResponse response = httpClient.execute(request);
					InputStream is = response.getEntity().getContent();
					is.close();
				} 
				catch (Exception e) 
				{
					ToastHelper.show(">_< 发送，请确保网络连接正常。");
				}		
				listener.postComplete();
			}
		}).start();
	}
}
