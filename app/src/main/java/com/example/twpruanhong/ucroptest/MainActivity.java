package com.example.twpruanhong.ucroptest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PICK_IMAGE = 1;
    private static final int REQUEST_CODE_CAMERA = 2;
    private static final int REQUEST_CODE_CROP = 3;

    private Button btn_crop, btn_crop1, btn_crop_pic;
    private Uri imgUri;
    private ImageView img_display;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_crop = (Button) findViewById(R.id.btn_crop);
        btn_crop1 = (Button) findViewById(R.id.btn_crop1);
        btn_crop_pic = (Button) findViewById(R.id.btn_crop_pic);
        img_display = (ImageView) findViewById(R.id.img_view);

        btn_crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCrop();
            }
        });
        btn_crop1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera();
            }
        });
        btn_crop_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPicFromGallery();
            }
        });
    }

    public void startCrop() {
//        Uri sourceUri = Uri.parse("http://172.16.0.56:8041/images/appImg/bodyHeadImg3.jpg");
        Uri sourceUri = Uri.parse("http://image1.nphoto.net/news/image/201005/37aac98dff811063.jpg");
        Uri destinationUri = Uri.fromFile(new File(getCacheDir(), System.currentTimeMillis() + "_testCrop.jpg"));
        UCrop.of(sourceUri, destinationUri).withAspectRatio(1, 1).withMaxResultSize(300, 300).start(this);
        // startActivityForResult(UCrop.REQUEST_CROP); // 69
    }

    public void camera() {
        File cameraOutImg = new File(Environment.getExternalStorageDirectory(), "camera_out.jpg");

        try {
            if (cameraOutImg.exists()) {
                cameraOutImg.delete();
            }
            cameraOutImg.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        imgUri = Uri.fromFile(cameraOutImg);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    public void getPicFromGallery() {
        /*
        File file = new File(Environment.getExternalStorageDirectory(),"camera_out.jpg");

        try {
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        imgUri = Uri.fromFile(file);
        */
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        // intent.putExtra(MediaStore.EXTRA_OUTPUT,imgUri);
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CAMERA) {
            // 拍照返回
            if (resultCode == RESULT_OK) {
                // 拍照成功
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(imgUri, "image/*");
                intent.putExtra("outputX", 300);
                intent.putExtra("outputY", 300);
                intent.putExtra("crop", true);
                intent.putExtra("scale", true);
                intent.putExtra("return-data", true);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
                startActivityForResult(intent, REQUEST_CODE_CROP);
            } else if (resultCode == RESULT_CANCELED) {
                // 拍照取消
            }

        } else if (requestCode == REQUEST_CODE_PICK_IMAGE) {
            // 选相册返回

            if (resultCode == RESULT_OK) {
                // 选相册成功
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(data.getData(), "image/*");
                intent.putExtra("outputX", 300);
                intent.putExtra("outputY", 300);
                intent.putExtra("crop", true);
                intent.putExtra("scale", true);
                intent.putExtra("return-data", true);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);
                startActivityForResult(intent, REQUEST_CODE_CROP);
            } else if (resultCode == RESULT_CANCELED) {
                // 选相册取消
            }
        } else if (requestCode == UCrop.REQUEST_CROP) {
            // 裁剪返回

            if (resultCode == RESULT_OK) {
                // 裁剪成功
                Uri cropedFileUri = UCrop.getOutput(data);
                String downLoadDirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                String fileName = String.format("%d_%s", Calendar.getInstance().getTimeInMillis(), cropedFileUri.getLastPathSegment());
                File save = new File(downLoadDirPath, fileName);

                FileInputStream inputStream = null;
                FileOutputStream outputStream = null;
                FileChannel inChannel = null;
                FileChannel outChannel = null;

                try {
                    inputStream = new FileInputStream(new File(cropedFileUri.getPath()));
                    outputStream = new FileOutputStream(save);
                    inChannel = inputStream.getChannel();
                    outChannel = outputStream.getChannel();
                    inChannel.transferTo(0, inChannel.size(), outChannel);
                    Toast.makeText(MainActivity.this, "裁剪后的图片保存在：" + save.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        outChannel.close();
                        outputStream.close();
                        inChannel.close();
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else if(resultCode == UCrop.RESULT_ERROR) {
                // 裁剪失败
                Toast.makeText(MainActivity.this, "裁剪图片失败", Toast.LENGTH_SHORT).show();
            } if (resultCode == RESULT_CANCELED) {
                // 裁剪取消
            }
        } else if (requestCode == REQUEST_CODE_CROP) {
            if (resultCode == RESULT_OK) {
                // 裁剪成功
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imgUri));
                    img_display.setImageBitmap(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else {
                // 裁剪取消
            }
        }
    }

}
