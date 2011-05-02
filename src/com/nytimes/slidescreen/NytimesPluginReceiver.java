package com.nytimes.slidescreen;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;

import com.larvalabs.slidescreen.PluginReceiver;
import com.nytimes.slidescreen.R;

/**
 * @author Jacob Harris
 */
public class NytimesPluginReceiver extends PluginReceiver {
    private static final String TAG = NytimesPluginReceiver.class.getSimpleName();

    @Override
    public int getColor() {
        return Color.rgb(255, 255, 255);
    }

    @Override
    public Uri getContentProviderURI() {
        return NytimesContentProvider.CONTENT_URI;
    }

    @Override
    public String getName() {
        return "New York Times";
    }

    @Override
    public int getIconResourceId() {
        return R.raw.icon;
    }

    @Override
    public Intent[] getSingleTapShortcutIntents() {
        Intent[] intents = new Intent[2];
        
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setComponent(new ComponentName("com.nytimes", "com.nytimes.android"));
        intent.addCategory("android.intent.category.LAUNCHER");
        intents[0] = intent;
        
        intents[1] = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.nytimes.com"));
        return intents;
    }

    // fix to link to NYT Android app
    @Override
    public Intent[] getLongpressShortcutIntents() {
        // Note this is the same as the short press intents right now, will hopefully replace later
        Intent[] intents = new Intent[1];
//        Intent groupIntent = new Intent(Intent.ACTION_MAIN);
//        groupIntent.setComponent(new ComponentName("com.nytimes"));
//        intents[0] = groupIntent;
        intents[0] = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.nytimes.com"));
        return intents;
    }

    @Override
    public Intent getPreferenceActivityIntent() {
        Intent prefsIntent = new Intent(Intent.ACTION_MAIN);
        prefsIntent.setComponent(new ComponentName("com.nytimes.slidescreen", "com.nytimes.slidescreen.NytimesPluginPreferences"));
        return prefsIntent;
    }

    @Override
    public void markedAsRead(String itemId) {
        Log.d(TAG, "Recevied item marked as read: " + itemId);
    }
}
