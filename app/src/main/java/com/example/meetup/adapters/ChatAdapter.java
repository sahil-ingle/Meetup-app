package com.example.meetup.adapters;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meetup.databinding.ItemContainerReceviedMsgBinding;
import com.example.meetup.databinding.ItemContainerSentMsgBinding;
import com.example.meetup.models.ChatMessage;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    private final List<ChatMessage> chatMessages;
    private final Bitmap receiverProfileImg;
    private final String senderId;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;


    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImg, String senderId) {
        this.chatMessages = chatMessages;
        this.receiverProfileImg = receiverProfileImg;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT){
            return new SentMsgViewHolder(
                    ItemContainerSentMsgBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }else {
            return new ReceivedMsgViewHolder(
                    ItemContainerReceviedMsgBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT){
            ((SentMsgViewHolder) holder).setData(chatMessages.get(position));
        }else {
            ((ReceivedMsgViewHolder) holder).setData(chatMessages.get(position), receiverProfileImg);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMsgViewHolder extends RecyclerView.ViewHolder {

        private final ItemContainerSentMsgBinding binding;

        SentMsgViewHolder(ItemContainerSentMsgBinding itemContainerSentMsgBinding){
            super(itemContainerSentMsgBinding.getRoot());
            binding = itemContainerSentMsgBinding;
        }

        void setData(ChatMessage chatMessage){
            binding.textMsg.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
        }
    }

    static class ReceivedMsgViewHolder extends RecyclerView.ViewHolder{
        private final ItemContainerReceviedMsgBinding binding;
        ReceivedMsgViewHolder(ItemContainerReceviedMsgBinding itemContainerReceviedMsgBinding){
            super(itemContainerReceviedMsgBinding.getRoot());
            binding = itemContainerReceviedMsgBinding;
        }

        void setData(ChatMessage chatMessage, Bitmap receiverProfileImg){
            binding.textMsg.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
            binding.profileImage.setImageBitmap(receiverProfileImg);

        }
    }
}
