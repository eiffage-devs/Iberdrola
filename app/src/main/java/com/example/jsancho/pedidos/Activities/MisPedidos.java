package com.example.jsancho.pedidos.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.jsancho.pedidos.Adapters.MisPedidosAdapter;
import com.example.jsancho.pedidos.DB.MySqliteOpenHelper;
import com.example.jsancho.pedidos.Objetos.Pedido;
import com.example.jsancho.pedidos.Objetos.Usuario;
import com.example.jsancho.pedidos.R;

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

        Intent i = getIntent();
        miUsuario = i.getParcelableExtra("miUsuario");
        Log.d("CODIGO DE RECURSO", miUsuario.getCod_recurso());


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
                    .setTitle("Mis tareas")
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
        listaPedidos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MisPedidos.this, DetallePedido.class);
                //intent.putExtra("miUsuario", miUsuario);
                startActivity(intent);
            }
        });
    }

    public void actualizarPedidos(View v){
        Toast.makeText(this, "ACTUALIZAR PEDIDOS", Toast.LENGTH_SHORT).show();
    }
}
