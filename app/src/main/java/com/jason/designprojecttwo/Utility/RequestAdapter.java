package com.jason.designprojecttwo.Utility;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.jason.designprojecttwo.R;

public class RequestAdapter extends FirestoreRecyclerAdapter<RequestModel, RequestAdapter.RequestHolder> {
    private OnItemClickListener listener;

    public RequestAdapter(@NonNull FirestoreRecyclerOptions<RequestModel> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull RequestHolder holder, int position, @NonNull RequestModel model) {
        holder.textViewStatus.setText(model.getStatus());
        setTextColor(holder);

        holder.textViewUniqueID.setText(model.getUniqueID());
        holder.textViewFault.setText(model.getFault());
        holder.textViewDescription.setText(model.getDescription());
        holder.textViewDate.setText(model.getDate());
    }

    @NonNull
    @Override
    public RequestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_list_item,
                parent, false);
        return new RequestHolder(v);
    }

    public void deleteItem(int position, View v) {
        final DocumentReference documentReference = getSnapshots().getSnapshot(position).getReference();
        final RequestModel requestModel = getSnapshots().getSnapshot(position).toObject(RequestModel.class);
        documentReference.delete();

        //Undo Function Below
        Snackbar.make(v, "Item Deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        documentReference.set(requestModel);
                    }
                }).show();

    }

    // Changed this to public might need to delete later
    class RequestHolder extends RecyclerView.ViewHolder {
        TextView textViewUniqueID;
        TextView textViewFault;
        TextView textViewDescription;
        TextView textViewDate;
        TextView textViewStatus;

        public RequestHolder(@NonNull View itemView) {
            super(itemView);
            textViewUniqueID = itemView.findViewById(R.id.list_unique_id);
            textViewFault = itemView.findViewById(R.id.list_fault);
            textViewDescription = itemView.findViewById(R.id.list_description);
            textViewDate = itemView.findViewById(R.id.list_date);
            textViewStatus = itemView.findViewById(R.id.list_status);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(position != RecyclerView.NO_POSITION && listener != null){
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });

        }
    }
    
    private void setTextColor(RequestHolder holder) {
        String textString = holder.textViewStatus.getText().toString();
        switch (textString) {
            case "Rejected":
                holder.textViewStatus.setTextColor(Color.parseColor("#FF0000"));
                break;
            case "Approved":
                holder.textViewStatus.setTextColor(Color.parseColor("#83F52C"));
                break;
            case "Completed":
                holder.textViewStatus.setTextColor(Color.parseColor("#F6BE00"));
                break;
            case "Closed":
                holder.textViewStatus.setTextColor(Color.parseColor("#000000"));
                break;
            default:
                holder.textViewStatus.setTextColor(Color.parseColor("#0198E1"));
                break;
        }
    }

    public interface OnItemClickListener{
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

}
