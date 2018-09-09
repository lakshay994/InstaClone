package com.example.lakshaysharma.instaclone.Utils;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.lakshaysharma.instaclone.DataModels.Comment;
import com.example.lakshaysharma.instaclone.DataModels.UserAccountSettings;
import com.example.lakshaysharma.instaclone.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentListAdapter extends ArrayAdapter<Comment> {

    private static final String TAG = "CommentListAdapter";

    private LayoutInflater mInflater;
    private int layoutResouce;
    private Context mContext;

    public CommentListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List<Comment> objects) {
        super(context, resource, objects);

        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
        layoutResouce = resource;

    }

    private static class ViewHolder{

        TextView comment, username, timestamp, reply, like;
        CircleImageView circleImageView;
        ImageView heartLike;


    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        final ViewHolder holder;

        if (convertView == null){

            convertView = mInflater.inflate(layoutResouce, parent, false);
            holder = new ViewHolder();

            holder.comment = convertView.findViewById(R.id.commentMainComment);
            holder.timestamp = convertView.findViewById(R.id.commentTimeStamp);
            holder.username = convertView.findViewById(R.id.commentUsername);
            holder.reply = convertView.findViewById(R.id.commentReply);
            holder.like = convertView.findViewById(R.id.commentLike);
            holder.circleImageView = convertView.findViewById(R.id.commentProfileImage);
            holder.heartLike = convertView.findViewById(R.id.commentHeart);

            convertView.setTag(holder);

        }
        else {

            holder = (ViewHolder) convertView.getTag();

        }

        holder.comment.setText(getItem(position).getComment());

        String timeStampDifference = getTimeStampDifference(getItem(position));
        if (timeStampDifference != "0"){
            holder.timestamp.setText(timeStampDifference + "d");
        }
        else {
            holder.timestamp.setText("today");
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        Query query = reference.child(mContext.getString(R.string.user_account_settings))
                .orderByChild(mContext.getString(R.string.user_id))
                .equalTo(getItem(position).getUser_id());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot data: dataSnapshot.getChildren()){
                    holder.username.setText(
                            data.getValue(UserAccountSettings.class).getUsername());
                    ImageLoader imageLoader = ImageLoader.getInstance();
                    imageLoader.displayImage(data.getValue(UserAccountSettings.class).getProfile_photo(),
                            holder.circleImageView);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if (position == 0){
            holder.like.setVisibility(View.GONE);
            holder.heartLike.setVisibility(View.GONE);
            holder.reply.setVisibility(View.GONE);
        }

        return convertView;
    }


    private String getTimeStampDifference(Comment comment){

        Log.d(TAG, "getTimeStampDifference: calculating the timestamp");

        String difference = "";
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy'T'HH:mm:ss'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("US/Central"));
        Date today = calendar.getTime();
        sdf.format(today);
        Date timeStamp;
        String photoTimeStamp = comment.getDate_created();
        try {

            timeStamp = sdf.parse(photoTimeStamp);
            difference = String.valueOf(Math.round(((today.getTime() - timeStamp.getTime()) / 1000 / 60 / 60/ 24)));

        }catch (ParseException e){
            Log.e(TAG, "getTimeStampDifference: " + e.getMessage() );
            difference = "0";
        }

        return difference;

    }
}
