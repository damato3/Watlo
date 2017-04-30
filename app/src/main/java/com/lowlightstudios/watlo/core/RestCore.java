package com.lowlightstudios.watlo.core;

import android.os.AsyncTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Provides helper methods to handle HTTP(S) requests.
 * Created by Appjetive on 23/11/2016.
 */

public class RestCore {
    public static final String _GET = "get";
    public static final String _POST = "post";

    private String host;

    private OkHttpClient httpClient;
    private MediaType mediaType;
    private RestCoreJob restCoreJob;
    private RestCoreAsync restCoreAsync;

    public RestCore(Object classContext, String host) {
        if (classContext instanceof RestCoreJob) {
            this.restCoreJob = (RestCoreJob) classContext;
        } else {
            throw new RuntimeException(classContext.toString()
                    + " must implement RestCoreJob");
        }
        this.host = host;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.mediaType = MediaType.parse("application/json; charset=utf-8");
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void httpGet(String url, int codeResult) {
        restCoreAsync = new RestCoreAsync();
        restCoreAsync.execute(new RestCoreRequest(_GET, codeResult, url));
    }

    public void httpGet(String url, HashMap<String, String> query, int codeResult) {
        restCoreAsync = new RestCoreAsync();
        RestCoreRequest restCore = new RestCoreRequest(_GET, codeResult, url);
        restCore.setQueryParams(query);
        restCoreAsync.execute(restCore);
    }

    public void httpPost(String url, int codeResult, JSONObject params) {
        restCoreAsync = new RestCoreAsync();
        RestCoreRequest restCore =  new RestCoreRequest(_GET, codeResult, url);
        restCore.setParams(params.toString());
        restCoreAsync.execute(restCore);
    }

    private class RestCoreAsync extends AsyncTask<RestCoreRequest, Void, RestCoreRequest> {

        @Override
        protected RestCoreRequest doInBackground(RestCoreRequest... restCore) {
            JSONObject data;
            Request request = null;
            switch(restCore[0].getMethod()) {
                case _GET:
                    HttpUrl.Builder builder = new HttpUrl.Builder();
                    builder.scheme("http");
                    builder.host(getHost());
                    builder.addPathSegments(restCore[0].getUrl());
                    if (restCore[0].getQueryParams() != null) {
                        Iterator it = restCore[0].getQueryParams().entrySet().iterator();
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry)it.next();
                            builder.addQueryParameter(pair.getKey().toString(), pair.getValue().toString());
                        }
                    }
                    request = new Request.Builder().url(builder.build()).build();
                    break;
                case _POST:
                    RequestBody body = RequestBody.create(mediaType, restCore[0].getParams());
                    request = new Request.Builder().url(restCore[0].getUrl()).post(body).build();
                    break;
            }

            try {
                Response response = httpClient.newCall(request).execute();
                String responseBody = response.body().string();
                // Since simple quotes are not allowed in JSON format we must parse the string first.
                responseBody = responseBody.replaceAll("'", "\"");

                // Clean bad formatted JSON string
                if (responseBody.startsWith("\"") && responseBody.endsWith("\"")) {
                    responseBody = responseBody.substring(1, responseBody.length() - 1);
                }
                restCore[0].setStatus(response.code());
                restCore[0].setMessage(response.message());
                restCore[0].setResponseStr(responseBody);
                data = new JSONObject(responseBody);
                restCore[0].setResponse(data);
            } catch (IOException | NullPointerException | JSONException e) {
                e.printStackTrace();
            }

            return restCore[0];
        }

        @Override
        protected void onPostExecute(RestCoreRequest response) {
            restCoreAsync = null;
            restCoreJob.requestDone(response);
        }

        @Override
        protected void onCancelled() {
            restCoreAsync = null;
            restCoreJob.requestDone(null);
        }
    }

    /**
     * Helper class to store doInBackground results.
     */
    public class RestCoreRequest implements Serializable {

        private static final long serialVersionUID = 1;

        private JSONObject response;
        private String responseStr;
        private String method;
        private int codeResult;
        private String url;
        private String params;
        private HashMap<String, String> queryParams;
        private int status;
        private String message;

        RestCoreRequest(String method, int codeResult, String url) {
            this.method = method;
            this.codeResult = codeResult;
            this.url = url;
        }

        public JSONObject getResponse() {
            return response;
        }

        public void setResponse(JSONObject response) {
            this.response = response;
        }

        public String getResponseStr() {
            return responseStr;
        }

        public void setResponseStr(String responseStr) {
            this.responseStr = responseStr;
        }

        public String getMethod() {
            return method;
        }

        public int getCodeResult() {
            return codeResult;
        }

        public String getUrl() {
            return url;
        }

        public String getParams() {
            return params;
        }

        public void setParams(String params) {
            this.params = params;
        }

        public HashMap<String, String> getQueryParams() {
            return queryParams;
        }

        public void setQueryParams(HashMap<String, String> queryParams) {
            this.queryParams = queryParams;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * Functional Interface to handle complete responses.
     */
    public interface RestCoreJob {
        void requestDone(RestCoreRequest response);
    }
}
