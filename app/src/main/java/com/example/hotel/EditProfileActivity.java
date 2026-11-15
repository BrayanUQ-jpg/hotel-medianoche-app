package com.example.hotel;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView imgFotoPerfilEditar;
    private EditText edtNombre, edtCorreo, edtTelefono;
    private MaterialButton btnGuardarCambios;
    private ActivityResultLauncher<String[]> selectorImagen;
    private String fotoUriActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageView btnCambiarFotoEditar = findViewById(R.id.btnCambiarFotoEditar);
        imgFotoPerfilEditar = findViewById(R.id.imgFotoPerfilEditar);
        edtNombre = findViewById(R.id.edtNombre);
        edtCorreo = findViewById(R.id.edtCorreo);
        edtTelefono = findViewById(R.id.edtTelefono);
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios);

        edtCorreo.setEnabled(false);
        edtCorreo.setFocusable(false);
        edtCorreo.setAlpha(0.7f);

        selectorImagen = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri == null) return;
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri,
                                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (Exception ignored) {
                    }
                    fotoUriActual = uri.toString();
                    imgFotoPerfilEditar.setImageURI(uri);
                }
        );

        cargarDatosActuales();
        btnBack.setOnClickListener(v -> finish());
        btnCambiarFotoEditar.setOnClickListener(v -> selectorImagen.launch(new String[]{"image/*"}));
        btnGuardarCambios.setOnClickListener(v -> guardarCambios());
    }

    private void cargarDatosActuales() {
        edtNombre.setText(SessionManager.obtenerNombre(this));
        edtCorreo.setText(SessionManager.obtenerCorreo(this));
        edtTelefono.setText(SessionManager.obtenerTelefono(this));

        fotoUriActual = SessionManager.obtenerFotoUri(this);
        if (fotoUriActual != null) {
            try {
                imgFotoPerfilEditar.setImageURI(Uri.parse(fotoUriActual));
            } catch (Exception ignored) {
            }
        }
    }

    private void guardarCambios() {
        String nombre = edtNombre.getText().toString().trim();
        String correo = SessionManager.obtenerCorreo(this);
        String telefono = edtTelefono.getText().toString().trim().replace(" ", "");

        if (TextUtils.isEmpty(nombre) || nombre.length() < 3) {
            edtNombre.setError("Ingresa tu nombre completo");
            edtNombre.requestFocus();
            return;
        }
        if (!telefono.matches("[0-9+]{7,15}")) {
            edtTelefono.setError("Número de teléfono no válido");
            edtTelefono.requestFocus();
            return;
        }


        SessionManager.guardarDatosPerfil(this, nombre, correo, telefono);
        if (fotoUriActual != null) SessionManager.guardarFotoUri(this, fotoUriActual);

        SQLiteHelper helper = new SQLiteHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("nombre", nombre);
        valores.put("telefono", telefono);
        db.update("USUARIOS", valores, "correo = ?", new String[]{correo});
        db.close();

        Toast.makeText(this, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show();
        finish();
    }
}
