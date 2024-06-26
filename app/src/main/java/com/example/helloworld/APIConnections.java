package com.example.helloworld;

import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class APIConnections {

    // NGROK base url to be appended to the start
    private String ngrok = "https://37fe7fd5bbd1.ngrok.app";
    // Get the distance between two objects
    public void getDistance(RequestQueue queue, double latitude, double longitude) {
        String url = ngrok + "/myapp/distance/";

        JSONObject postData = new JSONObject();
        try {
            postData.put("latitude", latitude);
            postData.put("longitude", longitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, postData,
                response -> {
                    try {
                        double distanceMeters = response.getDouble("distance_meters");
                        Log.d("Distance", "Distance in meters: " + distanceMeters);
                        //tv.setText(String.valueOf(distanceMeters));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.d("Error.Response", error.toString());
                });
        queue.add(jsonObjectRequest);
    }

    // Get the closest bus stops to the user (for button)
    public void getClosestBusStops(RequestQueue queue, double latitude, double longitude, int radius, TextView tv, BusStopCallback callback) {
        String url = ngrok + "/myapp/transport/";
        StringBuilder sb = new StringBuilder();
        JSONObject postData = new JSONObject();
        try {
            postData.put("latitude", latitude);
            postData.put("longitude", longitude);
            postData.put("radius", radius);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postData,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        ArrayList<String> busStops = new ArrayList<>();
                        //System.out.println("length" + results.length());
                        // Get all the results from the response and print to main app
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject busStopObject = results.getJSONObject(i);
                            busStops.add(String.valueOf(busStopObject.optInt("osm_id")));
                            int gid = busStopObject.optInt("osm_id");
                            String name = busStopObject.optString("name");
                            // OSM id for unique identifier and name is the bus stop code
                            sb.append("OSM_ID: ").append(gid).append(", Name: ").append(name).append("\n");
                        }
                        tv.setText(sb.toString());
                        callback.onResult(results.length());
                        callback.onAllBusStops(busStops);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    tv.setText("No bus stops found");
                    Log.d("Error.Response", error.toString());
                });
        queue.add(request);
    }

    // Get the closest bus stops to the user (for API call)
    public void getClosestStops(RequestQueue queue, double latitude, double longitude, int radius, BusStopCallback callback) {
        String url = ngrok + "/myapp/transport/";
        JSONObject postData = new JSONObject();
        try {
            postData.put("latitude", latitude);
            postData.put("longitude", longitude);
            postData.put("radius", radius);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postData,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        ArrayList<String> busStops = new ArrayList<>();
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject busStopObject = results.getJSONObject(i);
                            busStops.add(String.valueOf(busStopObject.optInt("osm_id")));
                        }
                        callback.onResult(results.length());
                        callback.onAllBusStops(busStops);
                    } catch (JSONException e) {
                        e.printStackTrace();

                    }
                },
                error -> {
                    Log.d("Error.Response", error.toString());
                    ArrayList<String> busStops = new ArrayList<>();
                    busStops.add("0");
                    callback.onResult(0);
                    callback.onAllBusStops(busStops);
                });
        queue.add(request);
    }

    // Check if on a bus route
    public void OnBusRoute(RequestQueue queue, double latitude, double longitude, int radius, BusRouteCallback callback) {
        String url = ngrok + "/myapp/busRoute/";
        JSONObject postData = new JSONObject();
        try {
            postData.put("latitude", latitude);
            postData.put("longitude", longitude);
            postData.put("radius", radius);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postData,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        ArrayList<String> busRoutes = new ArrayList<>();
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject jsonObject = results.getJSONObject(i);
                            busRoutes.add(jsonObject.getString("name"));
                        }
                        // Call back used to retrieve the results
                        callback.onResult(busRoutes);
                    } catch (JSONException e) {
                        e.printStackTrace();

                    }
                },
                error -> {
                    Log.d("Error.Response", error.toString());
                    ArrayList<String> busRoutes = new ArrayList<>();
                    callback.onResult(busRoutes);
                });
        queue.add(request);
    }

    // Get the buses that go to a particular bus stop
    public void GetBusses(RequestQueue queue, String stopCode, BusRouteCallback callback) {
        String url = ngrok + "/myapp/busData/";
        JSONObject postData = new JSONObject();
        try {
            postData.put("column_name", stopCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postData,
                response -> {
                    try {
                        JSONArray results = response.getJSONArray("results");
                        JSONArray busNumbersArray = results.getJSONArray(0);
                        ArrayList<String> busNumbers = new ArrayList<>();
                        for (int i = 0; i < busNumbersArray.length(); i++) {
                            busNumbers.add(busNumbersArray.getString(i));
                        }
                        // Callback used to retrieve the results
                        callback.onResult(busNumbers);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    Log.d("Error.Response", error.toString());
                    ArrayList<String> busRoutes = new ArrayList<>();
                    callback.onResult(busRoutes);
                });
        queue.add(request);
    }

    // Post a new record to the PG database
    public void postNewRecord(RequestQueue queue, JSONObject postData, TextView tv, Integer ID) {
        String url = ngrok + "/myapp/";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postData,
                response -> {
                    float speed = response.optLong("speed");
                    System.out.println("Returned response" + speed);
                    tv.setText(ID + ": Response returned SUCCESS");

                }, error -> {
                    tv.setText(ID + ": Response returned FAILURE");
                });
        queue.add(request);
    }

    // Deprecated predictions using the old ML algorithm
    public void GetPredictions(RequestQueue queue, JSONObject postData, TextView tv, PredictionCallback callback) {
        String url = ngrok + "/myapp/predict/";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, postData,
                response -> {
                    try {
                        String prediction = response.getString("prediction");
                        //System.out.println("Predicted activity type was: " + prediction);
                        tv.setText("Prediction: " + prediction);
                        callback.onResult(prediction);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }, error -> {
            tv.setText("Prediction encountered error");
        });
        queue.add(request);
    }
}
