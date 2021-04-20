/*
 * Copyright (c) 2012-2013 NetEase, Inc. and other contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package com.netease.qa.emmagee.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.qa.emmagee.R;
import com.netease.qa.emmagee.common.adapter.ReportListAdapter;
import com.netease.qa.emmagee.utils.Constants;
import com.netease.qa.emmagee.utils.RxSubscribe;
import com.netease.qa.emmagee.utils.RxSubscriptions;
import com.netease.qa.emmagee.utils.Settings;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Test Report
 *
 * @author andrewleo
 */
public class TestListActivity extends Activity {

    private static final String LOG_TAG = Constants.LOG_PREFIX + TestListActivity.class.getSimpleName();
    public static final String CSV_PATH_KEY = "csvPath";

    private ReportListAdapter mAdapter;
    private RecyclerView lstViReport;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.test_list);

        TextView title = (TextView) findViewById(R.id.nb_title);
        lstViReport = (RecyclerView) findViewById(R.id.test_list);
        ImageView btnSave = (ImageView) findViewById(R.id.btn_set);

        btnSave.setVisibility(ImageView.INVISIBLE);
        title.setText(R.string.test_report);

        LinearLayout layGoBack = (LinearLayout) findViewById(R.id.lay_go_back);

        layGoBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                TestListActivity.this.finish();
            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        RxSubscribe.toSubscribe(Observable.create(new ObservableOnSubscribe<List<String>>() {
            @Override
            public void subscribe(ObservableEmitter<List<String>> emitter) throws Exception {
                emitter.onNext(listReports());
                emitter.onComplete();
            }
        })).subscribe(new Observer<List<String>>() {
            @Override
            public void onSubscribe(Disposable d) {
                RxSubscriptions.add(String.valueOf(TestListActivity.this), d);
            }

            @Override
            public void onNext(List<String> list) {
                if (list != null) {
                    mAdapter = new ReportListAdapter(list);
                } else {
                    mAdapter = new ReportListAdapter(new ArrayList<String>());
                }
                lstViReport.setAdapter(mAdapter);
                if (lstViReport.getLayoutManager() != null
                        && lstViReport.getLayoutManager().findViewByPosition(0) != null
                        && lstViReport.getLayoutManager().findViewByPosition(0).getVisibility() == View.VISIBLE) {
                    lstViReport.getLayoutManager().findViewByPosition(0).requestFocus();
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    protected void onDestroy() {
        RxSubscriptions.dispose(String.valueOf(this));
        super.onDestroy();
    }

    /**
     * list all test report
     */
    private List<String> listReports() {
        List<String> reportList = new ArrayList<String>();
        File reportDir = new File(Settings.EMMAGEE_RESULT_DIR);
        if (reportDir.isDirectory()) {
            File[] files = reportDir.listFiles();
            Arrays.sort(files, Collections.reverseOrder());
            for (File file : files) {
                if (isLegalReport(file)) {
                    String baseName = file.getName().substring(0, file.getName().lastIndexOf("."));
                    reportList.add(baseName);
                }
            }
        }
        return reportList;
    }

    private boolean isLegalReport(File file) {
        return !file.isDirectory() && file.getName().endsWith(".csv");
    }
}
