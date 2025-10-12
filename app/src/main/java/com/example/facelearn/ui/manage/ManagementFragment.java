package com.example.facelearn.ui.manage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.facelearn.MainActivity;
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

        final TextView textView = binding.textManage;
        managementViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);

        MainActivity activity = (MainActivity) requireActivity();
        DataStore dataStore = activity.getDataStore();

        final RecyclerView listViwe = binding.list;
        listViwe.setAdapter(new CustomAdapter(dataStore.getList().toArray(new DataStore.Record[0])));

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    void test(){
        MainActivity activity = (MainActivity) requireActivity();
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
            private final TextView textView1;
            private final TextView textView2;

            public ViewHolder(View view) {
                super(view);
                // Define click listener for the ViewHolder's View

                textView1 = (TextView) view.findViewById(android.R.id.text1);
                textView2 = (TextView) view.findViewById(android.R.id.text2);
            }

            public TextView getTextView1() {
                return textView1;
            }
            public TextView getTextView2() {
                return textView2;
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
                    .inflate(android.R.layout.simple_list_item_2, viewGroup, false);

            return new ViewHolder(view);
        }

        // Replace the contents of a view (invoked by the layout manager)
        @Override
        public void onBindViewHolder(ViewHolder viewHolder, final int position) {

            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            viewHolder.getTextView1().setText(localDataSet[position].key);
            viewHolder.getTextView2().setText(localDataSet[position].name);
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return localDataSet.length;
        }
    }
}