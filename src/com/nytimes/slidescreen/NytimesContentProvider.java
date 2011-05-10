package com.nytimes.slidescreen;

import android.content.*;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import com.larvalabs.slidescreen.PluginUtils;
import com.larvalabs.slidescreen.PluginConstants;
import android.os.Bundle;
import android.content.res.Resources;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import static com.larvalabs.slidescreen.PluginConstants.*;

public class NytimesContentProvider extends ContentProvider {

    private static final String TAG = NytimesContentProvider.class.getName();
    public static final String LOG_TAG = "SlideScreen-nytimes";
    
    public static final Uri CONTENT_URI = Uri.parse("content://com.nytimes.slidescreen");
    public static final String NYT_JSON_URL = "http://json8.nytimes.com/pages/index.json";

    public boolean onCreate() {
        Log.d(getClass().getName(), "* CREATED.");
        return true;
    }

    public Cursor query(Uri uri, String[] fields, String s, String[] strings1, String s1) {
        if (fields == null || fields.length == 0) {
            fields = FIELDS_ARRAY;
        }
        Log.d(TAG, "* QUERY Called.");
        for (String string : fields) {
            Log.d(TAG, "  ARG: " + string);
        }

        // get the feed from NYT
        DefaultHttpClient client = new DefaultHttpClient();
        HttpResponse httpResp = null;
        String response = null;
        boolean requestError = false;
        int statusCode = HttpStatus.SC_OK;
        
        try {
          HttpGet get = new HttpGet(NYT_JSON_URL);
          httpResp = client.execute(get);
          
          statusCode = httpResp.getStatusLine().getStatusCode();
           
          StringBuilder sb = new StringBuilder();
          BufferedReader r = new BufferedReader(new InputStreamReader(httpResp.getEntity().getContent()), 1000);
           
          
          //InputStream instream = getContext().getResources().openRawResource(R.raw.nytimestest);
          //BufferedReader r = new BufferedReader(new InputStreamReader(instream), 1000);

          for (String line = r.readLine(); line != null; line = r.readLine()) {
              sb.append(line);
          }
          
          response = sb.toString();

          if (statusCode != HttpStatus.SC_OK) {
            Log.e(LOG_TAG, "Status " + statusCode + " returned from HTTP request");
            requestError = true;
          }
        } catch (ClientProtocolException e) {
          Log.e(LOG_TAG, "Client protocol exception: " + e.toString());
          requestError = true;
        } catch (IOException e) {
          Log.e(LOG_TAG, "IO exception: " + e.toString());
          requestError = true;
        } finally {
          if (client != null) {
              client.getConnectionManager().shutdown();
          }
        }

        if (requestError) {
          return null;
        }
        
        Log.d(LOG_TAG, "FILE RETRIEVED");

        MatrixCursor cursor = new MatrixCursor(PluginConstants.FIELDS_ARRAY);
        Bundle extras;
        Intent intent;
        
        try {
          JSONObject obj = new JSONObject(response);
          JSONArray arr = obj.getJSONArray("items");
          int size = arr.length();
          
          Log.d(LOG_TAG, "Number of items: " + size);
              
          SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");
          format.setLenient(true);
                      
          for (int i = 0; i < size; i++) {
            JSONObject item = arr.getJSONObject(i);
            extras = new Bundle();
            intent = new Intent(Intent.ACTION_MAIN);
            intent.setComponent(new ComponentName("com.nytimes.slidescreen", 
                    "com.nytimes.slidescreen.DetailActivity"));
                
            Date published = format.parse(item.getString("pubdate"));
          
            /* build intent for details. */
            extras.putString("id", item.getString("guid"));
            extras.putString("title", item.getString("title"));
            extras.putString("description", item.getString("description"));
            extras.putLong("published", published.getTime());
            extras.putString("link", item.getString("link"));
            extras.putString("byline", item.getString("byline"));
            
            Log.d(LOG_TAG, "Title: " + item.getString("title"));
            Log.d(LOG_TAG, "Link: " + item.getString("link"));
            
            if (item.has("media")) {
              JSONArray photos = item.getJSONArray("media");
            
              int numPhotos = photos.length();
              ArrayList<String> images = new ArrayList<String>();
                  
              Log.d(LOG_TAG, "Number of photos: " + numPhotos);

              for (int j = 0; j < numPhotos; j++) {
                JSONObject photo = photos.getJSONObject(j);
                      
                if (photo.getString("type").equals("image")) {
                  images.add(photo.getJSONArray("media-metadata").getJSONObject(0).getString("url"));
                }
              }

              if (!images.isEmpty()) {
                String[] p = new String[images.size()];
                extras.putStringArray("photos", images.toArray(p));
              }
            }
                        
            intent.putExtras(extras);   
           
            MatrixCursor.RowBuilder builder = cursor.newRow();
            
            for (String field : fields) {
                if (FIELD_ID.equals(field)) {
                    builder.add("" + item.getString("guid"));
                } else if (FIELD_TITLE.equals(field)) {
                    builder.add("" + item.getString("title"));
                } else if (FIELD_LABEL.equals(field)) {
                    builder.add("");
                } else if (FIELD_TEXT.equals(field)) {
                    builder.add("" + item.getString("description"));
                } else if (FIELD_DATE.equals(field)) {
                    builder.add(published.getTime());
                } else if (FIELD_PRIORITY.equals(field)) {
                    builder.add(published.getTime());
                } else if (FIELD_INTENT.equals(field)) {
                    Intent intents[] = new Intent[1];
                    //FIXME: DETAIL intents[0] = intent;
                    intents[0] = new Intent(Intent.ACTION_VIEW, Uri.parse(item.getString("link")));
                    builder.add(PluginUtils.encodeIntents(intents));
                } else {
                    builder.add("");
                }
            }
            
            
          }
            /* TODO: add bundle ala Buzz for detail screen? */
        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSON Parse error: " + e.toString());
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Date Parse error: [" + e.toString() + "]");
        }
        
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return TYPE_ENTRY;
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    public void sendUpdatedNotification() {
        getContext().getContentResolver().notifyChange(CONTENT_URI, null);
    }
}
