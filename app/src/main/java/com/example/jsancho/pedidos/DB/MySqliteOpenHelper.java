package com.example.jsancho.pedidos.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.example.jsancho.pedidos.Objetos.Foto;
import com.example.jsancho.pedidos.Objetos.Pedido;
import com.example.jsancho.pedidos.Objetos.Tarea;

import java.io.ByteArrayOutputStream;

public class MySqliteOpenHelper  extends SQLiteOpenHelper {

    Context context;
    private static final String DATABASE_NAME = "Pedidos";
    private static final int DATABASE_VERSION = 3;


    private static final String CREAR_TABLA_PEDIDO = "CREATE TABLE Pedido (" +
            "codigo TEXT PRIMARY KEY," +
            "descripcion TEXT, fecha TEXT, marco TEXT, coordenadas TEXT," +
            "localidad TEXT, empresa TEXT)";

    private static final String CREAR_TABLA_TAREA = "CREATE TABLE Tarea (" +
            "cod_tarea TEXT PRIMARY KEY, descripcion TEXT," +
            "cod_recurso TEXT, cod_pedido TEXT," +
            "cargoRecurso TEXT, nombreRecurso TEXT," +
            "FOREIGN KEY(cod_pedido) REFERENCES Pedido(codigo))";

    private static final String CREAR_TABLA_FOTO = "CREATE TABLE Foto (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, imagen TEXT, descripcion TEXT," +
            "categoria TEXT, subcategoria TEXT, fecha TEXT, hora TEXT, coordenadasFotos TEXT," +
            "idTarea TEXT," +
            "FOREIGN KEY(idTarea) REFERENCES Tarea(cod_tarea))";

    public MySqliteOpenHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREAR_TABLA_PEDIDO);
        db.execSQL(CREAR_TABLA_TAREA);
        db.execSQL(CREAR_TABLA_FOTO);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + "Pedido");
        db.execSQL("DROP TABLE IF EXISTS " + "Tarea");
        db.execSQL("DROP TABLE IF EXISTS " + "Foto");
        this.onCreate(db);
    }


    //----------Actualización del listado de tareas----------\\

    public void limpiarTablaTareas(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS " + "Tarea");
        db.execSQL(CREAR_TABLA_TAREA);
        limpiarTablaPedidos(db);
    }

    public void limpiarTablaPedidos(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS " + "Tarea");
        db.execSQL(CREAR_TABLA_TAREA);
    }

    public void insertarTarea(SQLiteDatabase db, Tarea tarea){
        String cod_tarea = tarea.getCod_tarea();
        String descripcion = tarea.getDescripcion();
        String cod_recurso = tarea.getCod_recurso();
        String cod_pedido = tarea.getCod_pedido();
        String cargoRecurso = tarea.getCargoRecurso();
        String nombreRecurso = tarea.getNombreRecurso();

        db.execSQL("INSERT INTO TAREA VALUES(" +
                " " + cod_tarea + "," + descripcion + "," + cod_recurso + "" +
                "," + cod_pedido + "," + cargoRecurso + "," + nombreRecurso +")");
    }

    public void insertarPedido(SQLiteDatabase db, Pedido pedido){
        String codigo = pedido.getCodigo();
        String descripcion = pedido.getDescripcion();
        String fecha = pedido.getFecha();
        String marco = pedido.getMarco();
        String coordenadas = pedido.getCoordenadas();
        String localidad = pedido.getLocalidad();
        String empresa = pedido.getEmpresa();

        db.execSQL("INSERT INTO PEDIDO VALUES(" + codigo + "," + descripcion + ","
                + fecha + "," + marco + "," + coordenadas + "," + localidad + "," + empresa + "')");
    }


    //----------Se guarda un informe con fotos----------\\

    public void insertarFoto(SQLiteDatabase db, ContentValues c){
        byte [] imagen = (byte[]) c.get("imagen");
        String descripcion = c.getAsString("descripcion");
        String categoria = c.getAsString("categoria");
        String subcategoria = c.getAsString("subcategoria");
        String fecha = c.getAsString("fecha");
        String hora = c.getAsString("hora");
        String coordenadasFotos = c.getAsString("coordenadasFotos");
        String idTarea = c.getAsString("idTarea");


        //----------Borramos todas las fotos
        db.execSQL("INSERT INTO Foto (imagen, descripcion, categoria, subcategoria, fecha, hora, coordenadasFotos, idTarea)" +
                "VALUES ('" + Base64.encodeToString(imagen, Base64.DEFAULT) + "', '" + descripcion + "', '" + categoria + "', '" +
                subcategoria + "', '" + fecha + "', '" + hora + "', '" + coordenadasFotos + "', '" + idTarea + "')");


    }


    //----------La foto se ha enviado con éxito al servidor, o se borra en la app y se guarda el informe----------\\

    public void borrarFoto(SQLiteDatabase db, Foto miFoto){
        //int borrar = miFoto.getId();

        //db.execSQL("DELETE FROM FOTO WHERE id = " + borrar + "");

    }


    //----------Se elimina la tarea porque se ha enviado con éxito, marcando la casilla de finalizar----------\\

    public void borrarTarea(SQLiteDatabase db, Tarea tarea){
        String borrar = tarea.getCod_tarea();

        db.execSQL("DELETE FROM TAREA WHERE ID LIKE '" + borrar + "'");
    }



    //----------Recuperar pedido----------\\

    public Pedido getDescripcionPedido(SQLiteDatabase db, String cod_pedido){

        db.execSQL("SELECT * FROM PEDIDO WHERE codigo LIKE '" + cod_pedido + "'");

        return null;
    }


    //----------Recuperar fotos de una tarea activa----------\\
    public Cursor recuperarFotos(SQLiteDatabase db, String idTarea){
        Cursor c = db.rawQuery("SELECT * FROM Foto WHERE idTarea LIKE '" + idTarea + "'", null);
        return c;
    }

    //----------Borrar todas las fotos de una tarea (se hace antes de guardar)----------\\
    public void borrarTodasLasFotosDeTarea(SQLiteDatabase db, String idTarea){
        db.execSQL("DELETE FROM Foto WHERE idTarea LIKE '" + idTarea + "'");
    }
}
