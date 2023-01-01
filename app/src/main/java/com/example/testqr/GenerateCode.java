package com.example.testqr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileOutputStream;

public class GenerateCode extends AppCompatActivity {
    public final static int QRCodeWidth = 500;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    Bitmap bitmap;
    private Button download;
    private ImageView iv;

    String id,email,name,confirm,numb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_code);
        download = findViewById(R.id.download);
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);



        iv = findViewById(R.id.image);
        Intent intent = getIntent();
         id = intent.getStringExtra("ID");
         name = intent.getStringExtra("Name");
         email = intent.getStringExtra("Email");
         confirm = intent.getStringExtra("Confirm");
        numb=intent.getStringExtra("Tickets");




        User user = new User(name,id,email,confirm,numb);

        FirebaseDatabase.getInstance().getReference("TicketSales")
                .child(confirm)
                .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(GenerateCode.this, "Database Updated", Toast.LENGTH_SHORT).show();
                        }

                    }
                });


                    try{
                        bitmap = textToImageEncode(confirm);
                        iv.setImageBitmap(bitmap);
                        download.setVisibility(View.VISIBLE);
                        download.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {





                                MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "code_scanner"
                                , null);

                                Toast.makeText(GenerateCode.this, "Saved to galary", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });

                    }catch (WriterException e){
                        e.printStackTrace();
                    }
        File file = BitmapSaver.saveImageToExternalStorage(this, bitmap);
        Uri photoUri=Uri.fromFile(file);

        if(Build.VERSION.SDK_INT >=Build.VERSION_CODES.N){
            photoUri= FileProvider.getUriForFile(this,BuildConfig.APPLICATION_ID+".provider",file);
        }
        sendEmail(photoUri);
    }

    private Bitmap textToImageEncode(String value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(value,
                    BarcodeFormat.DATA_MATRIX.QR_CODE, QRCodeWidth, QRCodeWidth, null);
        } catch (IllegalArgumentException e) {
            return null;
        }

        int bitMatrixWidth = bitMatrix.getWidth();
        int bitMatrixHeight = bitMatrix.getHeight();
        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offSet = y * bitMatrixWidth;
            for (int x = 0; x < bitMatrixWidth; x++) {
                pixels[offSet + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.black) : getResources().getColor(R.color.white);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);
        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }
    private void sendEmail(Uri file){

        Log.d("error", file.toString());

        Intent intent2=new Intent(Intent.ACTION_SEND);
        intent2.setType("image/*");
        String[] recepient=email.split(",");
        intent2.putExtra(Intent.EXTRA_EMAIL,recepient);
        intent2.putExtra(Intent.EXTRA_SUBJECT,"Mr & Mrs USIU 2022 Finals Ticket");
        intent2.putExtra(Intent.EXTRA_STREAM, file);
        intent2.setType("message/rfc822");
        Log.d("error", file.toString());
        startActivity(Intent.createChooser(intent2,"Choose an Email Client"));
    }



    private String saveImageToStorage(Bitmap bitmap) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/req_images");
        myDir.mkdirs();
        String fname = "Image-Report.jpg";
        File file = new File(myDir, fname);
        Log.i("myActivity", "" + file);
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            return root + "/req_images" + fname;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap getBitmapFromView(View view, int totalHeight, int totalWidth) {
        Bitmap returnedBitmap = Bitmap.createBitmap(totalWidth,totalHeight , Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            // add other cases for more permissions
        }
    }


}







