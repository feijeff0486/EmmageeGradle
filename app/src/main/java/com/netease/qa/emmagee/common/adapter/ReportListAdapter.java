package com.netease.qa.emmagee.common.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.qa.emmagee.R;
import com.netease.qa.emmagee.activity.TestListActivity;
import com.netease.qa.emmagee.activity.TestReportActivity;
import com.netease.qa.emmagee.service.EmailSendService;
import com.netease.qa.emmagee.utils.Constants;
import com.netease.qa.emmagee.utils.Settings;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * customizing adapter.
 *
 * @author andrewleo
 */
public class ReportListAdapter extends RecyclerView.Adapter<ReportListAdapter.ReportVH> {
    private static final String TAG = Constants.LOG_PREFIX + "ReportListAda";
    private List<String> mDatas;

    private String sender, recipients;

    public ReportListAdapter(List<String> data) {
        mDatas = data;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        readSettingInfo(recyclerView.getContext()).subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io()).subscribe();
    }

    @NonNull
    @Override
    public ReportVH onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.test_list_item,
                viewGroup, false);
        return new ReportVH(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ReportVH holder, int position) {
        holder.tvName.setText(getItem(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(v.getContext(), TestReportActivity.class);
                    intent.putExtra(TestListActivity.CSV_PATH_KEY, getCSVPath(holder.getAdapterPosition()));
                    v.getContext().startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showDialog(v.getContext(), holder.getAdapterPosition());
                return true;
            }
        });
        holder.itemView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
                    showSendEmailDialog(v.getContext(), holder.getAdapterPosition());
                }
                return false;
            }
        });
    }

    private AlertDialog mSendEmailDialog;

    private void showSendEmailDialog(final Context context, final int position) {
        if (mSendEmailDialog != null) {
            mSendEmailDialog.dismiss();
        }
        mSendEmailDialog = new AlertDialog.Builder(context)
                .setTitle("是否通过邮件发送该条测试报告？")
                .setMessage(getCSVPath(position) + "\n\nsend to: " + recipients + "\nby: " + sender)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            Intent intent = new Intent(context, EmailSendService.class);
                            intent.putExtra("attachment", getCSVPath(position));
                            context.startService(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mSendEmailDialog.dismiss();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSendEmailDialog.dismiss();
                    }
                })
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                            mSendEmailDialog.dismiss();
                            return true;
                        }
                        return false;
                    }
                })
                .setCancelable(true)
                .create();
        mSendEmailDialog.show();
    }

    /**
     * read configuration file.
     */
    private Observable<Boolean> readSettingInfo(final Context context) {
        return Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(ObservableEmitter<Boolean> emitter) throws Exception {
                SharedPreferences preferences = Settings
                        .getDefaultSharedPreferences(context.getApplicationContext());
                sender = preferences.getString(Settings.KEY_SENDER, "");
                recipients = preferences.getString(Settings.KEY_RECIPIENTS, "");
                Log.i(TAG, String.format("readSettingInfo#subscribe: sender[%s],recipients[%s]",
                        sender, recipients));
                emitter.onNext(true);
                emitter.onComplete();
            }
        });
    }

    private AlertDialog mDeleteDialog;

    private void showDialog(final Context context, final int position) {
        if (mDeleteDialog != null) {
            mDeleteDialog.dismiss();
        }
        mDeleteDialog = new AlertDialog.Builder(context)
                .setTitle("是否删除该条测试报告？")
                .setMessage(getCSVPath(position) + "")
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteItem(context, position);
                        mDeleteDialog.dismiss();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDeleteDialog.dismiss();
                    }
                })
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_BACK) {
                            mDeleteDialog.dismiss();
                            return true;
                        }
                        return false;
                    }
                })
                .setCancelable(true)
                .create();
        mDeleteDialog.show();
    }

    private void deleteItem(Context context, int position) {
        String fileName = getCSVPath(position);
        Log.e(TAG, "deleteItem: todo delete " + fileName);
        boolean result = FileUtils.deleteQuietly(new File(fileName));
        Log.i(TAG, String.format("deleteItem: delete %s!", result ? "succeed" : "failed"));
        if (result) {
            mDatas.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, getItemCount() - position);
            Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    public String getItem(int position) {
        if (mDatas == null) {
            return null;
        }
        return mDatas.get(position);
    }

    public String getCSVPath(int position) {
        return Settings.EMMAGEE_RESULT_DIR + getItem(position) + ".csv";
    }

    static class ReportVH extends RecyclerView.ViewHolder {
        TextView tvName;

        ReportVH(@NonNull View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.package_name);
        }
    }
}
