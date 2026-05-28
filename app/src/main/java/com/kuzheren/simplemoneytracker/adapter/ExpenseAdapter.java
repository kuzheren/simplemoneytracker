package com.kuzheren.simplemoneytracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kuzheren.simplemoneytracker.R;
import com.kuzheren.simplemoneytracker.model.Expense;
import com.kuzheren.simplemoneytracker.util.Categories;
import com.kuzheren.simplemoneytracker.util.DateUtils;
import com.kuzheren.simplemoneytracker.util.MoneyFormat;

import java.util.ArrayList;
import java.util.List;

public class ExpenseAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnItemClick {
        void onClick(long expenseId);
    }

    public static class Header {
        public final String dateIso;
        public final double dayTotal;
        public Header(String dateIso, double dayTotal) {
            this.dateIso = dateIso;
            this.dayTotal = dayTotal;
        }
    }

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private final List<Object> rows = new ArrayList<>();
    private final OnItemClick listener;

    public ExpenseAdapter(OnItemClick listener) {
        this.listener = listener;
    }

    public void submit(List<Object> data) {
        rows.clear();
        rows.addAll(data);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position) instanceof Header ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            return new HeaderVH(inf.inflate(R.layout.item_date_header, parent, false));
        }
        return new ItemVH(inf.inflate(R.layout.item_expense, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object row = rows.get(position);
        if (holder instanceof HeaderVH) {
            Header h = (Header) row;
            ((HeaderVH) holder).tvDate.setText(DateUtils.formatHeader(h.dateIso));
            ((HeaderVH) holder).tvTotal.setText(MoneyFormat.format(h.dayTotal));
        } else {
            Expense e = (Expense) row;
            ItemVH vh = (ItemVH) holder;
            vh.tvCategory.setText(e.getCategory());
            String desc = e.getDescription();
            if (desc == null || desc.isEmpty()) {
                vh.tvDescription.setVisibility(View.GONE);
            } else {
                vh.tvDescription.setVisibility(View.VISIBLE);
                vh.tvDescription.setText(desc);
            }
            vh.tvAmount.setText("−" + MoneyFormat.format(e.getAmount()));
            vh.ivIcon.setImageResource(Categories.iconFor(e.getCategory()));
            vh.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onClick(e.getId());
            });
        }
    }

    @Override
    public int getItemCount() {
        return rows.size();
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        final TextView tvDate;
        final TextView tvTotal;
        HeaderVH(@NonNull View v) {
            super(v);
            tvDate = v.findViewById(R.id.tv_date);
            tvTotal = v.findViewById(R.id.tv_day_total);
        }
    }

    static class ItemVH extends RecyclerView.ViewHolder {
        final ImageView ivIcon;
        final TextView tvCategory;
        final TextView tvDescription;
        final TextView tvAmount;
        ItemVH(@NonNull View v) {
            super(v);
            ivIcon = v.findViewById(R.id.iv_icon);
            tvCategory = v.findViewById(R.id.tv_category);
            tvDescription = v.findViewById(R.id.tv_description);
            tvAmount = v.findViewById(R.id.tv_amount);
        }
    }
}
