package com.example.overlist.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.overlist.CommentsActivity;
import com.example.overlist.HomeActivity;
import com.example.overlist.Model.Post;
import com.example.overlist.Model.User;
import com.example.overlist.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter  extends  RecyclerView.Adapter<PostAdapter.ViewHolder>{
    public Context mContext;
    public List<Post> mPostList;
    public FirebaseUser firebaseUser;

    public PostAdapter(Context mContext, List<Post> mPostList) {
        this.mContext = mContext;
        this.mPostList = mPostList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.question_retrived_layout, parent, false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final Post post = mPostList.get(position);

        if(post.getQuestionImage() == null){
            holder.questionImage.setVisibility(View.GONE);
        }else{
            holder.questionImage.setVisibility(View.VISIBLE);
        }

        Glide.with(mContext).load(post.getQuestionImage()).into(holder.questionImage);
        holder.expandable_text.setText(post.getQuestion());
        holder.topicTextView.setText(post.getTopic());
        holder.askedonTextView.setText(post.getDate());

        publisherInformation(holder.publisher_profile_image, holder.asked_by_Textview, post.getPublisher());
        isLiked(post.getPostid(), holder.like);
        isDisliked(post.getPostid(), holder.dislike);
        getLikes(holder.likes, post.getPostid());
        getDislikes(holder.dislikes, post.getPostid());
        holder.like.setOnClickListener(v -> {
            if (holder.like.getTag().equals("like") && holder.dislike.getTag().equals("dislike")){
                FirebaseDatabase.getInstance().getReference().child("likes").child(post.getPostid()).child(firebaseUser.getUid()).setValue(true);
//                    addNotifications(post.getPublisher(), post.getPostid());
            }
            else if (holder.like.getTag().equals("like") && holder.dislike.getTag().equals("disliked")){
                FirebaseDatabase.getInstance().getReference().child("dislikes").child(post.getPostid()).child(firebaseUser.getUid()).removeValue();
                FirebaseDatabase.getInstance().getReference().child("likes").child(post.getPostid()).child(firebaseUser.getUid()).setValue(true);
//                    addNotifications(post.getPublisher(), post.getPostid());

            }else {
                FirebaseDatabase.getInstance().getReference().child("likes").child(post.getPostid()).child(firebaseUser.getUid()).removeValue();
            }
        });

        holder.dislike.setOnClickListener(v -> {
            if (holder.dislike.getTag().equals("dislike") && holder.like.getTag().equals("like")){
                FirebaseDatabase.getInstance().getReference().child("dislikes").child(post.getPostid()).child(firebaseUser.getUid()).setValue(true);
            }else if (holder.dislike.getTag().equals("dislike") && holder.like.getTag().equals("liked")){
                FirebaseDatabase.getInstance().getReference().child("likes").child(post.getPostid()).child(firebaseUser.getUid()).removeValue();
                FirebaseDatabase.getInstance().getReference().child("dislikes").child(post.getPostid()).child(firebaseUser.getUid()).setValue(true);
            }else {
                FirebaseDatabase.getInstance().getReference().child("dislikes").child(post.getPostid()).child(firebaseUser.getUid()).removeValue();
            }
        });

        holder.comment.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, CommentsActivity.class);
            intent.putExtra("postid", post.getPostid());
            intent.putExtra("publisher", post.getPublisher());
            mContext.startActivity(intent);
        });

        holder.comments.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, CommentsActivity.class);
            intent.putExtra("postid", post.getPostid());
            intent.putExtra("publisher", post.getPublisher());
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return mPostList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public CircleImageView publisher_profile_image;
        public TextView asked_by_Textview, likes, dislikes, comments ;
        public ImageView more, questionImage, like, dislike, comment, save;
        public TextView topicTextView, askedonTextView;
        public ExpandableTextView expandable_text;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            publisher_profile_image = itemView.findViewById(R.id.publisher_profile_image);
            asked_by_Textview = itemView.findViewById(R.id.asked_by_Textview);
            likes = itemView.findViewById(R.id.likes);
            dislikes = itemView.findViewById(R.id.dislikes);
            comments = itemView.findViewById(R.id.comments);
            more = itemView.findViewById(R.id.more);
            questionImage = itemView.findViewById(R.id.questionImage);
            like = itemView.findViewById(R.id.like);
            dislike = itemView.findViewById(R.id.dislike);
            comment = itemView.findViewById(R.id.comment);
            save = itemView.findViewById(R.id.save);
            topicTextView = itemView.findViewById(R.id.topicTextView);
            askedonTextView = itemView.findViewById(R.id.askedOnTextView);
            expandable_text = itemView.findViewById(R.id.expand_text_view);
        }
    }

    private void publisherInformation(final CircleImageView publisherImage, final TextView askedBy, String userid){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("user").child(userid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                Glide.with(mContext).load(user.getProfileimageurl()).into(publisherImage);
                askedBy.setText(user.getFullname());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void isLiked(String postid, ImageView imageView){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("likes").child(postid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(firebaseUser.getUid()).exists()){
                    imageView.setImageResource(R.drawable.ic_liked);
                    imageView.setTag("liked");
                }else{
                    imageView.setImageResource(R.drawable.ic_thumb_up);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void isDisliked(String postid, ImageView imageView){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("dislikes").child(postid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(firebaseUser.getUid()).exists()){
                    imageView.setImageResource(R.drawable.ic_disliked);
                    imageView.setTag("disliked");
                }else{
                    imageView.setImageResource(R.drawable.ic_thumb_down);
                    imageView.setTag("dislike");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getLikes(TextView likes, String postid){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("likes").child(postid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long numberOfLikes = snapshot.getChildrenCount();
                int NOL = (int) numberOfLikes;
                if(NOL  > 1){
                    likes.setText(snapshot.getChildrenCount() + " likes");
                }else if(NOL == 0){
                    likes.setText("0 likes");
                }else{
                    likes.setText(snapshot.getChildrenCount() + " like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDislikes(TextView likes, String postid){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("dislikes").child(postid);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long numberOfDislikes = snapshot.getChildrenCount();
                int NOD = (int) numberOfDislikes;
                if(NOD  > 1){
                    likes.setText(snapshot.getChildrenCount() + " dislikes");
                }else if(NOD == 0){
                    likes.setText("0 dislikes");
                }else{
                    likes.setText(snapshot.getChildrenCount() + " dislike");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(mContext, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
