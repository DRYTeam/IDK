package com.dry.idk.idk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.dry.POCO.Comment;
import com.dry.POCO.Subject;
import com.dry.POCO.Topic;
import com.dry.POCO.User;
import com.shamanland.fab.FloatingActionButton;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yakov on 20/08/2015.
 */
public class PostViewActivity  extends Activity{

    TextView post_title;
    ImageView post_image;
    TextView post_info;
    TextView post_username;
    TextView post_votes;
    TextView post_share;
    ImageView post_voteup;
    ImageView post_votedown;
    int SelectedTopicNum;
    Topic currentTopic;

    SwipeRefreshLayout swipeLayout;

    boolean isInfoLonest = false;
    boolean isOpen = false;

    List<Comment> commentsLoaded;
    //SwipeRefreshLayout swipeLayout;
    CommentRVAdapter adapter;
    RecyclerView rv;
    LinearLayoutManager llm;

    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topic_layout);

        //*not recommended* - fix NetworkCrashed by overriding the android regular behaviour
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //*not recommended*

        commentsLoaded = new ArrayList<>();

        Intent i = getIntent();
        SelectedTopicNum = i.getIntExtra("selectedTopic", -1);
        Log.d("postView",String.valueOf(SelectedTopicNum));
        currentTopic = new Topic();

        post_title = (TextView) this.findViewById(R.id.post_Title);
        post_image = (ImageView) this.findViewById(R.id.post_image);
        post_info = (TextView) this.findViewById(R.id.post_info);
        post_username = (TextView) this.findViewById(R.id.post_view_username);
        post_votes = (TextView) this.findViewById(R.id.post_votes);
        post_share = (TextView) this.findViewById(R.id.post_share);
        post_voteup = (ImageView) this.findViewById(R.id.post_view_voteup);
        post_votedown = (ImageView) this.findViewById(R.id.post_view_votedown);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PostViewActivity.this,newComment.class);
                intent.putExtra("cmTopicNum",SelectedTopicNum);
                startActivity(intent);
            }
        });

        rv = (RecyclerView)findViewById(R.id.rv_comments);
        llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

        if(SelectedTopicNum != -1 ) {
            updateListFromDB();
            post_title.setText(currentTopic.getTitle());
            post_info.setText(currentTopic.getContent());

            ViewTreeObserver vto = post_info.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    Layout l = post_info.getLayout();
                    if (l != null) {
                        int lines = l.getLineCount();
                        if (lines > 0) {
                            for(int d = 0; d < lines;d++) {
                                if (l.getEllipsisCount(d) > 0)
                                    isInfoLonest = true;
                            }
                        } else {
                            if (l.getEllipsisCount(0) > 0)
                                isInfoLonest = true;
                        }
                    }

                    Log.d("longText",String.valueOf(isInfoLonest));

                    if(isInfoLonest){
                            post_info.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(!isOpen) {
                                        post_info.setMaxLines(Integer.MAX_VALUE);
                                        post_info.setEllipsize(null);
                                        post_info.setLayoutParams(new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, RadioGroup.LayoutParams.WRAP_CONTENT));
                                        isOpen = true;
                                    } else {
                                        post_info.setMaxLines(4);
                                        post_info.setEllipsize(TextUtils.TruncateAt.END);
                                        float density = getResources().getDisplayMetrics().density;
                                        post_info.setLayoutParams(new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT, (int)(60 * density + 0.5f)));
                                        isOpen = false;
                                    }
                                }
                            });
                    }

                    ViewTreeObserver obs = post_info.getViewTreeObserver();
                    obs.removeOnGlobalLayoutListener(this);
                }
            });


            if(currentTopic.getImage() == null){
                post_image.setVisibility(View.GONE);
            } else {
                post_image.setVisibility(View.VISIBLE);
                post_image.setImageBitmap(currentTopic.getImage());
                post_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GlobalVar.photoStatic = currentTopic.getImage();
                        Intent intent = new Intent(PostViewActivity.this,PostImageViewActivity.class);
                        startActivity(intent);
                    }
                });
            }
            post_username.setText(currentTopic.getUsername().getUsername());
            String sign = "";
            if(currentTopic.getVotesup() > 0)
                sign = "+";
            post_votes.setText(sign + String.valueOf(currentTopic.getVotesup()));

            int userValue = currentTopic.getUserVoteValue();
            switch(userValue){
                case 1:
                    post_voteup.setImageResource(R.drawable.ic_voteup_checked);
                    post_votedown.setImageResource(R.drawable.ic_votedown);
                    break;
                case -1:
                    post_voteup.setImageResource(R.drawable.ic_hardware_keyboard_arrow_up);
                    post_votedown.setImageResource(R.drawable.ic_votedown_checked);
                    break;
                case 0:
                    post_voteup.setImageResource(R.drawable.ic_hardware_keyboard_arrow_up);
                    post_votedown.setImageResource(R.drawable.ic_votedown);
                    break;
                default:
                    post_voteup.setImageResource(R.drawable.ic_hardware_keyboard_arrow_up);
                    post_votedown.setImageResource(R.drawable.ic_votedown);
                    break;

            }

            post_voteup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(currentTopic.getUserVoteValue() != 1){
                        currentTopic.setVotesup(currentTopic.getVotesup()-currentTopic.getUserVoteValue());
                        currentTopic.setUserVoteValue(1);
                        currentTopic.setVotesup(currentTopic.getVotesup()+1);
                        String sign = "";
                        if(currentTopic.getVotesup() > 0)
                            sign = "+";
                        post_votes.setText(sign+String.valueOf(currentTopic.getVotesup()));
                        ((ImageView) v).setImageResource(R.drawable.ic_voteup_checked);
                        post_votedown.setImageResource(R.drawable.ic_votedown);
                    } else {
                        currentTopic.setUserVoteValue(0);
                        currentTopic.setVotesup(currentTopic.getVotesup()-1);
                        String sign = "";
                        if(currentTopic.getVotesup() > 0)
                            sign = "+";
                        post_votes.setText(sign+String.valueOf(currentTopic.getVotesup()));
                        ((ImageView) v).setImageResource(R.drawable.ic_hardware_keyboard_arrow_up);
                        post_votedown.setImageResource(R.drawable.ic_votedown);
                    }
                    try {
                        HttpClient client = new DefaultHttpClient();
                        HttpGet request = new HttpGet();
                        HttpResponse response;
                        try {
                            response = null;
                            request.setURI(new URI("http://drycorporations.wc.lt/UserVoteUp.php?user="+GlobalVar.LoggedInUser.getNUM()+"&topic="+currentTopic.getNUM()+"&value="+currentTopic.getUserVoteValue()));
                            response = client.execute(request);
                            String result = GlobalMethods.convertStreamToString(response.getEntity().getContent());
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        } catch (ClientProtocolException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception ex) {

                    }
                }
            });

            post_votedown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (currentTopic.getUserVoteValue() != -1) {
                        currentTopic.setVotesup(currentTopic.getVotesup() - currentTopic.getUserVoteValue());
                        currentTopic.setUserVoteValue(-1);
                        currentTopic.setVotesup(currentTopic.getVotesup() - 1);
                        String sign = "";
                        if (currentTopic.getVotesup() > 0)
                            sign = "+";
                        post_votes.setText(sign + String.valueOf(currentTopic.getVotesup()));
                        ((ImageView) v).setImageResource(R.drawable.ic_votedown_checked);
                        post_voteup.setImageResource(R.drawable.ic_hardware_keyboard_arrow_up);
                    } else {
                        currentTopic.setUserVoteValue(0);
                        currentTopic.setVotesup(currentTopic.getVotesup() + 1);
                        String sign = "";
                        if (currentTopic.getVotesup() > 0)
                            sign = "+";
                        post_votes.setText(sign + String.valueOf(currentTopic.getVotesup()));
                        ((ImageView) v).setImageResource(R.drawable.ic_votedown);
                        post_voteup.setImageResource(R.drawable.ic_hardware_keyboard_arrow_up);
                    }
                    try {
                        HttpClient client = new DefaultHttpClient();
                        HttpGet request = new HttpGet();
                        HttpResponse response;
                        try {
                            response = null;
                            request.setURI(new URI("http://drycorporations.wc.lt/UserVoteUp.php?user=" + GlobalVar.LoggedInUser.getNUM() + "&topic=" + currentTopic.getNUM() + "&value=" + currentTopic.getUserVoteValue()));
                            response = client.execute(request);
                            String result = GlobalMethods.convertStreamToString(response.getEntity().getContent());
                        } catch (URISyntaxException e) {
                            e.printStackTrace();
                        } catch (ClientProtocolException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception ex){

                    }
                }
            });

            adapter = new CommentRVAdapter(commentsLoaded);
            rv.setAdapter(adapter);

            swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_topic);
            swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    commentsLoaded.removeAll(commentsLoaded);
                    updateListFromDB();
                    adapter.notifyDataSetChanged();
                    //swipeLayout.setRefreshing(false);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            swipeLayout.setRefreshing(false);
                        }
                    }, 1000);
                }
            });
            swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);
        }
    }

    private void updateListFromDB(){
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            HttpResponse response;
            try {
                response = null;
                request.setURI(new URI("http://drycorporations.wc.lt/selectTopicByNum.php?num="+SelectedTopicNum+"&ruser="+GlobalVar.LoggedInUser.getNUM()));
                response = client.execute(request);
                String result = GlobalMethods.convertStreamToString(response.getEntity().getContent());
                if (!result.equals("null")) {
                    JSONObject root = new JSONObject(result);
                    currentTopic.setNUM(root.getInt("Num"));
                    currentTopic.setTitle(root.getString("Title"));
                    currentTopic.setContent(root.getString("Content"));
                    currentTopic.setVotesup(root.getInt("Votes"));
                    Log.d("TopicViewVote", root.getString("Votes"));
                    currentTopic.setUserVoteValue(root.getInt("RUserVoteValue"));
                    if(!root.getString("Image").equals("")){
                        currentTopic.setImage(GlobalMethods.base64ToBitmap(root.getString("Image")));
                        //Bitmap p = BitmapFactory.decodeResource(getResources(), R.drawable.background);
                        Log.d("TopicViewimg", root.getString("Image"));
                    }
                    User user = new User();
                    request.setURI(new URI("http://drycorporations.wc.lt/selectByNum.php?table=User&num=" + root.getInt("UserNum")));
                    response = client.execute(request);
                    String userR = GlobalMethods.convertStreamToString(response.getEntity().getContent());
                    JSONObject userObj = new JSONObject(userR);
                    user.setNUM(userObj.getInt("Num"));
                    user.setUsername(userObj.getString("Username"));
                    currentTopic.setUsername(user);
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                Toast.makeText(getApplicationContext(), "ישנה בעיה בחיבור לאינטרנט", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                response = null;
                request.setURI(new URI("http://drycorporations.wc.lt/selectComments.php?topic="+String.valueOf(SelectedTopicNum)));
                response = client.execute(request);
                String result = GlobalMethods.convertStreamToString(response.getEntity().getContent());
                Log.d("Comment","getting comments");
                if (!result.equals("null")) {
                    JSONObject root = new JSONObject(result);
                    JSONArray commentsarray = root.getJSONArray("Comments");

                    for (int i = 0; i < commentsarray.length(); i++) {
                        Log.d("CommentI",String.valueOf(i));
                        JSONObject CommentObj = commentsarray.getJSONObject(i);
                        Comment comment = new Comment();
                        comment.setNUM(CommentObj.getInt("Num"));
                        comment.setContent(CommentObj.getString("Content"));
                        User user = new User();
                        request.setURI(new URI("http://drycorporations.wc.lt/selectByNum.php?table=User&num=" + CommentObj.getInt("UserNum")));
                        response = client.execute(request);
                        String userR = GlobalMethods.convertStreamToString(response.getEntity().getContent());
                        JSONObject userObj = new JSONObject(userR);
                        user.setNUM(userObj.getInt("Num"));
                        user.setUsername(userObj.getString("Username"));
                        comment.setUser(user);
                        if(!CommentObj.getString("Image").equals("")){
                            comment.setImage(GlobalMethods.base64ToBitmap(CommentObj.getString("Image")));
                            //Bitmap p = BitmapFactory.decodeResource(getResources(), R.drawable.background);
                        }
                        commentsLoaded.add(comment);
                    }
                }
            } catch (URISyntaxException e) {
                Log.d("CommentArray","error1");
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                Toast.makeText(getApplicationContext(), "ישנה בעיה בחיבור לאינטרנט", Toast.LENGTH_SHORT).show();
                Log.d("CommentArray","error2");
                e.printStackTrace();
            } catch (IOException e) {
                Log.d("CommentArray","error3");
                e.printStackTrace();
            } catch (JSONException e) {
                Log.d("CommentArray","error4");
                e.printStackTrace();
            }
        } catch (Exception ex){

        }
    }

}

