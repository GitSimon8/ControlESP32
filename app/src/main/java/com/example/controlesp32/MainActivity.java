package com.example.controlesp32;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
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

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.azeesoft.lib.colorpicker.ColorPickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    public int currentColor = Color.rgb(0, 0, 0);
    public Utilities utils;
    public String fName = "config.json";
    public ColorPickerDialog colorPickerDialog = null;

    public void onProgessChanged(SeekBar sb, int progess, boolean fromUser) {
        LinearLayout colorShow = (LinearLayout) findViewById(R.id.colorSHow);
        //SeekBar r = (SeekBar) findViewById(R.id.seekBarR);
        //SeekBar g = (SeekBar) findViewById(R.id.seekBarG);
        //SeekBar b = (SeekBar) findViewById(R.id.seekBarB);

        //currentColor = Color.rgb(r.getProgress(), g.getProgress(), b.getProgress());
        colorShow.setBackgroundColor(currentColor);
        //okkk.setText("Clr: " + currentColor);
        utils.writeFileData(fName, String.valueOf(currentColor));
        TextView twww = (TextView) findViewById(R.id.textView17);
        twww.setText(String.valueOf(utils.readFileData(fName)));
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }
            try {
                InputStream inputStream = utils.getContext().getContentResolver().openInputStream(data.getData());
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.tableLayoutt);
                if (bitmap.getWidth() == 8 && bitmap.getHeight() == 8) {
                    for (int y = 0; y < 8; y++) {
                        for (int x = 0; x < 8; x++) {
                            Color clr = bitmap.getColor(x, y);
                            int clrr = Color.rgb(clr.red(), clr.green(), clr.blue());
                            View wr = linearLayout.getChildAt(y);
                            TableRow tr = (TableRow) wr;
                            View vv = tr.getChildAt(x);
                            if (!(vv instanceof TextView)) continue;
                            TextView tvv = (TextView) vv;
                            tvv.setBackgroundColor(clrr);
                        }
                    }
                }
                //utils.showSnackbar(String.valueOf(bitmap.getWidth()) + "," + String.valueOf(bitmap.getHeight()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //Now you can do whatever you want with your inpustream, save it as file, upload to a server, decode a bitmap...
        }
    }

    boolean pickUpColor = false;
    int curImg = 0;

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TableLayout linearLayout = (TableLayout) findViewById(R.id.tableLayoutt);
        ConstraintLayout cl = (ConstraintLayout) findViewById(R.id.constLayout);
        //Snackbar.make(cl, "daw: ", Snackbar.LENGTH_SHORT).show();
        utils = new Utilities(this, cl);
        //utils.showSnackbar("Hello", Snackbar.LENGTH_SHORT);
        TextView twww = (TextView) findViewById(R.id.textView17);
        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        EditText editText = (EditText) findViewById(R.id.editTextTextPersonName);

        //StrictMode.setThreadPolicy(policy);

        //if(1 == 1) return;

        Button upload = (Button) findViewById(R.id.buttonUploadImage);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                //startActivityForResult(intent, 1);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
                //registerForActivityResult(Intent.createChooser(intent, "Select Picture"), 1);
            }
        });


        Button save = (Button) findViewById(R.id.buttonSave);
        Button apply = (Button) findViewById(R.id.buttonApply);
        Button del = (Button) findViewById(R.id.buttonSaveDelete);
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (String s : utils.getFiles())
                    if (s.replace(".json", "").equals(editText.getText().toString())) {

                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        //Yes button clicked
                                        utils.deleteFile(s);
                                        utils.showSnackbar("Bild '" + s.replace(".json", "") + "' wurde gelöscht");

                                        ArrayList imgs = new ArrayList<String>();
                                        for (String img : utils.getFiles()) {
                                            if ((!img.endsWith(".json")) || img.equals("config.json"))
                                                continue;
                                            imgs.add(img);
                                        }
                                        if (imgs.size() > 0) {
                                            if (imgs.size() - 1 > curImg)
                                                curImg++;
                                            else
                                                curImg = 0;
                                        }
                                        editText.setText(imgs.get(curImg).toString().replace(".json", ""));
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

                    for (int w = 0; w < linearLayout.getChildCount(); w++) {
                        View wr = linearLayout.getChildAt(w);
                        if (!(wr instanceof TableRow))
                            continue;
                        //row number = w
                        TableRow tr = (TableRow) wr;
                        for (int i = 0; i < tr.getChildCount(); i++) {
                            //column number = i
                            View vv = tr.getChildAt(i);
                            if (!(vv instanceof TextView)) continue;
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
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    try {
                                        save.put("pixelColors", pixelColors);
                                        utils.writeFileData(editText.getText().toString() + ".json", save.toString());

                                        Bitmap bmp = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888);


                                        for (int w = 0; w < linearLayout.getChildCount(); w++) {
                                            View wr = linearLayout.getChildAt(w);
                                            if (!(wr instanceof TableRow))
                                                continue;
                                            //row number = w
                                            TableRow tr = (TableRow) wr;
                                            for (int i = 0; i < tr.getChildCount(); i++) {
                                                //column number = i
                                                View vv = tr.getChildAt(i);
                                                if (!(vv instanceof TextView)) continue;
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


                    if (utils.getFilesArrayList().contains(editText.getText().toString() + ".json")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(utils.getContext());
                        builder.setMessage("Neues Bild speichern und altes überschreiben?").setPositiveButton("Ja", dialogClickListener)
                                .setNegativeButton("Nein", dialogClickListener).show();
                    } else {
                        save.put("pixelColors", pixelColors);
                        utils.writeFileData(editText.getText().toString() + ".json", save.toString());
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
                    JSONObject appl = new JSONObject(utils.readFileData(editText.getText().toString() + ".json"));

                    JSONArray pixelColorsArr = appl.getJSONArray("pixelColors");
                    StringBuilder allPixelColors = new StringBuilder();
                    for (int a = 0; a < pixelColorsArr.length(); a++) {
                        JSONArray r = pixelColorsArr.getJSONArray(a);
                        for (int x = 0; x < r.length(); x++) {
                            int clr = r.getInt(x);

                            View wr = linearLayout.getChildAt(a);
                            TableRow tr = (TableRow) wr;
                            View vv = tr.getChildAt(x);
                            if (!(vv instanceof TextView)) continue;
                            TextView tvv = (TextView) vv;
                            tvv.setBackgroundColor(clr);

                            //SEND BIG GET REQUEST WITH ALL PIXEL COLORS
                            //OR DO POST REQUEST WITH JSON
                            //--- "UPLOAD" / SEND ALL DATA TO ESP32
                            allPixelColors.append(((TextView) tvv).getText().toString() + "," + clr + ";");
                        }
                    }
                    allPixelColors.deleteCharAt(allPixelColors.length() - 1);

                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            try {
                                utils.doHttpGETRequest("http://192.168.178.41/" + allPixelColors.toString());
                            } catch (IOException e) {
                                utils.showSnackbar(e.toString());
                                e.printStackTrace();
                                Log.i("Eror", e.toString());
                            }
                        }
                    }).start();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        Button up = (Button) findViewById(R.id.buttonUp);
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList imgs = new ArrayList<String>();
                for (String img : utils.getFiles()) {
                    if ((!img.endsWith(".json")) || img.equals("config.json")) continue;
                    imgs.add(img);
                }
                if (imgs.size() > 0) {
                    if (imgs.size() - 1 > curImg)
                        curImg++;
                    else
                        curImg = 0;
                }
                editText.setText(imgs.get(curImg).toString().replace(".json", ""));
            }
        });

        Button down = (Button) findViewById(R.id.buttonDown);
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList imgs = new ArrayList<String>();
                for (String img : utils.getFiles()) {
                    if ((!img.endsWith(".json")) || img.equals("config.json")) continue;
                    imgs.add(img);
                }
                if (imgs.size() > 0) {
                    if (curImg > 0)
                        curImg--;
                    else
                        curImg = imgs.size() - 1;
                }
                editText.setText(imgs.get(curImg).toString().replace(".json", ""));
            }
        });

      //  SeekBar r = (SeekBar) findViewById(R.id.seekBarR);
      //  SeekBar g = (SeekBar) findViewById(R.id.seekBarG);
     //   SeekBar b = (SeekBar) findViewById(R.id.seekBarB);

        if (!utils.doesFileExist(fName)) {
            utils.showSnackbar("creating");
            utils.writeFileData(fName, String.valueOf(currentColor));
        } else {
            String readd = utils.readFileData(fName);
            currentColor = Integer.parseInt(readd);
        }
        twww.setText(utils.readFileData(fName));

        //r.setProgress(Color.red(currentColor));
        //g.setProgress(Color.green(currentColor));
       // b.setProgress(Color.blue(currentColor));
        LinearLayout colorShow = (LinearLayout) findViewById(R.id.colorSHow);
        colorShow.setBackgroundColor(currentColor);
        Button fillBackgroundButton = (Button) findViewById(R.id.fillBackgroundButton);

        //SharedPreferences sharedPreferences=utils.getContext().getSharedPreferences("colpick", Context.MODE_PRIVATE);
        //SharedPreferences.Editor editor=sharedPreferences.edit();
        //editor.remove("lastColor");
        //editor.commit();
        try {
            colorPickerDialog = ColorPickerDialog.createColorPickerDialog(this, ColorPickerDialog.DARK_THEME);
            colorPickerDialog.hideOpacityBar();// cp_showOpacityBar
        } catch (Exception e) {
            SharedPreferences sharedPreferences = utils.getContext().getSharedPreferences("colpick", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove("lastColor");
            editor.commit();
        }


        colorShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //pickUpColor = true;
                colorPickerDialog.show();
            }
        });


        colorPickerDialog.setOnColorPickedListener(new ColorPickerDialog.OnColorPickedListener() {
            @Override
            public void onColorPicked(int color, String hexVal) {
                //Your code here
                currentColor = color;
               // r.setProgress(Color.red(currentColor));
               // g.setProgress(Color.green(currentColor));
               // b.setProgress(Color.blue(currentColor));
                colorShow.setBackgroundColor(currentColor);
                twww.setText(String.valueOf(currentColor));
                utils.writeFileData(fName, String.valueOf(currentColor));
            }
        });


        fillBackgroundButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int w = 0; w < linearLayout.getChildCount(); w++) {
                    View wr = linearLayout.getChildAt(w);
                    if (!(wr instanceof TableRow))
                        continue;
                    TableRow tr = (TableRow) wr;
                    for (int i = 0; i < tr.getChildCount(); i++) {
                        View vv = tr.getChildAt(i);
                        if (!(vv instanceof TextView)) continue;
                        TextView tvv = (TextView) vv;
                        tvv.setBackgroundColor(currentColor);
                    }
                }
            }
        });

        /*r.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
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
                if (fromUser)
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
                if (fromUser)
                    onProgessChanged(seekBar, progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });*/
