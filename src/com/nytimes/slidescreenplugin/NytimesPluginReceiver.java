package com.nytimes.slidescreenplugin;

import android.content.ComponentName;
import android.content.Intent;
import android.graphics.*;
import android.net.Uri;
import android.util.Log;
import com.larvalabs.slidescreen.PluginReceiver;

/**
 * @author John Watkinson
 */
public class NytimesSlideScreenPluginReceiver extends PluginReceiver {
    private static final String TAG = NytimesSlideScreenPluginReceiver.class.getSimpleName();

    @Override
    public int getColor() {
        return Color.rgb(255, 255, 255);
    }

    @Override
    public Uri getContentProviderURI() {
        return NytimesSlideScreenPluginReceiver.CONTENT_URI;
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
        Intent groupIntent = new Intent(Intent.ACTION_MAIN);
        groupIntent.setComponent(new ComponentName("com.nytimes"));
        intents[0] = groupIntent;
        intents[1] = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.nytimes.com"));
        return intents;
    }

    // fix to link to NYT Android app
    @Override
    public Intent[] getLongpressShortcutIntents() {
        // Note this is the same as the short press intents right now, will hopefully replace later
        Intent[] intents = new Intent[2];
        Intent groupIntent = new Intent(Intent.ACTION_MAIN);
        groupIntent.setComponent(new ComponentName("com.nytimes"));
        intents[0] = groupIntent;
        intents[1] = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.nytimes.com"));
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
