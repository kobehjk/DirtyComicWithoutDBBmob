/*
 * Copyright 2016 Hippo Seven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blackman.ehviewer.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.blackman.ehviewer.EhApplication;

public abstract class EhActivity extends AppCompatActivity {

    private boolean mTrackStarted;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //添加当前activity到栈中
        ((EhApplication) getApplication()).registerActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //从activity栈中去掉
        ((EhApplication) getApplication()).unregisterActivity(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        //开始检测
//        if (Settings.getEnableAnalytics()) {
//            EasyTracker.getInstance(this).activityStart(this);
//            mTrackStarted = true;
//        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //停止检测
//        if (mTrackStarted) {
//            EasyTracker.getInstance(this).activityStop(this);
//            mTrackStarted = false;
//        }
    }
}
