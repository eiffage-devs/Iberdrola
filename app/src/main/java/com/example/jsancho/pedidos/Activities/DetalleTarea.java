package com.example.jsancho.pedidos.Activities;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jsancho.pedidos.DB.MySqliteOpenHelper;
import com.example.jsancho.pedidos.Objetos.Foto;
import com.example.jsancho.pedidos.Adapters.ListaFotosAdapter;
import com.example.jsancho.pedidos.Objetos.Tarea;
import com.example.jsancho.pedidos.R;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class DetalleTarea extends AppCompatActivity {

    Button abrirCamara;
    ArrayList<Foto> myPictures;
    ListView listaFotos;
    ListaFotosAdapter listaFotosAdapter;
    MySqliteOpenHelper mySqliteOpenHelper;
    SQLiteDatabase db;
    String idTarea;

    private static final int REQUEST_LOCATION = 1;

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
        String cod_pedido = i.getStringExtra("cod_pedido");
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

    public void guardarInforme(View v) {

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
        Toast.makeText(this, "Se han insertado: \n" + i + "filas.", Toast.LENGTH_SHORT).show();
    }

    public void enviarInforme(View v) {

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
                Log.d("TAMAÑO DEL ARRAY", myPictures.size() + "");

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
}
