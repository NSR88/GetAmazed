package com.narendra.amazonbucketuploader.AmazonUtils;

import java.util.ArrayList;

/**
 * Created by narendra on 07/01/2017.
 */

public interface AmazonInterface {
    public void onAmazingUploadingCompleted(String imageURL);
    public void onAmazingUploadingFailed();
    public void onAmazingUploadingError(Exception e);
}
