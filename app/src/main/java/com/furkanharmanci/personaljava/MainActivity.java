package com.furkanharmanci.personaljava;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.Person;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.furkanharmanci.personaljava.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // Propertys
    private ActivityMainBinding binding;
    ArrayList<Personal> personalArrayList;
    //Adapter init
    PersonalAdapter personalAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Personal arraylist init
        personalArrayList = new ArrayList<>();

        //Layout seçimi
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // Adapter kullanma

        personalAdapter = new PersonalAdapter(personalArrayList);
        // recyclerView adapter setting
        binding.recyclerView.setAdapter(personalAdapter);

        getData();
    }


    //Data çağırma
    public void getData() {
        SQLiteDatabase database = this.openOrCreateDatabase("personal", MODE_PRIVATE, null);

        Cursor cursor = database.rawQuery("SELECT * FROM personal", null);

        int personIx = cursor.getColumnIndex("personName");
        int idIx = cursor.getColumnIndex("id");

        while(cursor.moveToNext()) {
            String name = cursor.getString(personIx);
            int id = cursor.getInt(idIx);

            Personal personal = new Personal(name, id);
            personalArrayList.add(personal);
        }

        // Veri setinin değiştiğini adapter'e bildirme
        personalAdapter.notifyDataSetChanged();

        cursor.close();
    }

    // Menu oluşturma
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.person_item, menu);

        return super.onCreateOptionsMenu(menu);
    }

    // Menü itemine tıklama
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.add_item) {
            Intent intent = new Intent(this, PersonalRegister.class);
            // Buradaki 'new' bilgi gönderimi bir durumu test etmek için.
            intent.putExtra("info", "new");

            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}