package com.example.meetup.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.meetup.R;
import com.example.meetup.utilities.Constants;
import com.example.meetup.utilities.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.makeramen.roundedimageview.RoundedImageView;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;


public class profile extends AppCompatActivity {

    private String encodedImg;
    private PreferenceManager preferenceManager;

    EditText mName, mInterest, mBio;
    TextView mAddImgText;
    android.widget.Button mSubmitBtn;
    ProgressBar mProgressBar;
    RoundedImageView mProfileImg;
    FrameLayout mImageLayout;


    AutoCompleteTextView autoCompleteTextView;
    String[] Interest = {"Chess","Coding","Cricket","Football","Movie","Painting"};
    ArrayAdapter<String> adapterItem;
    private String interestSelected;
    private boolean isInterestSelected;


    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mName = findViewById(R.id.Name);
        mBio = findViewById(R.id.Bio);
        mSubmitBtn = findViewById(R.id.submitbtn);
        mProgressBar = findViewById(R.id.progressBar);
        mProfileImg = findViewById(R.id.profileImage);
        mAddImgText = findViewById(R.id.addImgText);
        mImageLayout = findViewById(R.id.imageLayout);

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();

        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        setListeners();
        selectInterest();
    }

    private void setListeners(){
        mSubmitBtn.setOnClickListener(v -> {
            if (isValidDetails()){
                setProfile();
            }
        });

        mImageLayout.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImg.launch(intent);
        });

    }

    private void selectInterest(){
        autoCompleteTextView = findViewById(R.id.autoCompleteTextView);
        adapterItem = new ArrayAdapter<String>(profile.this, R.layout.dropdown_items,Interest);

        autoCompleteTextView.setAdapter(adapterItem);

        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
                interestSelected = item;
                isInterestSelected = true;
            }
        });
    }


    private void setProfile(){
        loading(true);

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
                                        preferenceManager.getString(Constants.KEY_USER_ID)
                                );
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        documentReference.update(Constants.KEY_NAME,mName.getText().toString().trim());
                        documentReference.update(Constants.KEY_INTEREST,interestSelected);
                        documentReference.update(Constants.KEY_BIO,mBio.getText().toString().trim());
                        documentReference.update(Constants.KEY_IMG,encodedImg);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_NAME, mName.getText().toString().trim());
                        preferenceManager.putString(Constants.KEY_INTEREST,interestSelected);
                        preferenceManager.putString(Constants.KEY_BIO,mBio.getText().toString().trim());
                        preferenceManager.putString(Constants.KEY_IMG, encodedImg);
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });

    }

    private String encodedImg(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap =Bitmap.createScaledBitmap(bitmap,previewWidth,previewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }



    private final ActivityResultLauncher<Intent> pickImg = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null){
                    Uri imageUri = result.getData().getData();
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        mProfileImg.setImageBitmap(bitmap);
                        mAddImgText.setVisibility(View.GONE);
                        encodedImg = encodedImg(bitmap);
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
            }
    );

    private Boolean isValidDetails(){
        if (encodedImg == null){
            Toast.makeText(getApplicationContext(), "Select Profile Image", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if (mName.getText().toString().isEmpty() || mBio.getText().toString().isEmpty()){
            Toast.makeText(getApplicationContext(), "Enter Something...", Toast.LENGTH_SHORT).show();
            return false;
        }else if (isInterestSelected == false){
            Toast.makeText(getApplicationContext(),"Select Interest", Toast.LENGTH_SHORT).show();
            return false;
        }
        else{
            return true;
        }
    }

    private void loading(Boolean isLoading){
        if (isLoading){
            mSubmitBtn.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
        } else{
            mProgressBar.setVisibility(View.INVISIBLE);
            mSubmitBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        //disable back button
    }
}

/*      To add A value in database....
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, mName.getText().toString().trim());
        user.put(Constants.KEY_AGE, mAge.getText().toString().trim());
        user.put(Constants.KEY_CITY, mCity.getText().toString().trim());
        user.put(Constants.KEY_COUNTRY, mCountry.getText().toString().trim());
        user.put(Constants.KEY_IMG, encodedImg);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                    preferenceManager.putString(Constants.KEY_USER_ID, firebaseAuth.getCurrentUser().getUid());
                    preferenceManager.putString(Constants.KEY_NAME,mName.getText().toString().trim());
                    preferenceManager.putString(Constants.KEY_IMG, encodedImg);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    Toast.makeText(getApplicationContext(), "Error in database", Toast.LENGTH_SHORT).show();
                });*/
