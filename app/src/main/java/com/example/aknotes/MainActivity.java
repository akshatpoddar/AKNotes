package com.example.aknotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText emailText, passwordText, passwordText2, nameText;
    private Button registerBtn;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();
        nameText = findViewById(R.id.editName);
        emailText = findViewById(R.id.editEmail);
        passwordText = findViewById(R.id.editPass1);
        passwordText2 = findViewById(R.id.editPass2);
        registerBtn = findViewById(R.id.registerBtn);


        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String name = nameText.getText().toString().trim();
                final String email = emailText.getText().toString().trim();
                final String password = passwordText.getText().toString().trim();
                final String password2 = passwordText2.getText().toString().trim();

                if(password.equals(password2)){

                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "createUserWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();

                                        //add data of new user to firestore
                                        Map<String, Object> data = new HashMap<>();
                                        data.put("name", name);
                                        data.put("email", email);
                                        data.put("last_login", FieldValue.serverTimestamp());
                                        data.put("last_logout", FieldValue.serverTimestamp());
                                        db.collection("users").document(user.getUid()).set(data)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Log.d(TAG, "User added"  );
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error adding user", e);
                                                    }
                                                });


                                        //moving to next activity
                                        Intent intent = new Intent(MainActivity.this, NoteListActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(MainActivity.this, "Authentication failed.",
                                                Toast.LENGTH_SHORT).show();
                                    }

                                    // ...
                                }
                            });

                }else
                {
                    Toast.makeText(MainActivity.this, "Passwords do not match",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    protected void clearFields(){
        nameText.setText("");
        emailText.setText("");
        passwordText.setText("");
        passwordText2.setText("");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        clearFields();
    }

    public void startLoginActivity(View view){
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }


}
