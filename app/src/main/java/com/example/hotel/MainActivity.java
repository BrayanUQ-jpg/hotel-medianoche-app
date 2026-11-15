package com.example.hotel;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.credentials.Credential;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.credentials.exceptions.NoCredentialException;

import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    private static final String WEB_CLIENT_ID = "640715407342-od1tg6rdmvr65o6varchpe4bfr4kfncd.apps.googleusercontent.com";

    private TextInputEditText edtCorreo;
    private TextInputEditText edtContrasena;
    private MaterialButton btnLogin;
    private TextView txtRegistrate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtCorreo = findViewById(R.id.edtCorreo);
        edtContrasena = findViewById(R.id.edtContrasena);
        btnLogin = findViewById(R.id.btnLogin);
        txtRegistrate = findViewById(R.id.txtRegistrate);
        MaterialButton btnGoogle = findViewById(R.id.btnGoogle);
        btnGoogle.setOnClickListener(v -> iniciarConGoogle());

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentarIniciarSesion();
            }
        });

        txtRegistrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, RegisterActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (SessionManager.haySesion(this)) {
            irAlDashboard();
        }
    }

    private void intentarIniciarSesion() {
        String correo = texto(edtCorreo).toLowerCase(Locale.ROOT);
        String contrasena = texto(edtContrasena);

        if (TextUtils.isEmpty(correo)) {
            edtCorreo.setError("Ingresa tu correo electrónico");
            edtCorreo.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            edtCorreo.setError("Correo electrónico no válido");
            edtCorreo.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(contrasena)) {
            edtContrasena.setError("Ingresa tu contraseña");
            edtContrasena.requestFocus();
            return;
        }
        if (contrasena.length() < 6) {
            edtContrasena.setError("La contraseña debe tener al menos 6 caracteres");
            edtContrasena.requestFocus();
            return;
        }


        SQLiteHelper helper = new SQLiteHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT nombre, correo, telefono FROM USUARIOS WHERE correo = ? AND contrasena = ?",
                new String[]{correo, contrasena});

        if (cursor.moveToFirst()) {
            String nombre = cursor.getString(0);
            String correoGuardado = cursor.getString(1);
            String telefono = cursor.getString(2) == null ? "" : cursor.getString(2);
            cursor.close();
            db.close();

            SessionManager.guardarDatosPerfil(this, nombre, correoGuardado, telefono);
            Toast.makeText(this, "Bienvenido", Toast.LENGTH_SHORT).show();
            irAlDashboard();
        } else {
            cursor.close();
            db.close();
            Toast.makeText(this, "Correo o contraseña incorrectos", Toast.LENGTH_LONG).show();
        }
    }



    private void iniciarConGoogle() {
        if (WEB_CLIENT_ID.startsWith("PEGA_AQUI")) {
            Toast.makeText(this, "Falta pegar el Client ID de Google en MainActivity", Toast.LENGTH_LONG).show();
            return;
        }

        GetGoogleIdOption opcionGoogle = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(WEB_CLIENT_ID)
                .build();

        GetCredentialRequest solicitud = new GetCredentialRequest.Builder()
                .addCredentialOption(opcionGoogle)
                .build();

        CredentialManager.create(this).getCredentialAsync(
                this,
                solicitud,
                new CancellationSignal(),
                ContextCompat.getMainExecutor(this),
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    @Override
                    public void onResult(GetCredentialResponse resultado) {
                        procesarCredencialGoogle(resultado.getCredential());
                    }

                    @Override
                    public void onError(GetCredentialException error) {
                        if (error instanceof GetCredentialCancellationException) {
                            return; // el usuario cerró la ventana
                        }
                        if (error instanceof NoCredentialException) {
                            Toast.makeText(MainActivity.this,
                                    "No hay cuentas de Google disponibles en este dispositivo",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }
                        Toast.makeText(MainActivity.this,
                                "No se pudo iniciar con Google: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void procesarCredencialGoogle(Credential credencial) {
        if (!(credencial instanceof CustomCredential)
                || !GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL.equals(credencial.getType())) {
            Toast.makeText(this, "Tipo de credencial no reconocido", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            GoogleIdTokenCredential google = GoogleIdTokenCredential.createFrom(credencial.getData());
            String correo = google.getId().trim().toLowerCase(Locale.ROOT);
            String nombre = google.getDisplayName();
            if (nombre == null || nombre.trim().isEmpty()) {
                nombre = correo.contains("@") ? correo.substring(0, correo.indexOf("@")) : "Usuario";
            }
            String telefono = "";


            SQLiteHelper helper = new SQLiteHelper(this);
            SQLiteDatabase db = helper.getWritableDatabase();
            Cursor cursor = db.rawQuery(
                    "SELECT nombre, telefono FROM USUARIOS WHERE correo = ?",
                    new String[]{correo});

            if (cursor.moveToFirst()) {
                nombre = cursor.getString(0);
                telefono = cursor.getString(1) == null ? "" : cursor.getString(1);
                cursor.close();
            } else {
                cursor.close();
                ContentValues valores = new ContentValues();
                valores.put("nombre", nombre);
                valores.put("correo", correo);
                valores.put("telefono", telefono);

                valores.put("contrasena", UUID.randomUUID().toString());
                db.insert("USUARIOS", null, valores);
            }
            db.close();

            SessionManager.guardarDatosPerfil(this, nombre, correo, telefono);
            Toast.makeText(this, "Bienvenido " + nombre, Toast.LENGTH_SHORT).show();
            irAlDashboard();
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo iniciar sesión con Google", Toast.LENGTH_LONG).show();
        }
    }

    private String texto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }

    private void irAlDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
