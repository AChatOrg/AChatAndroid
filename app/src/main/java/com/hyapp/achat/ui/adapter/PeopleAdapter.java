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
import com.hyapp.achat.model.People;
import com.hyapp.achat.model.SortedList;

import java.util.ArrayList;
import java.util.List;

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
        this.people = null;
        this.people = people;
        notifyDataSetChanged();
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

            binding.avatar.setImageURI(people.getAvatar().getUrl());

            if (people.getGender() == People.MALE) {
                binding.genderCircle.setBackgroundResource(R.drawable.gender_circle_people_male_bg);
            } else {
                binding.genderCircle.setBackgroundResource(R.drawable.gender_circle_people_female_bg);
            }
            setRank(people.getKey().getRank());
        }

        private void setRank(byte rank) {
            switch (rank) {
                case People.RANK_GUEST:
                    binding.rank.setText(R.string.guest);
                    binding.rank.setTextColor(ContextCompat.getColor(context, R.color.rank_guest));
                    break;
                case People.RANK_MEMBER:
                    binding.rank.setText(R.string.member);
                    binding.rank.setTextColor(ContextCompat.getColor(context, R.color.rank_member));
                    break;
                case People.RANK_SPECIAL:
                    binding.rank.setText(R.string.special);
                    binding.rank.setTextColor(ContextCompat.getColor(context, R.color.rank_special));
                    break;
                case People.RANK_ACTIVE:
                    binding.rank.setText(R.string.active);
                    binding.rank.setTextColor(ContextCompat.getColor(context, R.color.rank_active));
                    break;
                case People.RANK_SENIOR:
                    binding.rank.setText(R.string.senior);
                    binding.rank.setTextColor(ContextCompat.getColor(context, R.color.rank_senior));
                    break;
                case People.RANK_ADMIN:
                    binding.rank.setText(R.string.admin);
                    binding.rank.setTextColor(ContextCompat.getColor(context, R.color.rank_admin));
                    break;
                case People.RANK_MANAGER:
                    binding.rank.setText(R.string.manager);
                    binding.rank.setTextColor(ContextCompat.getColor(context, R.color.rank_manager));
                    break;
            }
        }

        @Override
        public void onClick(View v) {

        }
    }
}
