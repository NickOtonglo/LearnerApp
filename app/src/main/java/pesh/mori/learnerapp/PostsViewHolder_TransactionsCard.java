package pesh.mori.learnerapp;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class PostsViewHolder_TransactionsCard extends RecyclerView.ViewHolder {
    View mView;

    public PostsViewHolder_TransactionsCard(View itemView) {
        super(itemView);
        mView = itemView;
    }

    public void setRefNumber(String refNumber){
        TextView txtRefNumber = mView.findViewById(R.id.txt_ref_number);
        txtRefNumber.setText(refNumber);
    }

    public void setItem(String item){
        TextView txtItem = mView.findViewById(R.id.txt_item);
        txtItem.setText(item);
    }

    public void setTime(String time){
        TextView txtTime = mView.findViewById(R.id.txt_time);
        txtTime.setText(time);
    }

    public void setNotSeen(){
        TextView txtNew = mView.findViewById(R.id.txt_read_indicator);
        txtNew.setText(R.string.info_new_indicator);
    }

    public void setAmount(String amount){
        TextView txtAmount = mView.findViewById(R.id.txt_amount);
        txtAmount.setText(amount);
    }
}
