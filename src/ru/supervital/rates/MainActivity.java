package ru.supervital.rates;

import java.util.ArrayList;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.ListView;

import java.io.EOFException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParser;

import ru.supervital.rates.R.drawable;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

	public static final String TAG = "rates.MainActivity";
	private RateArrayAdapter mAdapter;
	ArrayList<Rate> rates = new ArrayList<Rate>();
	ArrayList<String> claRates = new ArrayList<String>();
	
//--
	private TextView lblTitle;
	private ListView lvMain;
	
	boolean isOnline;

//--	
	List<NameValuePair> Profiles;
	
	private int mYear;
	private int mMonth;
	private int mDay;
//--
	private String sDateRate = "";
	private String sActiveProfileName = "";
//--
	final int GROUP_PROFILE = 10;
	
	private LinearLayout mProgressBar;

	public CurrSendPost mt;
	public CurrSendPost mtP;

    BackgroundContainer mBackgroundContainer;
    boolean mSwiping = false;
    boolean mItemPressed = false;
    private static final int SWIPE_DURATION = 250;
    private static final int MOVE_DURATION = 150;

    
    // имя файла настроек
    public static final String av_APP_PREFERENCES = "supervital.rate.settings";
    // параметр в настройках
    public static final String av_PROFILES_LIST = "profileslist";
    public static final String av_LAST_PROFILE = "lastprofile";

    MenuItem menuProfile_item = null, menuAllVal_item = null, menuDelete_item = null;
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mBackgroundContainer = (BackgroundContainer) findViewById(R.id.lvBackground);
		
		lblTitle = (TextView) findViewById(R.id.lblTitle);
		lblTitle.setText("");
		
		lvMain = (ListView) findViewById(R.id.lvMain);
		android.util.Log.d("Debug", "d=" + lvMain.getDivider());
		
		lvMain.setClickable(true);
		//registerForContextMenu(lvMain);
		
		mProgressBar = (LinearLayout) findViewById(R.id.ll_progressbar);
		isOnline = isOnline();
		checkOnline();
		
		lvMain.setOnItemClickListener( new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
			    Rate val = (Rate) lvMain.getItemAtPosition(position);
			    ShowToast(val);
			}
		});
		LastRate();
	}

	public void ShowToast(Rate val){
	    String sStr = val.Name + "\n"+ getString(R.string.sPred) + " " + val.RatePrev;
	    double vCh = val.Rate - val.RatePrev;
	    sStr = sStr + "\n"+ getString(R.string.sChange)  + " " +  (vCh > 0 ? "+": "") + String.format("%.2g%n", vCh);
        Toast.makeText(getApplicationContext(), sStr, Toast.LENGTH_SHORT).show();
	}
	
	public void RefreshRate(){
		rates.clear();
		claRates.clear();
		LoadRate(getDateRate());
	}
	
	public void LastRate(){
		Calendar c = Calendar.getInstance(); 
		c.add(Calendar.DATE, 1);
		LoadProfiles();
		rates.clear();
		claRates.clear();
		LoadRate(c.getTime());
		
	}

	public void checkOnline(){
		if (isOnline) { 
			lblTitle.setTextColor(Color.BLUE);
		} else {
			ShowNotOnline(getString(R.string.sNotLogin));
		}
	}
	
	void ShowNotOnline(String sStr){
		lblTitle.setText(sStr);
		lblTitle.setTextColor(Color.RED);
	}
	
	public void setTitle(String aDateRate){
		String sStr = "";
		if (aDateRate.length() == 0) return;
		sStr = String.format(getString(R.string.sTitleMain), aDateRate + ":");
		lblTitle.setText(sStr);
		return;
	}
	
	public boolean isOnline() {
		  String cs = Context.CONNECTIVITY_SERVICE;
		  ConnectivityManager cm = (ConnectivityManager) getSystemService(cs);
		  if (cm.getActiveNetworkInfo() == null) {
		    return false;
		  }
		  return cm.getActiveNetworkInfo().isConnectedOrConnecting();
	}

	public Boolean aResult = false;
	public void LoadRate(Date aDateRate){
		if (rates.size()==0) {
			mt = new CurrSendPost(null, null, aResult);
			mAdapter = new RateArrayAdapter(this, rates, mTouchListener);
			lvMain.setAdapter(mAdapter);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			String sCurrDate = new SimpleDateFormat("dd.MM.yyyy", new Locale("ru")).format(aDateRate);
			nameValuePairs.add(new BasicNameValuePair("date_req", sCurrDate)); 
			mt.mActivity = this; 			
			mt.mProgressBar = mProgressBar;
			mt.Url = "http://www.cbr.ru/scripts/XML_daily.asp";   
			mt.Params = nameValuePairs;
			mt.execute(mt.Url);
			mt.showProgress(true);
		} else {
			setTitle(sDateRate);
			lvMain.setAdapter(mAdapter);
		}
	}
	
	public void SortRates(){
		Collections.sort(rates, new Comparator<Rate>() {
			  @Override
			  public int compare(Rate o1, Rate o2) {
			    return o1.Code.compareTo(o2.Code);
			  }
			});
		Collections.sort(claRates, new Comparator<String>() {
			  @Override
			  public int compare(String o1, String o2) {
			    return o1.compareTo(o2);
			  }
			});
	}
	
	 public Date getDateRate(){
		Date res = new Date();		
		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy", new Locale("ru"));
        try {
         	res = format.parse(sDateRate);
        } catch (Exception exception) {
        	Log.e(TAG, "Получено исключение в getDateRate()", exception);
        }
 	     return res;
 	  }
 	
	  public void setDateRate(String sStr) {
		  sDateRate = sStr;
		  setTitle(sDateRate);
	  }
	
	  public void LoadRatePrev() {
		if (rates.size()==0) return;
		
		mtP = new CurrSendPost(null, null, aResult);			
		mtP.isRatePrev = true;
        
        Calendar c = Calendar.getInstance();
        c.setTime(getDateRate());
		c.add(Calendar.DATE, -1);
		String sDate = new SimpleDateFormat("dd.MM.yyyy", new Locale("ru")).format(c.getTime());
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("date_req", sDate)); 
		mtP.mActivity = this; 			
		mtP.mProgressBar = mProgressBar;
		mtP.Url = "http://www.cbr.ru/scripts/XML_daily.asp";   
		mtP.Params = nameValuePairs;
		mtP.execute(mtP.Url);
		mtP.showProgress(true);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true; 
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
	    switch (item.getItemId()) {
		    case R.id.action_ratedate:	
		        Calendar c = Calendar.getInstance(); 
		        c.setTime(getDateRate());
				mYear =  c.get(Calendar.YEAR); 
				mMonth = c.get(Calendar.MONTH);   
				mDay = c.get(Calendar.DAY_OF_MONTH); 
				DatePickerDialog tpd = new DatePickerDialog(this, myCallBackDateDialog, mYear, mMonth, mDay);
				tpd.show();
		        return true;
		    case R.id.action_refresh:
		    	RefreshRate();
		    	return true;		        
		    case R.id.action_ratedate_last:
		    	LastRate();
		    	return true;
		    case R.id.action_profile:
		    	LoadProfiles();
		    	boolean fl = false;
		    	String ch;
		    	String sLastProfile = getLastProfile();
		    	menuProfile_item = item;		    	
		    	
		    	for (int x = 0; x < Profiles.size(); x++){
		    		ch = Profiles.get(x).getName();
		    		if (ch.length() == 0) continue;
		    		fl = false;
	    			for (int i=0; i<menuProfile_item.getSubMenu().size(); i++){
		    			MenuItem mi = item.getSubMenu().getItem(i);
		    			if (mi.getTitle().equals(getString(R.string.mnuAllVal)))
		    				menuAllVal_item = mi;
		    			if (mi.getTitle().equals(getString(R.string.mnuDel)))
		    				menuDelete_item = mi;

		    				
		    				
		    			if (mi.getTitle().equals(ch)) {
		    				fl = true;
		    				break;
		    			}
		    		}
		    		if (!fl) {
		    			item.getSubMenu().add(GROUP_PROFILE, ch.hashCode(), 10 + x, ch);
		    		}
		    	}
		    	setCheckProfile(sLastProfile);

		    	menuDelete_item.setEnabled(!menuAllVal_item.isChecked());
		    	
		    	return true;
		    case R.id.action_saveprof:
		    	SaveProfile();
		    	return true;
		    case R.id.action_delprof:
		    	delCheckedPropfile();
		    	return true;
		    	
		    default: 
		    	break;
	    }
	    
	    if (    (item.getGroupId() == GROUP_PROFILE)
	    	    || (item.getItemId() == R.id.action_allval)   ) {
	    	setLastProfile((String) item.getTitle());
	    	setCheckProfile(item);
		    if (item.getItemId() == R.id.action_allval) {
		    	LastRate();
		    	return true;
		    }
		    RefreshRate();
	    }
	    return false;
	}

	void setCheckProfile(MenuItem item){
		if (menuProfile_item==null) return;
		boolean fl;
		for (int i=0; i<menuProfile_item.getSubMenu().size(); i++){
			MenuItem mi = menuProfile_item.getSubMenu().getItem(i);
			fl = mi.equals(item);
			mi.setCheckable(fl);
			mi.setChecked(fl);
		}
	}

	void setCheckProfile(String aProfileName){
		if (menuProfile_item==null) return;
		boolean fl;
		for (int i=0; i<menuProfile_item.getSubMenu().size(); i++){
			MenuItem mi = menuProfile_item.getSubMenu().getItem(i);
			String mt = (String) mi.getTitle();
			fl = mt.equals(aProfileName);
			mi.setCheckable(fl);
			mi.setChecked(fl);
		}
	}
	
	void delCheckedPropfile(){
		for (int i=0; i<menuProfile_item.getSubMenu().size(); i++){
			MenuItem mi = menuProfile_item.getSubMenu().getItem(i);
			if (mi.isChecked()) {
				delProfile(mi.getTitle().toString());
				menuProfile_item.getSubMenu().removeItem(mi.getItemId());
				break;
			}
				
		}
	}
	
	private void SaveProfile() {
	    AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    View linearlayout = getLayoutInflater().inflate(R.layout.dialog_saveprofile, null);
	    final EditText txtProfile = (EditText) linearlayout.findViewById(R.id.textstring);
	    String sStr = getLastProfile();
	    if (sStr == getString(R.string.mnuAllVal)) 
	    	sStr = "";
	    txtProfile.setText(sStr);
	    builder.setView(linearlayout)
	    	   .setTitle(R.string.sTitleSaveProfile)
    	       .setIcon(R.drawable.ic_android)
	           .setPositiveButton(R.string.sOk, new DialogInterface.OnClickListener() {
	               @Override
	               public void onClick(DialogInterface dialog, int id) {
	            	   String sStr = txtProfile.getText().toString() + "";
	            	   if (sStr.equals(getString(R.string.mnuAllVal)))
	            			   return;
	            	   addProfile2List(sStr);
	            	   addProfile(sStr);
	            	   if (getMenuIdProfile(sStr) == -1)
	            		   menuProfile_item.getSubMenu().add(GROUP_PROFILE, sStr.hashCode(), 20, sStr);
	            	   setLastProfile(sStr);
	               }
	           })
	           .setNegativeButton(R.string.sCancel, new DialogInterface.OnClickListener() {
	        	   @Override
	               public void onClick(DialogInterface dialog, int id) {
	                   dialog.cancel();
	               }
	           });      
	   builder.show();
	}
	
	private int getMenuIdProfile(String aProfileName) {
		
		for (int i=0; i<menuProfile_item.getSubMenu().size(); i++){
			MenuItem mi = menuProfile_item.getSubMenu().getItem(i);
			if (mi.getTitle().equals(aProfileName)) {
				return mi.getItemId();
			}
		}
		return -1;
	}

	
	private void addProfile(String sProfileName) {
    	String sStr = "";
    	for (int i=0; i<claRates.size(); i++){
    		String sCode = claRates.get(i);
    		int idx = getIndexInRates(sCode);
    		if (idx == -1) 
    				sStr = sStr + sCode + ";";
    	}
    	if (sStr.length() == 0) {
    		return; 
    	} else {
        	Editor editor = getSharedPreferences(av_APP_PREFERENCES, Context.MODE_PRIVATE).edit();    		
	    	editor.putString(sProfileName, ";"+ sStr);
	    	editor.apply();    	
    	}
	}
	
	private void addProfile2List(String sProfileName) {
		String lp = getSharedPreferences(av_APP_PREFERENCES, Context.MODE_PRIVATE).getString(av_PROFILES_LIST, "");
		if (!lp.contains(";" + sProfileName + ";")){
	    	Editor editor = getSharedPreferences(av_APP_PREFERENCES, Context.MODE_PRIVATE).edit();
	    	editor.putString(av_PROFILES_LIST, lp + (lp.length()==0 ? ";" : "") + sProfileName + ";");
	    	editor.apply();    	
		}
	}

	private void delProfileInList(String sProfileName) {
		String lp = getSharedPreferences(av_APP_PREFERENCES, Context.MODE_PRIVATE).getString(av_PROFILES_LIST, "");
		String sStr = ";" + sProfileName + ";";
		if (lp.contains(sStr)){
			lp = lp.replace(sStr, ";");
	    	Editor editor = getSharedPreferences(av_APP_PREFERENCES, Context.MODE_PRIVATE).edit();
	    	editor.putString(av_PROFILES_LIST, lp);
	    	editor.apply();    	
		}
	}
	
	private int getIndexInRates(String aCode) {
		int res = -1;
    	for (int i=0; i<rates.size(); i++){
    		String sCode = ((Rate) rates.get(i)).Code;
    		if (sCode.contains(aCode)) 
    			return i;
    	}
		return res;
	}
	
	private void delProfile(String aProfileName) {
		Editor editor = getSharedPreferences(av_APP_PREFERENCES, Context.MODE_PRIVATE).edit();    		
		editor.remove(aProfileName);
		delProfileInList(aProfileName);
		editor.apply();    	
	}
		
	private void LoadProfiles(){
		String lp = getSharedPreferences(av_APP_PREFERENCES, Context.MODE_PRIVATE).getString(av_PROFILES_LIST, "");
		String sStr;
		String[] as = lp.split(";");
		Profiles = new ArrayList<NameValuePair>(as.length);
		Profiles.clear();
		for (int i=0; i < as.length; i++){
			if (as[i].length()==0) continue;
			sStr = getSharedPreferences(av_APP_PREFERENCES, Context.MODE_PRIVATE).getString(as[i], "");
			if (sStr.length()==0) continue;
			Profiles.add(new BasicNameValuePair(as[i],sStr));	
		}
	}
	
	OnDateSetListener myCallBackDateDialog = new OnDateSetListener() {
      public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
    	  	mYear = year;
        	mMonth = monthOfYear + 1;
        	mDay = dayOfMonth;
        	String sStr = (mDay < 10 ? "0" : "") + mDay + "." + (mMonth < 10 ? "0" : "") + mMonth + "." + mYear;
        	setDateRate(sStr);
        	rates.clear();
        	claRates.clear();
        	setTitle(sStr);
        	LoadRate(getDateRate());
      	}
	  };
	  
