package com.example.jsancho.pedidos;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.jsancho.pedidos.Clases_Auxiliares.Usuario;

import org.w3c.dom.Text;

public class Menu extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Intent intent = getIntent();
        Usuario miUsuario = (Usuario) intent.getParcelableExtra("miUsuario");

        TextView tv = findViewById(R.id.tv);
        tv.setText(miUsuario.getToken() + "\n\n" + miUsuario.getNombre() + "\n\n"
                    + miUsuario.getEmail() + "\n\n" + miUsuario.getEmpresa() + "\n\n" + miUsuario.getDelegacion() + "\n\n" + miUsuario.getCod_recurso());
    }
}
