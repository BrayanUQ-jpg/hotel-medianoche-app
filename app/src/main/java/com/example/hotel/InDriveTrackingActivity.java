package com.example.hotel;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class InDriveTrackingActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private MaterialButton btnLlamar, btnChat, btnCancelarViaje;
    private TextView txtVerDetalleAcuerdo, txtPrecioAcordado;
    private TextView txtOrigen, txtDestino, txtVehiculo;
    private String tripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_in_drive_tracking);

        // Conectar variables con el XML
        btnBack = findViewById(R.id.btnBack);
        btnLlamar = findViewById(R.id.btnLlamar);
        btnChat = findViewById(R.id.btnChat);
        btnCancelarViaje = findViewById(R.id.btnCancelarViaje);
        txtVerDetalleAcuerdo = findViewById(R.id.txtVerDetalleAcuerdo);
        txtPrecioAcordado = findViewById(R.id.txtPrecioAcordado);
        txtOrigen = findViewById(R.id.txtOrigen);
        txtDestino = findViewById(R.id.txtDestino);
        txtVehiculo = findViewById(R.id.txtVehiculo);

        // Datos reales enviados desde la pantalla de solicitud de taxi
        tripId = getIntent().getStringExtra("tripId");
        String origen = getIntent().getStringExtra("origen");
        String destino = getIntent().getStringExtra("destino");
        String vehiculo = getIntent().getStringExtra("vehiculo");
        String costo = getIntent().getStringExtra("costo");

        if (origen != null) txtOrigen.setText(origen);
        if (destino != null) txtDestino.setText(destino);
        if (vehiculo != null) txtVehiculo.setText(vehiculo);
        if (costo != null) txtPrecioAcordado.setText(costo);
        actualizarEstadoViaje("Aceptado");

        // Boton regresar
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Boton llamar
        btnLlamar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(InDriveTrackingActivity.this,
                        "Llamando al conductor...",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Boton chat
        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(InDriveTrackingActivity.this,
                        "Abriendo chat con el conductor...",
                        Toast.LENGTH_SHORT).show();
            }
        });


        txtVerDetalleAcuerdo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String precio = txtPrecioAcordado.getText().toString();
                new AlertDialog.Builder(InDriveTrackingActivity.this)
                        .setTitle("Detalle del acuerdo")
                        .setMessage("Precio acordado con el conductor: " + precio +
                                "\n\nEste precio fue negociado directamente y no incluye cargos adicionales.")
                        .setPositiveButton("Entendido", null)
                        .show();
            }
        });


        btnCancelarViaje.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarConfirmacionCancelar();
            }
        });
    }

    private void mostrarConfirmacionCancelar() {
        new AlertDialog.Builder(this)
                .setTitle("Cancelar viaje")
                .setMessage("¿Estás seguro de que deseas cancelar este viaje?")
                .setPositiveButton("Sí, cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        actualizarEstadoViaje("Cancelado");
                        Toast.makeText(InDriveTrackingActivity.this,
                                "Viaje cancelado",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void actualizarEstadoViaje(String estado) {
        if (tripId == null) return;
        SQLiteHelper helper = new SQLiteHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues valores = new ContentValues();
        valores.put("estado", estado);
        db.update("VIAJES", valores, "id = ?", new String[]{tripId});
        db.close();
    }

}