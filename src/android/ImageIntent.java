/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package com.bourgein.cordova.imageintent;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.UUID;
import org.apache.cordova.CordovaActivity;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.database.Cursor;

public class ImageIntent extends CordovaPlugin {
    public static final String TAG = "FileOpen";
	public Context context;


    /**
     * Constructor.
     */
    public ImageIntent() {
		
    }

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        Log.e("JEM","action: " + action);
		context = cordova.getActivity().getApplicationContext();
		if (action.equals("getExtra")) {
			Intent intent = ((CordovaActivity)this.cordova.getActivity()).getIntent();
            String intentAction = intent.getAction();
			String type = intent.getType();
			
			if (Intent.ACTION_SEND.equals(intentAction) && type != null) {
				
				if (type.startsWith("image/")) {
					Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
					String destinationFilename = context.getExternalFilesDir(null).getParentFile().getPath()+File.separatorChar+UUID.randomUUID().toString()+".jpg";
					Log.e("JEM","destinationFilename: " + destinationFilename);
					if (imageUri != null) {
						if(copyFileToAppStorage(imageUri,destinationFilename)){
							callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, destinationFilename));
							return true;
						}
						else{
							return false;
						}						
					}
					else{
						return false;
					}				
				}
			} 
			else if (Intent.ACTION_SEND_MULTIPLE.equals(intentAction) && type != null) {
				if (type.startsWith("image/")) {
					ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
					
					if (imageUris != null) {
						ArrayList<String> picList = new ArrayList<String>();
						for(Uri uri : imageUris){
							String destinationFilename = context.getExternalFilesDir(null).getParentFile().getPath()+File.separatorChar+UUID.randomUUID().toString()+".jpg";
							if(copyFileToAppStorage(uri,destinationFilename)){
								picList.add(destinationFilename);
							}
							else{
								return false;
							}
						}
						if(picList.size() > 0){
							JSONArray picturesJson = new JSONArray(picList);
							callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, picturesJson));
							return true;					
						}
						else{
							return false;
						}
					}
				}
			} 
		}
		return false;
        
    }

	public boolean copyFileToAppStorage(Uri imageUri, String destinationFilePath){
		String sourceFile = imageUri.getPath();
		Log.e("JEM","sourceFile : " + sourceFile);
		Log.e("JEM","destinationFilePath: " + destinationFilePath);
		
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		
		try {
			bis = new BufferedInputStream(context.getContentResolver().openInputStream(imageUri));
			bos = new BufferedOutputStream(new FileOutputStream(destinationFilePath, false));
			byte[] buf = new byte[5120];
			bis.read(buf);
			do {
				bos.write(buf);
			} while(bis.read(buf) != -1);
		}
		catch (IOException e) {
			Log.e("JEM","IO ERROR: " + e.getMessage());	
			return false;
		} 
		finally {
			try {
				if (bis != null) bis.close();
				if (bos != null) bos.close();
			} 
			catch (IOException e) {
				Log.e("JEM","IO ERROR closing streams: " + e.getMessage());	
				return false;
			}
		}
		return true;
	}
	
	
	/*public String getRealPathFromURI(Context context, Uri contentUri) {
		Cursor cursor = null;
		try { 
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
			int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} 
		finally {
			if (cursor != null) {
				cursor.close();
			}
		}	
	}*/
}

