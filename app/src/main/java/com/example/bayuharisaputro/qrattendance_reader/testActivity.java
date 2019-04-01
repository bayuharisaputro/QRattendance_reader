package com.example.bayuharisaputro.qrattendance_reader;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;


import java.io.File;

public class testActivity extends AppCompatActivity {

    private String mainImgPath, templateImgPath, resultImgPath;
    private EditText etFirst, etSecond;
    private static final String TAG = "MainActivity";
    private Button btnFind;
    private Button btnFind2;
    ImageView preview;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    // static initializer to find wheather openCV loaded in your app or not.
    static {
        if (!OpenCVLoader.initDebug()) {
            Log.e(TAG, "OpenCV not loaded");
        } else {
            Log.e(TAG, "OpenCV loaded");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
// Load open cv .so native library using loadLibrary that exist in jniLibs folder
        System.loadLibrary("opencv_java");
        btnFind = (Button) findViewById(R.id.btnFind);
        btnFind2 = (Button) findViewById(R.id.btnFind2);
        etFirst = (EditText) findViewById(R.id.etFirst);
        etSecond = (EditText) findViewById(R.id.etSecond);
        preview = findViewById(R.id.imgResult);
// get the absolute path of stored image in DCIM folder.
        final String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        resultImgPath = baseDir + "/Download/result.png";
// enter the name of main image and search image.
        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mainImgPath = baseDir + "/DCIM/Camera/" + etFirst.getText().toString() + ".jpg";
                templateImgPath = baseDir + "/DCIM/Camera/" + etSecond.getText().toString() + ".jpg";
                File imgFile = new File(mainImgPath);
                Bitmap tmp = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                if (imgFile.exists()) {
                    Toast.makeText(getApplicationContext(), mainImgPath, Toast.LENGTH_SHORT).show();
                    preview.setImageBitmap(tmp);
                }
                // matchingDemo(mainImgPath, templateImgPath, resultImgPath, Imgproc.TM_SQDIFF);
            }
        });
        btnFind2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                preview.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher_foreground));

                // matchingDemo(mainImgPath, templateImgPath, resultImgPath, Imgproc.TM_SQDIFF);
            }
        });
    }


//    public void run(String inFile, String templateFile, String outFile,
//                    int match_method) {
//        System.out.println("\nRunning Template Matching");
//
//
//        // / Create the result matrix
//        int result_cols = img.cols() - templ.cols() + 1;
//        int result_rows = img.rows() - templ.rows() + 1;
//        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);
//
//        // / Do the Matching and Normalize
//        Imgproc.matchTemplate(img, templ, result, match_method);
//        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
//
//        // / Localizing the best match with minMaxLoc
//        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
//
//        Point matchLoc;
//        if (match_method == Imgproc.TM_SQDIFF
//                || match_method == Imgproc.TM_SQDIFF_NORMED) {
//            matchLoc = mmr.minLoc;
//        } else {
//            matchLoc = mmr.maxLoc;
//        }
//    }
}
