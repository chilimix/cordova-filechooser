package com.megster.cordova;
import android.app.Activity;
import android.content.Intent;
import android.content.Context;
import android.content.ContentResolver;
import android.net.Uri;
import android.util.Log;

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

    private String resolveNativePath(Uri uri) throws JSONException {
        Context appContext = this.cordova.getActivity().getApplicationContext();
        return "file://" + getPath(appContext, uri);
    }

    private static String getPath(final Context context, final Uri uri) {
        //final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
        // if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
        //     if (isMediaDocument(uri)) {
        //         final String docId = DocumentsContract.getDocumentId(uri);
        //         final String[] split = docId.split(":");
        //         final String type = split[0];
        //         Uri contentUri = null;
        //         if ("image".equals(type)) {
        //             contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        //         } else if ("video".equals(type)) {
        //             contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        //         } else if ("audio".equals(type)) {
        //             contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        //         }
        //         final String selection = "_id=?";
        //         final String[] selectionArgs = new String[] { split[1] };
        //         return getDataColumn(context, contentUri, selection, selectionArgs);
        //     }
        // }
        // else 
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = { column };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
}
