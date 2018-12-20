package com.eiffage.companias;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.eiffage.companias.Activities.Documentacion;
import com.eiffage.companias.Activities.PDFViewer;
import com.eiffage.companias.DB.MySqliteOpenHelper;
import com.eiffage.companias.Objetos.Documento;
import com.eiffage.companias.Objetos.InputStreamVolleyRequest;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class DocumentacionGeneral extends AppCompatActivity {

    private static final String URL_Actualizar_Documentos = "http://192.168.97.198:8000/eiffage_intranet_2/api_iberdrola/getDocumentosGeneralesPedidoIBE";
    ProgressDialog progressDialog;
    SharedPreferences sp;
    String token;
    ArrayList<Documento> documentos;
    ListView listaDocs;
    MySqliteOpenHelper mySqliteOpenHelper;
    SQLiteDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documentacion_general);

        sp = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        token = sp.getString("token", "Sin valor");

        mySqliteOpenHelper = new MySqliteOpenHelper(DocumentacionGeneral.this);
        db = mySqliteOpenHelper.getWritableDatabase();

        documentos = new ArrayList<>();

        listaDocs = findViewById(R.id.listaDocsGeneral);
        llenarArrayListLocal();

        listaDocs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(documentos.get(position).getNombreFichero().contains(".PDF") || documentos.get(position).getNombreFichero().contains(".pdf")){
                    String rutaLocal = documentos.get(position).getRutaLocal();
                    Intent i = new Intent(DocumentacionGeneral.this, PDFViewer.class);
                    i.putExtra("rutaFichero", rutaLocal);
                    startActivity(i);
                }
                else {
                        String sourcePath = documentos.get(position).getRutaLocal();
                        File source = new File(sourcePath);

                        String destinationPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + documentos.get(position).getNombreFichero();
                        File destination = new File(destinationPath);
                        try
                        {
                            FileUtils.copyFile(source, destination);
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }

                        Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                        Uri data = Uri.fromFile(destination);
                        String [] parts = documentos.get(position).getNombreFichero().split(Pattern.quote("."));
                        String ext = parts[1];
                        String mimetype = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
                        intent.setDataAndType(data, mimetype);
                        Intent i = Intent.createChooser(intent, "Elige un lector");
                        startActivity(i);

                }
            }
        });

    }

    public void actualizarDocumentos(View view) {
        documentos = new ArrayList<>();
        muestraLoader("Actualizando documentos...");
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest sr = new StringRequest(Request.Method.GET, URL_Actualizar_Documentos,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String content = jsonObject.getString("content");

                            JSONArray job = new JSONArray(content);
                            for (int i = 0; i < job.length(); i++) {
                                JSONObject actual = (JSONObject) job.get(i);

                                String ruta = actual.getString("ruta");
                                String nombreFichero = actual.getString("fichero");
                                String delegacion = actual.getString("delegacion");

                                String [] parts = actual.get("fichero").toString().split(Pattern.quote("."));
                                String ext = parts[1];

                                Documento doc = new Documento("Docu_General_IBE", delegacion, generarNombreFicheroPDF() + "." + ext, ruta, nombreFichero);
                                documentos.add(doc);
                            }

                            List<String> lista = new ArrayList<>();
                            for(int i=0; i<documentos.size(); i++){
                                lista.add(documentos.get(i).getNombreFichero());
                            }

                            String[] values2 = new String[lista.size()];
                            lista.toArray(values2);

                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                                    android.R.layout.simple_list_item_1, android.R.id.text1, values2);

                            // Assign adapter to ListView
                            listaDocs.setAdapter(adapter);

                            peticionesDescargas(0);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                mensajeAlert("Error al conectar. Por favor, revisa tu conexión e inténtalo de nuevo");
                Log.d("Error doc general", error.toString());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", "Bearer " + token);

                return params;
            }
        };
        queue.add(sr);
    }

    public void peticionesDescargas(final int pos){
        if(isOnline(DocumentacionGeneral.this)) {
            RequestQueue mRequestQueue = Volley.newRequestQueue(DocumentacionGeneral.this);


            if(documentos.size() > pos){
                final Documento actual = documentos.get(pos);
                //this.cat = cat;

                final ArrayList<Documento> finalDocumentos = documentos;
                InputStreamVolleyRequest request = new InputStreamVolleyRequest(Request.Method.GET, actual.getUrl(),
                        new Response.Listener<byte[]>() {
                            @Override
                            public void onResponse(byte[] response) {
                                // TODO handle the response
                                try {
                                    if (response != null) {

                                        String name = actual.getNombreFichero();
                                        String rutaLocal = actual.getRutaLocal();

                                        mySqliteOpenHelper.insertarDocumento(db, actual.getCod_pedido(), getFilesDir().getAbsolutePath() + "/" + actual.getRutaLocal(), name, actual.getCategoria());

                                        FileOutputStream outputStream;
                                        outputStream = openFileOutput(actual.getRutaLocal(), Context.MODE_PRIVATE);
                                        outputStream.write(response);
                                        outputStream.close();

                                        if(pos == finalDocumentos.size() - 1){
                                            llenarArrayListLocal();
                                            progressDialog.dismiss();
                                        }
                                        else {
                                            peticionesDescargas(pos+1);
                                        }

                                    }
                                    else {
                                        progressDialog.dismiss();
                                    }
                                } catch (Exception e) {
                                    // TODO Auto-generated catch block
                                    Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE");
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(DocumentacionGeneral.this, "Ha habido algún problema. Inténtalo de nuevo en unos minutos.", Toast.LENGTH_SHORT).show();

                    }
                }
                ) {
                    // httpbin.org needs proper accept headers
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String>  params = new HashMap<String, String>();
                        //params.put("Content-Type", "application/pdf");
                        params.put("Authorization", "Bearer " + token);

                        return params;
                    }

                };
                request.setRetryPolicy((new DefaultRetryPolicy(60 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)));
                mRequestQueue.add(request);
            }
            else {
                progressDialog.dismiss();
                if(documentos.size() == 0){
                    Toast.makeText(getApplicationContext(), "NO HAY DOCUMENTOS", Toast.LENGTH_SHORT).show();
                }
            }
        }
        else {
            //Alert dialog diciendo que no hay conexión
            Toast.makeText(getApplicationContext(), "NO HAY INTERNET", Toast.LENGTH_SHORT).show();
        }

    }

    public void llenarArrayListLocal() {
        documentos = mySqliteOpenHelper.getDocumentos(db, "Docu_General_IBE", "DocTecnica");
        mostrarFicherosLocales();
    }

    public void mostrarFicherosLocales() {

        try{
            List<String> listaDocs = new ArrayList<>();
            for(int i=0; i<documentos.size(); i++){
                listaDocs.add(documentos.get(i).getNombreFichero());
            }

            String[] values = new String[listaDocs.size()];
            listaDocs.toArray(values);


        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    public String generarNombreFicheroPDF() {
        SimpleDateFormat format = new SimpleDateFormat("ddMMyyyy_HMmmss_SSS", Locale.getDefault());
        Date Ahora = new Date();
        String nombreFichero = format.format(Ahora);
        return nombreFichero;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
    }


    //----------Mostrar mensaje mediante alert en la Activity----------\\

    public void mensajeAlert(String message){
        AlertDialog.Builder alertdialogobuilder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
        alertdialogobuilder
                .setTitle("Documentación general")
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();
        if (!DocumentacionGeneral.this.isFinishing()){
            alertdialogobuilder.show();
        }
    }

    public void muestraLoader(String message){
        progressDialog = new ProgressDialog(DocumentacionGeneral.this);
        progressDialog.setMessage(message); // Setting Message
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressDialog.setCancelable(true);
        progressDialog.show();
    }

}
