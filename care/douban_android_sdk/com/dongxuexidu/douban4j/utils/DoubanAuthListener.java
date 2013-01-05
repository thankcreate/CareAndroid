package com.dongxuexidu.douban4j.utils;

import android.os.Bundle;

import com.dongxuexidu.douban4j.model.app.AccessToken;
import com.weibo.sdk.android.WeiboDialogError;
import com.weibo.sdk.android.WeiboException;

public interface DoubanAuthListener {
	   /**
     * 认证结束后将调用此方法
     * 
     * @param values
     *            Key-value string pairs extracted from the response.
     *            从responsetext中获取的键值对，键值包括"access_token"，"expires_in"，“refresh_token”
     */
    public void onComplete(AccessToken values);

    /**
     * Oauth2.0认证过程中，当认证对话框中的webview接收数据出现错误时调用此方法
     * @param e WeiboDialogError
     * 
     */
    public void onError(String e);

    /**
     * Oauth2.0认证过程中，如果认证窗口被关闭或认证取消时调用
     * 
     * 
     */
    public void onCancel();
}