//==============================================================================================================	  
//==============================================================================================================	
//==============================================================================================================	
	public class CurrSendPost extends SendPost {
		
		String sRateList = "";
		boolean isRatePrev = false;
		
		public CurrSendPost(String Url, List<NameValuePair> Params, Boolean Result) {
			super(Url, Params, Result);
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			super.onPostExecute(success);	
			String sMsg = "Сервер ЦБ, подлец, не вернул данные!!!!\n";
			sRateList = sMessage;

			showProgress(true);
			if (success) {
				String tmpCharCode, tmpValue, tmpNominal, sCourTag, tmpName, tmpID, tmpNumCode;				
				tmpCharCode = tmpValue = tmpNominal = sCourTag = tmpName = tmpID = tmpNumCode = "";
				try {
			      XmlPullParser xpp = prepareXpp(sRateList);
			      String sValNotShowList = getValNotShowList(getLastProfile());
			      setCheckProfile(getLastProfile());
			      while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) { // 1

			        switch (xpp.getEventType()) {
				        // начало тэга
			        	case XmlPullParser.START_TAG: // 2
				          
				          sCourTag = xpp.getName();
				          if (sCourTag.equals("ValCurs") & !isRatePrev) {
					          for (int i = 0; i < xpp.getAttributeCount(); i++) {
						            if (xpp.getAttributeName(i).equals("Date")){
						            	setDateRate(xpp.getAttributeValue(i));
						            }
						          }
				          } else if (sCourTag.equals("Valute")) {
					          for (int i = 0; i < xpp.getAttributeCount(); i++) {
						            if (xpp.getAttributeName(i).equals("ID"))
						            	tmpID = xpp.getAttributeValue(i);
						          }
				          } 
			          
				          break;
				          
				        // конец тэга
				        case XmlPullParser.END_TAG: // 3
				          sCourTag = "";
				          break;
				          
				        // содержимое тэга
				        case XmlPullParser.TEXT: // 4
				          if (sCourTag.equals("CharCode")) {
				        	  tmpCharCode = xpp.getText();
				          } else if (sCourTag.equals("Value")) {
				        	  tmpValue = xpp.getText();
				          } else if (sCourTag.equals("Nominal")){
				        	  tmpNominal = xpp.getText();
				          } else if (sCourTag.equals("Name")){
				        	  tmpName = xpp.getText();
				          } else if (sCourTag.equals("NumCode")){
				        	  tmpNumCode = xpp.getText();
				          } 				          
				          break;
				          
				        default:
				          break;
			        } // case
			        
			        if (tmpCharCode.length() != 0 && tmpValue.length() != 0 && tmpNominal.length() != 0 && tmpName.length() != 0
			        	&& tmpID.length() != 0 && tmpNumCode.length() != 0) {
			        	
			        	double ValRate = Double.valueOf(tmpValue.trim().replace(',','.')).doubleValue();
			        	
			        	if (!isRatePrev) 
			        		claRates.add(tmpCharCode);
			        	
			        	if (sValNotShowList.length()==0 || !sValNotShowList.contains(";" + tmpCharCode + ";")) {
				        	if (!isRatePrev) {
					        	Rate rate = new Rate(tmpCharCode, tmpNominal, tmpName);
					        	rate.ID = tmpID;
					        	rate.Rate = ValRate; 
					        	rate.NumCode = tmpNumCode;
					        	rates.add(mAdapter.getCount(), rate);
				        	} else {
				        		int pos = getPosById(tmpID);
				        		if (pos >= 0) {
				        			Rate rate = rates.get(pos);
				        			rate.RatePrev = ValRate;
				        			rate.isPrevLoaded = true;
				        		}
				        	}
				        					        	
			        	}
			        	tmpCharCode = tmpValue = tmpNominal = sCourTag = tmpName = tmpID = tmpNumCode = "";
			        }
		        	xpp.next();// следующий элемент
			    } // while
			} catch (Exception exception) {
				Log.e(TAG, "Получено исключение", exception);
	    	}			      
			if (!isRatePrev) {
				SortRates();
				LoadRatePrev();
			} else{  
				setTitle(sDateRate);
				lvMain.setAdapter(mAdapter);
			}
			
			} else { // success
				sMsg = sMsg + sRateList;
				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
				builder.setTitle("Ошибка");
				builder.setMessage(sMsg);
				builder.setCancelable(true);
				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() { // Кнопка ОК
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				    	dialog.cancel();
				    }
				});
				ShowNotOnline(sMsg);			
				AlertDialog dialog = builder.create();
				dialog.show();
			}
			mAdapter.notifyDataSetChanged();
			showProgress(false);
		}
			
	    public int getPosById(String aID){
	    	int res = -1;
	        for (int i = 0; i < rates.size() ; i++) {
	        	Rate rate = rates.get(i); 
	        	if (rate.ID.contains(aID)){
	        		res = i;
	        		break;
	        	}
	        }
			return res;    	
	    }
			
			
		@Override
	    protected void onPreExecute() {
		    super.onPreExecute();
		    showProgress(true);
		}
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			mt = null;
		}
		
		@Override
		public void showProgress(final boolean show) {
			super.showProgress(show);
			lblTitle.setVisibility((show ?  View.INVISIBLE : View.VISIBLE));
		}
		
		
	} // class post
