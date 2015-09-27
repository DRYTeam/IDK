package com.dry.idk.idk;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dry.POCO.Comment;
import com.dry.POCO.Subject;
import com.dry.POCO.Tag;
import com.dry.POCO.Topic;
import com.dry.POCO.User;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    int start_row;
    int rows_num;
    List<Topic> topicsLoaded;
    SwipeRefreshLayout swipeLayout;
    RVAdapter adapter;
    RecyclerView rv;
    LinearLayoutManager llm;
    SharedPreferences loggedInUser;

    static final int REQUEST_IMAGE_CAPTURE=1;
    private static int RESULT_LOAD_IMG = 1;
    String imgDecodableString;
    private int decision=0;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        this.setTitle(GlobalVar.LoggedInUser.getUsername());

        //*not recommended* - fix NetworkCrashed by overriding the android regular behaviour
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        //*not recommended*

        View.OnClickListener mItemClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int TopicNum = Integer.parseInt(v.getTag().toString());
                Intent intent = new Intent(MainActivity.this, PostViewActivity.class);
                intent.putExtra("selectedTopic",TopicNum);
                Log.d("selectTopic",String.valueOf(TopicNum));
                startActivity(intent);
            }
        };

        loggedInUser = getSharedPreferences(GlobalVar.userdata, 0);

        topicsLoaded = new ArrayList<>();
        start_row = 0;
        rows_num = 20;

        updateListFromDB();

        Log.d("tag",String.valueOf(topicsLoaded.size()));

        rv = (RecyclerView)findViewById(R.id.rv);
        llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);

        adapter = new RVAdapter(topicsLoaded,mItemClick,(LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE));
        rv.setAdapter(adapter);

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                topicsLoaded.removeAll(topicsLoaded);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_newpost:
                Intent intent = new Intent(this, newTextPostActivity.class);
                intent.putExtra("Mode",Mode.Text);
                startActivity(intent);
                return true;
            case R.id.action_logout:
                SharedPreferences.Editor editor = loggedInUser.edit();
                editor.putBoolean("isConnected",false);
                editor.putString("Username",null);
                editor.putString("Password",null);
                editor.commit();
                Intent loading = new Intent(this,LoadingActivity.class);
                loading.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                loading.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(loading);
                finish();
                return true;
            case R.id.action_newpic:
                if(HasCamera()){
                    LaunchCamera();
                } else {
                    Toast.makeText(this, "אין במכשירך מצלמה",
                            Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.action_loadpic:
                loadImageFromGallery();
                return true;
            default:
                return false;
        }
    }

    private  boolean HasCamera(){
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    public void LaunchCamera(){

        decision=1;
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent,REQUEST_IMAGE_CAPTURE);

    }

    //Load the image from the device gallery
    public void loadImageFromGallery() {
        decision=2;
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (decision){
            case 1:
                if(requestCode==REQUEST_IMAGE_CAPTURE && resultCode==RESULT_OK){


                    Bitmap thumbnail;

                    try {
                        thumbnail = MediaStore.Images.Media.getBitmap(
                                getContentResolver(), imageUri);
                        Log.d("newpicsize","W:"+thumbnail.getWidth() + ",H:"+thumbnail.getHeight());
                        Bitmap photo = Bitmap.createScaledBitmap(thumbnail,thumbnail.getWidth()/4,thumbnail.getHeight()/4,false);
                        Log.d("newpicsize","W:"+photo.getWidth() + ",H:"+photo.getHeight());
                        Intent newpic = new Intent(MainActivity.this,newTextPostActivity.class);
                        //newpic.putExtra("Picture",photo);
                        GlobalVar.photoStatic = photo;
                        newpic.putExtra("Mode",Mode.Image);
                        startActivity(newpic);
                        decision=0;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
//                    Bundle extras=data.getExtras();
//                    Bitmap photo=(Bitmap)extras.get("data");
//                    Intent newpic = new Intent(MainActivity.this,newTextPostActivity.class);
//                    //newpic.putExtra("Picture",photo);
//                    GlobalVar.photoStatic = photo;
//                    newpic.putExtra("Mode",Mode.Image);
//                    startActivity(newpic);
//                    decision=0;
                }
                break;
            case 2:
                super.onActivityResult(requestCode, resultCode, data);
                try {
                    // When an Image is picked
                    if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK
                            && null != data) {
                        // Get the Image from data

                        Uri selectedImage = data.getData();
                        String[] filePathColumn = { MediaStore.Images.Media.DATA };

                        // Get the cursor
                        Cursor cursor = getContentResolver().query(selectedImage,
                                filePathColumn, null, null, null);
                        // Move to first row
                        cursor.moveToFirst();

                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                        imgDecodableString = cursor.getString(columnIndex);
                        cursor.close();

                        //ImageView imgView = (ImageView) findViewById(R.id.myImageView);
                        // Set the Image in ImageView after decoding the String
                        //imgView.setImageBitmap(BitmapFactory
                        //        .decodeFile(imgDecodableString));

                        BitmapFactory.Options imageOpts = new BitmapFactory.Options ();
                        imageOpts.inSampleSize = 4;

                        Bitmap photo = BitmapFactory.decodeFile(imgDecodableString,imageOpts);

                        Log.d("loadpicsize","W:"+photo.getWidth() + ",H:"+photo.getHeight());


                        Intent loadpic = new Intent(this,newTextPostActivity.class);
                        //loadpic.putExtra("Picture",photo);
                        GlobalVar.photoStatic = photo;
                        loadpic.putExtra("Mode",Mode.Image);
                        startActivity(loadpic);

                    } else {
                        Toast.makeText(this, "You haven't picked Image",
                                Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
                            .show();
                    Log.d("loadImageError", e.getMessage());
                    decision=0;
                }
                break;

        }
    }

    private void updateListFromDB(){
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            HttpResponse response;
            try {
                response = null;
                request.setURI(new URI("http://drycorporations.wc.lt/selectTopics.php?start="+start_row+"&rownum="+rows_num+"&ruser="+GlobalVar.LoggedInUser.getNUM()));
                response = client.execute(request);
                String result = GlobalMethods.convertStreamToString(response.getEntity().getContent());
                if (!result.equals("null")) {
                    JSONObject root = new JSONObject(result);
                    JSONArray topicsarray = root.getJSONArray("Topic");

                    for (int i = 0; i < topicsarray.length(); i++) {
                        JSONObject TopicObj = topicsarray.getJSONObject(i);
                        Topic topic = new Topic();
                        topic.setNUM(TopicObj.getInt("Num"));
                        topic.setTitle(TopicObj.getString("Title"));
                        topic.setContent(TopicObj.getString("Content"));
                        topic.setVotesup(TopicObj.getInt("Votes"));
                        topic.setSubject((Subject) GlobalMethods.getObjectByNum(TopicObj.getInt("SubjectNum"), GlobalVar.subjectsList));
                        topic.setUserVoteValue(TopicObj.getInt("RUserVoteValue"));
                        if(!TopicObj.getString("Image").equals("")){
                            topic.setImage(GlobalMethods.base64ToBitmap(TopicObj.getString("Image")));
                            //Bitmap p = BitmapFactory.decodeResource(getResources(), R.drawable.background);
                            Log.d("TopicImageView", TopicObj.getString("Image"));
                        }
                        User user = new User();
                        request.setURI(new URI("http://drycorporations.wc.lt/selectByNum.php?table=User&num=" + TopicObj.getInt("UserNum")));
                        response = client.execute(request);
                        String userR = GlobalMethods.convertStreamToString(response.getEntity().getContent());
                        JSONObject userObj = new JSONObject(userR);
                        user.setNUM(userObj.getInt("Num"));
                        user.setUsername(userObj.getString("Username"));
                        topic.setUsername(user);
                        Log.d("username", topic.getUsername().getUsername());
                        ArrayList<Tag> tagsList = new ArrayList<>();
                        ArrayList<Comment> commentsList = new ArrayList<>();
                        try {
                            response = null;
                            request.setURI(new URI("http://drycorporations.wc.lt/selectTopicTag.php?topic="+TopicObj.getInt("Num")));
                            response = client.execute(request);
                            String result2 = GlobalMethods.convertStreamToString(response.getEntity().getContent());
                            if (!result2.equals("null")) {
                                JSONObject root2 = new JSONObject(result2);
                                JSONArray tagsarray = root2.getJSONArray("Tags");

                                for (int j = 0; j < tagsarray.length(); j++) {
                                    JSONObject TagsObj = tagsarray.getJSONObject(j);
                                    Tag tag = new Tag();
                                    tag.setNUM(TagsObj.getInt("Num"));
                                    tag.setName(TagsObj.getString("Name"));
                                    tagsList.add(tag);
                                }
                            }
                            response = null;
                            request.setURI(new URI("http://drycorporations.wc.lt/selectComments.php?topic="+topic.getNUM()));
                            response = client.execute(request);
                            result2 = GlobalMethods.convertStreamToString(response.getEntity().getContent());
                            if (!result2.equals("null")) {
                                JSONObject root2 = new JSONObject(result2);
                                JSONArray array = root2.getJSONArray("Comments");

                                for (int j = 0; j < array.length(); j++) {
                                    JSONObject Obj = array.getJSONObject(j);
                                    //need to add more details here!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
                                    Comment comment = new Comment();
                                    comment.setNUM(Obj.getInt("Num"));
                                    comment.setContent(Obj.getString("Content"));
                                    commentsList.add(comment);
                                }
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
                        topic.setTags(tagsList);
                        topic.setComments(commentsList);

                        topicsLoaded.add(topic);
                    }
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
        } catch (Exception ex){

        }
    }

    public void OpenPostView(){
        Intent openPost = new Intent(this,PostViewActivity.class);
    }

}

class RVAdapter extends RecyclerView.Adapter<RVAdapter.TopicsViewHolder>{

    List<Topic> topics;

    View.OnClickListener mItemClick;
    LayoutInflater inflater;

    Resources res;

    String commentString = "";

    public RVAdapter(List<Topic> topics,View.OnClickListener mItemClick,LayoutInflater in){
        this.topics = topics;
        this.mItemClick = mItemClick;
        this.inflater = in;
    }

    @Override
    public TopicsViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.maincard, viewGroup, false);
            TopicsViewHolder pvh = new TopicsViewHolder(v);
            res = viewGroup.getResources();
            //commentString = viewGroup.getResources().getString(R.string.post_comment);
            return pvh;
    }

    @Override
    public void onBindViewHolder(TopicsViewHolder topicsViewHolder, int i) {
        topicsViewHolder.txbTitle.setText(topics.get(i).getTitle());
        topicsViewHolder.txbTitle.setOnClickListener(mItemClick);
        topicsViewHolder.txbTitle.setTag(String.valueOf(topics.get(i).getNUM()));
        topicsViewHolder.txbContent.setText(topics.get(i).getContent());
        topicsViewHolder.txbContent.setTag(String.valueOf(topics.get(i).getNUM()));
        topicsViewHolder.txbContent.setOnClickListener(mItemClick);
        topicsViewHolder.txbSubject.setText(topics.get(i).getSubject().getName());
        topicsViewHolder.txbComments.setText(topics.get(i).getComments().size() + " תגובות");
        topicsViewHolder.txbComments.setTag(String.valueOf(topics.get(i).getNUM()));
        topicsViewHolder.txbComments.setOnClickListener(mItemClick);
        String sign = "";
        if(topics.get(i).getVotesup() > 0)
            sign = "+";
        topicsViewHolder.txbVotesUp.setText(sign + String.valueOf(topics.get(i).getVotesup()));

        if(topics.get(i).getImage() == null) {
            topicsViewHolder.imgExamImage.setVisibility(View.GONE);
        } else {
            topicsViewHolder.imgExamImage.setVisibility(View.VISIBLE);
            topicsViewHolder.imgExamImage.setImageBitmap(topics.get(i).getImage());
            topicsViewHolder.imgExamImage.setOnClickListener(mItemClick);
            topicsViewHolder.imgExamImage.setTag(String.valueOf(topics.get(i).getNUM()));
        }

        topicsViewHolder.tagsLayout.removeAllViews();

        for(Tag t : topics.get(i).getTags()){
            Log.d("TagsTopic:" + String.valueOf(topics.get(i).getNUM()),t.getName());
            TextView tv =(TextView) inflater.inflate(R.layout.tag, null);
            tv.setText(t.getName());
            tv.setTextSize(10);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            llp.setMargins(5, 5, 5, 5);
            tv.setLayoutParams(llp);
            topicsViewHolder.tagsLayout.addView(tv);
        }

        if(topics.get(i).getTags().size() == 0){
            topicsViewHolder.tagsLayout.setVisibility(View.GONE);
        } else {
            topicsViewHolder.tagsLayout.setVisibility(View.VISIBLE);
        }

        int userValue = topics.get(i).getUserVoteValue();
        switch(userValue){
            case 1:
                topicsViewHolder.btnVoteUp.setImageResource(R.drawable.ic_voteup_checked);
                topicsViewHolder.btnVoteDown.setImageResource(R.drawable.ic_votedown);
                break;
            case -1:
                topicsViewHolder.btnVoteUp.setImageResource(R.drawable.ic_hardware_keyboard_arrow_up);
                topicsViewHolder.btnVoteDown.setImageResource(R.drawable.ic_votedown_checked);
                break;
            case 0:
                topicsViewHolder.btnVoteUp.setImageResource(R.drawable.ic_hardware_keyboard_arrow_up);
                topicsViewHolder.btnVoteDown.setImageResource(R.drawable.ic_votedown);
                break;
            default:
                topicsViewHolder.btnVoteUp.setImageResource(R.drawable.ic_hardware_keyboard_arrow_up);
                topicsViewHolder.btnVoteDown.setImageResource(R.drawable.ic_votedown);
                break;

        }

        final Topic t = topics.get(i);
        final ImageView voted = topicsViewHolder.btnVoteDown;
        final  ImageView voteu = topicsViewHolder.btnVoteUp;
        final TextView voteUp = topicsViewHolder.txbVotesUp;

        topicsViewHolder.btnVoteUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(t.getUserVoteValue() != 1){
                    t.setVotesup(t.getVotesup()-t.getUserVoteValue());
                    t.setUserVoteValue(1);
                    t.setVotesup(t.getVotesup()+1);
                    String sign = "";
                    if(t.getVotesup() > 0)
                        sign = "+";
                    voteUp.setText(sign+String.valueOf(t.getVotesup()));
                    ((ImageView) v).setImageResource(R.drawable.ic_voteup_checked);
                    voted.setImageResource(R.drawable.ic_votedown);
                } else {
                    t.setUserVoteValue(0);
                    t.setVotesup(t.getVotesup()-1);
                    String sign = "";
                    if(t.getVotesup() > 0)
                        sign = "+";
                    voteUp.setText(sign+String.valueOf(t.getVotesup()));
                    ((ImageView) v).setImageResource(R.drawable.ic_hardware_keyboard_arrow_up);
                    voted.setImageResource(R.drawable.ic_votedown);
                }
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet();
                    HttpResponse response;
                    try {
                        response = null;
                        request.setURI(new URI("http://drycorporations.wc.lt/UserVoteUp.php?user="+GlobalVar.LoggedInUser.getNUM()+"&topic="+t.getNUM()+"&value="+t.getUserVoteValue()));
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

        topicsViewHolder.btnVoteDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(t.getUserVoteValue() != -1){
                    t.setVotesup(t.getVotesup()-t.getUserVoteValue());
                    t.setUserVoteValue(-1);
                    t.setVotesup(t.getVotesup()-1);
                    String sign = "";
                    if(t.getVotesup() > 0)
                        sign = "+";
                    voteUp.setText(sign+String.valueOf(t.getVotesup()));
                    ((ImageView) v).setImageResource(R.drawable.ic_votedown_checked);
                    voteu.setImageResource(R.drawable.ic_hardware_keyboard_arrow_up);
                } else {
                    t.setUserVoteValue(0);
                    t.setVotesup(t.getVotesup()+1);
                    String sign = "";
                    if(t.getVotesup() > 0)
                        sign = "+";
                    voteUp.setText(sign+String.valueOf(t.getVotesup()));
                    ((ImageView) v).setImageResource(R.drawable.ic_votedown);
                    voteu.setImageResource(R.drawable.ic_hardware_keyboard_arrow_up);
                }
                try {
                    HttpClient client = new DefaultHttpClient();
                    HttpGet request = new HttpGet();
                    HttpResponse response;
                    try {
                        response = null;
                        request.setURI(new URI("http://drycorporations.wc.lt/UserVoteUp.php?user="+GlobalVar.LoggedInUser.getNUM()+"&topic="+t.getNUM()+"&value="+t.getUserVoteValue()));
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

    }

    @Override
    public int getItemCount() {
        return topics.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public static class TopicsViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView txbTitle;
        TextView txbSubject;
        LinearLayout tagsLayout;
        TextView txbContent;
        TextView txbComments;
        TextView txbShare;
        TextView txbVotesUp;
        ImageView imgExamImage;

        ImageView btnVoteUp;
        ImageView btnVoteDown;

        TopicsViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.main_cardview);
            txbTitle = (TextView)itemView.findViewById(R.id.title_text);
            txbContent = (TextView)itemView.findViewById(R.id.info_text);
            txbComments = (TextView)itemView.findViewById(R.id.comments_text);
            txbVotesUp = (TextView)itemView.findViewById(R.id.votesup_post);
            txbShare = (TextView)itemView.findViewById(R.id.post_share);
            txbSubject = (TextView) itemView.findViewById(R.id.subject_text);
            tagsLayout = (LinearLayout) itemView.findViewById(R.id.TagsLayout_post);
            imgExamImage = (ImageView)itemView.findViewById(R.id.page_image_content);
            imgExamImage.setVisibility(View.GONE);

            btnVoteUp = (ImageView) itemView.findViewById(R.id.voteup_action_post);
            btnVoteDown = (ImageView) itemView.findViewById(R.id.votedown_action_post);
        }
    }

}
