package com.example.overlist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
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
import java.util.HashMap;

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

    private void uploadQuestionWithNoImage() {
        startLoader();

        String postId = ref.push().getKey();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("postid", postId);
        hashMap.put("question", getQuestionText());
        hashMap.put("publisher", onlineUserId);
        hashMap.put("topic", getTopic());
        hashMap.put("askedby", askedByName);
        hashMap.put("date", mDate);

        ref.child(postId).setValue(hashMap).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(AskQuestionActivity.this, "Question posted successfully", Toast.LENGTH_SHORT).show();
                loader.dismiss();
                startActivity(new Intent(AskQuestionActivity.this,HomeActivity.class));
                finish();
            }else{
                Toast.makeText(AskQuestionActivity.this,"Couldn't upload question" + task.getException().toString(),Toast.LENGTH_SHORT).show();
                loader.dismiss();
            }
        });
    }

    private void uploadQuestionWithImage() {
        startLoader();
        final StorageReference fileReference;
        fileReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtention(imageUri));
        uploadTask = fileReference.putFile(imageUri);
        uploadTask.continueWithTask(new Continuation() {
            @Override
            public Object then(@NonNull Task task) throws Exception {
                if (!task.isComplete()){
                    throw task.getException();
                }
                return fileReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    Uri downloadUri = (Uri) task.getResult();
                    myUrl = downloadUri.toString();

                    String postId = ref.push().getKey();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("postid", postId);
                    hashMap.put("question", getQuestionText());
                    hashMap.put("publisher", onlineUserId);
                    hashMap.put("topic", getTopic());
                    hashMap.put("askedby", askedByName);
                    hashMap.put("questionImage",myUrl);
                    hashMap.put("date", mDate);

                    ref.child(postId).setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(AskQuestionActivity.this, "Question posted successfully", Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                                startActivity(new Intent(AskQuestionActivity.this,HomeActivity.class));
                                finish();
                            }else{
                                Toast.makeText(AskQuestionActivity.this,"Couldn't upload question" + task.getException().toString(),Toast.LENGTH_SHORT).show();
                                loader.dismiss();
                            }
                        }
                    });
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AskQuestionActivity.this, "Failed to upload the question", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startLoader(){
        loader.setMessage("Posting your question");
        loader.setCanceledOnTouchOutside(false);
        loader.show();
    }

    private String getFileExtention(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
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