class CommentRVAdapter extends RecyclerView.Adapter<CommentRVAdapter.CommentsViewHolder>{

    List<Comment> comments;

    public CommentRVAdapter(List<Comment> comments){
        this.comments = comments;
    }

    @Override
    public CommentsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comment_card, viewGroup, false);
        CommentsViewHolder pvh = new CommentsViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(CommentsViewHolder topicsViewHolder, int i) {
        //topicsViewHolder.txbUsername.setText(comments.get(i).getUser().getUsername());
        topicsViewHolder.txbContent.setText(comments.get(i).getContent());
        if(comments.get(i).getImage() == null) {
            topicsViewHolder.imgCommentImage.setVisibility(View.GONE);
        } else {
            topicsViewHolder.imgCommentImage.setVisibility(View.VISIBLE);
            topicsViewHolder.imgCommentImage.setImageBitmap(comments.get(i).getImage());
        }
        topicsViewHolder.txbUsername.setText(comments.get(i).getUser().getUsername());
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class CommentsViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView txbUsername;
        TextView txbContent;
        ImageView imgCommentImage;

        CommentsViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.comment_cardview);
            txbUsername = (TextView)itemView.findViewById(R.id.comment_username);
            txbContent = (TextView)itemView.findViewById(R.id.comment_info);
            imgCommentImage = (ImageView)itemView.findViewById(R.id.comment_image);
        }
    }

}
