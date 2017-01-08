package com.narendra.amazonbucketuploader;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.narendra.amazonbucketuploader.AmazonUtils.AmazonInterface;
import com.narendra.amazonbucketuploader.AmazonUtils.AmazonUploader;

import java.io.File;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AmazonInterface {

    Button btn_choose_image, btn_upload;
    private Context ctx;
    private AmazonUploader amazonUploader;


    private Uri fileURI;
    public static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    public static final int GALLERY_PICKED_IMAGE_REQUEST_CODE = 101;


    String imageFilePath = "";

    private ProgressDialog pDialog;
    private NetworkConsumer _networkcomsumer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialization();
        initViews();
        updateViews();

    }



    public void initialization()
    {
        ctx = MainActivity.this;
        pDialog = new ProgressDialog(ctx);
        _networkcomsumer = new NetworkConsumer(this);
        amazonUploader = AmazonUploader.getInstance(ctx, MainActivity.this);
    }


    private void updateViews() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }
    }

    private   boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {


                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        return true;
    }

    private void initViews(){
        btn_choose_image = (Button) findViewById(R.id.btn_choose_image);
        btn_upload = (Button) findViewById(R.id.btn_upload);
        btn_choose_image.setOnClickListener(this);
        btn_upload.setOnClickListener(this);


        //Lolly pop status bar permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(Color.BLUE);
        }

    }



    @Override
    public void onClick(View v) {

        //handle to all click event;

        switch (v.getId())
        {
            case  R.id.btn_choose_image:

                // this condition use camera permission and pic image from gallery!
                isStoragePermissionGranted();


                    selectORpickImage();


                break;



        }

    }





    @Override
    public void onAmazingUploadingCompleted(String imageURL) {
        pDialog.hide();
        Toast.makeText(MainActivity.this,"Uploading Completed",Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onAmazingUploadingFailed() {
        pDialog.hide();
        Toast.makeText(MainActivity.this,"Uploading Failed",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAmazingUploadingError(Exception e) {
        pDialog.hide();
        Toast.makeText(MainActivity.this,"Uploading Error",Toast.LENGTH_SHORT).show();
    }


    private void selectORpickImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Gallery", "Cancel" };
//        final CharSequence[] items = { "Choose from Gallery", "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    fileURI = Uri.fromFile(getOutputMediaFile());
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileURI);

                    startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
                    dialog.dismiss();
                } else if (items[item].equals("Choose from Gallery")) {

                    Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                    startActivityForResult( i, GALLERY_PICKED_IMAGE_REQUEST_CODE);
                    dialog.dismiss();
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }


    public File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CameraDemo");

        if (!mediaStorageDir.exists()){
            if (!mediaStorageDir.mkdirs()){
                return null;
            }
        }


        return new File(mediaStorageDir.getPath() + File.separator +
                "test_"+ (System.currentTimeMillis()/1000) + "_img.jpg");
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        try {
            if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
                if (resultCode == RESULT_OK && fileURI!=null) {
                    //                new ImageCompressionAsyncTask(false, ctx, this).execute(fileUri.toString());


//                    Uri selectedImage = data.getData();
                    imageFilePath = getRealPathFromURI(fileURI);


                    if(_networkcomsumer.isNetworkAvailable()) {
                        pDialog = new ProgressDialog(MainActivity.this);
                        pDialog.setCancelable(false);
                        pDialog.setMessage("Please wait...");
                        pDialog.show();
                        amazonUploader.amazonCode(imageFilePath,MainActivity.this,"Test");



                    } else {
                        Toast.makeText(this, "internet unavailable", Toast.LENGTH_SHORT).show();
                    }


                } else if (resultCode == RESULT_CANCELED) {
                    // user cancelled Image capture
                    Toast.makeText(ctx , "User cancelled image capture.", Toast.LENGTH_SHORT).show();
                } else {
                    // failed to capture image
                    Toast.makeText(ctx , "Sorry! Failed to capture image.", Toast.LENGTH_SHORT).show();
                }
            }else if (requestCode == GALLERY_PICKED_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && null != data) {
                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };

                Cursor cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                //          Check for file if exists
                File file = new File(picturePath);
                if(file.exists())
                {
                    imageFilePath = picturePath;

                    if(_networkcomsumer.isNetworkAvailable()) {
                        pDialog = new ProgressDialog(MainActivity.this);
                        pDialog.setCancelable(false);
                        pDialog.setMessage("please wait");
                        pDialog.show();
                        amazonUploader.amazonCode(imageFilePath,MainActivity.this,"Test");



                    } else {
                        Toast.makeText(this, "internet unavailable", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "File doesn't exist", Toast.LENGTH_SHORT).show();
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




    public String getRealPathFromURI(Uri contentUri) {
        try
        {
            String[] proj = {MediaStore.Images.Media.DATA};
            Cursor cursor = managedQuery(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        catch (Exception e)
        {
            return contentUri.getPath();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                btn_choose_image.setEnabled(true);
            }
        }
    }

}
