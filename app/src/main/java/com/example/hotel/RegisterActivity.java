package com.example.hotel;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText edtNombre, edtCorreo, edtTelefono, edtContrasena, edtConfirmarContrasena;
    private MaterialButton btnRegistrarse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView txtIniciarSesion = findViewById(R.id.txtIniciarSesion);
        edtNombre = findViewById(R.id.edtNombre);
        edtCorreo = findViewById(R.id.edtCorreo);
        edtTelefono = findViewById(R.id.edtTelefono);
        edtContrasena = findViewById(R.id.edtContrasena);
        edtConfirmarContrasena = findViewById(R.id.edtConfirmarContrasena);
        btnRegistrarse = findViewById(R.id.btnRegistrarse);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        txtIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intentarRegistrar();
            }
        });
    }

    private void intentarRegistrar() {
        String nombre = texto(edtNombre);
        String correo = texto(edtCorreo).toLowerCase(Locale.ROOT);
        String telefono = texto(edtTelefono).replace(" ", "");
        String contrasena = texto(edtContrasena);
        String confirmar = texto(edtConfirmarContrasena);

        if (TextUtils.isEmpty(nombre) || nombre.length() < 3) {
            edtNombre.setError("Ingresa tu nombre completo");
            edtNombre.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            edtCorreo.setError("Correo electrónico no válido");
            edtCorreo.requestFocus();
            return;
        }
        if (!telefono.matches("[0-9+]{7,15}")) {
            edtTelefono.setError("Número de teléfono no válido");
            edtTelefono.requestFocus();
            return;
        }
        if (contrasena.length() < 6) {
            edtContrasena.setError("La contraseña debe tener al menos 6 caracteres");
            edtContrasena.requestFocus();
            return;
        }
        if (!contrasena.equals(confirmar)) {
            edtConfirmarContrasena.setError("Las contraseñas no coinciden");
            edtConfirmarContrasena.requestFocus();
            return;
        }
        SQLiteHelper helper = new SQLiteHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();


        Cursor cursor = db.rawQuery("SELECT id FROM USUARIOS WHERE correo = ?", new String[]{correo});
        boolean existe = cursor.moveToFirst();
        cursor.close();

        if (existe) {
            db.close();
            edtCorreo.setError("Este correo ya está registrado");
            edtCorreo.requestFocus();
            return;
        }


        ContentValues valores = new ContentValues();
        valores.put("nombre", nombre);
        valores.put("correo", correo);
        valores.put("telefono", telefono);
        valores.put("contrasena", contrasena);

        long resultado = db.insert("USUARIOS", null, valores);
        db.close();

        if (resultado != -1) {
            SessionManager.guardarDatosPerfil(this, nombre, correo, telefono);
            Toast.makeText(this, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "No se pudo crear la cuenta. Intenta de nuevo.", Toast.LENGTH_LONG).show();
        }
    }

    private String texto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }
}
