package com.eiffage.companias.companias;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.eiffage.companias.companias.Adapters.Usuario;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class Login extends AppCompatActivity {

    EditText user, pass;
    SharedPreferences myPrefs;
    Button login;
    private boolean validado = false;
    static String urlLogin;
    static String urlCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        urlLogin = getResources().getString(R.string.urlBase) + getResources().getString(R.string.urlLogin);
        urlCheck = getResources().getString(R.string.urlBase) + getResources().getString(R.string.urlCheck);

        user = findViewById(R.id.loginEmail);
        pass = findViewById(R.id.loginPassword);
        login = findViewById(R.id.btnLogin);

        myPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String tokenGuardado = myPrefs.getString("token", "Sin valor");
        if(tokenGuardado.equals("Sin valor")){

            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        hacerLogin();
                    } catch (TimeoutError timeoutError) {
                        Toast.makeText(getApplicationContext(), "Fallo al conectar con el servidor.\nCompruebe su conexi??n y vuelva a intentarlo.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else {
            recuperarUsuario(tokenGuardado);
        }



    }

    public void hacerLogin() throws TimeoutError {
        final String username = user.getText().toString();
        final String password = pass.getText().toString();


        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.POST, urlLogin,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject job = new JSONObject(response);
                            String token = job.getString("token");
                            //Guardamos el token en Shared Preferences
                            SharedPreferences.Editor editor = getSharedPreferences("myPrefs", MODE_PRIVATE).edit();
                            editor.putString("token", token);
                            editor.apply();
                            recuperarUsuario(token);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                falloDeLogin();
            }
        })
        {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
                params.put("username", username);
                params.put("password", password);

                return params;
            }
        };
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void recuperarUsuario(final String token){
        final Usuario[] nuevoUsuario = new Usuario[1];
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest sr = new StringRequest(Request.Method.GET, urlCheck,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {

                            JSONObject job=new JSONObject(response);
                            String email=job.getString("email");
                            String empresa=job.getString("empresa");
                            String nombre=job.getString("nombre");
                            String delegacion=job.getString("delegacion");
                            String cod_recurso=job.getString("cod_recurso");
                            nuevoUsuario[0] = new Usuario(token, email, empresa, nombre, delegacion, cod_recurso);

                            //Enviar a la siguiente pantalla
                            Intent intent = new Intent(Login.this, Menu.class);
                            intent.putExtra("miUsuario", nuevoUsuario[0]);
                            startActivity(intent);
                            finish();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                falloDeDatos();
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

    public void falloDeLogin(){

        final TextView message = new TextView(this);
        final SpannableString s = new SpannableString(" \n\t Usuario o contrase??a incorrectos.\n\n\tSi es la primera vez que accede, debe activar su perfil \n\t accediendo a INET en el siguiente enlace");
        Pattern pattern = Pattern.compile("enlace");
        Linkify.addLinks(s, pattern , "http://inet.energia.eiffage.es?q=");
        message.setText(s);
        message.setMovementMethod(LinkMovementMethod.getInstance());

        AlertDialog.Builder alertdialogobuilder = new AlertDialog.Builder(this);
        alertdialogobuilder
                .setTitle("Login")
                .setView(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .create();
        alertdialogobuilder.show();
    }

    public void falloDeDatos(){

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    hacerLogin();
                } catch (TimeoutError timeoutError) {
                    Toast.makeText(getApplicationContext(), "Fallo al conectar con el servidor.\nCompruebe su conexi??n y vuelva a intentarlo.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}