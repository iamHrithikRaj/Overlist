package com.example.overlist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.text.DateFormat;
import java.util.Date;

public class AskQuestionActivity extends AppCompatActivity {
    private Toolbar toolbar;
    private Spinner spinner;
    private EditText questionBox;
    private ImageView imageView;
    private Button cancelBtn, postQuestionBtn;

    private String askedByName = "";
    private DatabaseReference askedByRef;
    private ProgressDialog loader;
    private String myUrl = "";
    StorageTask uploadTask;
    StorageReference storageReference;
    private Uri imageUri;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String onlineUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_ask_question);

        toolbar = findViewById(R.id.question_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Ask a Question");

        spinner = findViewById(R.id.spinner);
        questionBox = findViewById(R.id.question_text);
        imageView = findViewById(R.id.questionImage);
        cancelBtn = findViewById(R.id.cancel);
        postQuestionBtn = findViewById(R.id.postQuestion);

        loader = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        onlineUserId = mUser.getUid();

        askedByRef = FirebaseDatabase.getInstance().getReference("user").child(onlineUserId);
        askedByRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                askedByName = snapshot.child("fullname").getValue(String.class);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        storageReference = FirebaseStorage.getInstance().getReference("questions");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,getResources().getStringArray(R.array.topics));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                 if(spinner.getSelectedItem().equals("select topic")){
                     Toast.makeText(AskQuestionActivity.this, "Please select a valid topic", Toast.LENGTH_SHORT).show();
                 }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        imageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/");
            startActivityForResult(intent, 1);

        });

        cancelBtn.setOnClickListener(v -> {
            finish();
        });

        postQuestionBtn.setOnClickListener(v -> {
            performValidation();
        });
    }

    private String getQuestionText(){
        return questionBox.getText().toString().trim();
    }

    private String getTopic(){
        return spinner.getSelectedItem().toString();
    }

    private String mDate = DateFormat.getDateInstance().format(new Date());
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("questions posts");

    private void performValidation() {
        if(getQuestionText().isEmpty()){
            questionBox.setError("Question Required");
        }
        if(getTopic().equals("select topic")){
            Toast.makeText(this,"Select a valid topic",Toast.LENGTH_SHORT).show();
        }else if(!getQuestionText().isEmpty() && !getTopic().equals("") && imageUri == null){
            uploadQuestionWithNoImage();
        }else if(!getQuestionText().isEmpty() && !getTopic().equals("") && imageUri != null){
            uploadQuestionWithImage();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == RESULT_OK && data != null){
            imageUri = data.getData();
            imageView.setImageURI(imageUri);


        }
    }
}