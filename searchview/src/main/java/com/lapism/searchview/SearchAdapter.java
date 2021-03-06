package com.lapism.searchview;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ResultViewHolder> implements Filterable{

    private SearchHistoryTable mHistoryDatabase;
    private Integer mDatabaseKey = null;
    private CharSequence mKey = "";
    private List<SearchItem> mSuggestions = new ArrayList<>();
    private List<SearchItem> mResults = new ArrayList<>();
    private List<SearchItem> mDatabase = new ArrayList<>();
    private OnSearchItemClickListener mListener;

    public SearchAdapter(Context context) {
        mHistoryDatabase = new SearchHistoryTable(context);
    }

    public SearchAdapter(Context context, List<SearchItem> suggestions) {
        mHistoryDatabase = new SearchHistoryTable(context);
        mDatabase = mHistoryDatabase.getAllItems(mDatabaseKey);
        mSuggestions = suggestions;
    }

    @Override
    public ResultViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View view = inflater.inflate(R.layout.search_item, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ResultViewHolder viewHolder, int position) {
        final SearchItem item = mResults.get(position);

        viewHolder.icon.setImageResource(item.getIconResource());
        viewHolder.icon.setColorFilter(SearchView.getIconColor(), PorterDuff.Mode.SRC_IN);
        viewHolder.text.setTypeface((Typeface.create(SearchView.getTextFont(), SearchView.getTextStyle())));
        viewHolder.text.setTextColor(SearchView.getTextColor());

        String itemText = item.getText().toString();
        String itemTextLower = itemText.toLowerCase(Locale.getDefault());

        if (itemTextLower.contains(mKey) && !mKey.toString().isEmpty()) {
            SpannableString s = new SpannableString(itemText);
            s.setSpan(new ForegroundColorSpan(SearchView.getTextHighlightColor()), itemTextLower.indexOf(mKey.toString()), itemTextLower.indexOf(mKey.toString()) + mKey.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            viewHolder.text.setText(s, TextView.BufferType.SPANNABLE);
        } else {
            viewHolder.text.setText(item.getText());
        }
    }

    @Override
    public int getItemCount() {
        return mResults.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public List<SearchItem> getSuggestionsList() {
        return mSuggestions;
    }

    public void setSuggestionsList(List<SearchItem> suggestionsList) {
        mSuggestions = suggestionsList;
    }

    public List<SearchItem> getResultList() {
        return mResults;
    }

    public void setDatabaseKey(Integer key) {
        mDatabaseKey = key;
    }

    public void setOnSearchItemClickListener(OnSearchItemClickListener listener) {
        mListener = listener;
    }

    void filter(CharSequence constraint) {
        List<SearchItem> results = new ArrayList<>();

        mKey = constraint;

        if (!TextUtils.isEmpty(mKey)) {
            mKey = constraint.toString().toLowerCase(Locale.getDefault());

            List<SearchItem> history = new ArrayList<>();

            if (!mDatabase.isEmpty()) {
                history.addAll(mDatabase);
            }

            if (!mSuggestions.isEmpty()) {
                history.addAll(mSuggestions);
            }

            if (!history.isEmpty()) {
                for (SearchItem item : history) {
                    String string = item.getText().toString().toLowerCase(Locale.getDefault());
                    if (string.contains(mKey)) {
                        results.add(item);
                    }
                }
            }
        } else {
            if (!mDatabase.isEmpty()) {
                results = mDatabase;
            }
        }

        if (!results.isEmpty() && results.size() > 0) {
            // mResults.clear();
            // mResults = results;
            // notifyDataSetChanged();
            setData(results);
        }
    }

    private void setData(List<SearchItem> data) {
        if (mResults.size() == 0) {
            mResults = data;
            // notifyDataSetChanged();
            if (data.size() != 0) {
                notifyItemRangeInserted(0, mResults.size());
            }
        } else {
            int previousSize = mResults.size();
            int nextSize = data.size();
            mResults = data;
            if (previousSize == nextSize && nextSize != 0)
                notifyItemRangeChanged(0, previousSize);
            else if (previousSize > nextSize) {
                if (nextSize == 0)
                    notifyItemRangeRemoved(0, previousSize);
                else {
                    notifyItemRangeChanged(0, nextSize);
                    notifyItemRangeRemoved(nextSize - 1, previousSize);
                }
            } else {
                notifyItemRangeChanged(0, previousSize);
                notifyItemRangeInserted(previousSize, nextSize - previousSize);
            }
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                // FilterResults filterResults = new FilterResults();
                return null;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

            }
        };
    }

    /*List<?> result = (ArrayList<?>) results.values;
    for (Object object : result) {
        if (object instanceof SearchItem) {
            mResults.add((SearchItem) object);
        }
    }*/

    // viewHolder.itemView.setOnClickListener

    public interface OnSearchItemClickListener {
        void onSearchItemClick(View view, int position);
    }

    // static
    // @Nullable Integer position)
    class ResultViewHolder extends RecyclerView.ViewHolder {

        final ImageView icon;
        final TextView text;

        ResultViewHolder(View view) {
            super(view);
            view.setOnClickListener(v -> {
                if (mListener != null) {
                    mListener.onSearchItemClick(v, getAdapterPosition());
                }
            });
            icon = (ImageView) view.findViewById(R.id.imageView);
            text = (TextView) view.findViewById(R.id.textView);
        }
    }

}
