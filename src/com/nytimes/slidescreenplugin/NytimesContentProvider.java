package com.nytimes.slidescreen;

import android.content.*;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import com.larvalabs.slidescreen.PluginUtils;

import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Xml;

import org.developerworks.android.Message;
import org.developerworks.android.MessageList;
import org.developerworks.android.AndroidSaxFeedParser;

import java.io.IOException;
import java.util.ArrayList;

import static com.larvalabs.slidescreen.PluginConstants.*;

public class NytimesContentProvider extends ContentProvider {

    private static final String TAG = NytimesContentProvider.class.getName();

    public static final Uri CONTENT_URI = Uri.parse("content://com.nytimes.slidescreen");

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

        try {
            // get the feed from NYT
            parser = new AndroidSaxFeedParser("http://feeds.nytimes.com/nyt/rss/HomePage");
            MessageList messages = parser.parse()
            
            MatrixCursor cursor = new MatrixCursor(fields);
            for (int i = 0; i < messages.length(); i++) {
              Message message = messages[i]
                    MatrixCursor.RowBuilder builder = cursor.newRow();
                    for (String field : fields) {
                        if (FIELD_ID.equals(field)) {
                            builder.add(""+message.getGuid());
                        } else if (FIELD_TITLE.equals(field)) {
                            builder.add(""+message.getTitle());
                        } else if (FIELD_LABEL.equals(field)) {
                            builder.add("");
                        } else if (FIELD_TEXT.equals(field)) {
                            builder.add(""+message.getDescription());
                        } else if (FIELD_DATE.equals(field)) {
                            builder.add(0);  //FIXME
                        } else if (FIELD_PRIORITY.equals(field)) {
                            builder.add(0); //FIXME
                        } else if (FIELD_INTENT.equals(field)) {
                          Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(message.getLink()));
                          builder.add(PluginUtils.encodeIntents(intent));
                        } else {
                            builder.add("");
                        }
                    }
            }
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
            return cursor;


            /*
//            Log.d(TAG, "  Inbox: " + inbox);
            String jsonRep = ParsingUtil.removeUninterestingParts(inbox, SMSParser.FilterResponse.JSON_BEGIN, SMSParser.FilterResponse.JSON_END, false);
//            Log.d(TAG, "  JSON Rep: " + jsonRep);
            JSONObject jsonTop = new JSONObject(jsonRep);
            Log.d(TAG, "Created json object.");
            JSONArray jsonArray = jsonTop.getJSONArray("messages");
            Log.d(TAG, "  Messages array: " + jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject message = jsonArray.getJSONObject(i);
                Log.d(TAG, "    " + message.getString("displayNumber") + ": " + message.getBoolean("isRead"));
            }
            */
        } catch (IOException e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        } catch (SAXException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return null;

        /*
        MatrixCursor cursor = new MatrixCursor(fields);
        for (int i = 1; i <= 2; i++) {
            MatrixCursor.RowBuilder builder = cursor.newRow();
            long time = Calendar.getInstance().getTime().getTime();
            for (String field : fields) {
                if (FIELD_ID.equals(field)) {
                    builder.add("" + i);
                } else if (FIELD_TITLE.equals(field)) {
                    builder.add("Title #" + i);
                } else if (FIELD_LABEL.equals(field)) {
                    builder.add("AWESOME");
                } else if (FIELD_TEXT.equals(field)) {
                    builder.add("Hello and welcome to item #" + i + ".");
                } else if (FIELD_DATE.equals(field)) {
                    builder.add(time - i);
                } else if (FIELD_PRIORITY.equals(field)) {
                    builder.add(time - i);
                } else {
                    builder.add("");
                }
            }
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
        */
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
