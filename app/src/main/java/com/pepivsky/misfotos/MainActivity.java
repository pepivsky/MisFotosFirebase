package com.pepivsky.misfotos;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final int RC_GALLERY = 21;
    private static final int RC_CAMERA = 22;

    private static final int RP_CAMERA = 121;
    private static final int RP_STORAGE = 122;
    //RUta en donde se almcenan las fotos
    private static final String DIRECTORIO_IMAGEN = "/MyPhotoApp";
    private static final String MY_PHOTO = "my_photo";


    private static final String PATH_PROFILE = "profile";
    private static final String PATH_PHOTO_URL = "photoUrl";


    @BindView(R.id.btnSubir)
    Button btnSubir;
    @BindView(R.id.imgFoto)
    AppCompatImageView imgFoto;
    @BindView(R.id.btnBorrar)
    ImageButton btnBorrar;
    @BindView(R.id.container)
    ConstraintLayout container;
    //VAriables globase s de Firebase - REferencia
    private StorageReference mstorageReference;
    //Referencia de realtime database
    private DatabaseReference mdatabaseReference;

    private String mCurrentPhotoPath;
    private Uri mPhotoSelectedUri;

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_galeria:
                    mTextMessage.setText(R.string.txt_bottom_navigation_galeria);

                    fromGallery();
                    return true;
                case R.id.navigation_camara:
                    mTextMessage.setText(R.string.txt_bottom_navigation_camara);
                    //fromCamera();
                    dispatchTakePictureIntent();
                    return true;

            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        inicializarFirebase();

    }

    private void inicializarFirebase() {
        mstorageReference = FirebaseStorage.getInstance().getReference();
        //Instancia de la base de datos
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //Nos ubicamos en el nodo Profile en la base de datos  y añadimos la url
        mdatabaseReference = database.getReference().child(PATH_PROFILE).child(PATH_PHOTO_URL);


    }

    //Cargar Imagen
    private void configurarFotoPerfil() {
        /*Obtener imagen de storage
        mstorageReference.child(PATH_PROFILE).child(MY_PHOTO).getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //Guarda la imagen original y la procesada, para consultar el archivo en caché
                        final RequestOptions options = new RequestOptions().centerCrop()
                                .diskCacheStrategy(DiskCacheStrategy.ALL);

                        configurarFotoPerfil();

                        //Imagen que se carga al inicio desde una url
                        //Glide
                        Glide.with(MainActivity.this)
                                .load(uri)
                                .apply(options)
                                .into(imgFoto);

                        btnBorrar.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        btnBorrar.setVisibility(View.GONE);
                        Snackbar.make(container, R.string.mensaje_error_imagen,
                        Snackbar.LENGTH_LONG).show();
                    }
                });*/
        //Obtener imagen de realtimeDataBase con la url
        mdatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final RequestOptions options = new RequestOptions().centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL);

                configurarFotoPerfil();

                //Imagen que se carga al inicio desde una url
                //Glide
                Glide.with(MainActivity.this)
                        .load(dataSnapshot.getValue())
                        .apply(options)
                        .into(imgFoto);

                btnBorrar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                btnBorrar.setVisibility(View.GONE);
                Snackbar.make(container, R.string.mensaje_error_imagen,
                        Snackbar.LENGTH_LONG).show();
            }
        });




    }

    //Seleccionar foto de la galeria
    private void fromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RC_GALLERY);
    }
    //Seleccionar  la imagen desde la camara
    private void fromCamera(){
        //Lanzar la cámara
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, RC_CAMERA);

    }
//Metodo para obtener la imagen con resolucion completa desde la camara
    private  void  dispatchTakePictureIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null){
            File fotoFile;
            fotoFile = createImageFile();

            if (fotoFile!= null){
                Uri fotoUri = FileProvider.getUriForFile(this,"com.pepivsky.misfotos", fotoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
                startActivityForResult(takePictureIntent, RC_CAMERA);

            }
        }

    }

    private File createImageFile() {
        final String timeStamp = new SimpleDateFormat("dd-MM-yyyy_HHmmss", Locale.ROOT)
                .format(new Date());
        final String imageFileName = MY_PHOTO + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imagen = null;
        try {
            imagen = File.createTempFile(imageFileName, ".jpg" , storageDir);
            //Ruta de la imagen temporal
            mCurrentPhotoPath = imagen.getAbsolutePath();
        }catch (IOException e){
            e.printStackTrace();
        }
        return imagen;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {

                case RC_GALLERY:
                    if (data != null) {
                        mPhotoSelectedUri = data.getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                                    mPhotoSelectedUri);
                            imgFoto.setImageBitmap(bitmap);
                            btnBorrar.setVisibility(View.GONE);
                            mTextMessage.setText(R.string.main_pregunta_subir);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;


                case RC_CAMERA:
                    /*//Extraer miniatura
                    Bundle extras = data.getExtras();
                    Bitmap bitmap = (Bitmap)extras.get("data");*/
                    //Extraer foto en tamaño real
                    try {
                        mPhotoSelectedUri = addPicGallery();
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),
                                mPhotoSelectedUri);
                        imgFoto.setImageBitmap(bitmap);
                        btnBorrar.setVisibility(View.GONE);
                        mTextMessage.setText(R.string.main_pregunta_subir);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    break;

            }
        }
    }

    private Uri addPicGallery() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(file);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
        mCurrentPhotoPath = null;
        return contentUri;
    }

    //Método para Subir foto
    @OnClick(R.id.btnSubir)
    public void onClicSubirFoto() {
        //Subir foto
        //Referencia
        StorageReference Profilereference = mstorageReference.child(PATH_PROFILE);

        //Creacion de carpetas - ruta
        StorageReference photoReference = Profilereference.child(MY_PHOTO);

        photoReference.putFile(mPhotoSelectedUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Snackbar.make(container, R.string.mensaje_subida_exitosa, Snackbar.LENGTH_LONG).show();
                Uri downloadUri = taskSnapshot.getDownloadUrl();

                //Método que recibe la url
                GuardarFotoUrl(downloadUri);
                //Habilitar botton de eliminar
                btnBorrar.setVisibility(View.VISIBLE);
                mTextMessage.setText(R.string.mensaje_hecho);
            }
            //Listener en caso de que faller
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //SnackBar
                Snackbar.make(container, R.string.mensaje_subida_error, Snackbar.LENGTH_LONG).show();
            }
        });

    }

    //Método que recibe la url para guardar la foto
    private void GuardarFotoUrl(Uri downloadUri) {
        mdatabaseReference.setValue(downloadUri.toString());
    }

    //Eliminar foto de firebase Storage
    @OnClick(R.id.btnBorrar)
    public void onclicBorrarFoto() {
        mstorageReference.child(PATH_PROFILE).child(MY_PHOTO).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                mdatabaseReference.removeValue();//Eliminar Url de la BD
                imgFoto.setImageBitmap(null);
                btnBorrar.setVisibility(View.GONE);
                Snackbar.make(container, R.string.mensaje_borrado_exitoso, Snackbar.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(container, R.string.mensaje_borrado_error, Snackbar.LENGTH_LONG).show();
            }
        });
    }
}
