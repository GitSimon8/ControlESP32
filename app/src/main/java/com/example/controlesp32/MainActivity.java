package com.example.controlesp32;

import androidx.activity.result.ActivityResultRegistry;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.JsonReader;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public int currentColor = Color.rgb(0, 0, 0);
    public Utilities utils;
    public String fName = "config.json";

    public void onProgessChanged(SeekBar sb, int progess, boolean fromUser) {
        LinearLayout colorShow = (LinearLayout)findViewById(R.id.colorSHow);
        SeekBar r = (SeekBar) findViewById(R.id.seekBarR);
        SeekBar g = (SeekBar) findViewById(R.id.seekBarG);
        SeekBar b = (SeekBar) findViewById(R.id.seekBarB);

        currentColor = Color.rgb(r.getProgress(), g.getProgress(), b.getProgress());
        colorShow.setBackgroundColor(currentColor);
        //okkk.setText("Clr: " + currentColor);
        utils.writeFileData(fName,String.valueOf(currentColor));
        TextView twww = (TextView)findViewById(R.id.textView17);
        twww.setText(String.valueOf(utils.readFileData(fName)));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            try {
                InputStream inputStream = utils.getContext().getContentResolver().openInputStream(data.getData());
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                utils.showSnackbar(String.valueOf(bitmap.getWidth()) + "," + String.valueOf(bitmap.getHeight()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //Now you can do whatever you want with your inpustream, save it as file, upload to a server, decode a bitmap...
        }
    }
    int curImg = 0;
    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TableLayout linearLayout =  (TableLayout) findViewById(R.id.tableLayoutt);
        ConstraintLayout cl = (ConstraintLayout)findViewById(R.id.constLayout);
        //Snackbar.make(cl, "daw: ", Snackbar.LENGTH_SHORT).show();
        utils = new Utilities(this, cl);
        //utils.showSnackbar("Hello", Snackbar.LENGTH_SHORT);
        TextView twww = (TextView)findViewById(R.id.textView17);
        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        EditText editText = (EditText)findViewById(R.id.editTextTextPersonName);

        //StrictMode.setThreadPolicy(policy);
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    //utils.showSnackbar(utils.doHttpRequest("https://api.thecatapi.com/v1/images/search"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

        //if(1 == 1) return;

        Button save = (Button)findViewById(R.id.buttonSave);
        Button apply = (Button)findViewById(R.id.buttonApply);
        Button del = (Button)findViewById(R.id.buttonSaveDelete);
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(String s : utils.getFiles())
                    if(s.replace(".json", "").equals(editText.getText().toString())) {

                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        //Yes button clicked
                                        utils.deleteFile(s);
                                        utils.showSnackbar("Bild '" + s.replace(".json", "") + "' wurde gelöscht");

                                        ArrayList imgs = new ArrayList<String>();
                                        for(String img : utils.getFiles()) {
                                            if((!img.endsWith(".json")) || img.equals("config.json")) continue;
                                            imgs.add(img);
                                        }
                                        if(imgs.size() > 0) {
                                            if(imgs.size() - 1 > curImg)
                                                curImg++;
                                            else
                                                curImg = 0;
                                        }
                                        editText.setText(imgs.get(curImg).toString().replace(".json",""));
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        break;
                                }
                            }
                        };


                        AlertDialog.Builder builder = new AlertDialog.Builder(utils.getContext());
                        builder.setMessage("Bild '" + s.replace(".json", "") + "' wirklich löschen?").setPositiveButton("Ja", dialogClickListener)
                                .setNegativeButton("Nein", dialogClickListener).show();


                    }

            }
        });
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject save = utils.makeConfigJSON(editText.getText().toString());
                    JSONArray pixelColors = save.getJSONArray("pixelColors");

                    for(int w = 0; w < linearLayout.getChildCount(); w++) {
                        View wr = linearLayout.getChildAt(w);
                        if(!(wr instanceof TableRow))
                            continue;
                        //row number = w
                        TableRow tr = (TableRow)wr;
                        for(int i = 0; i < tr.getChildCount(); i++) {
                            //column number = i
                            View vv = tr.getChildAt(i);
                            if(!(vv instanceof TextView)) continue;
                            TextView tvv = (TextView) vv;
                            JSONArray rowww = pixelColors.getJSONArray(w);
                            if (tvv.getBackground() instanceof ColorDrawable) {
                                ColorDrawable cd = (ColorDrawable) tvv.getBackground();
                                int colorCode = cd.getColor();
                                rowww.put(i, colorCode);
                                pixelColors.put(w, rowww);
                            }

                            //tvv.setBackgroundColor(currentColor);
                        }
                    }

                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    try {
                                        save.put("pixelColors", pixelColors);
                                        utils.writeFileData(editText.getText().toString()+".json", save.toString());

                                        Bitmap bmp = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888);


                                        for(int w = 0; w < linearLayout.getChildCount(); w++) {
                                            View wr = linearLayout.getChildAt(w);
                                            if(!(wr instanceof TableRow))
                                                continue;
                                            //row number = w
                                            TableRow tr = (TableRow)wr;
                                            for(int i = 0; i < tr.getChildCount(); i++) {
                                                //column number = i
                                                View vv = tr.getChildAt(i);
                                                if(!(vv instanceof TextView)) continue;
                                                TextView tvv = (TextView) vv;
                                                //JSONArray rowww = pixelColors.getJSONArray(w);
                                                if (tvv.getBackground() instanceof ColorDrawable) {
                                                    ColorDrawable cd = (ColorDrawable) tvv.getBackground();
                                                    int colorCode = cd.getColor();
                                                    //rowww.put(i, colorCode);
                                                    //pixelColors.put(w, rowww);
                                                    bmp.setPixel(i, w, colorCode);
                                                }

                                                //tvv.setBackgroundColor(currentColor);
                                            }
                                        }


                                        utils.saveImage(bmp, editText.getText().toString());
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked
                                    break;
                            }
                        }
                    };


                    if(utils.getFilesArrayList().contains(editText.getText().toString()+".json")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(utils.getContext());
                        builder.setMessage("Neues Bild speichern und altes überschreiben?").setPositiveButton("Ja", dialogClickListener)
                                .setNegativeButton("Nein", dialogClickListener).show();
                    } else {
                        save.put("pixelColors", pixelColors);
                        utils.writeFileData(editText.getText().toString()+".json", save.toString());
                    }

                    //ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    //ClipData clip = ClipData.newPlainText(save.toString(), save.toString());
                    //clipboard.setPrimaryClip(clip);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject appl = new JSONObject(utils.readFileData(editText.getText().toString()+".json"));

                    JSONArray pixelColorsArr = appl.getJSONArray("pixelColors");
                    for(int a = 0; a < pixelColorsArr.length(); a++) {
                        JSONArray r = pixelColorsArr.getJSONArray(a);
                        for(int x = 0; x < r.length(); x++) {
                            int clr = r.getInt(x);

                            View wr = linearLayout.getChildAt(a);
                            TableRow tr = (TableRow)wr;
                            View vv = tr.getChildAt(x);
                            if(!(vv instanceof TextView)) continue;
                            TextView tvv = (TextView) vv;
                            tvv.setBackgroundColor(clr);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Button up = (Button)findViewById(R.id.buttonUp);
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList imgs = new ArrayList<String>();
                for(String img : utils.getFiles()) {
                    if((!img.endsWith(".json")) || img.equals("config.json")) continue;
                    imgs.add(img);
                }
                if(imgs.size() > 0) {
                    if(imgs.size() - 1 > curImg)
                        curImg++;
                    else
                        curImg = 0;
                }
                editText.setText(imgs.get(curImg).toString().replace(".json",""));
                //Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                //intent.setType("image/*");
                //startActivityForResult(intent, 1);
                //startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
                //registerForActivityResult(Intent.createChooser(intent, "Select Picture"), 1);
            }
        });

        Button down = (Button)findViewById(R.id.buttonDown);
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList imgs = new ArrayList<String>();
                for(String img : utils.getFiles()) {
                    if((!img.endsWith(".json")) || img.equals("config.json")) continue;
                    imgs.add(img);
                }
                if(imgs.size() > 0) {
                    if(curImg > 0)
                        curImg--;
                    else
                        curImg = imgs.size()-1;
                }
                editText.setText(imgs.get(curImg).toString().replace(".json",""));
                //Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                //intent.setType("image/*");
                //startActivityForResult(intent, 1);
                //startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
                //registerForActivityResult(Intent.createChooser(intent, "Select Picture"), 1);
            }
        });

        SeekBar r = (SeekBar) findViewById(R.id.seekBarR);
        SeekBar g = (SeekBar) findViewById(R.id.seekBarG);
        SeekBar b = (SeekBar) findViewById(R.id.seekBarB);

        if(!utils.doesFileExist(fName)) {
            utils.showSnackbar("creating");
            utils.writeFileData(fName,String.valueOf(currentColor));
        } else {
            String readd = utils.readFileData(fName);
            currentColor = Integer.parseInt(readd);

        }
        twww.setText(utils.readFileData(fName));

        r.setProgress(Color.red(currentColor));
        g.setProgress(Color.green(currentColor));
        b.setProgress(Color.blue(currentColor));
        LinearLayout colorShow = (LinearLayout)findViewById(R.id.colorSHow);
        colorShow.setBackgroundColor(currentColor);
        Button fillBackgroundButton = (Button)findViewById(R.id.fillBackgroundButton);

        fillBackgroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int w = 0; w < linearLayout.getChildCount(); w++) {
                    View wr = linearLayout.getChildAt(w);
                    if(!(wr instanceof TableRow))
                        continue;
                    TableRow tr = (TableRow)wr;
                    for(int i = 0; i < tr.getChildCount(); i++) {
                        View vv = tr.getChildAt(i);
                        if(!(vv instanceof TextView)) continue;
                        TextView tvv = (TextView) vv;
                        tvv.setBackgroundColor(currentColor);
                    }
                }
            }
        });

        r.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                onProgessChanged(seekBar, progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        g.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                onProgessChanged(seekBar, progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        b.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                onProgessChanged(seekBar, progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        int size = Math.round(utils.getPXfromDP(37.5f)); //37.5f bei 8 columns

        for(int w = 0; w < linearLayout.getChildCount(); w++) {
            View wr = linearLayout.getChildAt(w);
            if(!(wr instanceof TableRow))
                continue;
            TableRow tr = (TableRow)wr;
            for(int i = 0; i < 8; i++) { //16 columns
                TextView tv = new TextView(this);
                tv.setHeight(size);
                tv.setWidth(size);
                tv.setBackgroundColor(Color.rgb(0,0,0));
               tv.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if(!(v instanceof TextView)) return false;
                        int x = (int) event.getX();
                        int y = (int) event.getY();
                        TextView w = (TextView)v;
                        TableLayout linearLayout =  (TableLayout) findViewById(R.id.tableLayoutt);
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                w.setBackgroundColor(currentColor);
                                break;
                            case MotionEvent.ACTION_MOVE:
                                twww.setText("moving " + event.getRawX() + ","+event.getRawY());
                                break;
                            case MotionEvent.ACTION_UP:
                                break;
                        }

                        return true;
                    }
                });
                tr.addView(tv);
            }
        }
    }
}