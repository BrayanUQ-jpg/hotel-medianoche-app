package com.example.hotel;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {

    private ImageView imgFotoPerfil;
    private TextView txtNombre, txtCorreo, txtTelefono;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ImageButton btnBack = findViewById(R.id.btnBack);
        imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
        FrameLayout frameFotoPerfil = findViewById(R.id.frameFotoPerfil);
        txtNombre = findViewById(R.id.txtNombre);
        txtCorreo = findViewById(R.id.txtCorreo);
        txtTelefono = findViewById(R.id.txtTelefono);
        MaterialButton btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
        MaterialButton btnCerrarSesion = findViewById(R.id.btnCerrarSesion);
        LinearLayout itemMisReservas = findViewById(R.id.itemMisReservas);
        LinearLayout itemMisViajes = findViewById(R.id.itemMisViajes);
        LinearLayout itemMisTours = findViewById(R.id.itemMisTours);
        LinearLayout itemMetodosPago = findViewById(R.id.itemMetodosPago);
        LinearLayout itemNotificaciones = findViewById(R.id.itemNotificaciones);
        LinearLayout itemAyuda = findViewById(R.id.itemAyuda);
        bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setSelectedItemId(R.id.nav_perfil);

        frameFotoPerfil.setOnClickListener(v -> abrirEditarPerfil());
        btnEditarPerfil.setOnClickListener(v -> abrirEditarPerfil());
        btnBack.setOnClickListener(v -> finish());
        itemMisReservas.setOnClickListener(v -> abrirHistorial("reservas"));
        itemMisViajes.setOnClickListener(v -> abrirHistorial("viajes"));
        itemMisTours.setOnClickListener(v -> abrirHistorial("tours"));
        itemMetodosPago.setOnClickListener(v -> mostrarMetodosPago());
        itemNotificaciones.setOnClickListener(v -> abrirAjustesNotificaciones());
        itemAyuda.setOnClickListener(v -> mostrarAyuda());
        btnCerrarSesion.setOnClickListener(v -> mostrarConfirmacionCerrarSesion());

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inicio) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_habitaciones) {
                startActivity(new Intent(this, RoomBookingActivity.class));
                return true;
            } else if (id == R.id.nav_taxi) {
                startActivity(new Intent(this, TaxiActivity.class));
                return true;
            } else if (id == R.id.nav_tours) {
                startActivity(new Intent(this, ToursActivity.class));
                return true;
            }
            return id == R.id.nav_perfil;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatosDeSesion();
    }

    private void abrirEditarPerfil() {
        startActivity(new Intent(this, EditProfileActivity.class));
    }

    private void abrirHistorial(String tipo) {
        Intent intent = new Intent(this, RecordsActivity.class);
        intent.putExtra("tipo", tipo);
        startActivity(intent);
    }

    private void cargarDatosDeSesion() {
        txtNombre.setText(SessionManager.obtenerNombre(this));
        String correo = SessionManager.obtenerCorreo(this);
        txtCorreo.setText(correo == null || correo.isEmpty() ? "Correo no registrado" : correo);
        String telefono = SessionManager.obtenerTelefono(this);
        txtTelefono.setText(telefono == null || telefono.isEmpty() ? "📞 Teléfono no registrado" : "📞 " + telefono);

        String fotoUri = SessionManager.obtenerFotoUri(this);
        if (fotoUri != null) {
            try {
                imgFotoPerfil.setImageURI(android.net.Uri.parse(fotoUri));
            } catch (Exception ignored) {
            }
        }
    }

    private void mostrarMetodosPago() {
        new AlertDialog.Builder(this)
                .setTitle("Métodos de pago disponibles")
                .setMessage("• Yape\n• Plin\n• Tarjeta de crédito o débito\n• Efectivo en recepción\n\nPor seguridad, la app no guarda números de tarjeta.")
                .setPositiveButton("Entendido", null)
                .show();
    }

    private void abrirAjustesNotificaciones() {
        try {
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        } catch (Exception e) {
            startActivity(new Intent(Settings.ACTION_SETTINGS));
        }
    }

    private void mostrarAyuda() {
        new AlertDialog.Builder(this)
                .setTitle("Ayuda y soporte")
                .setMessage("Para asistencia con una reserva, un pago o un viaje, comunícate con la recepción del hotel. Incluye el código que aparece en tu comprobante.")
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private void mostrarConfirmacionCerrarSesion() {
        new AlertDialog.Builder(this)
                .setTitle("Cerrar sesión")
                .setMessage("¿Estás seguro de que deseas cerrar sesión?")
                .setPositiveButton("Sí, cerrar sesión", (dialog, which) -> {
                    SessionManager.cerrarSesion(this);
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}
