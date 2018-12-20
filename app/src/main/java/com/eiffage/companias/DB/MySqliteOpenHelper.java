package com.eiffage.companias.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;
import android.util.Log;

import com.eiffage.companias.Objetos.Averia;
import com.eiffage.companias.Objetos.Documento;

import java.util.ArrayList;

public class MySqliteOpenHelper  extends SQLiteOpenHelper {

    Context context;
    private static final String DATABASE_NAME = "Pedidos";
    private static final int DATABASE_VERSION = 8;


    private static final String CREAR_TABLA_PEDIDO = "CREATE TABLE Pedido (" +
            "codigo TEXT PRIMARY KEY," +
            "descripcion TEXT, fecha TEXT, marco TEXT, coordenadas TEXT," +
            "localidad TEXT, empresa TEXT, fechaFinMeco TEXT)";

    private static final String CREAR_TABLA_TAREA = "CREATE TABLE Tarea (" +
            "cod_tarea TEXT PRIMARY KEY, descripcion TEXT," +
            "cod_recurso TEXT, cod_pedido TEXT," +
            "cargoRecurso TEXT, nombreRecurso TEXT, algunaFotoEnviada TEXT, creadaPor TEXT," +
            "FOREIGN KEY(cod_pedido) REFERENCES Pedido(codigo))";

    private static final String CREAR_TABLA_FOTO = "CREATE TABLE Foto (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, imagen TEXT, descripcion TEXT," +
            "categoria TEXT, subcategoria TEXT, fecha TEXT, hora TEXT, coordenadasFotos TEXT," +
            "idTarea TEXT, urlFoto TEXT," +
            "FOREIGN KEY(idTarea) REFERENCES Tarea(cod_tarea))";

    private static final String CREAR_TABLA_DOCUMENTOS = "CREATE TABLE Documento ( " +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, cod_pedido TEXT, rutaInterna TEXT, nombreQueSeMuestra TEXT, " +
            "categoria TEXT, FOREIGN KEY(cod_pedido) REFERENCES Pedido(codigo))";

    private static final String CREAR_TABLA_AVERIAS = "CREATE TABLE Averia ( " +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, cod_averia TEXT, cod_recurso TEXT, descripcion TEXT, gestor TEXT, observaciones TEXT, fecha TEXT, localidad TEXT)";

    public MySqliteOpenHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREAR_TABLA_PEDIDO);
        db.execSQL(CREAR_TABLA_TAREA);
        db.execSQL(CREAR_TABLA_FOTO);
        db.execSQL(CREAR_TABLA_DOCUMENTOS);
        db.execSQL(CREAR_TABLA_AVERIAS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + "Pedido");
        db.execSQL("DROP TABLE IF EXISTS " + "Tarea");
        db.execSQL("DROP TABLE IF EXISTS " + "Foto");
        db.execSQL("DROP TABLE IF EXISTS " + "Documento");
        db.execSQL("DROP TABLE IF EXISTS " + "Averia");
        this.onCreate(db);
    }

    //---------- AVERÍAS ----------\\
    public void insertarAveria(SQLiteDatabase db, Averia averia){
        db.execSQL("INSERT INTO Averia (cod_averia, cod_recurso, descripcion, gestor, observaciones, fecha, localidad) VALUES" +
                "('" + averia.getCod_averia() + "','" + averia.getCod_recurso() + "','" + averia.getDescripcion() + "','" +
                averia.getGestor() + "','" + averia.getObservaciones() + "','" + averia.getFecha() + "','" + averia.getLocalidad() +"')" );
    }

    public Cursor getAveriasLocales(SQLiteDatabase db, String cod_recurso){
        Cursor c = db.rawQuery("SELECT cod_averia, descripcion, gestor, fecha, observaciones, localidad FROM Averia WHERE cod_recurso LIKE'" + cod_recurso + "'", null);
        return c;
    }

    public void borrarAverias(SQLiteDatabase db, String cod_recurso){
        db.execSQL("DELETE FROM Averia WHERE cod_recurso LIKE '" + cod_recurso + "'");
    }










    public void insertarDocumento(SQLiteDatabase db, String cod_pedido, String rutaInterna, String nombreQueSeMuestra, String categoria){
        db.execSQL("INSERT INTO Documento (cod_pedido, rutaInterna, nombreQueSeMuestra, categoria)VALUES ( '" + cod_pedido + "', '" + rutaInterna + "', '" +
                nombreQueSeMuestra + "','" + categoria + "')");
        Cursor c = db.rawQuery("SELECT * FROM Documento WHERE cod_pedido LIKE '" + cod_pedido + "'", null);
        Log.d("INSERCION DOCUMENTO", "AHORA HAY " + c.getCount() + " FILAS EN LA TABLA DOCUMENTO");
        c.close();

    }

    public void borrarFicherosDePedido(SQLiteDatabase db, String cod_pedido, String categoria){
        db.execSQL("DELETE FROM Documento WHERE cod_pedido LIKE '" + cod_pedido + "' AND categoria LIKE '" + categoria + "'");
    }

    public ArrayList<Documento> getDocumentos(SQLiteDatabase db, String cod_pedido, String categoria){
        ArrayList<Documento> actuales = new ArrayList<>();
        Cursor c = db.rawQuery("SELECT * FROM Documento WHERE cod_pedido LIKE '" + cod_pedido + "' and categoria LIKE '" + categoria + "'", null);
        if(c.getCount() > 0){
            c.moveToFirst();
            do{
                String pedido = c.getString(1);
                String cat = c.getString(4);
                String rutaLocal = c.getString(2);
                String url = null;
                String nom = c.getString(3);
                actuales.add(new Documento(pedido, cat, rutaLocal, url, nom));
            }while(c.moveToNext());
        }
        c.close();
        return actuales;
    }


    //----------Actualización del listado de tareas----------\\

    public void limpiarTablaPedidos(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS " + "Tarea");
        db.execSQL(CREAR_TABLA_TAREA);
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
        String urlFoto = c.getAsString("urlFoto");

        db.execSQL("INSERT INTO Foto (imagen, descripcion, categoria, subcategoria, fecha, hora, coordenadasFotos, idTarea, urlFoto)" +
                "VALUES ('" + Base64.encodeToString(imagen, Base64.DEFAULT) + "', '" + descripcion + "', '" + categoria + "', '" +
                subcategoria + "', '" + fecha + "', '" + hora + "', '" + coordenadasFotos + "', '" + idTarea + "', '" + urlFoto + "')");

    }

    //----------Se elimina la tarea porque se ha enviado con éxito, marcando la casilla de finalizar----------\\

    public void borrarTarea(SQLiteDatabase db, String idTarea){

        db.execSQL("DELETE FROM TAREA WHERE cod_tarea LIKE '" + idTarea + "'");
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

    public void fotosEnviadas(SQLiteDatabase db, String idTarea){
        db.execSQL("UPDATE Tarea SET algunaFotoEnviada = 'true' WHERE cod_tarea LIKE '" + idTarea + "'");
    }

}
