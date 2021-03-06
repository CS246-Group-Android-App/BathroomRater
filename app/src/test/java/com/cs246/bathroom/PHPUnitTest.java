package com.cs246.bathroom;

import android.webkit.WebView;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.Exception;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class PHPUnitTest {
    @Test
    public void assertPHPFileExists() throws Exception{
        URL url = new URL("http://php-tsorenson.rhcloud.com/androidPost.php");
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con =  (HttpURLConnection) url.openConnection();
            con.setRequestMethod("HEAD");
            System.out.println(con.getResponseCode());
            assert (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            assert (1 == 2);
        }
    }
}