package com.ds.zibzy;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.security.Key;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    StorageReference storageReference;
    String storagePath = "Users_cover_photos/";

    TextView nameTv,emailTv,phoneTv;
    ImageView avatar,coverIv;
    FloatingActionButton fab;
    ProgressDialog pd;

    private static final int CAMERA_REQUEST_CODE =100;
    private static final int STORAGE_REQUEST_CODE =200;
    private static final int IMAGE_PICK_GALLERY_CODE =300;
    private static final int IMAGE_PICK_CAMERA_CODE =400;

    String cameraPermissions[];
    String storagePermissions[];

    String ProfileOrCoverPhoto;
    Uri image_uri;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_profile,container,false);

        firebaseAuth=FirebaseAuth.getInstance();
        user=firebaseAuth.getCurrentUser();
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();

        cameraPermissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        avatar=view.findViewById(R.id.avatar);
        coverIv=view.findViewById(R.id.coverIv);
        nameTv=view.findViewById(R.id.nameTv);
        emailTv=view.findViewById(R.id.emailTv);
        phoneTv=view.findViewById(R.id.phoneTv);
        fab=view.findViewById(R.id.fab);

        pd= new ProgressDialog(getActivity());

        Query query=databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String name= ""+ds.child("name").getValue();
                    String email= ""+ds.child("email").getValue();
                    String phone= ""+ds.child("phone").getValue();
                    String image= ""+ds.child("image").getValue();
                    String cover= ""+ds.child("cover").getValue();

                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);

                    try{
                        Picasso.get().load(image).into(avatar);
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.avatar).into(avatar);
                    }
                    try{
                        Picasso.get().load(cover).into(coverIv);
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.backgroundtest).into(coverIv);
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

        return view;
    }

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return result;
    }
    private void requestStoragePermission(){
        requestPermissions(storagePermissions,STORAGE_REQUEST_CODE);
    }


    private boolean checkCameraPermission(){
        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return result && result1;
    }
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(getActivity(),cameraPermissions,CAMERA_REQUEST_CODE);
    }


    private void showEditProfileDialog() {
        String options[]={"Change Profile Picture","Change Cover Photo","Edit Your Name","Edit Mobile number"};
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Action");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0)
                {
                    pd.setMessage("Profile Pic updating...");
                    ProfileOrCoverPhoto = "image";
                    showImagePicDialog();
                }
                else if(which == 1)
                {
                    pd.setMessage("Cover Photo updating...");
                    ProfileOrCoverPhoto = "cover";
                    showImagePicDialog();
                }
                else if(which == 2)
                {
                    showNamePhoneUpdateDialog("name");
                    pd.setMessage("UserName updating...");
                }
                else if(which == 3)
                {
                    pd.setMessage("Mobile number updating...");
                    showNamePhoneUpdateDialog("phone");
                }
            }
        });
        builder.create().show();
    }

    private void showNamePhoneUpdateDialog(final String key) {
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        builder.setTitle("Update "+key);

        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);

        final EditText editText = new EditText(getActivity());
        editText.setHint("Enter "+key);
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String value = editText.getText().toString().trim();
                if(!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String,Object> result =new HashMap<>();
                    result.put(key,value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    pd.dismiss();
                                    Toasty.success(getActivity(),"Updated...",Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    pd.dismiss();
                                    Toast.makeText(getActivity(),""+e,Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                else {
                    Toasty.info(getActivity(),"Please Enter "+key,Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.create().show();

    }

    private void showImagePicDialog() {


        String options[]={"Camera","Gallery"};
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Image from");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0)
                {
                   //camera clicked
                    if (!checkCameraPermission()){
                        requestCameraPermission();}
                    else {pickfromCamera();}
                }
                else if(which == 1)
                {
                    //gallery
                    if (!checkStoragePermission()){
                        requestStoragePermission();}
                    else {pickfromGallery();}
                }

            }
        });
        builder.create().show();


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case CAMERA_REQUEST_CODE:
            {
                if(grantResults.length>0)
                {
                    boolean cameraAccepted = grantResults [0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults [1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writeStorageAccepted)
                    {
                        pickfromCamera();
                    }
                    else
                    {
                        Toasty.info(getActivity(),"Please enable Camera & Storage Permissions",Toasty.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:
            {
                if(grantResults.length>0)
                {
                    boolean writeStorageAccepted = grantResults [1] == PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted)
                    {
                        pickfromGallery();
                    }
                    else
                    {
                        Toasty.info(getActivity(),"Please enable Storage Permission",Toasty.LENGTH_SHORT).show();
                    }
                }
            }
            break;
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK){
            if(requestCode==IMAGE_PICK_GALLERY_CODE){
                image_uri=data.getData();
                uploadProfileCoverPhoto(image_uri);
            }
            if(requestCode==IMAGE_PICK_CAMERA_CODE){
                uploadProfileCoverPhoto(image_uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri uri) {
        pd.show();

        String filePathAndName = storagePath + ""+ ProfileOrCoverPhoto +"_" + user.getUid();

        StorageReference storageReference2= storageReference.child(filePathAndName);
        storageReference2.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();
                        if(uriTask.isSuccessful()){
                            //uploaded
                            HashMap<String , Object> results= new HashMap<>();
                            results.put(ProfileOrCoverPhoto,downloadUri.toString());

                            databaseReference.child(user.getUid()).updateChildren(results)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toasty.success(getActivity(),"Image Updated...",Toast.LENGTH_SHORT).show();
                                            pd.dismiss();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toasty.error(getActivity(),"Some Error occured....",Toast.LENGTH_SHORT).show();
                                            pd.dismiss();
                                        }
                                    });
                        }
                        else {
                            pd.dismiss();
                            Toasty.error(getActivity(),"Some Error Occured",Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void pickfromCamera() {
        //intenet of picking image from device camera
        ContentValues values=new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"Temp Description");
        //put image uri
        image_uri =getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

    // start camera intent
        Intent cameraIntent =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);
    }

    private void pickfromGallery() {
        Intent galleryIntent=new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);
    }


    private void checkUserStatus(){
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if(user!=null){
            //  mProfileTv.setText(user.getEmail());
        }
        else{
            Intent ma= new Intent(getActivity(),MainActivity.class);
            startActivity(ma);
            getActivity().finish();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        if(id==R.id.action_logout)
        {
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);

    }

}
