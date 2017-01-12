package blopa.beacons;

import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class HttpHandler {

    private static final String TAG = "JsonSend";

    public String makeServiceCall(String reqUrl, JSONObject json)throws IOException {

        OkHttpClient client = new OkHttpClient();

        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        builder.addFormDataPart("data", json.toString());
        RequestBody body = builder.build();

        Log.d(TAG, json.toString());

        Request request = new Request.Builder()
                .url(reqUrl)
                .header("Content-type", "application/json")
                .post(body)
                .build();
        Response response = client.newCall(request).execute();

        Log.d(TAG, response.message() + " "+ response.code());


        return response.body().string();
    }
}