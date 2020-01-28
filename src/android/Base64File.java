package com.spatialdatalogic.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;

import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;
import android.util.Base64;
import android.util.Log;
import android.os.Environment;



public class Base64File extends CordovaPlugin {

    private enum TRAVEL_MODE {
        driving,
        transit,
        bicycling,
        walking
    }


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (action.equals("save")) {
            String b64String = args.getString(0);
            String filename = args.getString(1);
            String folder = args.getString(2);

            callbackContext.sendPluginResult(this.saveFile(b64String, filename, folder));
            return true;


        } else if (action.equals("load")) {

            callbackContext.sendPluginResult(this.loadFile(args.getString(0)));
            return true;

        } else if (action.equals("open")) {
            this.openFile(args.getString(0), args.getString(1), callbackContext);
            return true;

        } else if (action.equals("launchNavigation")) {
            this.launchNavigation(args, callbackContext);
            return true;
        } else if (action.equals("watermarkImage")) {
            callbackContext.sendPluginResult(this.watermarkImage(args));
            return true;
        } else {
            
            return false;

        }
    }





    private PluginResult saveFile(String b64String, String fileName, String dirName) throws JSONException {

        try {
            if (dirName.equals("")) {
                dirName = this.getDownloadDir();
            } else {
                File dir = new File(dirName);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
            }
            Log.i("Base64File", "dirName: " + dirName);


            File file = new File(dirName, fileName);
            Log.i("Base64File", fileName + " length: " + file.length());



            //Decode Base64 back to Binary format
            byte[] decodedBytes = Base64.decode(b64String.getBytes(), Base64.DEFAULT);

            //Save Binary file to phone
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            fOut.write(decodedBytes);
            fOut.flush();
            fOut.close();
            Log.i("Base64File", fileName + " length after: " + file.length() + " exists: " + file.exists());



            return new PluginResult(PluginResult.Status.OK, file.toString());

        } catch (FileNotFoundException e) {
            return new PluginResult(PluginResult.Status.ERROR, "File not Found!");
        } catch (IOException e) {
            return new PluginResult(PluginResult.Status.ERROR, e.getMessage());
        }

    }

    private PluginResult watermarkImage(JSONArray args) throws JSONException {

        try {
            JSONObject params = args.getJSONObject(0);
            String fileName = params.getString("fileName");
            String text = params.getString("text");
            String textColor = params.getString("textColor");
            int textSize = params.getInt("textSize");


            File imageFile = new File(fileName);
            Log.i("Base64File.watermarkImage", fileName + " length: " + imageFile.length());


            Bitmap image = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            int w = image.getWidth();
            int h = image.getHeight();
            Bitmap result = Bitmap.createBitmap(w, h, image.getConfig());
            Canvas canvas = new Canvas(result);
            canvas.drawBitmap(image, 0, 0, null);

            Paint paint = new Paint();
            paint.setColor(Color.parseColor(textColor));
            //paint.setAlpha(alpha);
            paint.setTextSize(textSize);
            paint.setAntiAlias(true);
            //paint.setUnderlineText(underline);
            canvas.drawText(text, 10, h-15, paint);




            FileOutputStream out = new FileOutputStream(imageFile);
            result.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();



            return new PluginResult(PluginResult.Status.OK, imageFile.toString());

        } catch (FileNotFoundException e) {
            return new PluginResult(PluginResult.Status.ERROR, "File not Found!");
        } catch (IOException e) {
            return new PluginResult(PluginResult.Status.ERROR, e.getMessage());
        }

    }


    private PluginResult loadFile(String fileName) throws JSONException {

        try {

            File file = new File(fileName);
            int size = (int) file.length();
            byte[] bytes = new byte[size];

            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();

            String b64String = Base64.encodeToString(bytes, Base64.DEFAULT);



            return new PluginResult(PluginResult.Status.OK, b64String);

        } catch (FileNotFoundException e) {
            return new PluginResult(PluginResult.Status.ERROR, "File not Found!");
        } catch (IOException e) {
            return new PluginResult(PluginResult.Status.ERROR, e.getMessage());
        }

    }

	private void openFile(String fileName, String contentType, CallbackContext callbackContext) throws JSONException {

		//fileName = this.stripFileProtocol(fileUri.toString());



		File file = new File(fileName);
		if (file.exists()) {
			try {
				Uri path = Uri.parse("file://" + fileName);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(path, contentType);
				intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				/*
				 * @see
				 * http://stackoverflow.com/questions/14321376/open-an-activity-from-a-cordovaplugin
				 */
				cordova.getActivity().startActivity(intent);
				//cordova.getActivity().startActivity(Intent.createChooser(intent,"Open File in..."));
				callbackContext.success();
			} catch (android.content.ActivityNotFoundException e) {
				JSONObject errorObj = new JSONObject();
				errorObj.put("status", PluginResult.Status.ERROR.ordinal());
				errorObj.put("message", "Activity not found: " + e.getMessage());
				callbackContext.error(errorObj);
			}
		} else {
			JSONObject errorObj = new JSONObject();
			errorObj.put("status", PluginResult.Status.ERROR.ordinal());
			errorObj.put("message", "File not found");
			callbackContext.error(errorObj);
		}
	}





    @SuppressWarnings("unused")
    private void launchNavigation(JSONArray args, CallbackContext callbackContext) throws JSONException {
        JSONObject params = args.getJSONObject(0);
        String from = params.getString("from");
        String to = params.getString("to");

        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
               .authority("maps.google.com")
               .appendPath("maps")
               .appendQueryParameter("saddr", from)
               .appendQueryParameter("daddr", to);

        if (params.has("travelMode")) {
          TRAVEL_MODE mode = null;
          try {
            mode = TRAVEL_MODE.valueOf(params.getString("travelMode"));
          } catch (Exception e){}
          //travel mode
          if (mode != null) {
            String dirFlag = "d";
            switch (mode) {
            case walking:
              dirFlag="w";
              break;

            case transit:
              dirFlag="r";
              break;

            case bicycling:
              dirFlag="b";
              break;
            default:
              break;

            }
            builder.appendQueryParameter("dirflg", dirFlag);
          }
        }

        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, builder.build());
        this.cordova.getActivity().startActivity(intent);

        PluginResult result = new PluginResult(PluginResult.Status.NO_RESULT);
        callbackContext.sendPluginResult(result);
    }


    private String getDownloadDir() throws IOException {
        // better check, otherwise it may crash the app
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
          // we need to use external storage since we need to share to another app
          final String dir = webView.getContext().getExternalFilesDir(null) + "/sdlmobile-downloads";
          this.createOrCleanDir(dir);
          return dir;
        } else {
          return null;
        }
    }

    private void createOrCleanDir(final String downloadDir) throws IOException {
        final File dir = new File(downloadDir);
        if (!dir.exists()) {
          if (!dir.mkdirs()) {
            throw new IOException("CREATE_DIRS_FAILED");
          }
        } else {
          //cleanupOldFiles(dir);
        }
    }

    private void cleanupOldFiles(File dir) {
        for (File f : dir.listFiles()) {
          //noinspection ResultOfMethodCallIgnored
          f.delete();
        }
    }


	private String stripFileProtocol(String uriString) {
		if (uriString.startsWith("file://")) {
			uriString = uriString.substring(7);
		}
		return uriString;
	}


}
