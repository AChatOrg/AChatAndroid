package com.hyapp.achat.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.hyapp.achat.R;
import com.hyapp.achat.databinding.ItemPeopleBinding;
import com.hyapp.achat.model.Contact;
import com.hyapp.achat.model.People;
import com.hyapp.achat.model.SortedList;
import com.hyapp.achat.ui.ChatActivity;

public class PeopleAdapter extends RecyclerView.Adapter<PeopleAdapter.Holder> {

    private final Context context;
    private SortedList<People> people;

    public PeopleAdapter(Context context) {
        this.context = context;
        this.people = new SortedList<>(People::compare);
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPeopleBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.item_people, parent, false);
        binding.setLifecycleOwner((LifecycleOwner) context);
        return new Holder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PeopleAdapter.Holder holder, int position) {
        holder.bind(people.get(position));
    }

    @Override
    public int getItemCount() {
        return people.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void resetList(SortedList<People> people) {
        this.people = people;
        notifyDataSetChanged();
    }

    public void addAt(SortedList<People> people, int index) {
        this.people = people;
        notifyItemInserted(index);
    }

    public void removeAt(SortedList<People> people, int index) {
        this.people = people;
        notifyItemRemoved(index);
    }

    public void updateAt(SortedList<People> people, int index) {
        this.people = people;
        notifyItemChanged(index);
    }

    public class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final ItemPeopleBinding binding;

        public Holder(ItemPeopleBinding itemPeopleBinding) {
            super(itemPeopleBinding.getRoot());
            binding = itemPeopleBinding;
            itemView.setOnClickListener(this);
        }

        public void bind(People people) {
            binding.setPeople(people);
            binding.executePendingBindings();

            String[] avatars = people.getAvatars();
            binding.avatar.setImageURI(avatars.length > 0 ? avatars[0] : null);

            binding.genderCircle.setBackgroundResource(people.getGenderCircleRes());
            binding.rank.setText(people.getRankStrRes());
            binding.rank.setTextColor(people.getRankColor());
        }

        @Override
        public void onClick(View v) {
            People p = people.get(getAdapterPosition());
            Contact contact = new Contact(p, Contact.TIME_ONLINE);
            ChatActivity.start(context, contact);
        }
    }
}
