package com.narendra.amazonbucketuploader.AmazonUtils;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.model.CannedAccessControlList;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by narendra on 07/01/2017.
 */

public class AmazonUploader {
    private static AmazonUploader mInstance = null;
    private Context ctx;

    private String imageURL = "";
    private Activity mActivity;
    private String parent_directory_name;
    private AmazonUploader(Context ctx, Activity mActivity){
        this.ctx = ctx;

        this.mActivity = mActivity;
    }

    public static AmazonUploader getInstance(Context ctx, Activity mActivity) {
        /**
         * use the application context as suggested by CommonsWare.
         * this will ensure that you dont accidentally leak an Activitys
         * context (see this article for more information:
         * http://android-developers.blogspot.nl/2009/01/avoiding-memory-leaks.html)
         */
        if (mInstance == null) {
            mInstance = new AmazonUploader(ctx.getApplicationContext(), mActivity);
        }
        return mInstance;
    }


    //code for amazon utility
    private TransferUtility transferUtility;
    private List<TransferObserver> observers;
    private ArrayList<HashMap<String, Object>> transferRecordMaps;
    // The SimpleAdapter adapts the data about transfers to rows in the UI

    private AmazonInterface amazonInterface;


    //For Uploading image on amazon server
    public void amazonCode(String imageFilePath, AmazonInterface amazonInterface, String parent_directory_name)
    {
        this.parent_directory_name = parent_directory_name;
        this.amazonInterface = amazonInterface;

        transferUtility = AmazonUtil.getTransferUtility(ctx);
        transferRecordMaps = new ArrayList<>();

        String image_name = imageFilePath;

        //just For Getting image name
        for(int i=0; i<image_name.length(); i++)
        {
            if(image_name.contains("/")) {
                int index = image_name.indexOf("/");
                image_name = image_name.substring(index+1);
            }
        }

        String thumbnail_img_name = image_name;
        //For Uploading image to amazon web server
        File fileToUpload = new File(imageFilePath);

        Log.v("amazon", "Image Name :  "+image_name);

        transferUtility.upload(AmazonConstants.BUCKET_NAME, "/"+parent_directory_name+"/"+thumbnail_img_name, fileToUpload, CannedAccessControlList.PublicReadWrite);
        initData2(image_name, imageFilePath);
    }

    //For Uploading image to amazon web server
    private void initData2(String image_name, String imageFilePath) {
        transferRecordMaps.clear();
        // Use TransferUtility to get all upload transfers.
        observers = transferUtility.getTransfersWithType(TransferType.UPLOAD);
        TransferListener listener = new UploadListener2(image_name, imageFilePath);
        for (TransferObserver observer : observers) {

            // For each transfer we will will create an entry in transferRecordMaps which will display
            // as a single row in the UI
            HashMap<String, Object> map = new HashMap<>();
            AmazonUtil.fillMap(map, observer, false);
            transferRecordMaps.add(map);

            // Sets listeners to in progress transfers
            if (TransferState.WAITING.equals(observer.getState())|| TransferState.WAITING_FOR_NETWORK.equals(observer.getState())|| TransferState.IN_PROGRESS.equals(observer.getState())) {
                observer.setTransferListener(listener);
            }
        }

        Log.d("amazon",AmazonConstants.AMAZON_PREFIX+"/Test/"+image_name);
        // simpleAdapter.notifyDataSetChanged();
    }


    //For Uploading image to amazon web server
    private class UploadListener2 implements TransferListener {


        String image_name;
        String imageFilePath;

        public UploadListener2(String image_name, String imageFilePath) {

            this.image_name = image_name;
            this.imageFilePath = imageFilePath;

        }
        // Simply updates the UI list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.v("amazon", "Error during upload: " + id, e);

          amazonInterface.onAmazingUploadingError(e);
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.v("amazon", String.format("onProgressChanged: %d, total: %d, current: %d",id, bytesTotal, bytesCurrent));

        }

        @Override
        public void onStateChanged(int id, TransferState newState) {

            if (newState.toString().equalsIgnoreCase("COMPLETED")) {
//                new sendimagenameonserver().execute();
                Log.v("amazon", "Completed ");
                String img_url = AmazonConstants.AMAZON_PREFIX+"/"+parent_directory_name+"/"+image_name;


                amazonInterface.onAmazingUploadingCompleted(img_url);




            }
            if (newState.toString().equalsIgnoreCase("Failed")) {
                Log.v("amazon", "Failed ");
                amazonInterface.onAmazingUploadingFailed();
            }

        }
    }
}
