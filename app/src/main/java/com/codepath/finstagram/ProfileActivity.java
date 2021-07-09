package com.codepath.finstagram;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.codepath.finstagram.models.Post;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * This activity allows the user to choose a picture from their phone's photo gallery to use as their
 * profile picture. Once a new profile picture is chosen, it is automatically updated throughout the app.
 *
 * This activity is started by ProfileFragment.java if the user clicks on the edit profile picture button.
 * Once a new profile picture is chosen, or the user clicks cancel, the user is taken back to their
 * profile page with the most up to date profile picture.
 */

public class ProfileActivity extends AppCompatActivity {

    public static final int UPLOAD_IMAGE_ACTIVITY_REQUEST_CODE = 77;
    Button btnAddPic;
    ImageView ivProfilePic;
    Button btnSetPic;
    Button btnCancel;
    Bitmap bitmap;
    File f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        btnAddPic = findViewById(R.id.btnAddPic);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        btnSetPic = findViewById(R.id.btnSetPic);
        btnCancel = findViewById(R.id.btnCancel);

        btnAddPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // open photos gallery to upload profile picture
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, UPLOAD_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        });
        btnSetPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ivProfilePic.getDrawable() == null) {
                    Toast.makeText(ProfileActivity.this, "No image found", Toast.LENGTH_SHORT).show();
                } else {
                    setProfilePicture();
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPLOAD_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Uri targetUri = data.getData();
                try {
                    // view preview of chosen picture
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                    ivProfilePic.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    Toast.makeText(ProfileActivity.this, "Unable to find image", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }

    private void setProfilePicture() {
        try {
            // resize image to store in Parse, so store the smaller image in this new file
            f = new File(ProfileActivity.this.getCacheDir(), "photo.jpg");
            f.createNewFile();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bos);
            byte[] bitmapdata = bos.toByteArray();
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
            ParseUser.getCurrentUser().put(Post.KEY_PFP, new ParseFile(f));
            ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e != null) {
                        Log.e("Profile", "Error while saving pfp: ", e);
                        Toast.makeText(ProfileActivity.this, "Error while saving profile picture", Toast.LENGTH_SHORT).show();
                    } else {
                        finish();
                    }
                }
            });
        } catch (IOException e) {
            Toast.makeText(ProfileActivity.this, "Unable to update profile picture", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}