package com.beraa.myfavoritebooks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> nameArray;
    ArrayList<Integer> idArray;
    ArrayAdapter arrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        nameArray = new ArrayList<>();
        idArray = new ArrayList<>();

        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,nameArray);
        listView.setAdapter(arrayAdapter);
        

        getData();
    }

    public void getData(){
        try{
            SQLiteDatabase database = this.openOrCreateDatabase("Books", MODE_PRIVATE, null);

            Cursor cursor = database.rawQuery("SELECT * FROM books", null);
            int nameIX = cursor.getColumnIndex("bookname");
            int idIx = cursor.getColumnIndex("id");

            while(cursor.moveToNext()){
                nameArray.add(cursor.getString(nameIX));
                idArray.add(cursor.getInt(idIx));
            }
            arrayAdapter.notifyDataSetChanged();
            cursor.close();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.add_book,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if(item.getItemId() == R.id.add_book_item){
            Intent intent = new Intent(MainActivity.this, Main2Activity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}
