package com.example.jsancho.pedidos;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.jsancho.pedidos.Clases_Auxiliares.Foto;
import com.example.jsancho.pedidos.Clases_Auxiliares.ListaFotosAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class DetalleTarea extends AppCompatActivity {

    Button abrirCamara;
    ArrayList<Foto> myPictures;
    ListView listaFotos;
    ListaFotosAdapter listaFotosAdapter;

    private static final int REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detalle_tarea);
        myPictures = new ArrayList<>();
        listaFotos = findViewById(R.id.listaFotos);
        listaFotos.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        abrirCamara = findViewById(R.id.btnAñadir);
        abrirCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){

                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 0);
                }else{
                    ActivityCompat.requestPermissions(DetalleTarea.this, new String[]{ Manifest.permission.CAMERA}, 0);
                }
            }
        });

        //----------Pedimos permisos GPS----------\\

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

    }

    public void informesGuardados(View v) {

    }

    public void descargarDatosDePedido(View v) {

    }

    public void guardarInforme(View v) {

    }

    public void enviarInforme(View v) {

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            try{
                //Guardar imagen en Bitmap.
                Bitmap image = (Bitmap) data.getExtras().get("data");
                Foto nuevaFoto = new Foto(image,"-","-","-","-", "-", "-");
                myPictures.add(nuevaFoto);
                Log.d("Numero de fotos: ", myPictures.size() + "");

                listaFotosAdapter= new ListaFotosAdapter(this, myPictures);
                listaFotos.setAdapter(listaFotosAdapter);
                Log.d("TAMAÑO DEL ARRAY", myPictures.size() + "");

            }
            catch (NullPointerException e){
                Toast.makeText(this, "No se ha adjuntado ninguna foto", Toast.LENGTH_SHORT).show();
            }

        }
    }
}
