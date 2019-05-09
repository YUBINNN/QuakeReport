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

/**
 * 地震类
 */
public class Earthquake {

    /** 地震等级 */
    private double mMagnitude;

    /** 地震地点 */
    private String mLocation;

    /** 地震时间 */
    private long mTimeInMilliseconds;

    /** 详细信息的网页URL */
    private String mUrl;

    /** 构造方法 */
    public Earthquake(double magnitude, String location, long timeInMilliseconds, String url) {
        mMagnitude = magnitude;
        mLocation = location;
        mTimeInMilliseconds = timeInMilliseconds;
        mUrl = url;
    }

    /**
     * 获取地震等级
     */
    public double getMagnitude() {
        return mMagnitude;
    }

    /**
     * 获取地震地点
     */
    public String getLocation() {
        return mLocation;
    }

    /**
     * 获取地震时间
     */
    public long getTimeInMilliseconds() {
        return mTimeInMilliseconds;
    }

    /**
     * 获取地震详细网页URL
     */
    public String getUrl() {
        return mUrl;
    }
}
