package pesh.mori.learnerapp;

import androidx.appcompat.app.AlertDialog;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class TransactionHistoryActivity_PurchaseFragment extends Fragment {
    private RecyclerView mRecycler;
    private DatabaseReference mDebit,mFiles,mDIY;
    private FirebaseAuth mAuth;
    private TextView txtEmpty;
    private AlertDialog.Builder mAlert;
    private String sourceNode;

    private ProgressDialog mProgress;

    public TransactionHistoryActivity_PurchaseFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View postsFragment = inflater.inflate(R.layout.fragment_transactions_history,container,false);

//        mAuthor = ((ViewAuthorActivity)getActivity()).getAuthorId();

        mProgress = new ProgressDialog(getContext());
        mAlert = new AlertDialog.Builder(getContext());

        mAuth = FirebaseAuth.getInstance();

        txtEmpty = (TextView) postsFragment.findViewById(R.id.txt_upload_empty);

        mDebit = FirebaseDatabase.getInstance().getReference().child("TransactionRecords").child("Debit");
        mDebit.keepSynced(true);
        mFiles = FirebaseDatabase.getInstance().getReference().child("Files");
        mFiles.keepSynced(true);
        mDIY = FirebaseDatabase.getInstance().getReference().child("DIY");
        mDIY.keepSynced(true);

        mDebit.orderByChild("User").equalTo(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    txtEmpty.setText("Your transaction history is empty!");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRecycler = (RecyclerView)postsFragment.findViewById(R.id.layout_recycler);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(layoutManager);

        return postsFragment;
    }

    public void listDebits(){
        FirebaseRecyclerAdapter<Transaction,PostsViewHolder_TransactionsCard> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Transaction, PostsViewHolder_TransactionsCard>(
              Transaction.class,
              R.layout.card_transaction_item,
              PostsViewHolder_TransactionsCard.class,
              mDebit.orderByChild("User").equalTo(mAuth.getCurrentUser().getUid())
        ) {
            @Override
            protected void populateViewHolder(final PostsViewHolder_TransactionsCard viewHolder, Transaction model, int position) {
                final String fileKey = getRef(position).getKey();

                mDebit.child(fileKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            if (dataSnapshot.getChildrenCount()>0){
                                txtEmpty.setVisibility(View.GONE);
                            }
                            viewHolder.setRefNumber("Ref No: "+dataSnapshot.child("RefNumber").getValue());
                            viewHolder.setTime(String.valueOf(dataSnapshot.child("Time").getValue()));
                            viewHolder.setAmount("KES "+dataSnapshot.child("TransactionCost").getValue());
                            if (dataSnapshot.child("ItemType").getValue().equals("Token")){
                                viewHolder.setItem("TOKENS SENT");
                            } else {
                                FirebaseDatabase.getInstance().getReference().child("AllPosts").child(String.valueOf(dataSnapshot.child("Item").getValue())).addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        viewHolder.setItem(String.valueOf(dataSnapshot.child("Title").getValue()));
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                            }
                            if (!dataSnapshot.child("Seen").exists() || dataSnapshot.child("Seen").getValue().equals("false")){
                                viewHolder.setNotSeen();
                            }
                            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent i = new Intent(getContext(),ViewTransactionActivity.class);
                                    i.putExtra("post_key",fileKey)
                                            .putExtra("trans_type","Debit")
                                            .putExtra("outgoing_intent","TransactionHistoryActivity_PurchaseFragment");
                                    startActivity(i);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        };
        mRecycler.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated( view, savedInstanceState );
        setHasOptionsMenu( true );

        listDebits();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.search, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

}