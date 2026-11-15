package com.example.hotel;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "user_session";

    public static void guardarCorreoLogin(Context context, String correo) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("correo", correo);

        // Si aun no hay nombre guardado, generamos uno por defecto a partir del correo
        String nombreActual = prefs.getString("nombre", null);
        if (nombreActual == null) {
            String nombrePorDefecto = correo.contains("@") ? correo.substring(0, correo.indexOf("@")) : "Usuario";
            editor.putString("nombre", nombrePorDefecto);
        }
        editor.apply();
    }

    public static boolean haySesion(Context context) {
        return !obtenerCorreo(context).isEmpty();
    }

    public static String obtenerNombre(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString("nombre", "Usuario");
    }

    public static String obtenerCorreo(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString("correo", "");
    }

    public static String obtenerTelefono(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString("telefono", "");
    }

    public static String obtenerFotoUri(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString("fotoUri", null);
    }

    public static void guardarDatosPerfil(Context context, String nombre, String correo, String telefono) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
                .putString("nombre", nombre)
                .putString("correo", correo)
                .putString("telefono", telefono)
                .apply();
    }

    public static void guardarFotoUri(Context context, String uri) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString("fotoUri", uri).apply();
    }

    public static void cerrarSesion(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}