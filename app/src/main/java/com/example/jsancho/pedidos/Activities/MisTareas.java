package com.example.jsancho.pedidos.Activities;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.jsancho.pedidos.Adapters.MisTareasAdapter;
import com.example.jsancho.pedidos.DB.MySqliteOpenHelper;
import com.example.jsancho.pedidos.Objetos.Tarea;
import com.example.jsancho.pedidos.Objetos.Usuario;
import com.example.jsancho.pedidos.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class MisTareas extends AppCompatActivity {

    ListView listaTareas;
    ArrayList<Tarea> misTareas;
    MisTareasAdapter adapter;
    Usuario miUsuario;
    SQLiteDatabase myDataBase;
    MySqliteOpenHelper mySqliteOpenHelper;
    String pedido;
    Button actualizarTareas;
    TextView ultimaActualizacion;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_tareas);
        getSupportActionBar().setTitle("Mis tareas");
        mostrarUltimaActualizacion();
        misTareas = new ArrayList<>();
        pedido = "todo";
        //----------Recibir datos de usuario de la activity anterior----------\\

        Intent i = getIntent();
        miUsuario = i.getParcelableExtra("miUsuario");

        //----------CREAR O ABRIR LA BASE DE DATOS----------\\

        mySqliteOpenHelper = new MySqliteOpenHelper(this);
        myDataBase = mySqliteOpenHelper.getWritableDatabase();

        //----------CARGAR DATOS LOCALES----------\\

        listaTareas = findViewById(R.id.listaTareas);
        listaTareas.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MisTareas.this, DetalleTarea.class);
                Tarea r = (Tarea) listaTareas.getItemAtPosition(position);
                intent.putExtra("cod_pedido", r.getCod_pedido());
                intent.putExtra("idTarea", r.getCod_tarea());
                startActivity(intent);

            }
        });

        if(i.getStringExtra("filtro_tareas") != null){
            pedido = i.getStringExtra("filtro_tareas");
            if(pedido.equals("todo")){

            }
            else {
                actualizarTareas = findViewById(R.id.btnActualizarTareas);
                actualizarTareas.setVisibility(View.INVISIBLE);
            }
        }
        mostrarTareasLocales();


    }

    @Override
    protected void onRestart() {
        mostrarTareasLocales();
        mostrarUltimaActualizacion();
        super.onRestart();
    }

    //---------------------Mostrar las tareas almacenadas localmente----------------------\\
    //----------(ESTE MÉTODO SE LLAMA AL ABRIR LA ACTIVITY Y AL ACTUALIZAR DATOS----------\\

    public void mostrarTareasLocales(){


        misTareas = new ArrayList<>();
        if(pedido.equals("todo")){
            Cursor c = myDataBase.rawQuery("SELECT cod_pedido, descripcion, cod_tarea FROM Tarea", null);
            Log.d("Nº de tareas", "" + c.getCount());
            if(c.getCount() > 0){
                c.moveToFirst();
                do{
                    String cod_pedido = c.getString(0);
                    String desc_tarea = c.getString(1);
                    String cod_tarea = c.getString(2);

                    Tarea t = new Tarea(cod_tarea, desc_tarea, "-", cod_pedido, "-", "-");
                    misTareas.add(t);

                }while (c.moveToNext());


                //----------Pintamos en el ListView las tareas proporcionadas por la base de datos----------\\
                adapter = new MisTareasAdapter(getApplicationContext(), misTareas);
                listaTareas.setAdapter(adapter);

            }
        }
        else {
            Cursor c = myDataBase.rawQuery("SELECT cod_pedido, descripcion, cod_tarea FROM Tarea WHERE cod_pedido LIKE '" + pedido + "'", null);
            Log.d("Nº de tareas", "" + c.getCount());
            Log.d(null, "ESTAMOS FILTRANDO");
            if(c.getCount() > 0){
                c.moveToFirst();
                do{
                    String cod_pedido = c.getString(0);
                    String desc_tarea = c.getString(1);
                    String cod_tarea = c.getString(2);

                    Tarea t = new Tarea(cod_tarea, desc_tarea, "-", cod_pedido, "-", "-");
                    misTareas.add(t);

                }while (c.moveToNext());


                //----------Pintamos en el ListView las tareas proporcionadas por la base de datos----------\\
                adapter = new MisTareasAdapter(getApplicationContext(), misTareas);
                listaTareas.setAdapter(adapter);

            }
        }
    }

    public void actualizarTareas(View v){

        progressDialog = new ProgressDialog(MisTareas.this);
        progressDialog.setMessage("Espere, por favor"); // Setting Message
        progressDialog.setTitle("Actualizando tareas..."); // Setting Title
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressDialog.show(); // Display Progress Dialog
        progressDialog.setCancelable(false);
        progressDialog.show();
        //----------Petición a la API para recuperar las tareas activas del usuario----------\\
        actualizarTareas = findViewById(R.id.btnActualizarTareas);
        actualizarTareas.setEnabled(false);
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest sr = new StringRequest(Request.Method.GET, getResources().getString(R.string.urlBase) + getResources().getString(R.string.urlGetTareas),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject job=new JSONObject(response);
                            Log.d("response", response);
                            String content=job.getString("content");
                            String error=job.getString("error");

                            if(error.equals("false")){
                                if(content.equals("null")){
                                    mensajeAlert("Ya tienes toda la información actualizada");
                                    guardarActualizacion();
                                }
                                else {
                                    //----------TODO: Recibir tareas y guardarlas en local----------\\
                                    JSONObject jsonObject = new JSONObject(content);
                                    Log.d(null, jsonObject.getJSONArray("tareas").toString());
                                    JSONArray tareas = jsonObject.getJSONArray("tareas");

                                    Log.d("Response", response);
                                    guardarTareasEnLocal(tareas);
                                    mostrarTareasLocales();
                                    guardarActualizacion();

                                    JSONObject jo = new JSONObject(content);
                                    JSONArray pedidos = jo.getJSONArray("pedidos");
                                    actualizarPedidos(pedidos);
                                    actualizarTareas.setEnabled(true);
                                    progressDialog.dismiss();

                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            mensajeAlert("Ya tienes toda la información actualizada");                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mensajeAlert("Error al conectar. Por favor, revisa tu conexión e inténtalo de nuevo");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", "Bearer " + miUsuario.getToken());

                return params;
            }
        };
        queue.add(sr);


    }

    //----------Mostrar mensaje mediante alert en la Activity----------\\

    public void mensajeAlert(String message){
        AlertDialog.Builder alertdialogobuilder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        alertdialogobuilder
                .setTitle("Mis tareas")
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();
        if (!MisTareas.this.isFinishing()){
            alertdialogobuilder.show();
        }
    }

    //-----------------------------Guardado de tareas en local----------------------------\\

    public void guardarTareasEnLocal(JSONArray response){

        //----------Limpiar tabla----------\\

        myDataBase.execSQL("DROP TABLE IF EXISTS Tarea");

        //----------CREAR TABLA TAREAS----------\

        myDataBase.execSQL(
                "CREATE TABLE IF NOT EXISTS Tarea (" +
                        "cod_tarea TEXT PRIMARY KEY, descripcion TEXT," +
                        "cod_recurso TEXT, cod_pedido TEXT," +
                        "cargoRecurso TEXT, nombreRecurso TEXT, algunaFotoEnviada TEXT," +
                        "FOREIGN KEY(cod_pedido) REFERENCES Pedido(codigo))");

        //----------Inserción de tareas obtenidas a través del API----------\\
        try {
            for(int i=0; i<response.length(); i++){
                JSONObject nuevaTarea = response.getJSONObject(i);
                String cargoRecurso = nuevaTarea.getString("CargoRecurso");
                String nombreRecurso = nuevaTarea.getString("NombreRecurso");
                String cod_pedido = nuevaTarea.getString("Pedido");
                String cod_recurso = nuevaTarea.getString("CodRecurso");
                String cod_tarea = nuevaTarea.getString("CodTarea");
                String descripcion = nuevaTarea.getString("Desc");

                ContentValues contentValues = new ContentValues();
                contentValues.put("cod_tarea", cod_tarea);
                contentValues.put("descripcion", descripcion);
                contentValues.put("cod_recurso", cod_recurso);
                contentValues.put("cod_pedido", cod_pedido);
                contentValues.put("cargoRecurso", cargoRecurso);
                contentValues.put("nombreRecurso", nombreRecurso);
                myDataBase.insert("Tarea", "-", contentValues);
            }
            Log.d("Tareas insertadas", "" + response.length());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mensajeAlert("Las tareas se han actualizado");

    }

    public void actualizarPedidos(JSONArray response){

        myDataBase.execSQL("DROP TABLE IF EXISTS Pedido");

        myDataBase.execSQL("CREATE TABLE Pedido (" +
                "codigo TEXT PRIMARY KEY," +
                "descripcion TEXT, fecha TEXT, marco TEXT, coordenadas TEXT," +
                "localidad TEXT, empresa TEXT)");
        try {
            for(int i=0; i<response.length(); i++){
                JSONObject nuevoPedido = response.getJSONObject(i);
                String codigo = nuevoPedido.getString("Pedido");
                String descripcion = nuevoPedido.getString("Desc");
                String fecha = nuevoPedido.getString("FechaPed");
                String marco = nuevoPedido.getString("ContratoM");
                String coordenadas = nuevoPedido.getString("Localizacion");
                String localidad = nuevoPedido.getString("Localidad");
                String empresa = nuevoPedido.getString("Empresa");

                ContentValues c = new ContentValues();
                c.put("codigo", codigo);
                c.put("descripcion", descripcion);
                c.put("fecha", fecha);
                c.put("marco", marco);
                c.put("coordenadas", coordenadas);
                c.put("localidad", localidad);
                c.put("empresa", empresa);
                myDataBase.insert("Pedido", "-", c);
            }
            Log.d("Pedidos insertados", "" + response.length());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void guardarActualizacion(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        Date date = new Date();
        String fecha = dateFormat.format(date);

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

        String hora = tempHora + ":" + tempMin + ":" + tempSeg;

        ultimaActualizacion  = findViewById(R.id.ultimaActualizacion);
        ultimaActualizacion.setText("Última actualización: AHORA");
        ultimaActualizacion.setBackgroundColor(getResources().getColor(R.color.VerdeBootstrap));
        SharedPreferences.Editor editor = getSharedPreferences("myPrefs", MODE_PRIVATE).edit();
        editor.putString("ultimaActualizacionFecha", fecha);
        editor.putString("ultimaActualizacionHora", hora);
        editor.apply();

    }

    public void mostrarUltimaActualizacion(){
        SharedPreferences myPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String f = myPrefs.getString("ultimaActualizacionFecha", "-");
        String s = myPrefs.getString("ultimaActualizacionHora", "-");
        ultimaActualizacion = findViewById(R.id.ultimaActualizacion);
        ultimaActualizacion.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
        ultimaActualizacion  = findViewById(R.id.ultimaActualizacion);
        if(!f.equals("-") && !s.equals("-")){
            ultimaActualizacion.setText("Última actualización: " + f + ", " + s);
        }

    }
}
