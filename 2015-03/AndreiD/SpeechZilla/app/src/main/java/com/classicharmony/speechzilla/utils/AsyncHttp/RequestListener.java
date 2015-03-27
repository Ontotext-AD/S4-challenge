package com.classicharmony.speechzilla.utils.AsyncHttp;
import org.apache.http.Header;
public interface RequestListener {
    public void onSuccess(int statusCode, Header[] headers, byte[] response);
}