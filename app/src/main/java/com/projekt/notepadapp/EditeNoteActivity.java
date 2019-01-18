package com.projekt.notepadapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.projekt.notepadapp.db.NotesDB;
import com.projekt.notepadapp.db.NotesDao;
import com.projekt.notepadapp.model.Note;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EditeNoteActivity extends AppCompatActivity {

    private EditText inputNote;
    private NotesDao dao;
    private Note temp;
    public static final String NOTE_EXTRA_KEY="note_id";
    private FloatingActionButton fab2;
    ImageView imageView;
    static final int CAPTURE_IMAGE_REQUEST = 1;
    File photoFile = null;
    private String mCurrentPhotoPath="";
    Uri photoURI=null;
    private FloatingActionButton play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edite_note);
        inputNote = findViewById(R.id.input_note);
        imageView = (ImageView) findViewById(R.id.imageView);
        dao = NotesDB.getInstance(this).notesDao();

        play = (FloatingActionButton) findViewById(R.id.play);

        play.setVisibility(View.GONE);

        if(getIntent().getExtras()!=null){

            int id = getIntent().getExtras().getInt(NOTE_EXTRA_KEY,0);

            temp = dao.getNoteById(id);

            inputNote.setText(temp.getNoteText());

            Uri imgUri=Uri.parse(temp.getNotePhoto());
            imageView.setImageURI(null);
            imageView.setImageURI(imgUri);


            if(!temp.getNoteAudio().equals("0")){
            play.setVisibility(View.VISIBLE);

            play.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    try {
                        mediaPlayer.setDataSource(temp.getNoteAudio());
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        Toast.makeText(getApplicationContext(), "Playing Audio", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        // make something
                    }
                }
            });

            }

        }


        fab2 = (FloatingActionButton) findViewById(R.id.fab2);


        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                captureImage();
            }
        });



    }

    private void captureImage() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 0);
        }else{

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

                try {

                    photoFile = createImageFile();

                    if (photoFile != null) {
                        photoURI = FileProvider.getUriForFile(this,
                                "com.projekt.notepadapp.fileprovider",
                                photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                        startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST);
                    }
                } catch (Exception ex) {


                }


            }else
            {
                displayMessage(getBaseContext(),"Nullll");
            }


        }

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Bitmap myBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
            imageView.setImageBitmap(myBitmap);
        }
        else
        {
            displayMessage(getBaseContext(),"Request cancelled or something went wrong.");
        }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                captureImage();
            }
        }else{
            displayMessage(getBaseContext(), "This app don't going to work without camera permission");
        }

    }


    private void displayMessage(Context context, String message)
    {
        Toast.makeText(context,message,Toast.LENGTH_LONG).show();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.edite_note_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();
        if(id==R.id.save_note)
            onSaveNote();
        return super.onOptionsItemSelected(item);
    }

    private void onSaveNote(){

        String text = inputNote.getText().toString();
        if(!text.isEmpty()) {
            long date = new Date().getTime();

            if(temp == null){
                temp = new Note(text, date, mCurrentPhotoPath, "0");
                dao.insertNote(temp);
            }else{
                temp.setNoteText(text);
                temp.setNoteDate(date);
                temp.setNotePhoto(mCurrentPhotoPath);
                dao.updateNote(temp);
            }

            finish();
        }

    }

}
