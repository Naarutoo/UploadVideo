package com.example.uploadvideo;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
private VideoView mTvVideoView;
private Button mBtnOpenGallery;
private Button mBtnUploadVideo;
private String videoPath;
private ActivityResultLauncher<Intent>resultFromGalleryActivity = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
    @Override
    public void onActivityResult(ActivityResult result) {
        Uri selectedVideoUri = result.getData().getData();

        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedVideoUri);
            mTvVideoView.setVideoURI(selectedVideoUri);
            getVideoPathFromUri(selectedVideoUri);
        }
        catch (FileNotFoundException e){
            e.printStackTrace();
        }
    }
});

    private Cursor getVideoPathFromUri(Uri selectedUri) {
        String[] filePath = {MediaStore.Video.Media.DATA};
        Cursor c = getContentResolver().query(selectedUri, filePath,
                null, null, null);
        c.moveToFirst();
        int columnIndex = c.getColumnIndex(filePath[0]);
         videoPath = c.getString(columnIndex);
        return c;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initviews();
    }

    private void initviews() {
    mTvVideoView = findViewById(R.id.videoView);
    mBtnOpenGallery = findViewById(R.id.btnGallery);
    mBtnUploadVideo = findViewById(R.id.btnUpload);
    mBtnOpenGallery.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
if (isPermissionGranted()){
    openGallery();
}
else
{
    requestPermission();
}
        }
    });
    mBtnUploadVideo.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ApiService apiService = Network.getInstance().create(ApiService.class);
            File file = new File(videoPath);
            RequestBody requestBody = RequestBody.create(MediaType.parse("video/*"), file);
            MultipartBody.Part multipart = MultipartBody.Part.createFormData("video", file.getName(), requestBody);
            apiService.uploadVideo(multipart,"Emulator Video").enqueue(new Callback<ResponseModel>() {
                @Override
                public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {

                }

                @Override
                public void onFailure(Call<ResponseModel> call, Throwable t) {

                }
            });

        }
    });
    }


    private void requestPermission() {
        String []permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        ActivityCompat.requestPermissions(MainActivity.this,permissions,101);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        resultFromGalleryActivity.launch(intent);

    }

    private boolean isPermissionGranted(){
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (grantResults[0]==PackageManager.PERMISSION_GRANTED){
        openGallery();
    }
    else
    {
        Toast.makeText(MainActivity.this,"Permission denied",Toast.LENGTH_LONG).show();
    }
    }
}