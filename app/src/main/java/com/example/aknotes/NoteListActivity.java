package com.example.aknotes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class NoteListActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private static final String TAG = "NoteListActivity";
    private FloatingActionButton fabAdd;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userId = mAuth.getCurrentUser().getUid();
        final ArrayList<String> titleList = new ArrayList<>();
        final ArrayList<String> noteIdList = new ArrayList<>();
        final ListView listView = findViewById(R.id.listView);
        fabAdd = findViewById(R.id.fab_add);

        listView.setEmptyView(findViewById(R.id.empty_list));

        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreateNoteActivity.class);
                startActivity(intent);
            }
        });


        db.collection("users").document(userId).collection("notes").orderBy("date_created", Query.Direction.DESCENDING).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if(task.isSuccessful()){
                                    for(DocumentSnapshot document: task.getResult() ){
                                        titleList.add(document.getString("name"));
                                        noteIdList.add(document.getId());
                                    }
                                    ArrayAdapter<String> arrayAdapter= new ArrayAdapter<>(NoteListActivity.this, android.R.layout.simple_list_item_1, titleList);
                                    listView.setAdapter(arrayAdapter);
                                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                                        {
                                            Intent intent = new Intent(NoteListActivity.this, CreateNoteActivity.class);
                                            intent.putExtra("noteId", noteIdList.get(position).toString());//to tell us which row of listView was tapped
                                            startActivity(intent);
                                        }
                                    });

                                }else{
                                    Log.w(TAG, "Error fetching notes: ", task.getException());
                                    Toast.makeText(NoteListActivity.this, "Error fetching notes",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        super.onOptionsItemSelected(item);

        if(item.getItemId() == R.id.logout_user){

            Map<String, Object> data = new HashMap<>();
            data.put("last_logout", FieldValue.serverTimestamp());
            db.collection("users").document(userId).update(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Last logout updated"  );
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error updating last logout: ", e);
                        }
                    });

            mAuth.signOut();
            Intent intent = new Intent(NoteListActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }

        return false;
    }
}
