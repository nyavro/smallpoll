package com.eny.smallpoll.view;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by eny on 16.05.15.
 */
public class Digest {
    public static final String UTF_8 = "UTF-8";
    private final String digest;

    public Digest(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        this.digest = new String(
            Base64.encode(
                MessageDigest.getInstance("MD5").digest(text.getBytes(UTF_8)),
                Base64.DEFAULT
            ),
            UTF_8
        );
    }

    public String text() {
        return digest;
    }
}
