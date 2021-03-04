package com.eiffage.companias.companias.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.eiffage.companias.companias.Adapters.MisPedidosAdapter;
import com.eiffage.companias.companias.DB.MySqliteOpenHelper;
import com.eiffage.companias.companias.Objetos.Pedido;
import com.eiffage.companias.companias.Objetos.Usuario;
import com.eiffage.companias.R;

import java.util.ArrayList;

public class MisPedidos extends AppCompatActivity {

    ArrayList<Pedido> misPedidos;
    ArrayAdapter<Pedido> pedidoArrayAdapter;
    ListView listaPedidos;
    MySqliteOpenHelper mySqliteOpenHelper;
    SQLiteDatabase db;
    Usuario miUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_pedidos);

        getSupportActionBar().setTitle("Mis pedidos");

        try{
            Intent i = getIntent();
            miUsuario = i.getParcelableExtra("miUsuario");
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }


        misPedidos = new ArrayList<Pedido>();

        mySqliteOpenHelper = new MySqliteOpenHelper(this);
        db = mySqliteOpenHelper.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT codigo, descripcion FROM PEDIDO", null);
        Log.d("Nº de PEDIDOS", c.getCount() + "");
        if(c.moveToFirst()){
            do {
                String codigo = c.getString(0);
                String descripcion = c.getString(1);
                Pedido p = new Pedido(codigo, descripcion, "-", "-", "-" ,"-", "-");
                misPedidos.add(p);
            }while(c.moveToNext());
        }
        else {
            AlertDialog.Builder alertdialogobuilder = new AlertDialog.Builder(this, R.style.MyDialogTheme);
            alertdialogobuilder
                    .setTitle("Sin tareas")
                    .setMessage("No tienes pedidos activos")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            finish();
                        }
                    })
                    .create();
            alertdialogobuilder.show();
        }

        pedidoArrayAdapter = new MisPedidosAdapter(getApplicationContext(), misPedidos);

        listaPedidos = findViewById(R.id.listaPedidos);
        listaPedidos.setAdapter(pedidoArrayAdapter);

    }

}
