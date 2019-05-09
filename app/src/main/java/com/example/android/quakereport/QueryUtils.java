/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.quakereport;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * 访问网络工具类
 */
public final class QueryUtils {

    /** 获取类名称 */
    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**空参构造方法*/
    private QueryUtils() {
    }

    /**
     *工具类的外部调用方法方法
     */
    public static List<Earthquake> fetchEarthquakeData(String requestUrl) {
        // 创建URL对象
        URL url = createUrl(requestUrl);

        // 访问网站获得数据，读取，解析，获得字符串
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem making the HTTP request.", e);
        }

        // 从响应的字符串中获取需要的信息
        List<Earthquake> earthquakes = extractFeatureFromJson(jsonResponse);

        // 返回需要的地震集合
        return earthquakes;
    }

    /**
     * 把字符串封装为URL对象
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Problem building the URL ", e);
        }
        return url;
    }

    /**
     * 连接网站，获取数据返回
     */
    private static String makeHttpRequest(URL url) throws IOException {

        String jsonResponse = "";
        // URL判断，提前返回
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            //网络连接设置
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */); // 10秒 连接主机的超时时间（单位：毫秒）
            urlConnection.setConnectTimeout(15000 /* milliseconds */);// 15秒 从主机读取数据的超时时间（单位：毫秒）
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // 返回码200，表示网络响应正常，获取输入流，并解析为字符串
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the earthquake JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                //最后关闭输入流
                inputStream.close();
            }
        }

        return jsonResponse;
    }

    /**
     * 读取网络输入流数据，转为字符串返回
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            //读取输入流，还原为字符
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            //全部读取为字符，封装为BufferedReader
            BufferedReader reader = new BufferedReader(inputStreamReader);
            //取出，装在StringBuilder
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }

        //转为字符串返回
        return output.toString();
    }

    /**
     * JSON数据解析，从中获取想要的数据信息
     */
    private static List<Earthquake> extractFeatureFromJson(String earthquakeJSON) {
        // 判断是否为空和Null
        if (TextUtils.isEmpty(earthquakeJSON)) {
            return null;
        }

        List<Earthquake> earthquakes = new ArrayList<>();

        // 解析JSON数据，一级级访问获取
        try {
            // 创建基类JSONObject
            JSONObject baseJsonResponse = new JSONObject(earthquakeJSON);
            // 获得次级JSONObject组
            JSONArray earthquakeArray = baseJsonResponse.getJSONArray("features");
            // 遍历，获取具体数据
            for (int i = 0; i < earthquakeArray.length(); i++) {
                //单个地震数据JSONObject
                JSONObject currentEarthquake = earthquakeArray.getJSONObject(i);
                //获取单个地震数据 JSONObject 下的properties JSONObject
                JSONObject properties = currentEarthquake.getJSONObject("properties");
                //获取具体的数据
                double magnitude = properties.getDouble("mag");
                String location = properties.getString("place");
                long time = properties.getLong("time");
                String url = properties.getString("url");
                //封装为Earthquake
                Earthquake earthquake = new Earthquake(magnitude, location, time, url);
                // 加进地震list集合
                earthquakes.add(earthquake);
            }
        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the earthquake JSON results", e);
        }

        // 数据list集合earthquakes
        return earthquakes;
    }
}
