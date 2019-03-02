package com.megster.cordova;
import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONException;

public class FileChooser extends CordovaPlugin {

    private static final String TAG = "FileChooser";
    private static final String ACTION_OPEN = "open";
    private static final int PICK_FILE_REQUEST = 1;
    CallbackContext callback;

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {

        if (action.equals(ACTION_OPEN)) {
            chooseFile(callbackContext);
            return true;
        }

        return false;
    }

    public void chooseFile(CallbackContext callbackContext) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        Intent chooser = Intent.createChooser(intent, "Select Files");
        cordova.startActivityForResult(this, chooser, PICK_FILE_REQUEST);

        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callback = callbackContext;
        callbackContext.sendPluginResult(pluginResult);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FILE_REQUEST && callback != null) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                   String uris = "";
                   if (data.getClipData() != null) {
                      for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                         uris += resolveNativePath(data.getClipData().getItemAt(i).getUri()) + ", ";
                      }
                   } else {
                      uris = resolveNativePath(data.getData());
                   }
                   callback.success(uris);
                } else {
                    callback.error(resultCode);
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                callback.sendPluginResult(pluginResult);
            } else {
                callback.error(resultCode);
            }
        }
    }

    private String resolveNativePath(Uri uri) {
        Context appContext = this.cordova.getActivity().getApplicationContext();
        return "file://" + this.getPath(appContext, uri);
    }

    private static String getPath(final Context context, final Uri uri) { 
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        final String column = "_data";
        final String[] projection = { column };
        final Cursor cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
        if (cursor != null && cursor.moveToFirst()) {
            final int column_index = cursor.getColumnIndexOrThrow(column);
            return cursor.getString(column_index);
        }
        return null;
    }
}
