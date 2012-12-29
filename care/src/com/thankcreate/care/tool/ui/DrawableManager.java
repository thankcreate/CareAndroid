package com.thankcreate.care.tool.ui;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

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
    private final WeakHashMap<String, Drawable> drawableMap;
    public  WeakHashMap<String, Rect> adjustSizeMap;

    public DrawableManager() {
        drawableMap = new WeakHashMap<String, Drawable>();
        adjustSizeMap = new WeakHashMap<String, Rect>();
    }
    
    public void removeFromCache(String url)
    {
    	drawableMap.remove(url);
    }

    public Drawable fetchDrawable(String urlString) {
        if (drawableMap.containsKey(urlString)) {
            return drawableMap.get(urlString);
        }

        Log.d(this.getClass().getSimpleName(), "image url:" + urlString);
        try {
            InputStream is = fetch(urlString);
            
            
            Drawable drawable = Drawable.createFromStream(is, "src");
            

            if (drawable != null) {            	
                drawableMap.put(urlString, drawable);
                Log.d(this.getClass().getSimpleName(), "got a thumbnail drawable: " + drawable.getBounds() + ", "
                        + drawable.getIntrinsicHeight() + "," + drawable.getIntrinsicWidth() + ", "
                        + drawable.getMinimumHeight() + "," + drawable.getMinimumWidth());
            } else {
              Log.w(this.getClass().getSimpleName(), "could not get thumbnail");
            }

            return drawable;
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
            imageView.setImageDrawable(drawableMap.get(urlString));
        }

        final Handler handler = new Handler() {
        	
            @Override
            public void handleMessage(Message message) {
                if(!isMatch(urlString, imageView))
                {
                	return;
                }                
            	Drawable drawable = (Drawable) message.obj;            	
                imageView.setImageDrawable((Drawable) message.obj);
            }
        };

        Thread thread = new Thread() {
            @Override
            public void run() {

                //TODO : set imageView to a "pending" image
                Drawable drawable = fetchDrawable(urlString);
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
            	Drawable drawable = (Drawable) message.obj;
            	callback.fetchComplete(drawable);
                //imageView.setImageDrawable((Drawable) message.obj);
            }
        };

        Thread thread = new Thread() {
            @Override
            public void run() {

                //TODO : set imageView to a "pending" image
                Drawable drawable = fetchDrawable(urlString);
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
                Drawable drawable = fetchDrawable(urlString);
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
            imageView.setImageDrawable(drawableMap.get(urlString));
        }

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                if(!isMatch(urlString, imageView))
                {
                	return;
                }
            	Drawable drawable = (Drawable) message.obj;
            	int sourceWidth = drawable.getIntrinsicWidth();
            	int sourceHeight = drawable.getIntrinsicHeight();
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
                imageView.setImageDrawable((Drawable) message.obj);                
            }
        };

        Thread thread = new Thread() {
            @Override
            public void run() {
                //TODO : set imageView to a "pending" image
                Drawable drawable = fetchDrawable(urlString);
                if(!isMatch(urlString, imageView))
                {
                	return;
                }
                Message message = handler.obtainMessage(1, drawable);
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
    	public void fetchComplete(Drawable d);
    }
}