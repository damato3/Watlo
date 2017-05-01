package com.lowlightstudios.watlo.services;

import android.content.Context;

import com.lowlightstudios.watlo.R;
import com.lowlightstudios.watlo.core.RestCore;
import com.lowlightstudios.watlo.core.Utils;
import com.lowlightstudios.watlo.models.InfoCard;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class WaterService implements RestCore.RestCoreJob {
    private RestCore rest;
    private Context context;
    private ServiceResponse serviceResponseInterface;

    public WaterService(Context context, Object classContext) {
        if (classContext instanceof ServiceResponse) {
            this.serviceResponseInterface = (ServiceResponse) classContext;
        } else {
            throw new RuntimeException(classContext.toString()
                    + " must implement ServiceResponse");
        }
        this.context = context;
        rest = new RestCore(this);
        rest.setScheme("https");
    }

    public void requestWaterNews(int nid, int page) {
        HashMap<String, String> watParams = new HashMap<>();
        if (nid > 0) {

        }
        else {
            watParams.put("topics[]", "3135");
            watParams.put("type", "ubernode");
            watParams.put("page", "" + (page > 0 ? page : 0));
            watParams.put("limit", "5");
        }

        rest.setHost(context.getResources().getString(R.string.nasa_host));
        rest.httpGet(context.getResources().getString(R.string.nasa_news_water_api), watParams, Utils.NEWS_WATER_RESULT);
    }

    public void requestGroundWater(double longX, double latY, double longY, double latX) {
        rest.setHost(context.getResources().getString(R.string.groundwater_host));
        HashMap<String, String> watParams = new HashMap<>();
        watParams.put("bBox", String.format(Locale.getDefault(), "%.4f", longX) + "," +
                String.format(Locale.getDefault(), "%.4f", latY) + "," + String.format(Locale.getDefault(), "%.4f", longY) + "," +
                String.format(Locale.getDefault(), "%.4f", latX));
        watParams.put("period", "P1D");
        watParams.put("format", "json");
        rest.cancel();
        rest.httpGet("nwis/iv", watParams, Utils.GROUND_WATER_RESULT);
    }

    public void cancelCurrentRequest() {
        rest.cancel();
    }

    @Override
    public void requestDone(RestCore.RestCoreRequest response) {
        if (response.getStatus() == 200) {
            JSONObject objectResponse = response.getResponse();

            if (objectResponse != null) {
                switch (response.getCodeResult()) {
                    case Utils.GROUND_WATER_RESULT:
                        break;
                    case Utils.NEWS_WATER_RESULT:

                        break;
                }
            }
        }

        this.serviceResponseInterface.onResponseComplete(response.getCodeResult());
    }

    public interface ServiceResponse {
        void onLoadPoints(int codeResult);

        void onLoadedNews(InfoCard infoCard);
    }
}
