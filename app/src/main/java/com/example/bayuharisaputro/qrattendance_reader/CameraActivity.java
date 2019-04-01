package com.example.bayuharisaputro.qrattendance_reader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.bayuharisaputro.qrattendance_reader.service.AppController;
import com.example.bayuharisaputro.qrattendance_reader.service.Server;
import com.mindorks.paracamera.Camera;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;


import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class CameraActivity extends AppCompatActivity {
    public static final String TAG = AppController.class.getSimpleName();
    String tag_json_obj = "json_obj_req";
    private String KEY_IMAGE = "image";
    private static final String TAG_SUCCESS = "success";
    int success=1;
    Mat img, templ, result;
    int bitmap_size = 50;
    private ImageView imageHolder;
    private final int requestCode = 20;
    TextView col, row, col2, row2;
    private String resultImgPath;
    int x, y;
    Camera camera;
    Bitmap foto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        imageHolder = (ImageView)findViewById(R.id.captured_photo);
        col = findViewById(R.id.col);
        row = findViewById(R.id.row);
        col2 = findViewById(R.id.col2);
        row2 = findViewById(R.id.row2);
        camera = new Camera.Builder()
                .resetToCorrectOrientation(true)
                .setTakePhotoRequestCode(1)
                .setDirectory("pics")
                .setName("ali_" + System.currentTimeMillis())
                .setImageFormat(Camera.IMAGE_JPEG)
                .setCompression(75)
                .setImageHeight(1000)
                .build(this);

        Button capturedImageButton = (Button)findViewById(R.id.photo_button);

        capturedImageButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent photoCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(photoCaptureIntent, requestCode);
                try {
                    camera.takePicture();
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Camera.REQUEST_TAKE_PHOTO){
            foto = camera.getCameraBitmap();
            //Drawable drawable = getApplicationContext().getResources().getDrawable(R.drawable.tempcoba);
            //Bitmap bitmap2 = ((BitmapDrawable)drawable).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            //bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
            Bitmap.createScaledBitmap(foto, 200, 300, false);
            imageHolder.setImageBitmap(foto);
            foto = getResizedBitmap(foto, 200);
            matching();
            //Bitmap bmp32 = bitmap2.copy(Bitmap.Config.ARGB_8888, true);
           // openCv(bmp32, bitmap,resultImgPath);

//            Drawable drawable2 = getApplicationContext().getResources().getDrawable(R.drawable.img);
//            Bitmap bitmap2 = ((BitmapDrawable)drawable2).getBitmap();
//            ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
//            bitmap2.compress(Bitmap.CompressFormat.PNG, 100, stream2);
//            Bitmap bmp323 = bitmap2.copy(Bitmap.Config.ARGB_8888, true);
        }
    }

    private void matching() {
        StringRequest strReq = new StringRequest(Request.Method.POST, Server.URL +"matching.php", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jObj = new JSONObject(response);
                    success = jObj.getInt(TAG_SUCCESS);
                    if (success == 1) {
                        Log.d("get edit data", jObj.toString());

                    }
                } catch (JSONException e) {
                    e.printStackTrace();

                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Error: " + error.getMessage());
                Toast.makeText(CameraActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put(KEY_IMAGE, getStringImage(foto));
                return params;
            }

        };
        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
    }


    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, bitmap_size, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public void openCv(Bitmap photo, Bitmap temp, String path) {

        img = new Mat();
        result = new Mat();
        Utils.bitmapToMat(photo, img);
        templ = new Mat();
        Utils.bitmapToMat(temp, templ);
        Mat check = new Mat();
        check = templ;

        Bitmap taken = null;
        Mat rgb2 = new Mat();
        Imgproc.cvtColor(templ, rgb2, Imgproc.COLOR_BGR2RGB);

        try {
            taken = Bitmap.createBitmap(rgb2.cols(), rgb2.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(rgb2, taken);
        }
        catch (CvException e){
            Log.d("Exception",e.getMessage());
        }

        int result_cols = templ.cols() - img.cols() + 1;
        int result_rows =  templ.rows() - img.rows() + 1;
        col.setText("img cols :" + img.cols() + "   " + "templ : " + templ.cols() + " = " + Integer.toString(result_cols));
        row.setText(Integer.toString(result_rows));

        result = new Mat(result_rows, result_cols,  CvType.CV_32F);
          Imgproc.matchTemplate(templ, img, result,  Imgproc.TM_SQDIFF);
//        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
//        double minMatchQuality = 0.1;
//        int type = Imgproc.THRESH_TOZERO;
//        Imgproc.threshold(result, result, 0.8, 1., type);
//        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
//        if (mmr.maxVal > minMatchQuality){
//            Toast.makeText(this.getApplicationContext(),"ada wajah",Toast.LENGTH_SHORT).show();
//        }
//       else {
//            Toast.makeText(this.getApplicationContext(),"tidak ada wajah",Toast.LENGTH_SHORT).show();
//        }
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);

        Point matchLoc;
//        if (Imgproc.TM_SQDIFF == Imgproc.TM_SQDIFF || Imgproc.TM_SQDIFF == Imgproc.TM_SQDIFF_NORMED) {
//            matchLoc = mmr.minLoc;
//
//        } else {
//            matchLoc = mmr.maxLoc;
//        }
        int type = Imgproc.THRESH_TOZERO;
          Imgproc.threshold(result, result, 0.8, 1., type);
        double minMatchQuality = 0.9;
        if (mmr.maxVal > minMatchQuality){
            Toast.makeText(this.getApplicationContext(),"ada wajah",Toast.LENGTH_SHORT).show();
        }
       else {
            Toast.makeText(this.getApplicationContext(),"tidak ada wajah",Toast.LENGTH_SHORT).show();
        }
//        Imgproc.rectangle(templ, matchLoc, new Point(matchLoc.x + img.cols(),
//                matchLoc.y + img.rows()), new Scalar(0, 255, 0));
//
//        Bitmap bmp = null;
//        Mat rgb = new Mat();
//        Imgproc.cvtColor(templ, rgb, Imgproc.COLOR_BGR2RGB);
//
//        try {
//            bmp = Bitmap.createBitmap(templ.cols(), templ.rows(), null);
//            Utils.matToBitmap(templ, bmp);
//        }
//        catch (CvException e){
//            Log.d("Exception",e.getMessage());
//        }



// if(taken == bmp) {
//            Toast.makeText(this.getApplicationContext(),"tidak ada wajah",Toast.LENGTH_SHORT).show();
//        }
//        else {
//            Toast.makeText(this.getApplicationContext(),"ada wajah",Toast.LENGTH_SHORT).show();
//        }
    // imageHolder.setImageBitmap(photo);


    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    img=new Mat();
                    templ=new Mat();
                    result=new Mat();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        camera.deleteImage();
    }
}
