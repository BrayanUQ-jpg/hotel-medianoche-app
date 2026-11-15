package com.example.hotel;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class CallSimuladaActivity extends AppCompatActivity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView txtEstadoLlamada, txtDuracionLlamada;
    private int segundos = 0;
    private boolean contestada = false;
    private boolean colgada = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_simulada);

        txtEstadoLlamada = findViewById(R.id.txtEstadoLlamada);
        txtDuracionLlamada = findViewById(R.id.txtDuracionLlamada);
        TextView txtNombreConductorLlamada = findViewById(R.id.txtNombreConductorLlamada);
        ImageView btnColgar = findViewById(R.id.btnColgar);

        String conductorNombre = getIntent().getStringExtra("conductorNombre");
        txtNombreConductorLlamada.setText(conductorNombre != null ? conductorNombre : "Conductor");

        btnColgar.setOnClickListener(v -> {
            colgada = true;
            Toast.makeText(this, "Llamada finalizada", Toast.LENGTH_SHORT).show();
            finish();
        });

        handler.postDelayed(() -> {
            if (colgada) return;
            contestada = true;
            txtEstadoLlamada.setText("En llamada");
            iniciarCronometro();
        }, 2000);
    }

    private void iniciarCronometro() {
        handler.postDelayed(() -> {
            if (colgada || !contestada) return;
            segundos++;
            int min = segundos / 60;
            int seg = segundos % 60;
            txtDuracionLlamada.setText(String.format(Locale.getDefault(), "%02d:%02d", min, seg));
            iniciarCronometro();
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        colgada = true;
        handler.removeCallbacksAndMessages(null);
    }
}
