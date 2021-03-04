package com.eiffage.companias.companias.Activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.eiffage.companias.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class NuevaFoto extends AppCompatActivity {

    private String mCurrentPhotoPath;
    private ImageView imagenFoto;
    Spinner spinnerCategorias, spinnerSubcategorias;
    EditText descripcion;

    private LocationManager locationManager;

    byte[] imagenParaIntent;
    Bitmap nuevaFoto;
    String categoria, subcategoria, desc, fecha, hora, lattitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nueva_foto);

        imagenFoto = findViewById(R.id.imagenFoto);
        spinnerCategorias = findViewById(R.id.spinner);
        spinnerSubcategorias = findViewById(R.id.spinner2);
        descripcion = findViewById(R.id.descripcion);

        Intent intent = getIntent();
        mCurrentPhotoPath = intent.getStringExtra("foto");

        if (intent.getStringExtra("esNuevo").equals("SI")) {
            setPic();
            llenarSpinners();
        }
        else if(intent.getStringExtra("esNuevo").equals("NO")){
            cargarInfo();
        }

    }

    private void setPic() {

        Glide.with(this)
                .load(mCurrentPhotoPath) // Uri of the picture
                .into(imagenFoto);
    }

    public void llenarSpinners(){

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.categorias, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerCategorias.setAdapter(adapter);

        spinnerCategorias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos == 0) {
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.sub1, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    spinnerSubcategorias
                            .setAdapter(adapter);

                } else if (pos == 1) {
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.sub2, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    spinnerSubcategorias.setAdapter(adapter);

                } else if (pos == 2) {
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.sub3, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    spinnerSubcategorias.setAdapter(adapter);

                } else {
                    spinnerSubcategorias.setAdapter(null);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.sub1, android.R.layout.simple_spinner_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


                spinnerSubcategorias.setAdapter(adapter);
            }
        });

    }

    public void aceptar(View v){
        recogerDatos();

        if(getIntent().getStringExtra("esNuevo").equals("SI")){
            Intent returnIntent = new Intent();
            returnIntent.putExtra("categoria", categoria);
            returnIntent.putExtra("subcategoria", subcategoria);
            returnIntent.putExtra("descripcion", desc);
            returnIntent.putExtra("fecha", fecha);
            returnIntent.putExtra("hora", hora);
            returnIntent.putExtra("coordenadas", longitude + "," + lattitude);
            returnIntent.putExtra("urlFoto", mCurrentPhotoPath);
            setResult(Activity.RESULT_OK,returnIntent);
            //nuevaFoto.recycle();
            finish();
        }
        else if(getIntent().getStringExtra("esNuevo").equals("NO")){
            Intent returnIntent = new Intent();
            returnIntent.putExtra("categoria", categoria);
            returnIntent.putExtra("subcategoria", subcategoria);
            returnIntent.putExtra("descripcion", desc);
            returnIntent.putExtra("posicion", getIntent().getIntExtra("posicion", 0));
            setResult(Activity.RESULT_OK,returnIntent);
            //nuevaFoto.recycle();
            finish();
        }
    }

    public void cancelar(View view){
        //nuevaFoto.recycle();
        finish();
    }

    public void recogerDatos(){

        //Categoría
        if(spinnerCategorias.getSelectedItem() == null){
            categoria = "-";
        }
        else categoria = spinnerCategorias.getSelectedItem().toString();

        //Subcategoría
        if(spinnerSubcategorias.getSelectedItem() == null){
            subcategoria = "-";
        }
        else subcategoria = spinnerSubcategorias.getSelectedItem().toString();

        //Descripción
        if(descripcion.getText().toString().equals("")){
            desc = "-";
        }
        else desc = descripcion.getText().toString();


        if(getIntent().getStringExtra("esNuevo").equals("SI")){
            //Recoger fecha y hora
            recogerFechaYHora();

            //Recoger coordenadas
            recogerCoordenadas();
        }

    }

    public void recogerFechaYHora(){
        //----------Fecha y hora de la foto----------\\
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-dd-MM", Locale.getDefault());
        Date date = new Date();
        fecha = dateFormat.format(date);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone("CET"));
        String tempHora = "" + calendar.get(Calendar.HOUR_OF_DAY);
        if (tempHora.length() == 1) {
            tempHora = "0" + tempHora;
        }
        String tempMin = "" + calendar.get(Calendar.MINUTE);
        if (tempMin.length() == 1) {
            tempMin = "0" + tempMin;
        }
        String tempSeg = "" + calendar.get(Calendar.SECOND);
        if (tempSeg.length() == 1) {
            tempSeg = "0" + tempSeg;
        }

        hora = tempHora + ":" + tempMin + ":" + tempSeg;
    }

    public void recogerCoordenadas(){
        //----------Recoger coordenadas GPS----------\\
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            mensajeDeAlertaGPS();

        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                    (getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                //ActivityCompat.requestPermissions(, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

            } else {
                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

                Location location2 = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

                if (location != null) {
                    double latti = location.getLatitude();
                    double longi = location.getLongitude();
                    lattitude = String.valueOf(latti);
                    longitude = String.valueOf(longi);

                } else if (location1 != null) {
                    double latti = location1.getLatitude();
                    double longi = location1.getLongitude();
                    lattitude = String.valueOf(latti);
                    longitude = String.valueOf(longi);

                } else if (location2 != null) {
                    double latti = location2.getLatitude();
                    double longi = location2.getLongitude();
                    lattitude = String.valueOf(latti);
                    longitude = String.valueOf(longi);

                } else {
                    //Toast.makeText(this, "Imposible localizar tu posición", Toast.LENGTH_SHORT).show();

                }
            }
        }
    }

    protected void mensajeDeAlertaGPS() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Por favor activa el GPS de tu dispositivo")
                .setCancelable(false)
                .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        getApplicationContext().startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }


    public void cargarInfo(){

        final int[] cont = {0};

        Intent i = getIntent();
        mCurrentPhotoPath = i.getStringExtra("foto");

        //Cargamos la foto
        setPic();

        //Cargamos los spinners
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.categorias, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerCategorias.setAdapter(adapter);

        if(!i.getStringExtra("categoria").equals("-")){

            String c = i.getStringExtra("categoria");
            c = c.substring(6, c.length());
            Log.d("categoria", c);

            String s = i.getStringExtra("subcategoria");
            s = s.substring(9, s.length());
            Log.d("subcategoria", s);

            ArrayList cats = new ArrayList(Arrays.asList(getApplicationContext().getResources().getStringArray(R.array.categorias)));
            spinnerCategorias.setSelection(cats.indexOf(c));

            if(cats.indexOf(c) == 0){
                ArrayAdapter<CharSequence> subadapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.sub1, android.R.layout.simple_spinner_item);
                subadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                spinnerSubcategorias.setAdapter(subadapter);

                ArrayList subcats = new ArrayList(Arrays.asList(getApplicationContext().getResources().getStringArray(R.array.sub1)));
                spinnerSubcategorias.setSelection(subcats.indexOf(s));
            }
            else if(cats.indexOf(c) == 1){
                ArrayAdapter<CharSequence> subadapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.sub2, android.R.layout.simple_spinner_item);
                subadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                spinnerSubcategorias.setAdapter(subadapter);

                ArrayList subcats = new ArrayList(Arrays.asList(getApplicationContext().getResources().getStringArray(R.array.sub2)));
                spinnerSubcategorias.setSelection(subcats.indexOf(s));
            }
            else if(cats.indexOf(c) == 2){
                ArrayAdapter<CharSequence> subadapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.sub3, android.R.layout.simple_spinner_item);
                subadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                spinnerSubcategorias.setAdapter(subadapter);

                ArrayList subcats = new ArrayList(Arrays.asList(getApplicationContext().getResources().getStringArray(R.array.sub3)));
                spinnerSubcategorias.setSelection(subcats.indexOf(s));
            }
            else {
                spinnerCategorias.setSelection(0);

                ArrayAdapter<CharSequence> subadapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.sub1, android.R.layout.simple_spinner_item);
                subadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                spinnerSubcategorias.setAdapter(subadapter);
                spinnerSubcategorias.setSelection(0);
            }
        }

        //Cargamos la descripción
        String d = i.getStringExtra("desc");
        d = d.substring(13, d.length());
        descripcion.setText(d);

        //Atentos a los cambios en los spinner

        spinnerCategorias.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if(++cont[0] > 1){
                    if (pos == 0) {
                        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.sub1, android.R.layout.simple_spinner_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        spinnerSubcategorias
                                .setAdapter(adapter);

                    } else if (pos == 1) {
                        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.sub2, android.R.layout.simple_spinner_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        spinnerSubcategorias.setAdapter(adapter);

                    } else if (pos == 2) {
                        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.sub3, android.R.layout.simple_spinner_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        spinnerSubcategorias.setAdapter(adapter);

                    } else {
                        spinnerSubcategorias.setAdapter(null);
                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if(++cont[0] > 1){
                    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.sub1, android.R.layout.simple_spinner_item);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


                    spinnerSubcategorias.setAdapter(adapter);
                }

            }
        });
    }
}
