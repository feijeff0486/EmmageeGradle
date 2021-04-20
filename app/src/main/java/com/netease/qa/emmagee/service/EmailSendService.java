package com.netease.qa.emmagee.service;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.netease.qa.emmagee.R;
import com.netease.qa.emmagee.utils.Constants;
import com.netease.qa.emmagee.utils.EncryptData;
import com.netease.qa.emmagee.utils.MailSender;
import com.netease.qa.emmagee.utils.RxSubscriptions;
import com.netease.qa.emmagee.utils.Settings;
import com.netease.qa.emmagee.utils.StringUtils;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * send email on background service
 *
 * @author Jeff
 * @date 2021/04/19
 *
 * <a href="mailto:feijeff0486@gmail.com">Contact me</a>
 * <a href="https://github.com/feijeff0486">Follow me</a>
 */
public class EmailSendService extends Service {
    private static final String TAG = Constants.LOG_PREFIX + "EmailSendS";

    private String sender, password, recipients, smtp;
    private String[] receivers;
    private EncryptData des;

    private boolean isRunning = false;
    private Queue<String> mQueue = new LinkedBlockingQueue<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: ");
        readSettingInfo().subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io()).subscribe();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String attachment = intent.getStringExtra("attachment");
            Log.i(TAG, "onStartCommand: attachment= " + attachment);
            if (!StringUtils.isEmpty(attachment)) {
                mQueue.add(attachment);
            }
            if (!isRunning) {
                sendEmail();
            }
        }
        return START_STICKY;
    }


    /**
     * read configuration file.
     */
    private Observable<Boolean> readSettingInfo() {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                SharedPreferences preferences = Settings
                        .getDefaultSharedPreferences(getApplicationContext());
                sender = preferences.getString(Settings.KEY_SENDER, "");
                password = preferences.getString(Settings.KEY_PASSWORD, "");
                recipients = preferences.getString(Settings.KEY_RECIPIENTS, "");
                receivers = recipients.split("\\s+");
                smtp = preferences.getString(Settings.KEY_SMTP, "");
                Log.i(TAG, String.format("readSettingInfo#subscribe: sender[%s],password[%s],recipients[%s],receivers[%s],smtp[%s]",
                        sender, password, recipients, receivers, smtp));
                des = new EncryptData("emmagee");
                emitter.onNext(true);
                emitter.onComplete();
            }
        });
    }

    private void sendEmail() {
        isRunning = true;
        String item = mQueue.poll();
        if (item == null) {
            stopSelf();
            isRunning = false;
        } else {
            Observable.just(item).map(new Function<String, Boolean>() {
                @Override
                public Boolean apply(String fileName) throws Exception {
                    boolean isSendSuccessfully = false;
                    try {
                        isSendSuccessfully = MailSender.sendTextMail(sender,
                                des.decrypt(password), smtp,
                                "Emmagee Performance Test Report", "see attachment",
                                fileName, receivers);
                    } catch (Exception e) {
                        isSendSuccessfully = false;
                    }
                    return isSendSuccessfully;
                }
            }).doFinally(new Action() {
                @Override
                public void run() throws Exception {
                    Log.i(TAG, "doFinally: ");
                    sendEmail();
                }
            }).subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Boolean>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            RxSubscriptions.add(String.valueOf(EmailSendService.this), d);
                        }

                        @Override
                        public void onNext(Boolean success) {
                            if (success) {
                                Toast.makeText(EmailSendService.this,
                                        getString(R.string.send_success_toast) + recipients,
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(
                                        EmailSendService.this,
                                        getString(R.string.send_fail_toast)
                                                + EmmageeService.resultFilePath, Toast.LENGTH_LONG)
                                        .show();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(
                                    EmailSendService.this,
                                    getString(R.string.send_fail_toast)
                                            + EmmageeService.resultFilePath, Toast.LENGTH_LONG)
                                    .show();
                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: ");
        RxSubscriptions.dispose(String.valueOf(this));
        super.onDestroy();
    }
}
