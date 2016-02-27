package com.cs246.bathroom;

import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by tyler on 2/27/16.
 */
public class GETUnitTest {
    @Test
    public void assertPHPFileExists() throws Exception{
        URL url = new URL("http://php-tsorenson.rhcloud.com/androidGet.php");
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con =  (HttpURLConnection) url.openConnection();
            con.setRequestMethod("HEAD");
            System.out.println(con.getResponseCode());
            assert  (con.getResponseCode() == HttpURLConnection.HTTP_OK);
        }
        catch (Exception e) {
            e.printStackTrace();
            assert (1 == 2);
        }
    }
}
