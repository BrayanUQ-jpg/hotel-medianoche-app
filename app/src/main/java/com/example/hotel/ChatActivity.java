package com.example.hotel;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.Random;

public class ChatActivity extends AppCompatActivity {

    private static final String[] RESPUESTAS_CONDUCTOR = {
            "Claro, ya voy en camino 🚕",
            "Estoy a unos minutos de llegar",
            "Perfecto, te espero en la puerta",
            "Entendido, gracias por avisarme",
            "Ya casi llego, gracias por tu paciencia"
    };

    private LinearLayout contenedorMensajes;
    private ScrollView scrollMensajes;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private int siguienteRespuesta = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        ImageButton btnBack = findViewById(R.id.btnBack);
        TextView txtNombreConductorChat = findViewById(R.id.txtNombreConductorChat);
        contenedorMensajes = findViewById(R.id.contenedorMensajes);
        scrollMensajes = findViewById(R.id.scrollMensajes);
        EditText edtMensaje = findViewById(R.id.edtMensaje);
        MaterialButton btnEnviarMensaje = findViewById(R.id.btnEnviarMensaje);

        String conductorNombre = getIntent().getStringExtra("conductorNombre");
        txtNombreConductorChat.setText(conductorNombre != null ? conductorNombre : "Conductor");

        agregarMensaje("Hola, ya estoy en camino hacia el punto de recojo 🚗", false);

        btnBack.setOnClickListener(v -> finish());
        btnEnviarMensaje.setOnClickListener(v -> {
            String texto = edtMensaje.getText().toString().trim();
            if (texto.isEmpty()) return;
            agregarMensaje(texto, true);
            edtMensaje.setText("");
            handler.postDelayed(() -> {
                agregarMensaje(RESPUESTAS_CONDUCTOR[siguienteRespuesta % RESPUESTAS_CONDUCTOR.length], false);
                siguienteRespuesta++;
            }, 1200 + new Random().nextInt(900));
        });
    }

    private void agregarMensaje(String texto, boolean esMio) {
        TextView burbuja = new TextView(this);
        burbuja.setText(texto);
        burbuja.setTextSize(14);
        burbuja.setTextColor(esMio ? Color.WHITE : Color.parseColor("#1A1A1A"));
        burbuja.setPadding(dp(14), dp(10), dp(14), dp(10));

        GradientDrawable fondo = new GradientDrawable();
        fondo.setCornerRadius(dp(16));
        fondo.setColor(esMio ? Color.parseColor("#2979FF") : Color.WHITE);
        burbuja.setBackground(fondo);
        burbuja.setElevation(esMio ? 0f : dp(1));

        LinearLayout fila = new LinearLayout(this);
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setGravity(esMio ? Gravity.END : Gravity.START);
        LinearLayout.LayoutParams filaParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        filaParams.setMargins(0, 0, 0, dp(10));
        fila.setLayoutParams(filaParams);

        LinearLayout.LayoutParams burbujaParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        burbujaParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        burbuja.setMaxWidth(dp(240));
        burbuja.setLayoutParams(burbujaParams);

        fila.addView(burbuja);
        contenedorMensajes.addView(fila);

        scrollMensajes.post(() -> scrollMensajes.fullScroll(android.view.View.FOCUS_DOWN));
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
