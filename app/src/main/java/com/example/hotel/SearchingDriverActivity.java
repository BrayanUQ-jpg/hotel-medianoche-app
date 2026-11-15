package com.example.hotel;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;
import java.util.Random;

public class SearchingDriverActivity extends AppCompatActivity {


    private static final String[][] CONDUCTORES = {
            {"Carlos Mendoza", "T4X-118", "Toyota Yaris · Blanco", "4.9"},
            {"Luis Torres", "ABC-902", "Hyundai Accent · Gris", "4.7"},
            {"Marco Gutiérrez", "V9Q-345", "Kia Rio · Negro", "4.8"},
            {"Jorge Paredes", "XYZ-771", "Chevrolet Sail · Plata", "4.6"},
            {"Renzo Alvarado", "BTR-556", "Suzuki Swift · Rojo", "4.9"}
    };

    private final Handler handler = new Handler(Looper.getMainLooper());
    private AnimatorSet radarAnimator;
    private boolean cancelado = false;

    private String tripId, servicio, origen, destino, vehiculo, costo, metodoPago;
    private double montoFinal, distanciaKm;
    private boolean esOferta;

    private TextView txtEstado, txtSubEstado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searching_driver);

        tripId = getIntent().getStringExtra("tripId");
        servicio = getIntent().getStringExtra("servicio");
        origen = getIntent().getStringExtra("origen");
        destino = getIntent().getStringExtra("destino");
        vehiculo = getIntent().getStringExtra("vehiculo");
        costo = getIntent().getStringExtra("costo");
        metodoPago = getIntent().getStringExtra("metodoPago");
        montoFinal = getIntent().getDoubleExtra("montoFinal", 0.0);
        distanciaKm = getIntent().getDoubleExtra("distanciaKm", 4.0);
        esOferta = getIntent().getBooleanExtra("esOferta", false);

        ImageView btnCerrar = findViewById(R.id.btnCerrar);
        txtEstado = findViewById(R.id.txtEstado);
        txtSubEstado = findViewById(R.id.txtSubEstado);
        TextView txtResumenOrigenDestino = findViewById(R.id.txtResumenOrigenDestino);
        TextView txtResumenMonto = findViewById(R.id.txtResumenMonto);
        MaterialButton btnCancelarBusqueda = findViewById(R.id.btnCancelarBusqueda);

        txtResumenOrigenDestino.setText(acortar(origen) + "  →  " + acortar(destino));
        txtResumenMonto.setText(costo != null ? costo : String.format(Locale.getDefault(), "S/ %.2f", montoFinal));

        if (esOferta) {
            txtEstado.setText("Enviando tu oferta a conductores cercanos…");
            txtSubEstado.setText("Tu oferta: " + txtResumenMonto.getText());
        }

        iniciarAnimacionRadar();

        btnCerrar.setOnClickListener(v -> cancelarBusqueda());
        btnCancelarBusqueda.setOnClickListener(v -> cancelarBusqueda());

        programarFlujoDeBusqueda();
    }

    private void iniciarAnimacionRadar() {
        View c1 = findViewById(R.id.circulo1);
        View c2 = findViewById(R.id.circulo2);
        View c3 = findViewById(R.id.circulo3);

        radarAnimator = new AnimatorSet();
        radarAnimator.playTogether(
                crearPulso(c1, 0),
                crearPulso(c2, 250),
                crearPulso(c3, 500)
        );
        radarAnimator.start();
    }

    private Animator crearPulso(View vista, long delay) {
        ObjectAnimator escalaX = ObjectAnimator.ofFloat(vista, "scaleX", 0.85f, 1.15f);
        ObjectAnimator escalaY = ObjectAnimator.ofFloat(vista, "scaleY", 0.85f, 1.15f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(vista, "alpha", 0.45f, 0.1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(escalaX, escalaY, alpha);
        set.setDuration(1400);
        set.setStartDelay(delay);
        set.setInterpolator(new LinearInterpolator());
        set.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!cancelado) {
                    vista.setScaleX(0.85f);
                    vista.setScaleY(0.85f);
                    vista.setAlpha(0.45f);
                    crearPulso(vista, 0).start();
                }
            }
        });
        return set;
    }


    private void programarFlujoDeBusqueda() {
        long tiempoBusqueda = 3200 + new Random().nextInt(1800);

        handler.postDelayed(() -> {
            if (cancelado) return;
            if (esOferta) {
                txtEstado.setText("¡Un conductor encontró tu oferta!");
                txtSubEstado.setText("Esperando que confirme tu precio de " + costo + "…");
                handler.postDelayed(this::asignarConductorYContinuar, 2200);
            } else {
                asignarConductorYContinuar();
            }
        }, tiempoBusqueda);
    }

    private void asignarConductorYContinuar() {
        if (cancelado) return;

        String[] conductor = CONDUCTORES[new Random().nextInt(CONDUCTORES.length)];
        String nombreConductor = conductor[0];
        String placa = conductor[1];
        String autoConductor = conductor[2];
        double calificacion = Double.parseDouble(conductor[3]);

        if (tripId != null) {
            SQLiteHelper helper = new SQLiteHelper(this);
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues valores = new ContentValues();
            valores.put("estado", "Conductor asignado");
            valores.put("conductorNombre", nombreConductor);
            valores.put("conductorPlaca", placa);
            valores.put("conductorAuto", autoConductor);
            valores.put("conductorCalificacion", calificacion);
            db.update("VIAJES", valores, "id = ?", new String[]{tripId});
            db.close();
        }

        txtEstado.setText("¡Conductor encontrado!");
        txtSubEstado.setText(nombreConductor + " está en camino");

        handler.postDelayed(() -> {
            if (cancelado) return;
            Intent intent = new Intent(SearchingDriverActivity.this, TripTrackingActivity.class);
            intent.putExtra("tripId", tripId);
            intent.putExtra("servicio", servicio);
            intent.putExtra("origen", origen);
            intent.putExtra("destino", destino);
            intent.putExtra("vehiculo", vehiculo);
            intent.putExtra("costo", costo);
            intent.putExtra("metodoPago", metodoPago);
            intent.putExtra("distanciaKm", distanciaKm);
            intent.putExtra("esOferta", esOferta);
            intent.putExtra("conductorNombre", nombreConductor);
            intent.putExtra("conductorPlaca", placa);
            intent.putExtra("conductorAuto", autoConductor);
            intent.putExtra("conductorCalificacion", calificacion);
            startActivity(intent);
            finish();
        }, 900);
    }

    private void cancelarBusqueda() {
        cancelado = true;
        handler.removeCallbacksAndMessages(null);
        if (tripId != null) {
            SQLiteHelper helper = new SQLiteHelper(this);
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues valores = new ContentValues();
            valores.put("estado", "Cancelado");
            db.update("VIAJES", valores, "id = ?", new String[]{tripId});
            db.close();
        }
        Toast.makeText(this, "Búsqueda cancelada", Toast.LENGTH_SHORT).show();
        finish();
    }

    private String acortar(String texto) {
        if (texto == null) return "";
        return texto.length() > 28 ? texto.substring(0, 28) + "…" : texto;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelado = true;
        handler.removeCallbacksAndMessages(null);
        if (radarAnimator != null) radarAnimator.cancel();
    }
}
