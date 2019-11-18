/*
 *  Copyright (c) 2018, Jaisel Rahman <jaiselrahman@gmail.com>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.jaiselrahman.filepicker.adapter;

import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jaiselrahman.filepicker.model.MediaFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class MultiSelectionAdapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {
    private static final String TAG = MultiSelectionAdapter.class.getSimpleName();
    private ArrayList<MediaFile> selectedItems = new ArrayList<>();
    private ArrayList<MediaFile> mediaFiles;

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private OnSelectionListener<VH> customOnSelectionListener;
    private boolean isSelectionStarted = false;
    private boolean enabledSelection = false;
    private boolean isSingleClickSelection = false;
    private boolean singleChoiceMode = false;
    private int maxSelection = -1;
    private int itemStartPosition = 0;
    private OnSelectionListener<VH> onSelectionListener = new OnSelectionListener<VH>() {
        @Override
        public void onSelectionBegin() {
            isSelectionStarted = true;
            if (!singleChoiceMode && customOnSelectionListener != null)
                customOnSelectionListener.onSelectionBegin();
        }

        @Override
        public void onSelected(VH viewHolder, int position) {
            if (singleChoiceMode && selectedItems.size() > 0) {
                int pos = mediaFiles.indexOf(selectedItems.get(0));
                if (pos >= 0) {
                    removeSelection(pos);
                    handleItemChanged(pos);
                }
            }
            if (maxSelection > 0 && selectedItems.size() >= maxSelection) {
                onMaxReached();
                return;
            }
            setItemSelected(viewHolder.itemView, position, true);
            if (customOnSelectionListener != null)
                customOnSelectionListener.onSelected(viewHolder, position);
        }

        @Override
        public void onUnSelected(VH viewHolder, int position) {
            setItemSelected(viewHolder.itemView, position, false);
            if (customOnSelectionListener != null)
                customOnSelectionListener.onUnSelected(viewHolder, position);
        }

        @Override
        public void onSelectAll() {
            isSelectionStarted = true;
            selectedItems.clear();
            selectedItems.addAll(mediaFiles);
            notifyDataSetChanged();
            if (customOnSelectionListener != null)
                customOnSelectionListener.onSelectAll();
        }

        @Override
        public void onUnSelectAll() {
            for (int i = selectedItems.size() - 1; i >= 0; i--) {
                int position = mediaFiles.indexOf(selectedItems.get(i));
                if (position < 0) continue;
                removeSelection(position);
                handleItemChanged(position);
            }
            isSelectionStarted = false;
            if (customOnSelectionListener != null)
                customOnSelectionListener.onUnSelectAll();
        }

        @Override
        public void onSelectionEnd() {
            isSelectionStarted = false;
            if (!singleChoiceMode && customOnSelectionListener != null)
                customOnSelectionListener.onSelectionEnd();
        }

        @Override
        public void onMaxReached() {
            if (!singleChoiceMode && customOnSelectionListener != null)
                customOnSelectionListener.onMaxReached();
        }
    };

    public MultiSelectionAdapter(ArrayList<MediaFile> items) {
        this.mediaFiles = items;
    }

    public void setItemStartPosition(int itemStartPosition) {
        this.itemStartPosition = itemStartPosition;
    }

    public int getMaxSelection() {
        return maxSelection;
    }

    public void setMaxSelection(int maxSelection) {
        this.maxSelection = maxSelection;
    }

    @CallSuper
    @Override
    public void onBindViewHolder(@NonNull final VH holder, int position) {
        final View view = holder.itemView;

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition() - itemStartPosition;
                if (enabledSelection && (isSelectionStarted || isSingleClickSelection)) {
                    if (selectedItems.contains(mediaFiles.get(position))) {
                        onSelectionListener.onUnSelected(holder, position);
                        if (selectedItems.isEmpty()) {
                            onSelectionListener.onSelectionEnd();
                        }
                    } else {
                        onSelectionListener.onSelected(holder, position);
                    }
                }
                if (onItemClickListener != null)
                    onItemClickListener.onClick(v, position);
            }
        });


        setItemSelected(view, position, selectedItems.contains(mediaFiles.get(position)));

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int position = holder.getAdapterPosition() - itemStartPosition;
                if (enabledSelection) {
                    if (!isSelectionStarted) {
                        onSelectionListener.onSelectionBegin();
                        onSelectionListener.onSelected(holder, position);
                    } else if (selectedItems.size() <= 1
                            && selectedItems.contains(mediaFiles.get(position))) {
                        onSelectionListener.onSelectionEnd();
                        onSelectionListener.onUnSelected(holder, position);
                    }
                }
                return onItemLongClickListener == null ||
                        onItemLongClickListener.onLongClick(view, position);
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    public boolean isSelected(MediaFile mediaFile) {
        return selectedItems.contains(mediaFile);
    }

    public void enableSelection(boolean enabledSelection) {
        this.enabledSelection = enabledSelection;
    }

    public void enableSingleClickSelection(boolean enableSingleClickSelection) {
        this.enabledSelection = enableSingleClickSelection || enabledSelection;
        this.isSingleClickSelection = enableSingleClickSelection;
    }

    public void setSingleChoiceMode(boolean singleChoiceMode) {
        this.singleChoiceMode = singleChoiceMode;
    }

    public void setOnItemClickListener(OnItemClickListener onClickListener) {
        this.onItemClickListener = onClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public void setOnSelectionListener(OnSelectionListener<VH> onSelectionListener) {
        this.customOnSelectionListener = onSelectionListener;
    }

    public ArrayList<MediaFile> getSelectedItems() {
        return selectedItems;
    }

    public void setSelectedItems(ArrayList<MediaFile> selectedItems) {
        if (selectedItems == null) {
            this.selectedItems = new ArrayList<>();
        } else {
            this.selectedItems = selectedItems;
        }
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public void unSelectAll() {
        onSelectionListener.onUnSelectAll();
    }

    public void selectAll() {
        onSelectionListener.onSelectAll();
    }

    public void handleDataSetChanged() {
        notifyDataSetChanged();
    }

    public void handleItemChanged(int position) {
        notifyItemChanged(position + itemStartPosition);
    }

    public void handleItemInserted(int position) {
        notifyItemInserted(position + itemStartPosition);
    }

    public void handleItemRangeInserted(int positionStart, int itemCount) {
        notifyItemRangeInserted(positionStart + itemStartPosition, itemCount);
    }

    public void handleItemRemoved(int position) {
        if (enabledSelection) {
            removeSelection(position);
        }
        notifyItemRemoved(position + itemStartPosition);
    }

    public void handleItemRangeRemoved(int positionStart, int itemCount) {
        if (enabledSelection) {
            for (int i = positionStart; i < itemCount; i++) {
                removeSelection(i);
            }
        }
        notifyItemRangeRemoved(positionStart + itemStartPosition, itemCount);
    }

    private void setItemSelected(View view, int position, boolean selected) {
        if (selected) {
            if (!selectedItems.contains(mediaFiles.get(position)))
                selectedItems.add(mediaFiles.get(position));
        } else {
            if (selectedItems.remove(mediaFiles.get(position))
                    && selectedItems.isEmpty()) {
                onSelectionListener.onSelectionEnd();
            }
        }
    }

    private void removeSelection(int position) {
        if (selectedItems.remove(mediaFiles.get(position))
                && selectedItems.isEmpty()) {
            onSelectionListener.onSelectionEnd();
        }
    }

    public boolean add(MediaFile mediaFile) {
        if (mediaFiles.add(mediaFile)) {
            handleItemInserted(mediaFiles.size() - 1);
            return true;
        }
        return false;
    }

    public void add(int position, MediaFile mediaFile) {
        mediaFiles.add(position, mediaFile);
        handleItemInserted(position);
    }

    public boolean addAll(Collection<MediaFile> itemSelection) {
        int lastPosition = mediaFiles.size();
        if (mediaFiles.addAll(itemSelection)) {
            handleItemRangeInserted(lastPosition, itemSelection.size());
            return true;
        }
        return false;
    }

    public boolean addAll(int position, Collection<MediaFile> itemCollection) {
        if (mediaFiles.addAll(position, itemCollection)) {
            handleItemRangeInserted(position, itemCollection.size());
            return true;
        }
        return false;
    }

    public void remove(MediaFile item) {
        int position = mediaFiles.indexOf(item);
        handleItemRemoved(position);
        mediaFiles.remove(position);
    }

    public MediaFile remove(int position) {
        handleItemRemoved(position);
        return mediaFiles.remove(position);
    }

    public void removeAll(Collection<MediaFile> itemCollection) {
        ArrayList<MediaFile> removeItems = new ArrayList<>(itemCollection);
        for (int i = itemCollection.size() - 1; i >= 0; i--) {
            remove(removeItems.get(i));
        }
    }

    public interface OnItemClickListener {
        void onClick(View v, int position);
    }

    public interface OnItemLongClickListener {
        boolean onLongClick(View v, int position);
    }

    public interface OnSelectionListener<VH> {
        void onSelectionBegin();

        void onSelected(VH viewHolder, int position);

        void onUnSelected(VH viewHolder, int position);

        void onSelectAll();

        void onUnSelectAll();

        void onSelectionEnd();

        void onMaxReached();
    }
}
