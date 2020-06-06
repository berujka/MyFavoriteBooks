package com.beraa.myfavoritebooks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Main2Activity extends AppCompatActivity {

    Bitmap selectedImage;
    ImageView imageView;
    EditText booksNameText, authorNameText;
    Button button;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        imageView = findViewById(R.id.imageView);
        booksNameText = findViewById(R.id.booksName);
        authorNameText = findViewById(R.id.authorNameText);
        button = findViewById(R.id.button);

        SQLiteDatabase database = this.openOrCreateDatabase("Books", MODE_PRIVATE, null);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if(info.matches("new")){
            booksNameText.setText("");
            authorNameText.setText("");
            button.setVisibility(View.VISIBLE);
        }else{
            int bookId = intent.getIntExtra("bookId", 1);
            button.setVisibility(View.INVISIBLE);

            try {
                Cursor cursor = database.rawQuery("SELECT * FROM books WHERE id = ?", new String[] {String.valueOf(bookId)});

                int bookNameIx = cursor.getColumnIndex("bookname");
                int authorNameIx = cursor.getColumnIndex("authorname");
                int imageIx = cursor.getColumnIndex("image");

                while(cursor.moveToNext()){
                    booksNameText.setText(cursor.getString(bookNameIx));
                    authorNameText.setText(cursor.getString(authorNameIx));

                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,bytes.length);
                    imageView.setImageBitmap(bitmap);

                }

                cursor.close();

            }catch (Exception e){

            }
        }

    }

    public void selectImage(View view){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},1);
        }else{
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intentToGallery,2);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == 1){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intentToGallery,2);
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == 2 && resultCode == RESULT_OK && data != null){
            Uri imageData = data.getData();

            try {
                if(Build.VERSION.SDK_INT >=28){
                    ImageDecoder.Source source = ImageDecoder.createSource(this.getContentResolver(), imageData);
                    selectedImage = ImageDecoder.decodeBitmap(source);
                    imageView.setImageBitmap(selectedImage);
                }else{
                    selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageData);
                    imageView.setImageBitmap(selectedImage);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void save(View view){
        String bookName = booksNameText.getText().toString();
        String authorName = authorNameText.getText().toString();

        Bitmap smallImage = makeSmallerImage(selectedImage, 300);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
        byte[] byteArray = outputStream.toByteArray();

        try{
            database = this.openOrCreateDatabase("Books", MODE_PRIVATE, null);
            database.execSQL("CREATE TABLE IF NOT EXISTS books (id INTEGER PRIMARY KEY, bookname VARCHAR, authorname VARCHAR, image BLOB)");

            String sqlString = "INSERT INTO books (bookname, authorname, image) VALUES (?, ?, ?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1, bookName);
            sqLiteStatement.bindString(2,authorName);
            sqLiteStatement.bindBlob(3, byteArray);
            sqLiteStatement.execute();
        }catch (Exception e){

        }
        finish();

    }

    public Bitmap makeSmallerImage(Bitmap image, int maxSize) {

        int width = image.getWidth();
        int height = image.getHeight();

        float ratio = (float) width / (float) height;

        if(ratio >1){
            width = maxSize;
            height = (int) (width / ratio);
        } else {
            height = maxSize;
            width = (int) (height * ratio);
        }
        return Bitmap.createScaledBitmap(image,width,height,true);
    }

}
