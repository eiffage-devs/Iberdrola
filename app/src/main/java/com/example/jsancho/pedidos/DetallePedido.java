package com.example.jsancho.pedidos;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.jsancho.pedidos.Clases_Auxiliares.DocumentosAdapter;
import com.example.jsancho.pedidos.Clases_Auxiliares.MisPedidosAdapter;
import com.example.jsancho.pedidos.Clases_Auxiliares.Pedido;

import java.util.ArrayList;

public class DetallePedido extends AppCompatActivity {

    ListView docTecnica, prevencion;
    DocumentosAdapter documentosAdapter;
    ArrayList<String> s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detalle_pedido);



        s = new ArrayList<String>();
        s.add("Hola");
        s.add("Mundo");
        s.add("3");
        s.add("4");
        s.add("5");

        documentosAdapter = new DocumentosAdapter(getApplicationContext(), s);

        docTecnica = findViewById(R.id.listaDocumentacion);
        prevencion = findViewById(R.id.listaPrevencion);

        docTecnica.setAdapter(documentosAdapter);
        prevencion.setAdapter(documentosAdapter);
    }
}
