package com.example.jsancho.pedidos.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
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
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.jsancho.pedidos.DB.MySqliteOpenHelper;
import com.example.jsancho.pedidos.Objetos.Foto;
import com.example.jsancho.pedidos.Adapters.ListaFotosAdapter;
import com.example.jsancho.pedidos.Objetos.Tarea;
import com.example.jsancho.pedidos.Objetos.Usuario;
import com.example.jsancho.pedidos.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.example.jsancho.pedidos.Activities.Login.urlCheck;

public class DetalleTarea extends AppCompatActivity {

    Button abrirCamara, enviarInforme, guardarInforme;
    ArrayList<Foto> myPictures;
    ListView listaFotos;
    ListaFotosAdapter listaFotosAdapter;
    MySqliteOpenHelper mySqliteOpenHelper;
    SQLiteDatabase db;
    String idTarea;
    String cod_pedido;
    private static final int REQUEST_LOCATION = 1;
    boolean fallo = false;
    ProgressDialog progressDialog;
    String token;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detalle_tarea);

        //----------Pintar datos del pedido----------\\

        mySqliteOpenHelper = new MySqliteOpenHelper(this);
        db = mySqliteOpenHelper.getWritableDatabase();

        Intent i = getIntent();
        idTarea = i.getStringExtra("idTarea");
        Log.d("ID TAREA", idTarea);
        cod_pedido = i.getStringExtra("cod_pedido");
        Cursor c = db.rawQuery("SELECT * FROM Pedido WHERE codigo LIKE '" + cod_pedido + "'", null);
        Log.d("Nº de tareas", "" + c.getCount());
        if(c.getCount() > 0) {
            c.moveToFirst();
            String descripcion = c.getString(1);
            String fecha = c.getString(2);
            String marco = c.getString(3);
            String coordenadas = c.getString(4);
            String localidad = c.getString(5);

            TextView cp = findViewById(R.id.numPedidoDT);
            TextView d = findViewById(R.id.descPedidoDT);
            TextView f = findViewById(R.id.fechaPedidoDT);
            TextView m = findViewById(R.id.marcoPedidoDT);
            TextView co = findViewById(R.id.localizacionPedidoDT);
            TextView l = findViewById(R.id.localidadPedidoDT);

            cp.setText(cod_pedido);
            d.setText(descripcion);
            f.setText(fecha);
            m.setText(marco);
            co.setText(coordenadas);
            l.setText(localidad);
        }

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

        //----------Mostrar fotos almacenadas localmente----------\\
        cargarFotosLocales(idTarea);
        //--------------------------------------------------------\\

        guardarInforme = findViewById(R.id.btnGuardar);
        guardarInforme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarInforme();
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
                listaFotosAdapter.setHaCambiado(false);

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

    public void guardarInforme() {

        //----------Borramos todas las fotos asociadas a la tarea antes de guardar los nuevos datos----------\\
        mySqliteOpenHelper.borrarTodasLasFotosDeTarea(db, idTarea);
        //---------------------------------------------------------------------------------------------------\\

        int i;
        for(i = 0; i<myPictures.size(); i++){
            ContentValues c = new ContentValues();
            Foto actual = myPictures.get(i);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            actual.getFoto().compress(Bitmap.CompressFormat.PNG, 100, stream);
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
            Log.d("Mi foto", c.toString());
            mySqliteOpenHelper.insertarFoto(db,c);
        }
    }

    public void lineasPedido(View view){

        TextView cp = findViewById(R.id.numPedidoDT);

        Intent i = new Intent(this, LineasPedido.class);
        i.putExtra("cod_pedido", cp.getText().toString());
        startActivity(i);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            try{
                //Guardar imagen en Bitmap.
                Bitmap image = (Bitmap) data.getExtras().get("data");
                Foto nuevaFoto = new Foto(image, "-", "-", "-", "-", "-", "-", "-", "-");
                myPictures.add(nuevaFoto);
                Log.d("Numero de fotos: ", myPictures.size() + "");

                listaFotosAdapter= new ListaFotosAdapter(this, myPictures);
                listaFotos.setAdapter(listaFotosAdapter);
                listaFotosAdapter.setHaCambiado(true);

            }
            catch (NullPointerException e){
                Toast.makeText(this, "No se ha adjuntado ninguna foto", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void cargarFotosLocales(String idTarea){
        Cursor c = mySqliteOpenHelper.recuperarFotos(db, idTarea);
        Log.d("Nº DE FOTOS de TAREA", c.getCount() + "");

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
            myPictures.add(new Foto(imagen, descripcion, categoria, subcategoria, fecha, hora, coordenadasFotos, idTarea, fecha+hora));
            c.moveToNext();
        }
        listaFotosAdapter= new ListaFotosAdapter(this, myPictures);
        listaFotos.setAdapter(listaFotosAdapter);
    }


    public void enviarInforme(View v) {

        CheckBox end = findViewById(R.id.checkFinalizar);
        if(end.isChecked()){
            AlertDialog.Builder alertdialogobuilder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
            alertdialogobuilder
                    .setTitle("Finalizar tarea")
                    .setMessage("¿Seguro que quieres finalizar la tarea?\nSe enviarán todas las fotos y se borrará la tarea.")
                    .setCancelable(true)
                    .setPositiveButton("Enviar y Terminar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            enviarYTerminar(true);

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
        else {
            enviarYTerminar(false);
        }
    }

        public void enviarYTerminar(boolean terminar){
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
            ArrayList<Foto> fotosParaEnviar = new ArrayList<>();
            c.moveToFirst();

            while(!c.isAfterLast()){
                byte[] bytarray = Base64.decode(c.getString(1), Base64.DEFAULT);
                Bitmap imagen = BitmapFactory.decodeByteArray(bytarray, 0,
                        bytarray.length);

                String descripcion = c.getString(2);
                String categoria = c.getString(3);
                String subcategoria = c.getString(4);
                String fecha = c.getString(5);
                String hora = c.getString(6);
                String coordenadasFotos = c.getString(7);
                fotosParaEnviar.add(new Foto(imagen, descripcion, categoria, subcategoria, fecha, hora, coordenadasFotos, idTarea, fecha+hora));

                c.moveToNext();
            }

            fallo = false;

            for(int i=0; i<fotosParaEnviar.size(); i++){
                Foto fotoActual = fotosParaEnviar.get(i);

                if(i == fotosParaEnviar.size() -1){
                    if(terminar){
                        enviarFoto(fotoActual, true, false);
                    }
                    else {
                        enviarFoto(fotoActual, true, true);
                    }

                }else{
                    enviarFoto(fotoActual, false, false);
                }
            }
            mySqliteOpenHelper.fotosEnviadas(db, idTarea);

            //Terminar...
            if(terminar){
                progressDialog.dismiss();
                borrarTarea(idTarea);
            }
        }

    public void enviarFoto(final Foto foto, final boolean ultimaFoto, final boolean mostrarResultado){

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        foto.getFoto().compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        final String encodedImage = "holapaco, " + Base64.encodeToString(byteArray, Base64.DEFAULT);

        SharedPreferences myPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        token = myPrefs.getString("token", "Sin valor");
        Log.d("TOKEN", token);
        RequestQueue queue = Volley.newRequestQueue(this);

                        StringRequest stringRequest = new StringRequest(Request.Method.POST, getResources().getString(R.string.urlBase) + getResources().getString(R.string.urlEnviarFoto),
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {

                                        try {
                                            JSONObject j = new JSONObject(response);
                                            if(j.getString("salida").equals("OK")) {
                                                if (ultimaFoto) {
                                                    {
                                                        progressDialog.dismiss();
                                                        if(mostrarResultado){
                                                            mostrarResultado(false);
                                                        }

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
                                fallo = true;
                                if(ultimaFoto){
                                    progressDialog.dismiss();
                                    if(mostrarResultado){
                                        mostrarResultado(false);
                                    }

                                }
                            }
                        })
                        {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> params = new HashMap<String, String>();
                                params.put("Content-Type", "application/json");
                                params.put("Authorization", "Bearer " + token);

                                return params;
                            }
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String, String> params = new HashMap<String, String>();
                                //params.put("foto", foto);
                                params.put("num_pedido" ,cod_pedido);
                                params.put("num_tarea" ,idTarea);
                                params.put("descripcion" ,foto.getDescripcion());
                                params.put("area" ,foto.getCategoria());
                                params.put("subarea" ,foto.getSubcategoria());
                                params.put("fecha" ,foto.getFecha() + ", " + foto.getHora());
                                params.put("coordenadas", foto.getCoordenadasFoto());
                                params.put("foto", encodedImage);

                                return params;
                            }
                        };
                        queue.add(stringRequest);
    }

    public void mostrarResultado(final boolean terminar){

        if(!terminar){
            if(fallo){
                AlertDialog.Builder alertdialogobuilder = new AlertDialog.Builder(DetalleTarea.this , R.style.MyDialogTheme);
                alertdialogobuilder
                        .setTitle("Error al enviar fotos")
                        .setMessage("Ha ocurrido un error.\nCompruebe su conexión y vuelva a intentarlo.\nTus fotos siguen guardadas.")
                        .setCancelable(false)
                        .setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        })
                        .create();
                if(!isFinishing()){
                    alertdialogobuilder.show();
                }

            }
            else {
                final AlertDialog.Builder alertdialogobuilder = new AlertDialog.Builder(DetalleTarea.this, R.style.MyDialogTheme);
                alertdialogobuilder
                        .setTitle("Fotos enviadas")
                        .setMessage("Se han enviado correctamente las fotos.\nLa tarea sigue activa.")
                        .setCancelable(true)
                        .setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                mySqliteOpenHelper.fotosEnviadas(db, idTarea);
                                mySqliteOpenHelper.borrarTodasLasFotosDeTarea(db, idTarea);
                                cargarFotosLocales(idTarea);
                                myPictures = new ArrayList<>();
                                listaFotos.setAdapter(null);
                            }
                        })
                        .create();
                if(!isFinishing()){
                    alertdialogobuilder.show();
                }
            }
        }
        else {
            if(fallo){
                AlertDialog.Builder alertdialogobuilder = new AlertDialog.Builder(DetalleTarea.this , R.style.MyDialogTheme);
                alertdialogobuilder
                        .setTitle("Error al enviar fotos")
                        .setMessage("Ha ocurrido un error.\nCompruebe su conexión y vuelva a intentarlo.\nTus fotos siguen guardadas.")
                        .setCancelable(false)
                        .setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        })
                        .create();
                if(!isFinishing()){
                    alertdialogobuilder.show();
                }

            }
            else {
                final AlertDialog.Builder alertdialogobuilder = new AlertDialog.Builder(DetalleTarea.this, R.style.MyDialogTheme);
                alertdialogobuilder
                        .setTitle("Fotos enviadas")
                        .setMessage("Se han enviado correctamente las fotos.\nLa tarea ha finalizado.")
                        .setCancelable(true)
                        .setPositiveButton("Entendido", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                mySqliteOpenHelper.fotosEnviadas(db, idTarea);
                                mySqliteOpenHelper.borrarTodasLasFotosDeTarea(db, idTarea);
                                cargarFotosLocales(idTarea);
                                myPictures = new ArrayList<>();
                                listaFotos.setAdapter(null);
                                alertdialogobuilder.setMessage("");
                                finish();
                                db.close();

                            }
                        })
                        .create();
                if(!isFinishing()){
                    alertdialogobuilder.show();
                }
            }
        }

    }

    public void borrarTarea(String tareaId){
        mySqliteOpenHelper.borrarTodasLasFotosDeTarea(db, tareaId);
        mySqliteOpenHelper.borrarTarea(db, tareaId);

        //Finalizar tarea en Navision
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, getResources().getString(R.string.urlBase) + getResources().getString(R.string.urlFinalizarTarea),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("FINALIZAR TAREA", response);
                        try {
                            JSONObject j = new JSONObject(response);
                            Log.d("FINALIZAR TAREA", response);
                            mostrarResultado(true);
                            /*if(j.getString("content").equals("OK")) {
                                Log.d("FINALIZAR TAREA", "OK");
                            }else{
                                Log.d("FINALIZAR TAREA", response);
                            }*/
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("FINALIZAR TAREA", error.toString());
            }
        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", "Bearer " + token);

                return params;
            }
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("num_pedido" ,cod_pedido);
                params.put("num_tarea" ,idTarea);

                return params;
            }
        };
        queue.add(stringRequest);


        //this.finish();
        //db.close();
    }

    @Override
    public void onBackPressed(){

        if(listaFotosAdapter.isHaCambiado()){

            AlertDialog.Builder alertdialogobuilder = new AlertDialog.Builder(DetalleTarea.this, R.style.MyDialogTheme);
            alertdialogobuilder
                    .setTitle("Salir sin guardar")
                    .setMessage("¿Seguro que quieres salir?\nLas fotos que no hayas guardado se perderán.")
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
        else {
            finish();
        }


    }
}
