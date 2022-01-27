package com.gabriel.aranias.go4lunch_v2.ui.chat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.gabriel.aranias.go4lunch_v2.databinding.ActivityChatBinding;
import com.gabriel.aranias.go4lunch_v2.model.Message;
import com.gabriel.aranias.go4lunch_v2.model.User;
import com.gabriel.aranias.go4lunch_v2.service.user.UserHelper;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private final UserHelper userHelper = UserHelper.getInstance();
    private final static String EXTRA_WORKMATE = "workmate";
    private static final String MESSAGES_COLLECTION = "messages";
    private static final String USER_ID = "uid";
    private static final String RECEIVER_ID_FIELD = "receiverId";
    private static final String CONTENT_FIELD = "content";
    private static final String TIMESTAMP_FIELD = "timestamp";
    private User workmate;
    private List<Message> messages;
    private ChatAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setListeners();
        loadReceiverDetails();
        initData();
        listenToMessages();
    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(USER_ID, userHelper.getCurrentUser().getUid());
        message.put(RECEIVER_ID_FIELD, workmate.getUid());
        message.put(CONTENT_FIELD, binding.chatMsgInput.getText().toString());
        message.put(TIMESTAMP_FIELD, new Date());
        db.collection(MESSAGES_COLLECTION).add(message);
        binding.chatMsgInput.setText(null);
    }

    private void listenToMessages() {
        db.collection(MESSAGES_COLLECTION)
                .whereEqualTo(USER_ID, userHelper.getCurrentUser().getUid())
                .whereEqualTo(RECEIVER_ID_FIELD, workmate.getUid())
                .addSnapshotListener(eventListener);
        db.collection(MESSAGES_COLLECTION)
                .whereEqualTo(USER_ID, workmate.getUid())
                .whereEqualTo(RECEIVER_ID_FIELD, userHelper.getCurrentUser().getUid())
                .addSnapshotListener(eventListener);
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = messages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    Message message = new Message();
                    message.senderId = documentChange.getDocument().getString(USER_ID);
                    message.receiverId = documentChange.getDocument().getString(RECEIVER_ID_FIELD);
                    message.content = documentChange.getDocument().getString(CONTENT_FIELD);
                    message.date = getReadableDate(documentChange.getDocument().getDate(TIMESTAMP_FIELD));
                    message.dateObject = documentChange.getDocument().getDate(TIMESTAMP_FIELD);
                    messages.add(message);
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Collections.sort(messages, Comparator.comparing(obj -> obj.dateObject));
            }
            if (count == 0) {
                adapter.notifyDataSetChanged();
            } else {
                adapter.notifyItemRangeInserted(messages.size(), messages.size());
                binding.chatRv.smoothScrollToPosition(messages.size() - 1);
            }
            binding.chatRv.setVisibility(View.VISIBLE);
        }
        binding.chatProgressBar.setVisibility(View.GONE);
    };

    private void initData() {
        messages = new ArrayList<>();
        adapter = new ChatAdapter(messages, workmate, userHelper.getCurrentUser().getUid(), this);
        binding.chatRv.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();
    }

    private void loadReceiverDetails() {
        Intent intent = this.getIntent();
        if (intent.getExtras() != null) {
            workmate = (User) intent.getSerializableExtra(EXTRA_WORKMATE);
            binding.workmateName.setText(workmate.getUsername());
        }
    }

    private void setListeners() {
        binding.chatBack.setOnClickListener(v -> onBackPressed());
        binding.chatSendLayout.setOnClickListener(v -> sendMessage());
    }

    private String getReadableDate(Date date) {
        return new SimpleDateFormat("MMMM dd, yyy - hh:mm a", Locale.getDefault()).format(date);
    }
}