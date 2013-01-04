package com.thankcreate.care.tool.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.thankcreate.care.tool.misc.StringTool;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class DrawableManager {
    private final WeakHashMap<String, Bitmap> drawableMap;
    public  WeakHashMap<String, Rect> adjustSizeMap;
    public List<String> cancelList;
    

    public DrawableManager() {
        drawableMap = new WeakHashMap<String, Bitmap>();
        adjustSizeMap = new WeakHashMap<String, Rect>();
        cancelList = new ArrayList<String>();
    }
    
    public void removeFromCache(String url)
    {
    	drawableMap.remove(url);
    	
    	// 目前偷了一下懒，直接在removeFromCache里做了cancelReqeust了
    	cancelReqeuest(url);
    }
    
    public void cancelReqeuest(String url)
    {
    	cancelList.add(url);
    }

    public Bitmap fetchDrawable(String urlString) {
    	// 一个主动的fetchDrawable过来后，需要把这个url从cancel列表中清除    	
    	cancelList.remove(urlString);
    	
    	// 第一次查缓存
        if (drawableMap.containsKey(urlString)) {
            return drawableMap.get(urlString);
        }
        
        Log.d(this.getClass().getSimpleName(), "image url:" + urlString);
        try {
        	
        	// 下面这一步可能是很耗时的，在这其间，什么都有可能发生
            InputStream is = fetch(urlString);
            if(cancelList.contains(urlString))
            	return null;
            
            // 第二次查缓存
            if (drawableMap.containsKey(urlString)) {
                return drawableMap.get(urlString);
            }
            
            // 开始做decode
            BitmapFactory.Options opt =new BitmapFactory.Options();
            opt.inPreferredConfig = Bitmap.Config.RGB_565;   
            opt.inSampleSize = 1;   //width，hight设为原来的一分之一
            opt.inPurgeable = true; 
            opt.inInputShareable = true;            
            // 下面这一句其实才是OOM的根本所在，尽量避免无意义的decode
            Bitmap bmp =BitmapFactory.decodeStream(is,null, opt);
            
//            Drawable drawable = Drawable.createFromStream(is, "src");
//            if (drawable != null) {            	
//                drawableMap.put(urlString, drawable);
//                Log.d(this.getClass().getSimpleName(), "got a thumbnail drawable: " + drawable.getBounds() + ", "
//                        + drawable.getIntrinsicHeight() + "," + drawable.getIntrinsicWidth() + ", "
//                        + drawable.getMinimumHeight() + "," + drawable.getMinimumWidth());
//            } else {
//              Log.w(this.getClass().getSimpleName(), "could not get thumbnail");
//            }
            
            if(bmp != null)
            	drawableMap.put(urlString, bmp);
            return bmp;
        } catch (MalformedURLException e) {
            Log.e(this.getClass().getSimpleName(), "fetchDrawable failed", e);
            return null;
        } catch (IOException e) {
            Log.e(this.getClass().getSimpleName(), "fetchDrawable failed", e);
            return null;
        }
    }

    public void fetchDrawableOnThread(final String urlString, final ImageView imageView) {
        if(!isMatch(urlString, imageView))
        {
        	return;
        }
    	
    	if (drawableMap.containsKey(urlString)) {
            //imageView.setImageDrawable(drawableMap.get(urlString));
    		imageView.setImageBitmap(drawableMap.get(urlString));
        }

        final Handler handler = new Handler() {
        	
            @Override
            public void handleMessage(Message message) {
                if(!isMatch(urlString, imageView))
                {
                	return;
                }                
//            	Drawable drawable = (Drawable) message.obj;            	
//                imageView.setImageDrawable((Drawable) message.obj);
                Bitmap bmp = (Bitmap) message.obj;
                imageView.setImageBitmap(drawableMap.get(urlString));
            }
        };

        Thread thread = new Thread() {
            @Override
            public void run() {

                //TODO : set imageView to a "pending" image
                //Drawable drawable = fetchDrawable(urlString);
            	if(StringTool.isNullOrEmpty(urlString))
            		return;
            	Bitmap drawable = fetchDrawable(urlString);
            	if(drawable == null)
            		return;
                Message message = handler.obtainMessage(1, drawable);
                handler.sendMessage(message);
            }
        };
        thread.start();
    }
    
    public void fetchDrawableOnThreadWithCallback(final String urlString, final ImageView imageView, final FetchDrawableCompleteListener callback) {
        if(!isMatch(urlString, imageView))
        {
        	return;
        }        
    	
    	if (drawableMap.containsKey(urlString)) {
    		callback.fetchComplete(drawableMap.get(urlString));
            //imageView.setImageDrawable(drawableMap.get(urlString));
    		return;
        }

        final Handler handler = new Handler() {
        	
            @Override
            public void handleMessage(Message message) {
                if(!isMatch(urlString, imageView))
                {
                	return;
                }
            	//Drawable drawable = (Drawable) message.obj;
                Bitmap bmp = (Bitmap) message.obj;
            	callback.fetchComplete(bmp);
                //imageView.setImageDrawable((Drawable) message.obj);
            }
        };

        Thread thread = new Thread() {
            @Override
            public void run() {

                //TODO : set imageView to a "pending" image
                //Drawable drawable = fetchDrawable(urlString);
            	Bitmap drawable = fetchDrawable(urlString);
            	if(drawable == null)
            		return;
                Message message = handler.obtainMessage(1, drawable);
                handler.sendMessage(message);
            }
        };
        thread.start();
    }
    

    public void fetchDrawableOnThreadWithDoNothing(final String urlString) {
       
    	if (drawableMap.containsKey(urlString)) {
    		return;
        }

        Thread thread = new Thread() {
            @Override
            public void run() {

                //TODO : set imageView to a "pending" image
                fetchDrawable(urlString);
            }
        };
        thread.start();
    }
    
    
    
    
    private Boolean isMatch(String urlString, ImageView imageView)
    {
    	String url;
    	if(imageView.getTag() instanceof String)
    	{
    		url = (String) imageView.getTag();
        	if(urlString.compareToIgnoreCase(url) == 0)
        	{
        		return true;
        	}
        	return false;
    	}
    	else
    	{
			return true;
		}
    }
    
    public void fetchDrawableOnThread(final String urlString, final ImageView imageView, final int size, final Boolean needFit, final BaseAdapter adapter) {
        if(!isMatch(urlString, imageView))
        {
        	return;
        }
    	
    	if (drawableMap.containsKey(urlString)) {
            //imageView.setImageDrawable(drawableMap.get(urlString));
    		imageView.setImageBitmap(drawableMap.get(urlString));
        }

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if(!isMatch(urlString, imageView))
                {
                	return;
                }
            	Bitmap bmp = (Bitmap) message.obj;
            	int sourceWidth = bmp.getWidth();
            	int sourceHeight = bmp.getHeight();
            	if(needFit)
            	{    
            		LayoutParams params = imageView.getLayoutParams();
            		if(sourceWidth > sourceHeight)
                	{
                		int newHeight = size * sourceHeight / sourceWidth;
                		params.height = newHeight;
                		params.width = size;
                		imageView.setLayoutParams(params);
                	}
                	else if (sourceHeight >= sourceWidth)
                	{
                		int newWidth = size * sourceWidth / sourceHeight;                		
                		// 针对新浪微博超长图
                		if(newWidth < 20)
                			newWidth = 60;
                		params.width = newWidth;
                		params.height = size;
                		imageView.setLayoutParams(params);
                	}
            		Rect cacheRecht = new Rect(0, 0, params.width, params.height);
            		adjustSizeMap.put(urlString, cacheRecht);
            	}
                imageView.setImageBitmap((Bitmap) message.obj);                
            }
        };

        Thread thread = new Thread() {
            @Override
            public void run() {
                //TODO : set imageView to a "pending" image
                //Drawable drawable = fetchDrawable(urlString);
            	Bitmap bmp = fetchDrawable(urlString);
            	if(bmp == null)
            		return;
            	
                if(!isMatch(urlString, imageView))
                {
                	return;
                }
                
                
                //Message message = handler.obtainMessage(1, drawable);
                Message message = handler.obtainMessage(1, bmp);
                handler.sendMessage(message);
            }
        };
        thread.start();
    }

    private InputStream fetch(String urlString) throws MalformedURLException, IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpGet request = new HttpGet(urlString);
        HttpResponse response = httpClient.execute(request);
        return response.getEntity().getContent();
    }
    
    public interface FetchDrawableCompleteListener
    {
    	//public void fetchComplete(Drawable d);
    	public void fetchComplete(Bitmap d);
    }
}