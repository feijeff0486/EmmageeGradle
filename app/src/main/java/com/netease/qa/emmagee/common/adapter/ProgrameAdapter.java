package com.netease.qa.emmagee.common.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.netease.qa.emmagee.R;
import com.netease.qa.emmagee.utils.Constants;
import com.netease.qa.emmagee.utils.Programe;

import java.util.List;

/**
 * @author Jeff
 * @date 2021/04/16
 *
 * <a href="mailto:feijeff0486@gmail.com">Contact me</a>
 * <a href="https://github.com/feijeff0486">Follow me</a>
 */
public class ProgrameAdapter extends RecyclerView.Adapter<ProgrameAdapter.VH> {
    private static final String TAG = Constants.LOG_PREFIX + "ProgrameAdapter";

    private List<Programe> mProgrames;
    public Programe checkedProg;
    int lastCheckedPosition = -1;

    public ProgrameAdapter(List<Programe> programes) {
        this.mProgrames = programes;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item, viewGroup, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final VH vh, int position) {
        Programe pr = getItem(position);
        vh.imgViAppIcon.setImageDrawable(pr.getIcon());
        vh.txtAppName.setText(pr.getProcessName());
        vh.rdoBtnApp.setId(position);
        vh.rdoBtnApp.setChecked(checkedProg != null && getItem(position) == checkedProg);

        vh.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vh.rdoBtnApp.setChecked(!vh.rdoBtnApp.isChecked());
            }
        });

        vh.rdoBtnApp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.i(TAG, String.format("onCheckedChanged: position=%s,isChecked=%s", vh.getAdapterPosition(), isChecked));
                if (isChecked) {
                    checkedProg = getItem(vh.getAdapterPosition());
                    if (lastCheckedPosition != -1 && lastCheckedPosition != vh.getAdapterPosition()) {
                        notifyItemChanged(lastCheckedPosition);
                        Log.i(TAG, "onCheckedChanged: notifyItemChanged: " + lastCheckedPosition);
                    }
                    lastCheckedPosition = vh.getAdapterPosition();
                    Log.i(TAG, "onCheckedChanged: lastCheckedPosition= " + lastCheckedPosition);
                } else {
                    if (lastCheckedPosition == vh.getAdapterPosition()) {
                        Log.i(TAG, "onCheckedChanged: cancel checked.");
                        //点击自身取消选中
                        lastCheckedPosition = -1;
                        checkedProg = null;
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mProgrames == null ? 0 : mProgrames.size();
    }

    public Programe getItem(int position) {
        if (mProgrames == null) return null;
        return mProgrames.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void swapItems(List<Programe> programes) {
        this.mProgrames = programes;
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {

        TextView txtAppName;
        ImageView imgViAppIcon;
        RadioButton rdoBtnApp;

        public VH(@NonNull View itemView) {
            super(itemView);
            itemView.setFocusable(true);
            itemView.setFocusableInTouchMode(true);

            imgViAppIcon = (ImageView) itemView.findViewById(R.id.image);
            txtAppName = (TextView) itemView.findViewById(R.id.text);
            rdoBtnApp = (RadioButton) itemView.findViewById(R.id.rb);
            rdoBtnApp.setFocusable(false);
            rdoBtnApp.setFocusableInTouchMode(false);
        }
    }
}
