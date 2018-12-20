package com.eiffage.companias.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SearchView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.eiffage.companias.Adapters.LineaAdapter;
import com.eiffage.companias.Objetos.Linea;
import com.eiffage.companias.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LineasPedido extends AppCompatActivity implements SearchView.OnQueryTextListener {

    ListView lineasPedido;
    String pedido;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.my_menu, menu);

        final MenuItem searchItem = menu.findItem(R.id.menuSearch);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setQueryHint("Desc. de producto");
        searchView.setOnQueryTextListener(this);

        return true;
    }

    SharedPreferences myPrefs;
    LineaAdapter lineaAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lineas_pedido);

        lineasPedido = findViewById(R.id.listaLineasPedido);
        myPrefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        final String tokenGuardado = myPrefs.getString("token", "Sin valor");

        Intent i = getIntent();
        String cod_pedido = i.getStringExtra("cod_pedido");

        //----------Petición a la API para recuperar las tareas activas del usuario----------\\
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest sr = new StringRequest(Request.Method.GET, getResources().getString(R.string.urlBase) + getResources().getString(R.string.urlLineasPedido) + cod_pedido,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("response", response);
                        ArrayList<Linea> misLineas = new ArrayList<>();
                        try {
                            JSONObject jo = new JSONObject(response);
                            JSONArray jsonArray = jo.getJSONArray("content");
                            for (int i =0; i<jsonArray.length(); i++){
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String empresa = jsonObject.getString("Empresa");
                                String numLinea = "" + jsonObject.getString("NumLinea");
                                String codProducto = jsonObject.getString("CodProducto");
                                String cant = "" + jsonObject.getString("Cant");
                                String descProducto = jsonObject.getString("DescProducto");
                                pedido = jsonObject.getString("Pedido");
                                String unMedida = jsonObject.getString("UnMedida");

                                Linea l = new Linea(empresa, numLinea, codProducto, cant, descProducto, pedido, unMedida);
                                misLineas.add(l);
                            }
                            getSupportActionBar().setTitle("Nº pedido: " + pedido);
                            lineaAdapter = new LineaAdapter(getApplicationContext(), misLineas);
                            lineasPedido.setAdapter(lineaAdapter);
                            Log.d("Líneas mostradas", misLineas.size() + "");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (NullPointerException e){
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Authorization", "Bearer " + tokenGuardado);

                return params;
            }
        };
        queue.add(sr);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        lineaAdapter.getFilter().filter(newText.toLowerCase());
        return false;
    }
}
