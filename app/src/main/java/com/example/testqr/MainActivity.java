package com.example.testqr;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    //vars
    public static final int CAMERA_PERMISSION_CODE = 100;

    //widgets
    private Button camera;



    Dialog dialog;
    boolean vcount=false;
    private ImageView send,scan;
    private TextView Name,id,Confirm,Email;
    AutoCompleteTextView autoCompleteTxt;
    ArrayAdapter<String> adapterItems;
    String item;
    int huse=0;
    ArrayList<String> array = new ArrayList<String>(); //creating array 1 for storing student names
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Name=findViewById(R.id.editTextName);
        Email=findViewById(R.id.editTextEmail);
        id=findViewById(R.id.editTextMobile);
        Confirm=findViewById(R.id.editTextPassword);
        send=findViewById(R.id.sendbtn);
        scan=findViewById(R.id.scanbtn);
        send.setOnClickListener(this);
        scan.setOnClickListener(this);


        array.add("1");array.add("2");array.add("3");array.add("4");array.add("5");
        autoCompleteTxt = findViewById(R.id.autoCompleteTextView);
        autoCompleteTxt.setInputType(InputType.TYPE_NULL | InputType.TYPE_NULL);

        dialog=new Dialog(this);

        adapterItems = new ArrayAdapter<String>(this,R.layout.items,array);
        autoCompleteTxt.setAdapter(adapterItems);

        autoCompleteTxt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                item = array.get(position).toString();


            }
        });

    }




    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.sendbtn:
                String name = Name.getText().toString().trim();
                String ID = id.getText().toString().trim();
                String email = Email.getText().toString().trim();
                String confirm = Confirm.getText().toString().trim();

                if (name.isEmpty()) {
                    Name.setError("Name Required");
                    return;
                }
                if (ID.isEmpty()) {
                    id.setError("ID Required");
                    return;
                }
                if (email.isEmpty()) {
                    Email.setError("Email Required");
                    return;
                }
                if (confirm.isEmpty()) {
                    Confirm.setError("M-PESA Confirmation Required");
                    return;
                }
                Intent intent = new Intent(this, GenerateCode.class);
                intent.putExtra("Name", name);
                intent.putExtra("ID", ID);
                intent.putExtra("Email", email);
                intent.putExtra("Confirm", confirm);
                intent.putExtra("Tickets",item);
                Log.d("hmm", ID);
                startActivity(intent);
                break;
            case R.id.scanbtn:
                vcount=false;
                scanCode();




        }
    }public void scanCode(){
        ScanOptions options=new ScanOptions();
        options.setPrompt("Volume up for flash");
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLauncher= registerForActivityResult(new ScanContract(), result -> {
        if(result.getContents()!=null){
            try{
                DatabaseReference jLoginDatabase = FirebaseDatabase.getInstance().getReference().child("TicketSales").child(result.getContents());

                jLoginDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try{
                            String hasUsed = snapshot.child("hasused").getValue().toString();
                            Toast.makeText(MainActivity.this, hasUsed, Toast.LENGTH_SHORT).show();
                            huse=Integer.parseInt(hasUsed);
                            String id = snapshot.child("id").getValue().toString();
                            if(huse>0){
                                openVerifiedDialog(result.getContents());

                            }else{
                                openUsedDialog(result.getContents());
                            }
                        }catch(Exception e){
                            openUsedDialog(result.getContents());
                        }



                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }catch(Exception e){
                Toast.makeText(MainActivity.this, "INVALID QR CODE!!!!\n"+e, Toast.LENGTH_SHORT).show();
            }



        }
    });



    public void openVerifiedDialog(String result){
        dialog.setContentView(R.layout.verified_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView imageClose=dialog.findViewById(R.id.imageClose);
        TextView ids=dialog.findViewById(R.id.textView3);

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
                        .child(result).child("hasused")
                        .setValue(huse-1);

            }
        });
        dialog.show();
    }
    public void openUsedDialog(String result){
        dialog.setContentView(R.layout.used_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView imageClose=dialog.findViewById(R.id.imageClose);
        TextView ids=dialog.findViewById(R.id.textView3);
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










