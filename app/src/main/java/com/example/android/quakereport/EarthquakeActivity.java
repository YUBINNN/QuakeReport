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

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class EarthquakeActivity extends AppCompatActivity
        implements LoaderCallbacks<List<Earthquake>>,   //异步线程接口，需重写三个方法
        SharedPreferences.OnSharedPreferenceChangeListener {       //SharedPreferences存储内容监听

    private static final String LOG_TAG = EarthquakeActivity.class.getName();

    /** 地震数据访问网址 */
    private static final String USGS_REQUEST_URL =
            "https://earthquake.usgs.gov/fdsnws/event/1/query";

    /**loader多线程id*/
    private static final int EARTHQUAKE_LOADER_ID = 1;

    /**地震数据listview适配器 */
    private EarthquakeAdapter mAdapter;

    /** 无数据下显示的文本 */
    private TextView mEmptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        // 初始化控件
        ListView earthquakeListView = (ListView) findViewById(R.id.list);
        // 初始化控件
        mEmptyStateTextView = (TextView) findViewById(R.id.empty_view);
        //设置listview无内容下显示mEmptyStateTextView
        earthquakeListView.setEmptyView(mEmptyStateTextView);

        // 初始化控件
        mAdapter = new EarthquakeAdapter(this, new ArrayList<Earthquake>());
        //listview使用适配器
        earthquakeListView.setAdapter(mAdapter);

        //获取SharedPreferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        // 注册SharedPreferences的监听，获得SharedPreferences的内容变化
        prefs.registerOnSharedPreferenceChangeListener(this);

        //监听listview的点击事件
        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // 获取点击的地震数据
                Earthquake currentEarthquake = mAdapter.getItem(position);

                // 获取网址
                Uri earthquakeUri = Uri.parse(currentEarthquake.getUrl());

                // 建立跳转到浏览器的Intent
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, earthquakeUri);

                // 开始活动跳转到浏览器页面
                startActivity(websiteIntent);
            }
        });

        // 获取系统网络管理服务
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        // 获取网络状况的数据
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // 判断网络是否连接,开启网络访问线程
        if (networkInfo != null && networkInfo.isConnected()) {
            // 获得多线程LoaderManager
            LoaderManager loaderManager = getLoaderManager();
            //初始化loader
            // loader接口监听回调
            loaderManager.initLoader(EARTHQUAKE_LOADER_ID, null, this);
        } else {

            //隐藏加载进度条
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            // 显示文本
            mEmptyStateTextView.setText(R.string.no_internet_connection);
        }
    }

    /**
     * SharedPreferences内容发生变化，重新加载内容
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        //键判断，清除内容，重写加载
        if (key.equals(getString(R.string.settings_min_magnitude_key)) ||
                key.equals(getString(R.string.settings_order_by_key))){
            mAdapter.clear();
            // 隐藏无内容下的文本
            mEmptyStateTextView.setVisibility(View.GONE);

            // 显示progressbar
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.VISIBLE);

            // 重启Loader,加载内容
            getLoaderManager().restartLoader(EARTHQUAKE_LOADER_ID, null, this);
        }
    }

    /**创建Loader*/
    @Override
    public Loader<List<Earthquake>> onCreateLoader(int i, Bundle bundle) {
        //获取一个SharedPreferences实例，该实例指向给定上下文中的首选项框架使用的默认文件。
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String minMagnitude = sharedPrefs.getString(
                getString(R.string.settings_min_magnitude_key),
                getString(R.string.settings_min_magnitude_default));

        String orderBy = sharedPrefs.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default)
        );

        Uri baseUri = Uri.parse(USGS_REQUEST_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        uriBuilder.appendQueryParameter("format", "geojson");
        uriBuilder.appendQueryParameter("limit", "10");
        uriBuilder.appendQueryParameter("minmag", minMagnitude);
        uriBuilder.appendQueryParameter("orderby", orderBy);

        return new EarthquakeLoader(this, uriBuilder.toString());
    }
    /**Loader加载内容完成*/
    @Override
    public void onLoadFinished(Loader<List<Earthquake>> loader, List<Earthquake> earthquakes) {
        // 隐藏progressBar。隐藏progressBar加载布局是自动出现
        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        // 设置无内容下的显示
        mEmptyStateTextView.setText(R.string.no_earthquakes);

        //listview显示新内容
        if (earthquakes != null && !earthquakes.isEmpty()) {
            //EarthquakeAdapter extends ArrayAdapter<Earthquake>
            mAdapter.addAll(earthquakes);
        }
    }

    /**Loader重启*/
    @Override
    public void onLoaderReset(Loader<List<Earthquake>> loader) {
        // Loader reset, so we can clear out our existing data.
        mAdapter.clear();
    }
    /**创造actionbar的菜单*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        //返回false则不显示
        return true;
    }

    /**actionbar菜单的点击事件*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
