package com.pepivsky.misfotos;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private static final  int RC_GALLERY = 21;
    private static final  int RC_CAMERA = 22;

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
    }

    //Seleccionar foto de la galeria
    private void fromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, RC_GALLERY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            switch (requestCode){

                case RC_GALLERY:
                    if (data != null){
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
                    break;

            }
        }
    }

    @OnClick(R.id.btnSubir)
    public void onViewClicked() {
    }
}
