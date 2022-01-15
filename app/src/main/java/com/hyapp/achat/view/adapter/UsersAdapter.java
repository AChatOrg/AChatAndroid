package com.hyapp.achat.view.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.hyapp.achat.R;
import com.hyapp.achat.databinding.ItemPeopleBinding;
import com.hyapp.achat.model.entity.Contact;
import com.hyapp.achat.model.entity.User;
import com.hyapp.achat.model.entity.SortedList;
import com.hyapp.achat.model.entity.UserConsts;
import com.hyapp.achat.model.entity.utils.PersonUtils;
import com.hyapp.achat.view.ChatActivity;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.Holder> {

    private final Context context;
    private SortedList<User> users;

    public UsersAdapter(Context context) {
        this.context = context;
        this.users = new SortedList<>(User::compare);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPeopleBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_people, parent, false);
        binding.setLifecycleOwner((LifecycleOwner) context);
        return new Holder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull UsersAdapter.Holder holder, int position) {
        holder.bind(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void resetList(SortedList<User> people) {
        this.users = people;
        notifyDataSetChanged();
    }

    public void addAt(SortedList<User> people, int index) {
        this.users = people;
        notifyItemInserted(index);
    }

    public void removeAt(SortedList<User> people, int index) {
        this.users = people;
        notifyItemRemoved(index);
    }

    public void updateAt(SortedList<User> people, int index) {
        this.users = people;
        notifyItemChanged(index);
    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ItemPeopleBinding binding;

        public Holder(ItemPeopleBinding itemPeopleBinding) {
            super(itemPeopleBinding.getRoot());
            binding = itemPeopleBinding;
            itemView.setOnClickListener(this);
        }

        public void bind(User user) {
            binding.setPeople(user);
            binding.executePendingBindings();

            List<String> avatars = user.getAvatars();
            binding.avatar.setImageURI(avatars.size() > 0 ? avatars.get(0) : null);

            switch (user.getGender()) {
                case UserConsts.GENDER_MALE:
                    binding.genderCircle.setBackgroundResource(R.drawable.gender_circle_people_male_bg);
                    break;
                case UserConsts.GENDER_FEMALE:
                    binding.genderCircle.setBackgroundResource(R.drawable.gender_circle_people_female_bg);
                    break;
            }

            Pair<Integer, Integer> pair = PersonUtils.rankInt2rankStrResAndColor(user.getRank());
            binding.rank.setText(pair.first);
            binding.rank.setTextColor(pair.second);
        }

        @Override
        public void onClick(View v) {
            User user = users.get(getAdapterPosition());
            ChatActivity.start(context, new Contact(user));
        }
    }
}
