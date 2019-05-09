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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

/**
 * 偏好设置活动类。android Preference的使用
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //加载一个fragment类型的主布局
        setContentView(R.layout.settings_activity);
    }



    /**
     * //内部类，实现fragent布局
     *  PreferenceFragment以列表形式显示Preference对象的层次结构。 这些首选项会在用户与它们交互时自动保存到SharedPreferences 。
     *   Preference.OnPreferenceChangeListener当用户更改了此Preference的值并且即将设置和/或Preference时，将调用回调的接口定义。
     */
    public static class EarthquakePreferenceFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {

        /**
         * 创建Fragment
         * @param savedInstanceState
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //加载PreferenceScreen主布局，即设置相关组件
            addPreferencesFromResource(R.xml.settings_main);

            //初始化ListPreference
            Preference minMagnitude = findPreference(getString(R.string.settings_min_magnitude_key));
            bindPreferenceSummaryToValue(minMagnitude);
            //初始化EditTextPreference
            Preference orderBy = findPreference(getString(R.string.settings_order_by_key));
            bindPreferenceSummaryToValue(orderBy);
        }
        /**
         * 当用户更改了此Preference的值,显示其摘要
         * @param preference
         * @param value
         * @return
         */
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(stringValue);
                if (prefIndex >= 0) {
                    CharSequence[] labels = listPreference.getEntries();
                    //使用资源ID设置此首选项的摘要,即显示摘要内容
                    preference.setSummary(labels[prefIndex]);
                }
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }

        /**
         * 设置监听，获取SharedPreferences的值并显示摘要
         * @param preference 偏好
         */
        private void bindPreferenceSummaryToValue(Preference preference) {
            //监听
            preference.setOnPreferenceChangeListener(this);
            //创建 SharedPreferences. PreferenceFragment的创建方法
            SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            //获取键对应的值，没有则返回""
            String preferenceString = preferences.getString(preference.getKey(), "");
            //回调,SharedPreferences
            onPreferenceChange(preference, preferenceString);
        }
    }
}
