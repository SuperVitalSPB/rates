package ru.supervital.rates;

import java.io.StringReader;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Build;
import android.os.NetworkOnMainThreadException;
import android.view.View;
import android.widget.LinearLayout;

public class SendPost extends AsyncTask<String, Void, Boolean> {
  public String Url;
  public List<NameValuePair> Params;
  public Activity mActivity;
  public String sMessage;
  public LinearLayout mProgressBar;

  
  /** 
   * конструктор
   */
  public SendPost(String Url, List<NameValuePair> Params, Boolean Result){
	  super();
  }

  
  /** 
   * перед выполнением
   */
  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    showProgress(true);
  }

  /** 
   * в отдельном потоке
   */
  @Override
  protected Boolean doInBackground(String... Url) {
  	try {
  		sMessage = postData(Url[0], Params);
  		return true;
	} catch (Exception e) {
		sMessage = e.getMessage();
		e.printStackTrace();
		return false;
	}
	
  }

  /** 
   * после выполнения
   */
  @Override  
  protected void onPostExecute(final Boolean success) {
	  super.onPostExecute(success);
//	  showProgress(false);
  }
	  
  /** 
   * нажали отменить
   */
  @Override
  protected void onCancelled() {
	super.onCancelled();
	showProgress(false);
  }
	  
  /** 
   * моя ф-я выполнение запроса
   */  
	public String postData(String aUrl, List<NameValuePair> aParam ) throws Exception {
	    // Создадим HttpClient и PostHandler
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost(aUrl); 
	    String sResp = "";
      
	    try {
			if (aParam != null)
				httppost.setEntity(new UrlEncodedFormEntity(aParam));
			
			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
	        // ответ
	        HttpEntity responseEntity = response.getEntity();
	       
	        if(responseEntity != null) {
	        	sResp = EntityUtils.toString(responseEntity, "windows-1251");
	        }
	    } 
	    catch (NetworkOnMainThreadException e) {
	    	throw e;
	    } catch (Exception e) {
	    	throw e;
	    }
	    return sResp;
	}
	
	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public void showProgress(final boolean show) {
		if (mProgressBar != null)
			mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
	}
		
	protected XmlPullParser prepareXpp(String sXML) throws XmlPullParserException {
	    // получаем фабрику
	    XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	    // включаем поддержку namespace (по умолчанию выключена)
	    factory.setNamespaceAware(true);
	    // создаем парсер
	    XmlPullParser xpp = factory.newPullParser();
	    // даем парсеру на вход Reader
	    xpp.setInput(new StringReader(sXML));
	    return xpp;
	}
	

	    
}	
//  publishProgress()