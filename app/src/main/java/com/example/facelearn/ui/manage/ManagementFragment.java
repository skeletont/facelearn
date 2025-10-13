package com.example.facelearn.ui.manage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.facelearn.FaceLearnActivity;
import com.example.facelearn.R;
import com.example.facelearn.databinding.FragmentManageBinding;
import com.example.facelearn.util.DataStore;

public class ManagementFragment extends Fragment {

    private FragmentManageBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ManagementViewModel managementViewModel =
                new ViewModelProvider(this).get(ManagementViewModel.class);

        binding = FragmentManageBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

//        final TextView textView = binding.textManage;
//        managementViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        FaceLearnActivity activity = (FaceLearnActivity) requireActivity();
        DataStore dataStore = activity.getDataStore();
        DataStore.Record[] records = dataStore.getList().toArray(new DataStore.Record[0]);
//        records = new DataStore.Record[] {
//                new DataStore.Record("1", "ant"),
//                new DataStore.Record("2", "bear"),
//                new DataStore.Record("3", "cat"),
//                new DataStore.Record("4", "dog"),
//                new DataStore.Record("5", "elephant"),
//                new DataStore.Record("6", "f"),
//                new DataStore.Record("7", "g"),
//                new DataStore.Record("8", "h"),
//                new DataStore.Record("9", "i"),
//                new DataStore.Record("10", "j"),
//                new DataStore.Record("11", "k"),
//                new DataStore.Record("12", "l"),
//                new DataStore.Record("13", "m"),
//        };
        final RecyclerView listView = binding.list;
        listView.setAdapter(new CustomAdapter(records));
        // listView.invalidate();
        listView.setLayoutManager(new LinearLayoutManager(this.requireActivity()));

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    void test(){
        FaceLearnActivity activity = (FaceLearnActivity) requireActivity();
        DataStore dataStore = activity.getDataStore();
        dataStore.getList();

    }

    public static class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        private DataStore.Record[] localDataSet;

        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder)
         */
        public static class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView textKey;
            private final TextView textName;

            public ViewHolder(View view) {
                super(view);
                // Define click listener for the ViewHolder's View

                textKey = (TextView) view.findViewById(R.id.textKey);
                textName = (TextView) view.findViewById(R.id.textName);
            }

            public TextView getTextKey() {
                return textKey;
            }
            public TextView getTextName() {
                return textName;
            }
        }

        /**
         * Initialize the dataset of the Adapter
         *
         * @param dataSet String[] containing the data to populate views to be used
         * by RecyclerView
         */
        public CustomAdapter(DataStore.Record[] dataSet) {
            localDataSet = dataSet;
        }

        // Create new views (invoked by the layout manager)
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            // Create a new view, which defines the UI of the list item
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_view, viewGroup, false);

            return new ViewHolder(view);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {

            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            viewHolder.getTextKey().setText(localDataSet[position].key);
            viewHolder.getTextName().setText(localDataSet[position].name);
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return localDataSet.length;
        }
    }
}