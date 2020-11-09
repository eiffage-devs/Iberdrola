package com.eiffage.companias.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.eiffage.companias.DB.MySqliteOpenHelper;
import com.eiffage.companias.ExpandableHeightListView;
import com.eiffage.companias.Objetos.Foto;
import com.eiffage.companias.Adapters.ListaFotosAdapter;
import com.eiffage.companias.R;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class DetalleTarea extends AppCompatActivity {

    private String URL_ENVIAR_FOTO = "-";
    private String URL_BORRAR_TAREA = "-";

    Button abrirCamara, enviar, guardarInforme, documentacion, traspasar;
    ArrayList<Foto> myPictures;
    ExpandableHeightListView listaFotos;
    ListAdapter listaFotosAdapter;
    MySqliteOpenHelper mySqliteOpenHelper;
    SQLiteDatabase db;
    String idTarea;
    String cod_pedido;
    private static final int REQUEST_LOCATION = 1;
    boolean fallo = false;
    ProgressDialog progressDialog;
    String token, mCurrentPhotoPath, fecha, hora;
    boolean nuevaFoto = true;
    RequestQueue queue = null;

    @SuppressLint({"ClickableViewAccessibility", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detalle_tarea);

        URL_ENVIAR_FOTO = getResources().getString(R.string.urlEnviarFoto);
        URL_BORRAR_TAREA = getResources().getString(R.string.urlFinalizarTarea);

        enviar = findViewById(R.id.btnEnviar);
        documentacion = findViewById(R.id.btnDocumentacion);
        traspasar = findViewById(R.id.btnTraspasar);

        //----------Pintar datos del pedido----------\\

        mySqliteOpenHelper = new MySqliteOpenHelper(this);
        db = mySqliteOpenHelper.getWritableDatabase();

        Intent i = getIntent();
        idTarea = i.getStringExtra("idTarea");
        Log.d("ID TAREA", idTarea);
        cod_pedido = i.getStringExtra("cod_pedido");

        Cursor c0 = db.rawQuery("SELECT creadaPor FROM Tarea WHERE cod_tarea LIKE '" + idTarea + "'", null);
        if (c0.getCount() > 0) {
            c0.moveToFirst();
            String creadaPor = c0.getString(0);

            TextView txtCreadaPor = findViewById(R.id.txtCreadaPor);
            txtCreadaPor.setText("Tarea creada por: " + creadaPor.toUpperCase());
        }
        c0.close();

        Cursor c = db.rawQuery("SELECT * FROM Pedido WHERE codigo LIKE '" + cod_pedido + "'", null);
        Log.d("Nº de tareas", "" + c.getCount());
        if (c.getCount() > 0) {
            c.moveToFirst();
            String descripcion = c.getString(1);
            String fecha = c.getString(2);
            String fechaFinMeco = c.getString(7);
            String localidad = c.getString(5);

            TextView cp = findViewById(R.id.numPedidoDT);
            TextView d = findViewById(R.id.descPedidoDT);
            TextView f = findViewById(R.id.fechaPedidoDT);
            TextView m = findViewById(R.id.marcoPedidoDT);
            TextView l = findViewById(R.id.localidadPedidoDT);

            cp.setText(cod_pedido);
            d.setText(descripcion);
            f.setText(fecha);
            m.setText(fechaFinMeco);
            l.setText(localidad);
        }
        c.close();

        myPictures = new ArrayList<>();

        listaFotos = findViewById(R.id.listaFotos);
        listaFotos.setScrollContainer(false);
        listaFotos.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(false);
                return false;
            }
        });

        //----------Mostrar fotos almacenadas localmente----------\\
        cargarFotosLocales(idTarea);
        //--------------------------------------------------------\\

        guardarInforme = findViewById(R.id.btnGuardar);
        guardarInforme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarInforme.setEnabled(false);
                guardarInforme.setClickable(false);
                guardarInforme();
                guardarInforme.setClickable(true);
                guardarInforme.setEnabled(true);
                AlertDialog.Builder alertdialogobuilder = new AlertDialog.Builder(DetalleTarea.this, R.style.MyDialogTheme);
                alertdialogobuilder
                        .setTitle("Fotos guardadas")
                        .setMessage("Las fotos se han guardado correctamente en el dispositivo.")
                        .setCancelable(true)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                alertdialogobuilder.show();

            }
        });

        abrirCamara = findViewById(R.id.btnAñadir);
        abrirCamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                guardarInforme();

                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    // Ensure that there's a camera activity to handle the intent
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        // Create the File where the photo should go
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) {
                            // Error occurred while creating the File
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
                            Uri photoURI = FileProvider.getUriForFile(DetalleTarea.this,
                                    "com.eiffage.companias",
                                    photoFile);
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                            nuevaFoto = true;
                            startActivityForResult(takePictureIntent, 0);
                        }
                    }

            }
        });

        documentacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetalleTarea.this, Documentacion.class);
                intent.putExtra("cod_pedido", cod_pedido);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        //----------Pedimos permisos GPS ----------\\

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        ScrollView scrollView = findViewById(R.id.scrollTarea);
        scrollView.smoothScrollTo(0,0);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d("RUTA IMAGEN", mCurrentPhotoPath);
        return image;
    }

    public void guardarInforme() {

        //----------Borramos todas las fotos asociadas a la tarea antes de guardar los nuevos datos----------\\
        mySqliteOpenHelper.borrarTodasLasFotosDeTarea(db, idTarea);
        //---------------------------------------------------------------------------------------------------\\

        int i;
        for (i = 0; i < myPictures.size(); i++) {
            ContentValues c = new ContentValues();
            Foto actual = myPictures.get(i);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            actual.getFoto().compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();
            c.put("imagen", byteArray);
            c.put("descripcion", actual.getDescripcion());
            c.put("categoria", actual.getCategoria());
            c.put("subcategoria", actual.getSubcategoria());
            c.put("fecha", actual.getFecha());
            c.put("hora", actual.getHora());
            c.put("coordenadasFotos", actual.getCoordenadasFoto());
            c.put("idTarea", idTarea);
            c.put("id", actual.getId());
            c.put("urlFoto", actual.getUrlFoto());
            Log.d("Mi foto", c.toString());
            mySqliteOpenHelper.insertarFoto(db, c);

            Log.d("Guardado " + i, actual.getCategoria() + ", " + actual.getSubcategoria());
        }
    }


    public void lineasPedido(View view) {

        TextView cp = findViewById(R.id.numPedidoDT);

        Intent i = new Intent(this, LineasPedido.class);
        i.putExtra("cod_pedido", cp.getText().toString());
        startActivity(i);
    }

    private void editarPic() {

        //Nada más hacer la foto, te reenvía para rellenar la información asociada a la foto

        Intent i = new Intent(DetalleTarea.this, NuevaFoto.class);
        i.putExtra("foto", mCurrentPhotoPath);
        i.putExtra("esNuevo", "SI");
        startActivityForResult(i, 1);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            try {
                if (nuevaFoto) {
                    nuevaFoto = false;
                    editarPic();

                    cargarFotosLocales(idTarea);

                    listaFotosAdapter = new ListaFotosAdapter(this, myPictures);
                    listaFotos.setAdapter(listaFotosAdapter);
                    ExpandableHeightListView listView = new ExpandableHeightListView(this);

                    listView.setAdapter(listaFotosAdapter);
                    listView.setExpanded(true);

                }
            } catch (NullPointerException e) {
                Toast.makeText(this, "No se ha adjuntado ninguna foto", Toast.LENGTH_SHORT).show();
            }
        }

        else if(requestCode == 1){
            if(resultCode == Activity.RESULT_OK){

                Bitmap nuevaFoto;

                BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                bmOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

                // Decode the image file into a Bitmap sized to fill the View
                bmOptions.inJustDecodeBounds = false;
                bmOptions.inPurgeable = true;

                int photoW = bmOptions.outWidth;
                int photoH = bmOptions.outHeight;
                if (photoW < photoH) {
                    Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
                    Bitmap bitmap1 = Bitmap.createScaledBitmap(bitmap, 893, 1263, true);
                    Matrix matrix = new Matrix();

                    matrix.postRotate(0);
                    nuevaFoto = Bitmap.createBitmap(bitmap1, 0, 0, bitmap1.getWidth(), bitmap1.getHeight(), matrix, true);

                    bitmap.recycle();
                    bitmap1.recycle();

                } else {
                    Bitmap bitmap2 = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
                    Bitmap bitmap3 = Bitmap.createScaledBitmap(bitmap2, 1263, 893, true);
                    Matrix matrix = new Matrix();

                    matrix.postRotate(90);
                    nuevaFoto = Bitmap.createBitmap(bitmap3, 0, 0, bitmap3.getWidth(), bitmap3.getHeight(), matrix, true);

                    bitmap2.recycle();
                    bitmap3.recycle();

                }

                String categ = data.getStringExtra("categoria");
                String subcateg = data.getStringExtra("subcategoria");
                String desc = data.getStringExtra("descripcion");
                String fecha = data.getStringExtra("fecha");
                String hora = data.getStringExtra("hora");
                String coord = data.getStringExtra("coordenadas");
                String urlFoto = data.getStringExtra("urlFoto");

                Foto nueva = new Foto(nuevaFoto, desc, categ, subcateg, fecha, hora, coord, idTarea, fecha+hora, urlFoto);
                myPictures.add(0, nueva);
                listaFotosAdapter = new ListaFotosAdapter(this, myPictures);
                listaFotos.setAdapter(listaFotosAdapter);
                //int moverVista = listaFotos.getBottom();
                ScrollView scrollView = findViewById(R.id.scrollTarea);
                scrollView.smoothScrollTo(0, 0);
                guardarInforme();
            }
        }
        else if(requestCode == 2){
            if(resultCode == Activity.RESULT_OK){
                String categ = data.getStringExtra("categoria");
                String subcateg = data.getStringExtra("subcategoria");
                String desc = data.getStringExtra("descripcion");

                Foto cambio = myPictures.get(data.getIntExtra("posicion",0));
                cambio.setCategoria(categ);
                cambio.setSubcategoria(subcateg);
                cambio.setDescripcion(desc);

                myPictures.set(data.getIntExtra("posicion", 0), cambio);

                listaFotosAdapter = new ListaFotosAdapter(this, myPictures);
                listaFotos.setAdapter(listaFotosAdapter);
            }
        }
        else if(requestCode == 3){
            if(resultCode == RESULT_OK){
                try {
                    final Uri imageUri = data.getData();
                    Log.d("URI PATH", imageUri.getPath());
                    final InputStream imageStream = getContentResolver().openInputStream(imageUri);

                    Bitmap nuevaFoto;

                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    bmOptions.inJustDecodeBounds = true;

                    // Decode the image file into a Bitmap sized to fill the View
                    bmOptions.inJustDecodeBounds = false;
                    bmOptions.inPurgeable = true;

                    Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

                    int photoW = bitmap.getWidth();
                    int photoH = bitmap.getHeight();

                    Log.d("PHOTO W", "" + photoW);
                    Log.d("PHOTO H", "" + photoH);

                    int width;
                    int height;

                    Matrix matrix = new Matrix();

                    //HORIZONTAL
                    if (photoW > photoH){
                        double coef = photoW / 1263.0;
                        Log.d("COEF HORIZ ", coef + "");

                        width = 1263;

                        double beforeInt = photoW / coef;
                        Log.d("BEFORE INT HORIZONTAL", beforeInt + "" );

                        height = (int) (photoH / coef);

                    }
                    //VERTICAL
                    else {
                        double coef = photoH / 893.0;
                        Log.d("COEF VERTI ", coef + "");

                        height = 893;
                        double beforeInt = photoW / coef;
                        Log.d("BEFORE INT VERTICAL", beforeInt + "" );
                        width = (int) (photoW / coef);

                    }

                    Bitmap bitmap1 = Bitmap.createScaledBitmap(bitmap, width, height, true);
                    nuevaFoto = bitmap1;
                    //nuevaFoto = Bitmap.createBitmap(bitmap1, 0, 0, bitmap1.getWidth(), bitmap1.getHeight(), matrix, true);

                    String urlFoto = imageUri.toString();
                    recogerFechaYHora();

                    Foto nueva = new Foto(nuevaFoto, "-", "-", "-", fecha, hora, "-", idTarea, fecha+hora, urlFoto);
                    myPictures.add(0, nueva);
                    listaFotosAdapter = new ListaFotosAdapter(this, myPictures);
                    listaFotos.setAdapter(listaFotosAdapter);
                    ScrollView scrollView = findViewById(R.id.scrollTarea);
                    scrollView.smoothScrollTo(0, 0);
                    guardarInforme();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            else {
                Toast.makeText(DetalleTarea.this, "No se ha seleccionado ninguna foto", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void cargarFotosLocales(String idTarea) {
        Cursor c = mySqliteOpenHelper.recuperarFotos(db, idTarea);
        Log.d("Nº DE FOTOS de TAREA", c.getCount() + "");
        myPictures = new ArrayList<>();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            byte[] bytarray = Base64.decode(c.getString(1), Base64.DEFAULT);
            Bitmap imagen = BitmapFactory.decodeByteArray(bytarray, 0,
                    bytarray.length);

            String descripcion = c.getString(2);
            String categoria = c.getString(3);
            String subcategoria = c.getString(4);
            String fecha = c.getString(5);
            String hora = c.getString(6);
            String coordenadasFotos = c.getString(7);
            String urlFoto = c.getString(9);
            myPictures.add(new Foto(imagen, descripcion, categoria, subcategoria, fecha, hora, coordenadasFotos, idTarea, fecha + hora, urlFoto));
            c.moveToNext();
        }
        c.close();
        listaFotosAdapter = new ListaFotosAdapter(this, myPictures);
        listaFotos.setAdapter(listaFotosAdapter);
        ScrollView scrollView = findViewById(R.id.scrollTarea);
        scrollView.fullScroll(ScrollView.FOCUS_UP);
    }

    public void enviarInforme(View v) {

        enviar.setEnabled(false);
        CheckBox end = findViewById(R.id.checkFinalizar);
        if (end.isChecked()) {
            AlertDialog.Builder alertdialogobuilder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
            alertdialogobuilder
                    .setTitle("Finalizar tarea")
                    .setMessage("¿Seguro que quieres finalizar la tarea?\nSe enviarán todas las fotos y se borrará la tarea.")
                    .setCancelable(true)
                    .setPositiveButton("Enviar y Terminar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            try {
                                enviarYTerminar(true);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
            alertdialogobuilder.show();
        } else {
            try {
                enviarYTerminar(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void enviarYTerminar(boolean terminar) throws IOException {
        progressDialog = new ProgressDialog(DetalleTarea.this);
        progressDialog.setMessage("Espere, por favor"); // Setting Message
        progressDialog.setTitle("Enviando fotos..."); // Setting Title
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressDialog.show(); // Display Progress Dialog
        progressDialog.setCancelable(false);
        progressDialog.show();
        //Guardar cambios activos
        guardarInforme();
        //Enviar...
        Cursor c = mySqliteOpenHelper.recuperarFotos(db, idTarea);
        if (c.getCount() == 0) {
            try{
                CheckBox end = findViewById(R.id.checkFinalizar);
                if(end.isChecked()){
                    borrarTarea(idTarea);
                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "No hay fotos para enviar", Toast.LENGTH_SHORT).show();
                    enviar.setEnabled(true);
                    enviar.setClickable(true);
                }
            }
            catch (Exception e){
                fallo = true;
                mostrarResultado(true);
            }
            //mostrarResultado(true);

        } else {
            ArrayList<Foto> fotosParaEnviar = new ArrayList<>();
            c.moveToFirst();

            while (!c.isAfterLast()) {
                byte[] bytarray = Base64.decode(c.getString(1), Base64.DEFAULT);
                Log.d("BASE64", "" + bytarray);
                Bitmap imagen = BitmapFactory.decodeByteArray(bytarray, 0,
                        bytarray.length);

                String descripcion = c.getString(2);
                String categoria = c.getString(3);
                String subcategoria = c.getString(4);
                String fecha = c.getString(5);
                String hora = c.getString(6);
                String coordenadasFotos = c.getString(7);
                String urlFoto = c.getString(8);
                fotosParaEnviar.add(new Foto(imagen, descripcion, categoria, subcategoria, fecha, hora, coordenadasFotos, idTarea, fecha + hora, urlFoto));

                c.moveToNext();
            }

            fallo = false;
            queue = Volley.newRequestQueue(this);
            for (int i = 0; i < fotosParaEnviar.size(); i++) {
                Foto fotoActual = fotosParaEnviar.get(i);

                if (i == fotosParaEnviar.size() - 1) {
                    if (terminar) {
                        enviarFoto(fotoActual, true, false);
                    } else {
                        enviarFoto(fotoActual, true, true);
                    }
                } else {
                    enviarFoto(fotoActual, false, false);
                }
            }
            mySqliteOpenHelper.fotosEnviadas(db, idTarea);

            //Terminar...
            if (terminar) {
                progressDialog.dismiss();
                borrarTarea(idTarea);
            }
        }
    }

    public void enviarFoto(final Foto foto, final boolean ultimaFoto, final boolean mostrarResultado) throws IOException {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        foto.getFoto().compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] byteArray = stream.toByteArray();
        final String encodedImage = "holapaco, " + Base64.encodeToString(byteArray, Base64.DEFAULT);

        SharedPreferences myPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        token = myPrefs.getString("token", "Sin valor");
        Log.d("TOKEN", token);

        Log.d("num_pedido", cod_pedido);
        Log.d("num_tarea", idTarea);
        Log.d("descripcion", foto.getDescripcion());
        Log.d("area", foto.getCategoria());
        Log.d("subarea", foto.getSubcategoria());
        Log.d("fecha", foto.getFecha() + ", " + foto.getHora());
        Log.d("coordenadas", foto.getCoordenadasFoto());
        Log.d("foto", encodedImage);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_ENVIAR_FOTO,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("RESPONSE", response);
                        try {
                            JSONObject j = new JSONObject(response);
                            if (ultimaFoto) {
                                {
                                    progressDialog.dismiss();
                                    if (mostrarResultado) {
                                        mostrarResultado(false);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(" ERROR RESPONSE", error.toString());
                fallo = true;
                if (ultimaFoto) {
                    progressDialog.dismiss();
                    if (mostrarResultado) {
                        mostrarResultado(false);
                    }
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + token);

                return params;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("num_pedido", cod_pedido);
                params.put("num_tarea", idTarea);
                params.put("descripcion", foto.getDescripcion());
                params.put("area", foto.getCategoria());
                params.put("subarea", foto.getSubcategoria());
                params.put("fecha", foto.getFecha() + ", " + foto.getHora());
                params.put("coordenadas", foto.getCoordenadasFoto());
                params.put("foto", encodedImage);

                return params;
            }
        };
        stringRequest.setTag("ENVIO_FOTOS");
        stringRequest.setRetryPolicy((new DefaultRetryPolicy(60 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)));

        queue.add(stringRequest);
    }

    public void añadirFotoDesdeGaleria(View view) {
        Intent getPictureIntent = new Intent(Intent.ACTION_PICK);
        // Ensure that there's a camera activity to handle the intent
        if (getPictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(DetalleTarea.this,
                        "com.eiffage.companias",
                        photoFile);
                getPictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                getPictureIntent.setType("image/");
                nuevaFoto = true;
                startActivityForResult(getPictureIntent, 3);
            }
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

    public void mostrarResultado(final boolean terminar) {
        if (!terminar) {
            if (fallo) {
                AlertDialog.Builder alertdialogobuilder = new AlertDialog.Builder(DetalleTarea.this, R.style.MyDialogTheme);
                alertdialogobuilder
                        .setTitle("Error al enviar fotos")
                        .setMessage("Ha ocurrido un error.\nCompruebe su conexión y vuelva a intentarlo.\nTus fotos siguen guardadas.")
                        .setCancelable(false)
                        .setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                enviar.setEnabled(true);
                                finish();
                            }
                        })
                        .create();
                if (!isFinishing()) {
                    alertdialogobuilder.show();
                }

            } else {
                final AlertDialog.Builder alertdialogobuilder = new AlertDialog.Builder(DetalleTarea.this, R.style.MyDialogTheme);
                alertdialogobuilder
                        .setTitle("Fotos enviadas")
                        .setMessage("Las fotos se han enviado correctamente.\nLa tarea sigue activa.")
                        .setCancelable(false)
                        .setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                enviar.setEnabled(true);
                                mySqliteOpenHelper.fotosEnviadas(db, idTarea);
                                mySqliteOpenHelper.borrarTodasLasFotosDeTarea(db, idTarea);
                                cargarFotosLocales(idTarea);
                                myPictures = new ArrayList<>();
                                listaFotosAdapter = null;
                                listaFotos.setAdapter(listaFotosAdapter);
                            }
                        })
                        .create();
                if (!isFinishing()) {
                    alertdialogobuilder.show();
                }
            }
        } else {
            if (fallo) {
                AlertDialog.Builder alertdialogobuilder = new AlertDialog.Builder(DetalleTarea.this, R.style.MyDialogTheme);
                alertdialogobuilder
                        .setTitle("Error al enviar fotos")
                        .setMessage("Ha ocurrido un error.\nCompruebe su conexión y vuelva a intentarlo.\nTus fotos siguen guardadas.")
                        .setCancelable(false)
                        .setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                enviar.setEnabled(true);
                                finish();
                            }
                        })
                        .create();
                if (!isFinishing()) {
                    alertdialogobuilder.show();
                }

            } else {
                final AlertDialog.Builder alertdialogobuilder = new AlertDialog.Builder(DetalleTarea.this, R.style.MyDialogTheme);
                alertdialogobuilder
                        .setTitle("Fotos enviadas")
                        .setMessage("Se han enviado correctamente las fotos.\nLa tarea ha finalizado.")
                        .setCancelable(false)
                        .setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                enviar.setEnabled(true);
                                mySqliteOpenHelper.fotosEnviadas(db, idTarea);
                                mySqliteOpenHelper.borrarTodasLasFotosDeTarea(db, idTarea);
                                cargarFotosLocales(idTarea);
                                myPictures = new ArrayList<>();
                                listaFotosAdapter = null;
                                listaFotos.setAdapter(listaFotosAdapter);
                                alertdialogobuilder.setMessage("");
                                finish();
                                db.close();

                            }
                        })
                        .create();
                if (!isFinishing()) {
                    alertdialogobuilder.show();
                }
            }
        }
    }

    public void borrarTarea(final String tareaId) {
        SharedPreferences myPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        token = myPrefs.getString("token", "Sin valor");

        //Finalizar tarea en Navision
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_BORRAR_TAREA,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mySqliteOpenHelper.borrarTodasLasFotosDeTarea(db, tareaId);
                        mySqliteOpenHelper.borrarTarea(db, tareaId);
                        Log.d("FINALIZAR TAREA", response);
                        try {
                            JSONObject j = new JSONObject(response);
                            mostrarResultado(true);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Log.d("FINALIZAR TAREA", error.toString());
                Toast.makeText(getApplicationContext(), "No hay conexión, prueba más tarde", Toast.LENGTH_SHORT).show();
                enviar.setEnabled(true);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + token);

                return params;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("num_pedido", cod_pedido);
                params.put("num_tarea", idTarea);

                return params;
            }
        };
        queue.add(stringRequest);

    }

    public void eliminarTarea(View v){
        AlertDialog.Builder alertdialogobuilder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        alertdialogobuilder
                .setTitle("Eliminar tarea")
                .setMessage("¿Seguro que quieres eliminar la tarea?\nSe borrará la tarea y no aparecerá en tu listado.")
                .setCancelable(true)
                .setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        progressDialog = muestraLoader("Eliminando tarea...");
                        SharedPreferences myPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                        token = myPrefs.getString("token", "Sin valor");

                        //Finalizar tarea en Navision
                        RequestQueue queue = Volley.newRequestQueue(DetalleTarea.this);

                        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_BORRAR_TAREA,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        mySqliteOpenHelper.borrarTodasLasFotosDeTarea(db, idTarea);
                                        mySqliteOpenHelper.borrarTarea(db, idTarea);
                                        Log.d("FINALIZAR TAREA", response);
                                        try {
                                            JSONObject j = new JSONObject(response);
                                            progressDialog.dismiss();
                                            finish();

                                        } catch (JSONException e) {
                                            progressDialog.dismiss();
                                            e.printStackTrace();
                                        }


                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                progressDialog.dismiss();
                                Log.d("FINALIZAR TAREA", error.toString());
                                Toast.makeText(getApplicationContext(), "No hay conexión, prueba más tarde", Toast.LENGTH_SHORT).show();
                                enviar.setEnabled(true);
                            }
                        }) {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> params = new HashMap<String, String>();
                                //params.put("Content-Type", "application/json");
                                params.put("Authorization", "Bearer " + token);

                                return params;
                            }

                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("num_pedido", cod_pedido);
                                params.put("num_tarea", idTarea);

                                return params;
                            }
                        };
                        queue.add(stringRequest);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertdialogobuilder.show();

    }
    @Override
    public void onBackPressed() {

        AlertDialog.Builder alertdialogobuilder = new AlertDialog.Builder(DetalleTarea.this, R.style.MyDialogTheme);
        alertdialogobuilder
                .setTitle("Salir sin guardar")
                .setMessage("¿Seguro que quieres salir?\n¿Has guardado todo lo que necesitas?")
                .setCancelable(false)
                .setNegativeButton("Salir", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .setPositiveButton("Seguir aquí", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
        alertdialogobuilder.show();
    }

    public void traspasar(View view) {

        if(myPictures.size() == 0){
            Intent i = new Intent(DetalleTarea.this, DelegarTarea.class);
            i.putExtra("idTarea", idTarea);
            i.putExtra("cod_pedido", cod_pedido);
            startActivity(i);
            finish();
        }
        else {
            Toast.makeText(DetalleTarea.this, "No puedes delegar una tarea con fotos pendientes.\nPor favor, envía o elimina las fotos actuales.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public ProgressDialog muestraLoader(String mensaje){
        progressDialog = new ProgressDialog(DetalleTarea.this);
        progressDialog.setMessage(mensaje); // Setting Message
        progressDialog.setTitle("Espere, por favor"); // Setting Title
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressDialog.show(); // Display Progress Dialog
        progressDialog.setCancelable(false);
        progressDialog.show();

        return progressDialog;
    }
}
