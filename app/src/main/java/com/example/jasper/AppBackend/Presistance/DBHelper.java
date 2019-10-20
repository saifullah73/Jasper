package com.example.jasper.AppBackend.Presistance;

import android.content.Context;
import android.util.Log;

import com.example.jasper.AppBackend.Interfaces.FetchCallbackListener;
import com.example.jasper.AppBackend.Interfaces.VolleyCallback;
import com.example.jasper.Constants;
import com.example.jasper.Models.MessageModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DBHelper {
    private static DBHelper instance;
    private Context context;

    public static DBHelper getInstance(Context context){
        if(instance == null){
            instance = new DBHelper(context);
        }
        return instance;
    }


    public DBHelper(Context context){
        this.context = context;
    }

    public void test(){
        DB fetch = new DB(context);
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        JSONObject jsonObject1 = new JSONObject();
        try {
            jsonObject.put("form_id","saif1");
            jsonObject.put("to_id","owais1");
            jsonObject.put("timestamp","1531160088000");
            jsonObject.put("msg","brobrorborbor");
            jsonObject1.put("form_id","owais1");
            jsonObject1.put("to_id","saif1");
            jsonObject1.put("timestamp","1561160088000");
            jsonObject1.put("msg","hohohoho");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        jsonArray.put(jsonObject);
        jsonArray.put(jsonObject1);
        Log.v("obj",jsonArray.toString());
        fetch.sendData(jsonArray);
    }

    public void putMessageInDB(String to,String from,String timestamp, String msg){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("form_id",from);
            jsonObject.put("to_id",to);
            jsonObject.put("timestamp",timestamp);
            jsonObject.put("msg",msg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendRequest(jsonObject);
    }

    private void sendRequest(JSONObject obj){
        DB fetch = new DB(context);
        JSONArray jsonArray = new JSONArray();
        jsonArray.put(obj);
        fetch.sendData(jsonArray);
    }

    public void getHistory(String from, String to, String duration, final FetchCallbackListener listener){
        final List<MessageModel> list = new ArrayList<>();
        DB fetch = new DB(context);
        fetch.getData(from, to, duration, new VolleyCallback() {
            @Override
            public void onSuccess(JSONArray result) {
                Log.i("DBTest",result.toString());
                JSONObject obj;
                try {
                    for (int i = 0; i < result.length(); i++) {
                        obj = result.getJSONObject(i);
                        if (obj.get("from_id").equals(Constants.currentUser)){
                            String msg = obj.getString("msg");
                            String timestamp = obj.getString("timestamp");
                            list.add(new MessageModel("sent",msg,timestamp,getMessageType(msg)));
                        }
                        else {
                            String msg = obj.getString("msg");
                            String timestamp = obj.getString("timestamp");
                            list.add(new MessageModel("received",msg,timestamp,getMessageType(msg)));
                        }
                    }
                    listener.onSuccess(list);
                }
                catch (Exception e){

                }
            }
        });
    }




    private String getMessageType(String msg){
        if(msg.contains("##image##")){
            return "image";
        }
        else if(msg.contains("##location##")){
            return "location";
        }
        else if(msg.contains("##file##")){
            return "file";
        }
        else{
            return "text";
        }

    }
}
