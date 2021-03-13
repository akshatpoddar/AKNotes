package com.example.aknotes;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateNoteActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private CollectionReference ref;
    private static final String TAG = "CreateNoteActivity";
    private EditText nameText, contentText;
    private boolean createNew;
    private String noteId;
    private FloatingActionButton fabSave;
    private Intent listIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        init();
        listIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(createNew) createNote();
                else editNote();
                startActivity(listIntent);
            }
        });

        //case when new note
        if(!createNew){
            //fetching item data to display
            noteId = getIntent().getStringExtra("noteId");
            ref.document(noteId).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                        DocumentSnapshot document = task.getResult();
                                        nameText.setText(document.getString("name"));
                                        contentText.setText(document.getString("content"));
                                        disableInput(nameText);
                                        disableInput(contentText);

                                }else{
                                    Log.w(TAG, "Error displaying note ", task.getException());
                                    Toast.makeText(CreateNoteActivity.this, "Error displaying note details",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.edit_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.delete_note)
        {
            deleteNote();
            startActivity(listIntent);
            return true;
        }else if(item.getItemId() == R.id.edit_note)
        {
            enableInput(nameText);
            enableInput(contentText);
            return true;
        }

        return false;
    }

    private void createNote(){
        Map<String,Object> data = new HashMap<>();
        data.put("name", nameText.getText().toString().trim());
        data.put("content", contentText.getText().toString().trim());
        data.put("last_edited", FieldValue.serverTimestamp());
        data.put("date_created", FieldValue.serverTimestamp());
        ref.add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "New note document created with id " + documentReference.getId());
                        Toast.makeText(CreateNoteActivity.this, "Note added successfully",
                                Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Error creating new note: ", e  );
                Toast.makeText(CreateNoteActivity.this, "Not could not be added",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void editNote(){
        Map<String,Object> data = new HashMap<>();
        data.put("name", nameText.getText().toString().trim());
        data.put("content", contentText.getText().toString().trim());
        data.put("last_edited", FieldValue.serverTimestamp());
        ref.document(noteId).update(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.w(TAG, "Document updated successfully ");
                        Toast.makeText(CreateNoteActivity.this, "Note edited successfully",
                                Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.w(TAG, "Document not updating ERROR: ", e);
                Toast.makeText(CreateNoteActivity.this, "Could not edit due to error "+ e,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void deleteNote(){
        ref.document(noteId).delete();
        Toast.makeText(CreateNoteActivity.this, "Note deleted successfully",
                Toast.LENGTH_SHORT).show();
    }

    private void disableInput(EditText editText){
        editText.setInputType(InputType.TYPE_NULL);
        editText.setClickable(false);
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return true;  // Blocks input from hardware keyboards.
            }
        });
    }

    private void enableInput(EditText editText){
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME|InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        editText.setClickable(true);
    }

    private void init(){
        listIntent = new Intent(getApplicationContext(), NoteListActivity.class);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        String userId = mAuth.getCurrentUser().getUid();
        ref = db.collection("users").document(userId).collection("notes");
        createNew = (getIntent().getExtras() == null);
        nameText = findViewById(R.id.editTitle);
        contentText = findViewById(R.id.editContent);
        fabSave = findViewById(R.id.fab_save);
    }

}
