package lk.dinuka.translate.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.dinuka.translate.R;

public class MyEditAdapter extends RecyclerView.Adapter<MyEditAdapter.MyViewHolder> {

    private List<String> mDataset;      // list of all english from db will be transferred here

    private int lastSelectedPosition = -1;      // stores the radio selection position
    private OnEditAdapterListener onEditAdapterListener;

    // constructor of adapter
    public MyEditAdapter(List<String> myDataset, OnEditAdapterListener onEditAdapterListener) {
        mDataset = myDataset;           // getting received english from db
        this.onEditAdapterListener = onEditAdapterListener;
    }


    @NonNull
    @Override
    public MyEditAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create new views (invoked by the layout manager)

        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_edit_text_view, parent, false);

        MyViewHolder vh = new MyViewHolder(v, onEditAdapterListener);
        return vh;
    }

    // Replaces content of the view with content required to be displayed
    @Override
    public void onBindViewHolder(@NonNull MyEditAdapter.MyViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.textView.setText(mDataset.get(position));

        // since only one radio button is allowed to be selected,
        // this condition un-checks previous selections
        holder.selectionState.setChecked(lastSelectedPosition == position);

        //-------------->>>>>>> send chosen word/phrase to main activity


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }


    // Provide a reference to the views for each data item
    // provide access to all the views for a data item in a view holder
    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView textView;       // each data item has a String
        public RadioButton selectionState;
        OnEditAdapterListener onEditAdapterListener;      // adding the interface to the view holder


        public MyViewHolder(@NonNull View itemView, OnEditAdapterListener onEditAdapterListener) {       // constructor of individual view element
            super(itemView);
            this.onEditAdapterListener = onEditAdapterListener;

            textView = itemView.findViewById(R.id.editText_display_line);
            selectionState = itemView.findViewById(R.id.radio_selection_english);

//            textView.setOnClickListener(new View.OnClickListener() {            // choose radio if text is clicked
//                @Override
//                public void onClick(View view) {
//                    lastSelectedPosition = getAdapterPosition();
//                    notifyDataSetChanged();
//                }
//            });
//
//            selectionState.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    lastSelectedPosition = getAdapterPosition();
//                    notifyDataSetChanged();
//
////                    System.out.println(lastSelectedPosition);         // test whether desired radio was selected
////                    System.out.println(textView.getText());
//                }
//            });

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            lastSelectedPosition = getAdapterPosition();
            onEditAdapterListener.onEnglishClick(getAdapterPosition());    //calling onEnglishClick and passing in the adapter position
            notifyDataSetChanged();
        }
    }


    public interface OnEditAdapterListener {
        void onEnglishClick(int position);
    }
}
