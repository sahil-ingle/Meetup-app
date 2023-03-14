package com.example.meetup.activities;

import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.meetup.R;
import com.example.meetup.activities.Login;
import com.example.meetup.adapters.RecentConversationsAdapter;
import com.example.meetup.listenrs.ConversionListener;
import com.example.meetup.models.ChatMessage;
import com.example.meetup.models.User;
import com.example.meetup.utilities.Constants;
import com.example.meetup.utilities.PreferenceManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversionListener {

    TextView mName;
    RoundedImageView mProfileImg;
    FloatingActionButton mFabNewChat;
    Button mLogOutbtn;
    RecyclerView mConversationRecycleView;
    ProgressBar mProgressBar;

    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private RecentConversationsAdapter conversationsAdapter;
    private FirebaseFirestore database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mName = findViewById(R.id.Name);
        mProfileImg = findViewById(R.id.profileImage);
        mFabNewChat = findViewById(R.id.findFriends);
        mLogOutbtn = findViewById(R.id.Logout);
        mConversationRecycleView = findViewById(R.id.conversationRecyclerView);
        mProgressBar = findViewById(R.id.progressBar);

        preferenceManager = new PreferenceManager(getApplicationContext());
        init();
        loadUserDetails();
        getToken();
        setListeners();
        listenConversations();
    }

    private void init(){
        conversations = new ArrayList<>();
        conversationsAdapter = new RecentConversationsAdapter(conversations, this);
        mConversationRecycleView.setAdapter(conversationsAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void setListeners(){
        //mProfileImg.setOnClickListener(v ->
                //startActivity(new Intent(getApplicationContext(), EditProfile.class)));
        mLogOutbtn.setOnClickListener(v -> Logout());
        mFabNewChat.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),UsersActivity.class)));

    }

        private void loadUserDetails(){
            mName.setText(preferenceManager.getString(Constants.KEY_NAME));
            byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMG), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,bytes.length);
            mProfileImg.setImageBitmap(bitmap);
        }

        private void listenConversations(){
            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                    .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                    .addSnapshotListener(eventListener);
            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                    .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                    .addSnapshotListener(eventListener);
        }

        private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
            if (error != null){
                return;
            }
            if (value != null){
                for (DocumentChange documentChange : value.getDocumentChanges()){
                    if (documentChange.getType() == DocumentChange.Type.ADDED){
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        ChatMessage chatMessage = new ChatMessage();
                        chatMessage.senderId = senderId;
                        chatMessage.receiverId = receiverId;
                        if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)){
                            chatMessage.conversionImg = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMG);
                            chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                            chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        }else {
                            chatMessage.conversionImg = documentChange.getDocument().getString(Constants.KEY_SENDER_IMG);
                            chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                            chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        }
                        chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MSG);
                        chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                        conversations.add(chatMessage);
                    }else if (documentChange.getType() == DocumentChange.Type.MODIFIED){
                        for (int i = 0; i < conversations.size(); i++){
                            String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                            String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                            if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)){
                                conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MSG);
                                conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                                break;
                            }
                        }
                    }
                }
                Collections.sort(conversations, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
                conversationsAdapter.notifyDataSetChanged();
                mConversationRecycleView.smoothScrollToPosition(0);
                mConversationRecycleView.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
            }
        };

         private void getToken(){
             FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
         }

         private void updateToken(String token){
             FirebaseFirestore database = FirebaseFirestore.getInstance();
             DocumentReference documentReference =
                     database.collection(Constants.KEY_COLLECTION_USERS).document(
                             preferenceManager.getString(Constants.KEY_USER_ID)
                     );
             documentReference.update(Constants.KEY_FCM, token)
                     //.addOnSuccessListener(unused -> Toast.makeText(getApplicationContext(), "Token Updated", Toast.LENGTH_SHORT).show())
                     .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Unable to update token", Toast.LENGTH_SHORT).show());
         }

    private void Logout() {
        FirebaseFirestore database =FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM, FieldValue.delete());
        documentReference.update(updates)
                        .addOnSuccessListener(unused -> {
                            preferenceManager.clear();
                            Toast.makeText(getApplicationContext(), "Logout Successfully!!!", Toast.LENGTH_SHORT).show();
                            FirebaseAuth.getInstance().signOut();
                            startActivity(new Intent(getApplicationContext(), Home.class));
                            finish();
                        })
                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Unable to Logout", Toast.LENGTH_SHORT).show());

    }

    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
    }
}