int lX = 59;
int lY = 665;
        int size = Math.round(utils.getPXfromDP(37.5f)); //37.5f bei 8 columns
        HashMap<Rect, TextView> rectsTextView = new HashMap<>();
        HashMap<TextView, Integer> textViewLastColorSent = new HashMap<>();
        for (int y = 0; y < linearLayout.getChildCount(); y++) {
            View wr = linearLayout.getChildAt(y);
            if (!(wr instanceof TableRow))
                continue;
            TableRow tr = (TableRow) wr;
            for (int x = 0; x < 8; x++) { //16 columns
                TextView tv = new TextView(this);
                tv.setHeight(size);
                tv.setWidth(size);
                tv.setBackgroundColor(Color.rgb(0, 0, 0));
                tv.post(new Runnable() {
                    @Override
                    public void run() {
                        //textVIEWSSPos.add(new Point((int)tv.getX(), (int)tv.getY()));
                        //Log.i("POS: ", new Point((int)tv.getX(), (int)tv.getY()).toString());
                        int[] location = new int[2];
                        tv.getLocationOnScreen(location);
                        int x = location[0];
                        int y = location[1];
                        int diffY = 827 - 665;
                        Rect TVRECT = new Rect(x, y-diffY, x+75, y-diffY+75);
                        rectsTextView.put(TVRECT, tv);
                        //Log.i("POS: ", new Point(x, y-diffY).toString());
                    }
                });
                //String ledNumberString = "";
                //((TextView) tv).setText(String.valueOf(y*8+x));
                tv.setText(String.valueOf(utils.getLedNumber(y, x)));
                Rect boxRect = new Rect(x*75, y*75, x*75+75, y*75+75);
                rectsTextView.put(boxRect, tv);
                ((TextView) tv).setTextColor(Color.argb(0, 0, 0, 0));
                textViewLastColorSent.put(tv, Color.rgb(0, 0, 0));
                tv.setOnTouchListener(new View.OnTouchListener() {
                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (!(v instanceof TextView)) return false;
                        TextView w = (TextView) v;
                        TableLayout linearLayout = (TableLayout) findViewById(R.id.tableLayoutt);
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                if (pickUpColor) {
                                    if (w.getBackground() instanceof ColorDrawable) {
                                        ColorDrawable cd = (ColorDrawable) w.getBackground();
                                        int colorCode = cd.getColor();
                                        currentColor = colorCode;
                                       // r.setProgress(Color.red(currentColor));
                                       // g.setProgress(Color.green(currentColor));
                                       // b.setProgress(Color.blue(currentColor));
                                        colorShow.setBackgroundColor(currentColor);
                                        pickUpColor = false;
                                    }

                                    return true;
                                }
                                w.setBackgroundColor(currentColor);
                                new Thread(new Runnable() {

                                    @Override
                                    public void run() {
                                        //utils.doHttpGETRequest2("http://192.168.178.41/" + ((TextView) v).getText().toString() + "," + currentColor);
                                    }
                                }).start();
                                //MAYBE SEND COLOR DATA TO ARDUINO
                                //twww.setText("click " + (int)event.getRawX() + "," + (int)event.getRawY());
                                //twww.setText("pos " + tv.getX() + "    :      " + tr.getY());
                                break;
                            case MotionEvent.ACTION_MOVE:
                                int diffY = 827 - 665;
                                twww.setText("moving " + (event.getRawX()-lX) + "," + (event.getRawY()-lY-160));
                                // twww.setText("moving " + tv.getX() + ","+tv.getY());
                                Point curPos = new Point((int)event.getRawX(), (int)event.getRawY()-diffY);
                               for(Map.Entry<Rect, TextView> allRects : rectsTextView.entrySet()) {
                                    if(allRects.getKey().contains(curPos.x, curPos.y)) {
                                        if(textViewLastColorSent.get(allRects.getValue()) != currentColor) {
                                            textViewLastColorSent.replace(allRects.getValue(), currentColor);
                                            //textViewLastColorSent.remove(allRects.getValue());
                                            //textViewLastColorSent.put(allRects.getValue(), currentColor);
                                            allRects.getValue().setBackgroundColor(currentColor);
                                            new Thread(new Runnable() {

                                                @Override
                                                public void run() {
                                                    try {
                                                        utils.doHttpGETRequest2("http://192.168.178.41/" + allRects.getValue().getText().toString() + "," + currentColor);
                                                    } catch (IOException e) {
                                                        //utils.showSnackbar(e.toString());
                                                        e.printStackTrace();
                                                        Log.i("Eror", e.toString());
                                                    }
                                                }
                                            }).start();
                                        }

                                    }
                                }
                                break;
                            case MotionEvent.ACTION_UP:
                                //twww.setText("pos " + tv.getX() + "    :      " + tr.getY());
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