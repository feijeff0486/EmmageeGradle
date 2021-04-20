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
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.qa.emmagee.R;
import com.netease.qa.emmagee.utils.Constants;
import com.netease.qa.emmagee.utils.RxSubscribe;
import com.netease.qa.emmagee.utils.RxSubscriptions;

import org.apache.commons.io.FileUtils;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * About Page of Emmagee
 *
 * @author andrewleo
 */
public class TestReportActivity extends Activity {

    private static final String TAG = Constants.LOG_PREFIX + TestReportActivity.class.getSimpleName();
    private TableLayout tl;
    private String mCsvPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.test_report);

        TextView title = (TextView) findViewById(R.id.nb_title);
        ImageView btnSave = (ImageView) findViewById(R.id.btn_set);
        tl = (TableLayout) findViewById(R.id.table_layout);

        btnSave.setVisibility(ImageView.INVISIBLE);
        title.setText(R.string.test_report);

        LinearLayout layGoBack = (LinearLayout) findViewById(R.id.lay_go_back);

        layGoBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                TestReportActivity.this.finish();
            }
        });

        Intent intent = getIntent();
        mCsvPath = intent.getStringExtra(TestListActivity.CSV_PATH_KEY);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Log.i(TAG, "onPostCreate: ");
        RxSubscribe.toSubscribe(Observable.create(new ObservableOnSubscribe<String[]>() {
            @Override
            public void subscribe(ObservableEmitter<String[]> emitter) throws Exception {
                String content = FileUtils.readFileToString(new File(mCsvPath), "gbk");
                String[] lines = content.split("\r\n");
                emitter.onNext(lines);
                emitter.onComplete();
            }
        })).subscribe(new Observer<String[]>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.i(TAG, "onSubscribe: ");
                RxSubscriptions.add(String.valueOf(TestReportActivity.this), d);
            }

            @Override
            public void onNext(String[] lines) {
                if (lines != null && lines.length > 0) {
                    Log.i(TAG, "onNext: " + lines.length);
                    for (int i = 0; i < lines.length; i++) {
                        addTableRow(lines[i], i);
                    }
                } else {
                    Log.e(TAG, "onNext: no data!");
                    Toast.makeText(TestReportActivity.this, "没有数据！", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                Log.i(TAG, "onComplete: ");
            }
        });
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy: ");
        RxSubscriptions.dispose(String.valueOf(this));
        super.onDestroy();
    }

    private void addTableRow(String line, int index) {
        TableRow row = new TableRow(this);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(lp);
        String[] items = line.split(",");
        int i = 0;
        for (String item : items) {
            TextView tv = new TextView(this);
            tv.setTextColor(Color.BLACK);
            tv.setTextSize(18);
            tv.setText(item);
            tv.setBackgroundResource(R.drawable.table_border);
            if (i != 0) {
                tv.setGravity(Gravity.RIGHT);
            }
            row.addView(tv, i);
            i++;
        }
        tl.addView(row, index);
    }

}
