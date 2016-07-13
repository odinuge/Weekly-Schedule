/*
 * Copyright (c) 2015 Odin Ugedal
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ugedal.weeklyschedule;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.WeekViewHolder> {

    private List<Schedule> scheduleList;
    private ListFragment mContext;

    public ScheduleAdapter(List<Schedule> contactList, ListFragment mContext) {
        this.scheduleList = contactList;
        this.mContext = mContext;
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    @Override
    public void onBindViewHolder(WeekViewHolder contactViewHolder, int i) {
        Schedule curr = scheduleList.get(i);
        contactViewHolder.title.setText(curr.getTitle());
        contactViewHolder.info.setText(curr.getInfo());

        // OnClickListener for the cards
        contactViewHolder.setClickListener(new ItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                mContext.openPDF(scheduleList.get(position));
            }
        });

        if (curr.getWeekNumber().isEmpty()) {
            contactViewHolder.bigNumber.setVisibility(View.GONE);
            return;
        }
        contactViewHolder.bigNumber.setVisibility(View.VISIBLE);
        contactViewHolder.bigNumber.setText(curr.getWeekNumber());

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
            this.title = (TextView) v.findViewById(R.id.title);
            this.info = (TextView) v.findViewById(R.id.info);
            this.bigNumber = (TextView) v.findViewById(R.id.big_number);
            v.setOnClickListener(this);
        }

        public void setClickListener(ItemClickListener itemClickListener) {
            this.clickListener = itemClickListener;
        }

        @Override
        public void onClick(View view) {
            clickListener.onClick(view, getAdapterPosition(), false);
        }
    }


}
