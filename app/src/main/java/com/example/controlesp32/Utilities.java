package com.example.controlesp32;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.JsonReader;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Map;

public class Utilities {
    private Context context;
    private View view;

    public Utilities(Context context, View view) {
        this.context = context;
        this.view = view;
    }

    public float getPXfromDP(float dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.getResources().getDisplayMetrics()
        );
    }

    public float getDPfromPX(float px) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_PX,
                px,
                context.getResources().getDisplayMetrics()
        );
    }

    public void showSnackbar(String text) {
        Snackbar.make(view, text, Snackbar.LENGTH_SHORT).show();
    }

    public void showSnackbar(long text) {
        Snackbar.make(view, String.valueOf(text), Snackbar.LENGTH_SHORT).show();
    }

    //https://www.baeldung.com/java-http-request
    public String doHttpGETRequest(String requestedURL) throws IOException {
        URL url = new URL(requestedURL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type", "application/json");
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        con.disconnect();
        return content.toString();
    }

    private String getParamsString(Map<String, String> params)
            throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0
                ? resultString.substring(0, resultString.length() - 1)
                : resultString;
    }

    public void doHttpPOSTRequest(String requestedURL, Map<String, String> params) throws IOException {
        URL url = new URL(requestedURL);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(con.getOutputStream());
        out.writeBytes(getParamsString(params));
        out.flush();
        out.close();
        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer content = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();

        con.disconnect();
    }

    public JSONArray getJSONArrayByName(JSONObject jsonObject, String arrayName) throws JSONException {
            return jsonObject.getJSONArray(arrayName);
    }

    public Context getContext() {
        return context;
    }

    public JSONArray getJSONArrayByIndex(JSONArray jsonArray, int index) throws JSONException {
        return (JSONArray)((JSONArray)jsonArray).get(index);
    }

    public JSONObject makeConfigJSON(String title) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("title", title);
        //Parse Timestamp with DateForm.getTime/DateInstance...
        json.put("creationDateTimestamp", String.valueOf(new Timestamp(System.currentTimeMillis())));
        json.put("lastEditDateTimestamp", String.valueOf(new Timestamp(System.currentTimeMillis())));
        //Create empty Array.
        //Create "pixelColors" Array
        //fill it 8x with empty Array
        int black = Color.rgb(0, 0, 0);
        long[] emptyArray = new long[8];
        for (int i = 0; i < emptyArray.length; i++)
            emptyArray[i] = black;
        JSONArray pixelColors = new JSONArray();
        for (int i = 0; i < emptyArray.length; i++)
            pixelColors.put(i, new JSONArray(emptyArray));
        json.put("pixelColors", pixelColors);
        return json;
    }

    public String[] getFiles() {
        return context.fileList();
    }

    public boolean isPointInRect(PointF pointF, RectF rectF) {
        if(pointF.x>=rectF.left && pointF.x<=rectF.right && pointF.y >= rectF.top && pointF.y <= rectF.bottom)
            return true;
        return false;
    }

    public ArrayList<String> getFilesArrayList() {
        ArrayList<String> files = new ArrayList<>();
        for(String f : getFiles())
            files.add(f);
        return files;
    }

    //https://stackoverflow.com/questions/36624756/how-to-save-bitmap-to-android-gallery
    public void saveImage(Bitmap finalBitmap, String image_name) {

        String root = Environment.getExternalStorageDirectory().toString()+"/Pictures/ESP32_APP_DEBUG/";
        File myDir = new File(root);
        myDir.mkdirs();
        String fname = "Image-" + image_name+ ".png";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        Log.i("LOAD", root + fname);
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean deleteFile(String fileName) {
        return context.deleteFile(fileName);
    }

    public String readFileData(String fileName) {
        try {
            /* We have to use the openFileInput()-method
             * the ActivityContext provides.
             * Again for security reasons with
             * openFileInput(...) */

            FileInputStream fIn = context.openFileInput(fileName);
            InputStreamReader isr = new InputStreamReader(fIn);

            /* Prepare a char-Array that will
             * hold the chars we read back in. */
            //using fIn.available() instead of hard-coded
            //because it automatically get's the needed buffersize
            char[] inputBuffer = new char[fIn.available()];

            // Fill the Buffer with data from the file
            isr.read(inputBuffer);

            // Transform the chars to a String
            StringBuffer sb = new StringBuffer(new String(inputBuffer));
            sb.trimToSize();
            return String.valueOf(sb.toString());
        } catch (IOException ioe) {
            return null;
        }
    }

    public boolean doesFileExist(String fileName) {
        for(String file : context.fileList())
            if(file.equals(fileName))
                return true;
        return false;
    }

    public boolean writeFileData(String fileName, String data) {
        try {
            /* We have to use the openFileOutput()-method
             * the ActivityContext provides, to
             * protect your file from others and
             * This is done for security-reasons.
             * We chose MODE_WORLD_READABLE, because
             *  we have nothing to hide in our file */
            FileOutputStream fOut = context.openFileOutput(fileName,
                    context.MODE_PRIVATE);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);

            // Write the string to the file
            osw.write(data);

            /* ensure that everything is
             * really written out and close */
            osw.flush();
            osw.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
