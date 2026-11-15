package com.example.hotel;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class SQLiteHelper extends SQLiteOpenHelper {

    public SQLiteHelper (Context context) {super(context, "HotelMedianoche.db", null, 1);}

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE USUARIOS("+"id INTEGER PRIMARY KEY AUTOINCREMENT,"+"nombre TEXT,"+"correo TEXT UNIQUE,"+"telefono TEXT,"+"contrasena TEXT)");

        db.execSQL("CREATE TABLE RESERVAS("+"id TEXT PRIMARY KEY,"+"usuario TEXT,"+"tipo TEXT,"+"titulo TEXT,"+"precioUnitario REAL,"+"checkIn TEXT,"+"checkOut TEXT,"+"cantidad INTEGER,"+"total REAL,"+"metodoPago TEXT,"+"pagoCompletado INTEGER,"+"estado TEXT,"+"creado INTEGER)");

        db.execSQL("CREATE TABLE VIAJES("+"id TEXT PRIMARY KEY,"+"usuario TEXT,"+"servicio TEXT,"+"metodoPago TEXT,"+"origen TEXT,"+"destino TEXT,"+"vehiculo TEXT,"+"costo TEXT,"+"estado TEXT,"+"conductorNombre TEXT,"+"conductorPlaca TEXT,"+"conductorAuto TEXT,"+"conductorCalificacion REAL,"+"creado INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion,
                          int newVersion){
        db.execSQL("DROP TABLE IF EXISTS USUARIOS");
        db.execSQL("DROP TABLE IF EXISTS RESERVAS");
        db.execSQL("DROP TABLE IF EXISTS VIAJES");
        onCreate(db);
    }
}
