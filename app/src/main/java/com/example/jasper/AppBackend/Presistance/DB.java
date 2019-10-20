package com.example.jasper.AppBackend.Presistance;


import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.jasper.AppBackend.Interfaces.VolleyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DB {


    Context context;
    //BASE URL
    String url = "http://192.168.10.8:3000/";

    //SPECIFIC ENDPOINTS
    String endpointPOST = "store";
    String endpointSEND = "gethistory/";
    JSONArray data;

    RequestQueue requestQueue;

    public DB(Context context) {
        this.context = context;
        requestQueue = Volley.newRequestQueue(context);
    }

    /**
     * DATA TYPES:
     * STRING ( FROM_ID, TO_ID, DURATION)
     * Duration should be specified using hyphens in dd-mm-yy format
     ***/
    public JSONArray getData(String from_id, String to_id, String duration,final VolleyCallback callback){
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url + endpointSEND + from_id+"/" +to_id+"/"+duration, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                callback.onSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ERROR1",error.toString());
            }
        }
        );
        requestQueue.add(jsonArrayRequest);
        return data;
    }


    public boolean sendData(JSONArray obj){
        JsonArrayRequest jobReq = new JsonArrayRequest(Request.Method.POST, url + endpointPOST, obj,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray jsonArray) {
                        Log.e("HALA","Success");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        volleyError.printStackTrace();
                        Log.e("HALA","Failed" + volleyError.toString());
                    }
                });

        requestQueue.add(jobReq);
        return true;
    }

}

