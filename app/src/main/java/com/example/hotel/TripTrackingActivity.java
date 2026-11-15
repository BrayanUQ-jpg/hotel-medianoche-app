package com.example.hotel;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import android.content.Intent;
import java.util.Locale;

public class TripTrackingActivity extends AppCompatActivity {

    private String tripId, conductorNombre, conductorPlaca, conductorAuto;
    private double distanciaKm;
    private int minutosRestantes;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ValueAnimator animadorAuto;
    private ImageView iconoAuto;
    private FrameLayout mapaContainer;
    private TextView txtEtaOverlay, txtEstadoViaje;
    private boolean llego = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_tracking);

        ImageButton btnBack = findViewById(R.id.btnBack);
        MaterialButton btnLlamar = findViewById(R.id.btnLlamar);
        MaterialButton btnChat = findViewById(R.id.btnChat);
        MaterialButton btnCancelarViaje = findViewById(R.id.btnCancelarViaje);
        TextView txtIdViaje = findViewById(R.id.txtIdViaje);
        TextView txtOrigen = findViewById(R.id.txtOrigen);
        TextView txtDestino = findViewById(R.id.txtDestino);
        TextView txtVehiculo = findViewById(R.id.txtVehiculo);
        TextView txtCosto = findViewById(R.id.txtCosto);
        TextView txtNombreConductor = findViewById(R.id.txtNombreConductor);
        TextView txtCalificacionConductor = findViewById(R.id.txtCalificacionConductor);
        TextView txtAutoConductor = findViewById(R.id.txtAutoConductor);
        TextView txtPlacaConductor = findViewById(R.id.txtPlacaConductor);
        txtEtaOverlay = findViewById(R.id.txtEtaOverlay);
        txtEstadoViaje = findViewById(R.id.txtEstadoViaje);
        iconoAuto = findViewById(R.id.iconoAuto);
        mapaContainer = findViewById(R.id.mapaContainer);

        tripId = getIntent().getStringExtra("tripId");
        String origen = getIntent().getStringExtra("origen");
        String destino = getIntent().getStringExtra("destino");
        String vehiculo = getIntent().getStringExtra("vehiculo");
        String costo = getIntent().getStringExtra("costo");
        distanciaKm = getIntent().getDoubleExtra("distanciaKm", 4.0);
        conductorNombre = getIntent().getStringExtra("conductorNombre");
        conductorPlaca = getIntent().getStringExtra("conductorPlaca");
        conductorAuto = getIntent().getStringExtra("conductorAuto");
        double calificacion = getIntent().getDoubleExtra("conductorCalificacion", 4.8);

        if (tripId != null) txtIdViaje.setText("ID #" + tripId.substring(0, Math.min(8, tripId.length())).toUpperCase());
        if (origen != null) txtOrigen.setText(origen);
        if (destino != null) txtDestino.setText(destino);
        if (vehiculo != null) txtVehiculo.setText(vehiculo);
        if (costo != null) txtCosto.setText(costo);
        if (conductorNombre != null) txtNombreConductor.setText(conductorNombre);
        if (conductorPlaca != null) txtPlacaConductor.setText(conductorPlaca);
        if (conductorAuto != null) txtAutoConductor.setText(conductorAuto);
        txtCalificacionConductor.setText(String.format(Locale.getDefault(), "★ %.1f", calificacion));

        minutosRestantes = Math.max((int) Math.round((distanciaKm / 28.0) * 60) + 2, 3);
        actualizarEta();

        actualizarEstado("En camino");
        iniciarSeguimientoEnMapa();
        iniciarCuentaRegresiva();

        btnBack.setOnClickListener(v -> finish());
        btnLlamar.setOnClickListener(v -> {
            Intent intent = new Intent(this, CallSimuladaActivity.class);
            intent.putExtra("conductorNombre", conductorNombre);
            startActivity(intent);
        });
        btnChat.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("conductorNombre", conductorNombre);
            startActivity(intent);
        });
        btnCancelarViaje.setOnClickListener(v -> mostrarConfirmacionCancelar());
    }


    private void iniciarSeguimientoEnMapa() {
        mapaContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mapaContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int anchoDisponible = mapaContainer.getWidth() - iconoAuto.getWidth() - dp(46);
                if (anchoDisponible <= 0) return;

                long duracionTotal = Math.max(minutosRestantes * 1000L, 6000L);
                animadorAuto = ValueAnimator.ofFloat(0f, 1f);
                animadorAuto.setDuration(duracionTotal);
                animadorAuto.addUpdateListener(animation -> {
                    float fraccion = (float) animation.getAnimatedValue();
                    iconoAuto.setTranslationX(fraccion * anchoDisponible);
                });
                animadorAuto.start();
            }
        });
    }

    private void iniciarCuentaRegresiva() {
        handler.postDelayed(this::disminuirTiempo, 4000);
    }

    private void disminuirTiempo() {
        if (llego) return;
        minutosRestantes = Math.max(minutosRestantes - 1, 0);
        actualizarEta();
        if (minutosRestantes <= 0) {
            llego = true;
            txtEstadoViaje.setText("Conductor llegó");
            txtEtaOverlay.setText("¡Llegó!");
            actualizarEstado("Conductor en el punto de recojo");
        } else {
            handler.postDelayed(this::disminuirTiempo, 4000);
        }
    }

    private void actualizarEta() {
        txtEtaOverlay.setText("ETA " + minutosRestantes + " min");
    }

    private void mostrarConfirmacionCancelar() {
        new AlertDialog.Builder(this)
                .setTitle("Cancelar viaje")
                .setMessage("¿Estás seguro de que deseas cancelar este viaje?")
                .setPositiveButton("Sí, cancelar", (dialog, which) -> {
                    actualizarEstado("Cancelado");
                    android.widget.Toast.makeText(this, "Viaje cancelado", android.widget.Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void actualizarEstado(String estado) {
        if (tripId == null) return;
        SQLiteHelper helper = new SQLiteHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("estado", estado);
        db.update("VIAJES", valores, "id = ?", new String[]{tripId});
        db.close();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (animadorAuto != null) animadorAuto.cancel();
    }
}
