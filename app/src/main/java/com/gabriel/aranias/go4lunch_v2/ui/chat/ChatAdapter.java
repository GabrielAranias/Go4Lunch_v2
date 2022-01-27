package com.gabriel.aranias.go4lunch_v2.ui.chat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.gabriel.aranias.go4lunch_v2.R;
import com.gabriel.aranias.go4lunch_v2.databinding.ReceivedMessageItemBinding;
import com.gabriel.aranias.go4lunch_v2.databinding.SentMessageItemBinding;
import com.gabriel.aranias.go4lunch_v2.model.Message;
import com.gabriel.aranias.go4lunch_v2.model.User;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Message> messages;
    private final User workmate;
    private final String senderId;
    private Context context;
    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<Message> messages, User workmate, String senderId, Context context) {
        this.messages = messages;
        this.workmate = workmate;
        this.senderId = senderId;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(SentMessageItemBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        } else {
            return new ReceivedMessageViewHolder(ReceivedMessageItemBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(messages.get(position));
        } else {
            ((ReceivedMessageViewHolder) holder).setData(messages.get(position), workmate);
        }
    }

    @Override
    public int getItemCount() {
        return messages == null ? 0 : messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position).senderId.equals(senderId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    public static class SentMessageViewHolder extends RecyclerView.ViewHolder {

        private final SentMessageItemBinding binding;

        public SentMessageViewHolder(SentMessageItemBinding sentBinding) {
            super(sentBinding.getRoot());
            binding = sentBinding;
        }

        public void setData(Message message) {
            binding.chatMsgContent.setText(message.content);
            binding.chatMsgDate.setText(message.date);
        }
    }

    public class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {

        private final ReceivedMessageItemBinding binding;

        public ReceivedMessageViewHolder(ReceivedMessageItemBinding receiverBinding) {
            super(receiverBinding.getRoot());
            binding = receiverBinding;
        }

        public void setData(Message message, User workmate) {
            binding.chatMsgContent.setText(message.content);
            binding.chatMsgDate.setText(message.date);
            getPhoto(workmate);
        }

        private void getPhoto(User workmate) {
            if (workmate.getPictureUrl() != null) {
                Glide.with(context)
                        .load(workmate.getPictureUrl())
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.chatMsgAvatar);
            } else {
                Glide.with(context)
                        .load(R.drawable.default_user_avatar)
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.chatMsgAvatar);
            }
        }
    }
}
