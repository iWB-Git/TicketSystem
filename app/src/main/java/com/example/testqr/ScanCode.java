package com.example.testqr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;

public class ScanCode extends AppCompatActivity {

    private CodeScanner mCodeScanner;
    Dialog dialog;
    int huse=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_code);
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
        mCodeScanner = new CodeScanner(this, scannerView);
        dialog=new Dialog(this);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 100);



        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try{
                            DatabaseReference jLoginDatabase = FirebaseDatabase.getInstance().getReference().child("TicketSales").child(result.getText());

                            jLoginDatabase.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    String hasUsed = snapshot.child("hasused").getValue().toString();
                                    int hused=Integer.parseInt(hasUsed);
                                    huse=hused;
                                    Toast.makeText(ScanCode.this, hasUsed, Toast.LENGTH_SHORT).show();
                                    String id = snapshot.child("id").getValue().toString();
                                    if(hused>0){

                                        openVerifiedDialog(result,id);

                                    }else{
                                        openUsedDialog(result,id);
                                    }


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }catch(Exception e){
                            Toast.makeText(ScanCode.this, "INVALID QR CODE!!!!\n"+e, Toast.LENGTH_SHORT).show();
                        }


                        Toast.makeText(ScanCode.this, huse, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }


    public void openVerifiedDialog(Result result, String id){
            dialog.setContentView(R.layout.verified_dialog);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView imageClose=dialog.findViewById(R.id.imageClose);
        TextView ids=dialog.findViewById(R.id.textView3);
        ids.setText(id);

        imageClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        Button btnVerify=dialog.findViewById(R.id.button);
        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase.getInstance().getReference("TicketSales")
                        .child(result.getText()).child("hasused")
                        .setValue((huse-1));

            }
        });
        dialog.show();
    }
    public void openUsedDialog(Result result, String id){
        dialog.setContentView(R.layout.used_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView imageClose=dialog.findViewById(R.id.imageClose);
        TextView ids=dialog.findViewById(R.id.textView3);
        ids.setText(id);
        imageClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        Button btnClose=dialog.findViewById(R.id.button);
        dialog.show();
    }
}