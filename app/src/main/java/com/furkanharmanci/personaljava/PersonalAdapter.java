package com.furkanharmanci.personaljava;

import android.app.Person;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.furkanharmanci.personaljava.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class PersonalAdapter extends RecyclerView.Adapter<PersonalAdapter.PersonalViewHolder> {

    ArrayList<Personal> personalArrayList;

    public PersonalAdapter(ArrayList<Personal> personalArrayList) {
        this.personalArrayList = personalArrayList;
    }

    @NonNull
    @Override
    public PersonalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Layout'u activite'ye bağlama(binding)
        RecyclerRowBinding binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent,false);

        return new PersonalViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonalViewHolder holder, int position) {
        holder.binding.recyclerViewTextView.setText(personalArrayList.get(position).name);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(holder.itemView.getContext(), PersonalRegister.class);
                // Buradaki 'old' bilgi gönderimi için bir test
                intent.putExtra("info","old");
                intent.putExtra("personId", personalArrayList.get(position).id);
                holder.itemView.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return personalArrayList.size();
    }

    public static class PersonalViewHolder extends RecyclerView.ViewHolder {
        private RecyclerRowBinding binding;
        public PersonalViewHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
