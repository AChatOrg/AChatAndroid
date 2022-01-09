package com.hyapp.achat.view.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.format.DateUtils;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;

import com.aghajari.rlottie.AXrLottieImageView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.hyapp.achat.R;
import com.hyapp.achat.model.entity.People;
import com.hyapp.achat.model.entity.SortedList;
import com.hyapp.achat.viewmodel.utils.TimeUtils;
import com.hyapp.achat.model.entity.ChatMessage;
import com.hyapp.achat.model.entity.Contact;
import com.hyapp.achat.model.entity.DetailsMessage;
import com.hyapp.achat.model.entity.LottieMessage;
import com.hyapp.achat.model.entity.Message;
import com.hyapp.achat.model.entity.ProfileMessage;
import com.hyapp.achat.model.entity.TextMessage;
import com.hyapp.achat.model.entity.utils.MessageUtils;
import com.hyapp.achat.model.entity.utils.PersonUtils;
import com.hyapp.achat.view.component.emojiview.view.AXEmojiTextView;
import com.hyapp.achat.view.component.GroupAvatarView;
import com.hyapp.achat.view.utils.UiUtils;

import java.util.LinkedList;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.Holder> {

    public static final byte PAYLOAD_BUBBLE = 0;
    public static final byte PAYLOAD_READ = 1;

    public static final int TEXT_SIZE_SP = 14;
    public static final int EMOJI_SIZE_LARGEST_SP = 36;

    private final Context context;
    private List<Message> messages;
    private final int sp1;

    public MessageAdapter(Context context) {
        this.context = context;
        this.messages = new LinkedList<>();
        sp1 = UiUtils.sp2px(context, 1);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        return message.getTransferType() + message.getType();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case Message.TRANSFER_TYPE_SEND + Message.TYPE_TEXT:
                return new TextHolder(LayoutInflater.from(context).inflate(R.layout.item_message_text_send, parent, false));
            case Message.TRANSFER_TYPE_RECEIVE + Message.TYPE_TEXT:
                return new TextHolder(LayoutInflater.from(context).inflate(R.layout.item_message_text_receive, parent, false));
            case Message.TRANSFER_TYPE_SEND + Message.TYPE_LOTTIE:
                return new LottieHolder(LayoutInflater.from(context).inflate(R.layout.item_message_lottie_send, parent, false));
            case Message.TRANSFER_TYPE_RECEIVE + Message.TYPE_LOTTIE:
                return new LottieHolder(LayoutInflater.from(context).inflate(R.layout.item_message_lottie_receive, parent, false));
            case Message.TRANSFER_TYPE_RECEIVE + Message.TYPE_DETAILS:
                return new DetailsHolder(LayoutInflater.from(context).inflate(R.layout.item_message_details, parent, false));
            case Message.TRANSFER_TYPE_RECEIVE + Message.TYPE_PROFILE:
                return new SingleProfileHolder(LayoutInflater.from(context).inflate(R.layout.item_message_profile, parent, false));
        }
        throw new RuntimeException("incorrect view type :" + getClass().getName());
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            holder.bind(messages.get(position), payloads);
        }
    }

    @Override
    public void onViewRecycled(@NonNull Holder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof LottieHolder) {
            ((LottieHolder) holder).recycle();
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void add(Message message) {
        boolean haveDateSeparatorPrev = false;
        if (message instanceof ChatMessage) {
            haveDateSeparatorPrev = setupMessagesBubble((ChatMessage) message);
        }
        if (haveDateSeparatorPrev) {
            Message detailsMessage = new DetailsMessage(message.getTimeMillis());
            messages.add(detailsMessage);
            messages.add(message);
            notifyItemRangeInserted(messages.size() - 2, 2);
        } else {
            messages.add(message);
            notifyItemInserted(messages.size() - 1);
        }
    }

    public void addAndScroll(List<Message> list) {
        add(message);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void resetList(List<Message> list) {
        messages = list;
        notifyDataSetChanged();
    }

    private boolean setupMessagesBubble(ChatMessage message) {
        boolean haveDateSeparatorPrev = false;
        if (message.getTransferType() == Message.TRANSFER_TYPE_SEND) {
            if (messages.size() == 1) {
                message.setBubbleRes(MessageUtils.BUBBLE_RES_SEND_SINGLE);
                haveDateSeparatorPrev = true;
            } else {
                Message prev = messages.get(messages.size() - 1);
                if (prev instanceof ChatMessage
                        && prev.getTransferType() == Message.TRANSFER_TYPE_SEND
                        && message.getTimeMillis() - prev.getTimeMillis() < 60000
                ) {
                    message.setBubbleRes(MessageUtils.BUBBLE_RES_SEND_END);
                    if (messages.size() >= 3) {
                        Message prevPrev = messages.get(messages.size() - 2);
                        if (prevPrev instanceof ChatMessage
                                && prevPrev.getTransferType() == Message.TRANSFER_TYPE_SEND
                                && prev.getTimeMillis() - prevPrev.getTimeMillis() < 60000
                        ) {
                            ((ChatMessage) prev).setBubbleRes(MessageUtils.BUBBLE_RES_SEND_MIDDLE);
                        } else {
                            ((ChatMessage) prev).setBubbleRes(MessageUtils.BUBBLE_RES_SEND_START);
                        }
                    } else {
                        ((ChatMessage) prev).setBubbleRes(MessageUtils.BUBBLE_RES_SEND_START);
                    }
                    notifyItemChanged(messages.indexOf(prev), PAYLOAD_BUBBLE);
                } else {
                    if (!DateUtils.isToday(prev.getTimeMillis())) {
                        haveDateSeparatorPrev = true;
                    }
                    message.setBubbleRes(MessageUtils.BUBBLE_RES_SEND_SINGLE);
                }
            }
        } else {
            if (messages.size() == 1) {
                message.setBubbleRes(MessageUtils.BUBBLE_RES_RECEIVE_SINGLE);
                haveDateSeparatorPrev = true;
            } else {
                Message prev = messages.get(messages.size() - 1);
                if (prev instanceof ChatMessage
                        && prev.getTransferType() == Message.TRANSFER_TYPE_RECEIVE
                        && message.getTimeMillis() - prev.getTimeMillis() < 60000
                ) {
                    message.setBubbleRes(MessageUtils.BUBBLE_RES_RECEIVE_END);
                    if (messages.size() >= 3) {
                        Message prevPrev = messages.get(messages.size() - 2);
                        if (prevPrev instanceof ChatMessage
                                && prevPrev.getTransferType() == Message.TRANSFER_TYPE_RECEIVE
                                && prev.getTimeMillis() - prevPrev.getTimeMillis() < 60000
                        ) {
                            ((ChatMessage) prev).setBubbleRes(MessageUtils.BUBBLE_RES_RECEIVE_MIDDLE);
                        } else {
                            ((ChatMessage) prev).setBubbleRes(MessageUtils.BUBBLE_RES_RECEIVE_START);
                        }
                    } else {
                        ((ChatMessage) prev).setBubbleRes(MessageUtils.BUBBLE_RES_RECEIVE_START);
                    }
                    notifyItemChanged(messages.indexOf(prev), PAYLOAD_BUBBLE);
                } else {
                    if (!DateUtils.isToday(prev.getTimeMillis())) {
                        haveDateSeparatorPrev = true;
                    }
                    message.setBubbleRes(MessageUtils.BUBBLE_RES_RECEIVE_SINGLE);
                }
            }
        }
        return haveDateSeparatorPrev;
    }

    public abstract static class Holder extends RecyclerView.ViewHolder {

        public Holder(@NonNull View itemView) {
            super(itemView);
        }

        public abstract void bind(Message message);

        public void bind(Message message, List<Object> payloads) {

        }
    }

    public abstract static class ProfileHolder extends Holder implements View.OnClickListener {

        private final TextView name, description;

        public ProfileHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            description = itemView.findViewById(R.id.description);
            itemView.setOnClickListener(this);
        }

        @Override
        public void bind(Message message) {
            ProfileMessage profileMessage = (ProfileMessage) message;
            name.setText(profileMessage.getContact().getName());
            description.setText(profileMessage.getContact().getBio());
        }

        @Override
        public void onClick(View v) {
            //todo go to profile page
        }
    }

    public static class SingleProfileHolder extends ProfileHolder {

        private final GroupAvatarView avatar;
        private final TextView rank, onlineTime;

        public SingleProfileHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            rank = itemView.findViewById(R.id.rank);
            onlineTime = itemView.findViewById(R.id.lastOnline);
        }

        @Override
        public void bind(Message message) {
            super.bind(message);
            ProfileMessage profileMessage = (ProfileMessage) message;
            Contact contact = profileMessage.getContact();
            if (profileMessage.getContact().getType() == Contact.TYPE_SINGLE) {
                String[] avatars = contact.getAvatars();
                avatar.setAvatars(avatars.length > 0 ? avatars[0] : null);
                if (contact.getOnlineTime() == Contact.TIME_ONLINE) {
                    onlineTime.setText("");
                    onlineTime.setBackgroundResource(R.drawable.last_online_profile_bg_green);
                } else {
                    onlineTime.setText(TimeUtils.timeAgoShort(System.currentTimeMillis() - contact.getOnlineTime()));
                    onlineTime.setBackgroundResource(R.drawable.last_online_profile_bg_grey);
                }
                onlineTime.setVisibility(View.VISIBLE);
            } else {
                avatar.setAvatars(contact.getAvatars());
                onlineTime.setVisibility(View.GONE);
            }

            Pair<Integer, Integer> pair = PersonUtils.rankInt2rankStrResAndColor(contact.getRank());
            rank.setText(pair.first);
            rank.setTextColor(pair.second);
        }
    }

    public static class DetailsHolder extends Holder {

        public DetailsHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void bind(Message message) {
            ((TextView) itemView).setText(((DetailsMessage) message).getDetails());
        }
    }

    public abstract class ChatHolder extends Holder implements View.OnClickListener {

        protected final SimpleDraweeView avatar;
        protected final TextView time;
        protected final AppCompatImageView read;
        protected final View online;

        public ChatHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            time = itemView.findViewById(R.id.time);
            read = itemView.findViewById(R.id.read);
            online = itemView.findViewById(R.id.lastOnline);

            itemView.setOnClickListener(this);
        }

        @Override
        public void bind(Message message) {
            ChatMessage chatMessage = (ChatMessage) message;
            setBubble(chatMessage);
            setRead(chatMessage);
            time.setText(chatMessage.getTime());
        }

        @Override
        public void bind(Message message, List<Object> payloads) {
            super.bind(message, payloads);
            for (Object payload : payloads) {
                switch ((byte) payload) {
                    case PAYLOAD_BUBBLE:
                        setBubble((ChatMessage) message);
                        break;
                    case PAYLOAD_READ:
                        setRead((ChatMessage) message);
                        break;
                }
            }
        }

        @SuppressLint("WrongConstant")
        protected void setBubble(ChatMessage message) {
            int bubbleRes = message.getBubbleRes();
            View bubbleView = getBubbleView();
            if (bubbleView != null) {
                bubbleView.setBackgroundResource(bubbleRes);
            }
            if (message.getTransferType() == Message.TRANSFER_TYPE_RECEIVE) {
                if (bubbleRes == MessageUtils.BUBBLE_RES_RECEIVE_SINGLE || bubbleRes == MessageUtils.BUBBLE_RES_RECEIVE_END) {
                    avatar.setVisibility(View.VISIBLE);
                    time.setVisibility(View.VISIBLE);

                    String[] avatars = message.getSender().getAvatars();
                    avatar.setImageURI(avatars.length > 0 ? avatars[0] : null);
                    online.setVisibility(message.getSender().getOnlineTime() == Contact.TIME_ONLINE ?
                            View.VISIBLE : View.GONE);

                } else {
                    avatar.setVisibility(View.GONE);
                    time.setVisibility(View.GONE);
                }
            } else if (message.getTransferType() == Message.TRANSFER_TYPE_SEND) {
                if (bubbleRes == MessageUtils.BUBBLE_RES_SEND_SINGLE || bubbleRes == MessageUtils.BUBBLE_RES_SEND_END) {
                    time.setVisibility(View.VISIBLE);
                } else {
                    time.setVisibility(View.GONE);
                }
            }
        }

        protected abstract View getBubbleView();

        private void setRead(ChatMessage chatMessage) {
            if (chatMessage.getTransferType() == Message.TRANSFER_TYPE_SEND) {
                read.setImageResource(chatMessage.getDeliveryRes());
            }
        }

        @Override
        public void onClick(View v) {
            int bubbleRes = ((ChatMessage) messages.get(getAdapterPosition())).getBubbleRes();
            if (bubbleRes != MessageUtils.BUBBLE_RES_RECEIVE_SINGLE
                    && bubbleRes != MessageUtils.BUBBLE_RES_RECEIVE_END
                    && bubbleRes != MessageUtils.BUBBLE_RES_SEND_SINGLE
                    && bubbleRes != MessageUtils.BUBBLE_RES_SEND_END
            ) {
                time.setVisibility(time.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
            }
        }
    }

    public class TextHolder extends ChatHolder {

        private final AXEmojiTextView textView;

        public TextHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text);
        }

        @Override
        public void bind(Message message) {
            super.bind(message);
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText();
            textView.setText(text);
            setTextSize(text, textMessage.getExtraTextSize());
        }

        @Override
        protected View getBubbleView() {
            return textView;
        }

        private void setTextSize(CharSequence text, int textSizeFactor) {
            float textSize = (TEXT_SIZE_SP + textSizeFactor) * sp1;
            float emojiSize = (TEXT_SIZE_SP + 3 + textSizeFactor) * sp1;
            boolean hasText = false;

            for (int i = 0; i < text.length(); i++) {
                if ((int) text.charAt(i) < 2000) {
                    hasText = true;
                    break;
                }
            }

            if (!hasText) {
                emojiSize = (EMOJI_SIZE_LARGEST_SP + textSizeFactor) * sp1;
                for (int i = 1; i < text.length(); i++) {
                    if (emojiSize <= (TEXT_SIZE_SP + 3 + textSizeFactor) * sp1) {
                        emojiSize = (TEXT_SIZE_SP + 3 + textSizeFactor) * sp1;
                        break;
                    }
                    emojiSize -= sp1;
                }
                textSize = emojiSize * 0.8f;
            }
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            textView.setEmojiSize((int) emojiSize);
        }
    }

    public class LottieHolder extends ChatHolder {

        private final AXrLottieImageView lottieImageView;

        public LottieHolder(@NonNull View itemView) {
            super(itemView);
            lottieImageView = itemView.findViewById(R.id.lottieImageView);
        }

        @Override
        public void bind(Message message) {
            super.bind(message);
            lottieImageView.setLottieDrawable(((LottieMessage) message).getDrawable());
            lottieImageView.playAnimation();
        }

        public void recycle() {
            lottieImageView.stopAnimation();
        }

        @Override
        protected View getBubbleView() {
            return null;
        }
    }
}