//==============================================================================================================	  
//==============================================================================================================	
//==============================================================================================================	

    /**
     * Handle touch events to fade/move dragged items as they are swiped out
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        float mDownX;
        private int mSwipeSlop = -1;
       
        @Override
        public boolean onTouch(final View v, MotionEvent event) {
            if (mSwipeSlop < 0) {
                mSwipeSlop = ViewConfiguration.get(MainActivity.this).getScaledTouchSlop();
            }
            switch (event.getAction()) {
	            case MotionEvent.ACTION_DOWN:
	                if (mItemPressed) {
	                    // Multi-item swipes not handled
	                    return false;
	                }
	                mItemPressed = true;
	                mDownX = event.getX();
	                break;
	            case MotionEvent.ACTION_CANCEL:
	                v.setAlpha(1);
	                v.setTranslationX(0);
	                mItemPressed = false;
	                break;
	            case MotionEvent.ACTION_MOVE:
	                {
	                    float x = event.getX() + v.getTranslationX();
	                    float deltaX = x - mDownX;
	                    float deltaXAbs = Math.abs(deltaX);
	                    
	                    if (!mSwiping) {
	                        if (deltaXAbs > mSwipeSlop) {
	                            mSwiping = true;
	                            lvMain.requestDisallowInterceptTouchEvent(true);
	                            mBackgroundContainer.showBackground(v.getTop(), v.getHeight());
	                        }
	                    }
	                    if (mSwiping) {
	                        v.setTranslationX((x - mDownX));
	                        v.setAlpha(1 - deltaXAbs / v.getWidth());
	                    }
	                }
	                break;
	            case MotionEvent.ACTION_UP:
	                {
	                    // User let go - figure out whether to animate the view out, or back into place
	                    if (mSwiping) {
	                        float x = event.getX() + v.getTranslationX();
	                        float deltaX = x - mDownX;
	                        float deltaXAbs = Math.abs(deltaX);
	                        float fractionCovered;
	                        float endX;
	                        float endAlpha;
	                        final boolean remove;
	                        if (deltaXAbs > v.getWidth() / 4) {
	                            // Greater than a quarter of the width - animate it out
	                            fractionCovered = deltaXAbs / v.getWidth();
	                            endX = deltaX < 0 ? -v.getWidth() : v.getWidth();
	                            endAlpha = 0;
	                            remove = true;
	                        } else {
	                            // Not far enough - animate it back
	                            fractionCovered = 1 - (deltaXAbs / v.getWidth());
	                            endX = 0;
	                            endAlpha = 1;
	                            remove = false;
	                        }
	                        // Animate position and alpha of swiped item
	                        // NOTE: This is a simplified version of swipe behavior, for the
	                        // purposes of this demo about animation. A real version should use
	                        // velocity (via the VelocityTracker class) to send the item off or
	                        // back at an appropriate speed.
	                        long duration = (int) ((1 - fractionCovered) * SWIPE_DURATION);
	                        lvMain.setEnabled(false);
	                        v.animate().setDuration(duration).
	                                alpha(endAlpha).translationX(endX).
	                                withEndAction(new Runnable() {
	                                    @Override
	                                    public void run() {
	                                        // Restore animated values
	                                        v.setAlpha(1);
	                                        v.setTranslationX(0);
	                                        if (remove) {
	                                            animateRemoval(lvMain, v);
	                                        } else {
	                                            mBackgroundContainer.hideBackground();
	                                            mSwiping = false;
	                                            lvMain.setEnabled(true);
	                                        }
	                                    }
	                                });
	                    } else {
	                    	// кликнули
	                    	int position = lvMain.getPositionForView(v);
	        			    Rate val = (Rate) lvMain.getItemAtPosition(position);
	        			    ShowToast(val);
	                    }
	                } // case MotionEvent.ACTION_UP
	                mItemPressed = false;
	                break;
	            default: 
	                return false;
            }
            return true;
        }
    };	

    /**
     * This method animates all other views in the ListView container (not including ignoreView)
     * into their final positions. It is called after ignoreView has been removed from the
     * adapter, but before layout has been run. The approach here is to figure out where
     * everything is now, then allow layout to run, then figure out where everything is after
     * layout, and then to run animations between all of those start/end positions.
     */
    private void animateRemoval(final ListView listview, View viewToRemove) {
        // Delete the item from the adapter
        int position = lvMain.getPositionForView(viewToRemove);
        
        mAdapter.remove(mAdapter.getItem(position));
        
        final ViewTreeObserver observer = listview.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            public boolean onPreDraw() {
                observer.removeOnPreDrawListener(this);
                boolean firstAnimation = true;
                int firstVisiblePosition = listview.getFirstVisiblePosition();
                for (int i = 0; i < listview.getChildCount(); ++i) {
                    final View child = listview.getChildAt(i);
                    int position = firstVisiblePosition + i;
                    Integer startTop = position;
                    int top = child.getTop();
                    if (startTop != null) {
                        if (startTop != top) {
                            int delta = startTop - top;
                            child.setTranslationY(delta);
                            child.animate().setDuration(MOVE_DURATION).translationY(0);
                            if (firstAnimation) {
                                child.animate().withEndAction(new Runnable() {
                                    public void run() {
                                        mBackgroundContainer.hideBackground();
                                        mSwiping = false;
                                        lvMain.setEnabled(true);
                                    }
                                });
                                firstAnimation = false;
                            }
                        }
                    }
                }
                return true;
            }
        });
    }
	
    private String getValNotShowList(String aProfileName){
    	if (aProfileName.length()==0) return "";
    	return getSharedPreferences(av_APP_PREFERENCES, Context.MODE_PRIVATE).getString(aProfileName, "");
    }

    private String getLastProfile(){
    	return getSharedPreferences(av_APP_PREFERENCES, Context.MODE_PRIVATE).getString(av_LAST_PROFILE, getString(R.string.mnuAllVal));
    }
    
    private void setLastProfile(String aProfileName) {
    	SharedPreferences mSettings = getSharedPreferences(av_APP_PREFERENCES, Context.MODE_PRIVATE);
    	Editor editor = mSettings.edit();
    	editor.putString(av_LAST_PROFILE, aProfileName);
    	editor.apply();    	   
    }
    
    
}  // main class
