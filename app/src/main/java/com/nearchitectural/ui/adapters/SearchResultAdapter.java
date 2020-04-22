package com.nearchitectural.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.nearchitectural.databinding.ListItemBinding;
import com.nearchitectural.ui.models.LocationModel;

import java.util.Comparator;
import java.util.List;

/* Author:  Kristiyan Doykov
 * Since:   13/12/19
 * Version: 1.0
 * Purpose: Handles operations for the search results recycler (i.e. a list of locations) for the search
 *          activity
 */
public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultViewHolder> {

    // Sorted list which ensures all locations are sorted by a provided comparator
    private final SortedList<LocationModel> mSortedList = new SortedList<>(LocationModel.class, new SortedList.Callback<LocationModel>() {

        @Override
        public void onInserted(int position, int count) {
            notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public int compare(LocationModel o1, LocationModel o2) {
            return mComparator.compare(o1, o2);
        }

        @Override
        public void onChanged(int position, int count) {
            notifyItemRangeChanged(position, count);
        }

        @Override
        public boolean areContentsTheSame(LocationModel oldItem, LocationModel newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areItemsTheSame(LocationModel item1, LocationModel item2) {
            return item1.getLocationInfo().getId().equals(item2.getLocationInfo().getId());
        }
    });

    private LayoutInflater mInflater; // Handles inflating the search results to the UI
    private Comparator<LocationModel> mComparator; // Comparator used to sort the location models

    public SearchResultAdapter(Context context, Comparator<LocationModel> comparator) {
        this.mInflater = LayoutInflater.from(context);
        this.mComparator = comparator;
        setHasStableIds(true);
    }

    // Adds model to sorted list
    public void add(LocationModel model) {
        mSortedList.add(model);
    }

    // Removes model from sorted list
    public void remove(LocationModel model) {
        mSortedList.remove(model);
    }

    // Adds multiple models to sorted list
    public void add(List<LocationModel> models) {
        mSortedList.addAll(models);
    }

    // Removes multiple models from sorted list
    public void remove(List<LocationModel> models) {
        mSortedList.beginBatchedUpdates();
        for (LocationModel model : models) {
            mSortedList.remove(model);
        }
        mSortedList.endBatchedUpdates();
    }

    // Replaces all models in list with new model
    public void replaceAll(List<LocationModel> models) {
        mSortedList.beginBatchedUpdates();
        for (int i = mSortedList.size() - 1; i >= 0; i--) {
            final LocationModel model = mSortedList.get(i);
            if (!models.contains(model)) {
                mSortedList.remove(model);
            }
        }
        mSortedList.addAll(models);
        mSortedList.endBatchedUpdates();
    }

    @NonNull
    @Override
    public SearchResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final ListItemBinding binding = ListItemBinding.inflate(mInflater, parent, false);
        return new SearchResultViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(SearchResultViewHolder holder, int position) {
        final LocationModel model = mSortedList.get(position);
        holder.performBind(model);
    }

    @Override
    public int getItemCount() {
        return mSortedList.size();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull SearchResultViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public long getItemId(int position) {
        return mSortedList.get(position).hashCode();
    }
}