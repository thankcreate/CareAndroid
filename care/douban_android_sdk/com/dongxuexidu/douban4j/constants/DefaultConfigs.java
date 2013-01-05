package com.dongxuexidu.douban4j.constants;

import com.dongxuexidu.douban4j.model.app.RequestGrantScope;
import com.google.api.client.xml.XmlNamespaceDictionary;
import com.thankcreate.care.AppConstants;

/**
 *
 * @author Zhibo Wei <uglytroll@dongxuexidu.com>
 */
public class DefaultConfigs {
  
  public static final String API_KEY = AppConstants.DOUBAN_APP_KEY;
  public static final String SECRET_KEY = AppConstants.DOUBAN_SECRET_KEY;
  public static final String API_URL_PREFIX = "https://api.douban.com";
  public static final String AUTH_URL = "https://www.douban.com/service/auth2/auth";
  public static final String ACCESS_TOKEN_URL = "https://www.douban.com/service/auth2/token";
  public static final String ACCESS_TOKEN_REDIRECT_URL = AppConstants.DOUBAN_REDIRECT_URL;
  
  public static final XmlNamespaceDictionary DOUBAN_XML_NAMESPACE = new XmlNamespaceDictionary()
          .set("", "http://www.w3.org/2005/Atom")
          .set("db", "http://www.douban.com/xmlns/")
          .set("gd", "http://schemas.google.com/g/2005")
          .set("opensearch", "http://a9.com/-/spec/opensearchrss/1.0/");
  
}
