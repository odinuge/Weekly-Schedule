package com.ugedal.ukeplanappen;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by odin on 11/8/15.
 */
public class WeekAdapter extends RecyclerView.Adapter<WeekAdapter.WeekViewHolder> {

    private List<Week> contactList;
    private Context mContext;

    public WeekAdapter(List<Week> contactList, Context mContext) {
        this.contactList = contactList;
        this.mContext = mContext;
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    @Override
    public void onBindViewHolder(WeekViewHolder contactViewHolder, int i) {
            Week curr = contactList.get(i);
            contactViewHolder.title.setText(curr.getTitle());
            contactViewHolder.info.setText(curr.getInfo());

            if (curr.getWeekNumber()== -1){
                contactViewHolder.bigNumber.setVisibility(View.INVISIBLE);
            } else {
                contactViewHolder.bigNumber.setVisibility(View.VISIBLE);
                contactViewHolder.bigNumber.setText(Integer.toString(curr.getWeekNumber()));
            }
            contactViewHolder.setClickListener(new ItemClickListener() {
                @Override
                public void onClick(View view, int position, boolean isLongClick) {
                    ((ListFragment)((MainActivity) mContext).getSupportFragmentManager().findFragmentById(R.id.list_fragment))
                            .openPDF(contactList.get(position));

                }
            });
    }

    @Override
    public WeekViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_view, viewGroup, false);

        return new WeekViewHolder(itemView);
    }
    public static class WeekViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView title;
        protected TextView info;
        protected TextView bigNumber;

        private ItemClickListener clickListener;

        public WeekViewHolder(View v) {
            super(v);
            this.title =  (TextView) v.findViewById(R.id.title);
            this.info = (TextView)  v.findViewById(R.id.info);
            this.bigNumber = (TextView) v.findViewById(R.id.big_number);
            v.setOnClickListener(this);
        }
        public void setClickListener(ItemClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }
        @Override
        public void onClick(View view) {
            clickListener.onClick(view, getPosition(), false);
        }
    }


}