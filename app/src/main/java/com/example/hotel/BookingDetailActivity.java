package com.example.hotel;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.Locale;
import java.util.UUID;

public class BookingDetailActivity extends AppCompatActivity {

    private TextView txtNombreHabitacion, txtPrecioNoche, txtCheckIn, txtCheckOut;
    private TextView txtNoches, txtTotal, txtMetodoPago, txtEstadoPagoDetalle;
    private MaterialButton btnMarcarPagado;

    private boolean pagoCompletado;
    private boolean soloLectura;
    private String bookingId;
    private String tipoReserva = "habitacion";
    private String nombreHabitacion = "Reserva";
    private double precioNoche;
    private String checkIn = "";
    private String checkOut = "";
    private int noches;
    private double total;
    private String metodoPago = "No especificado";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_detail);

        txtNombreHabitacion = findViewById(R.id.txtNombreHabitacion);
        txtPrecioNoche = findViewById(R.id.txtPrecioNoche);
        txtCheckIn = findViewById(R.id.txtCheckIn);
        txtCheckOut = findViewById(R.id.txtCheckOut);
        txtNoches = findViewById(R.id.txtNoches);
        txtTotal = findViewById(R.id.txtTotal);
        txtMetodoPago = findViewById(R.id.txtMetodoPago);
        txtEstadoPagoDetalle = findViewById(R.id.txtEstadoPagoDetalle);
        ImageButton btnBack = findViewById(R.id.btnBack);
        MaterialButton btnVerComprobante = findViewById(R.id.btnVerComprobante);
        MaterialButton btnVerMisReservas = findViewById(R.id.btnVerMisReservas);
        btnMarcarPagado = findViewById(R.id.btnMarcarPagado);

        cargarDatos();
        mostrarDatos();
        actualizarEstadoPago();


        btnVerMisReservas.setText(tipoReserva.equals("tour") ? "Ver mis tours reservados" : "Ver mis reservas");
        btnVerMisReservas.setOnClickListener(v -> irAlHistorialCorrespondiente());

        btnBack.setOnClickListener(v -> finish());
        btnVerComprobante.setOnClickListener(v -> compartirComprobante());
        btnMarcarPagado.setOnClickListener(v -> marcarComoPagado());
    }

    private void cargarDatos() {
        soloLectura = getIntent().getBooleanExtra("soloLectura", false);
        bookingId = getIntent().getStringExtra("bookingId");
        tipoReserva = valor(getIntent().getStringExtra("tipoReserva"), "habitacion");

        String nombreIntent = getIntent().getStringExtra("nombreHabitacion");
        if (nombreIntent != null) {
            nombreHabitacion = nombreIntent;
            precioNoche = getIntent().getDoubleExtra("precioNoche", 0.0);
            checkIn = valor(getIntent().getStringExtra("checkIn"), "");
            checkOut = valor(getIntent().getStringExtra("checkOut"), checkIn);
            noches = getIntent().getIntExtra("noches", 0);
            total = getIntent().getDoubleExtra("total", 0.0);
            metodoPago = valor(getIntent().getStringExtra("metodoPago"), "No especificado");
            pagoCompletado = getIntent().getBooleanExtra("pagoCompletado", false);

            if (bookingId == null || bookingId.trim().isEmpty()) {
                bookingId = UUID.randomUUID().toString();
            }

            guardarReservaLocal();
            if (!soloLectura) guardarReservaEnBD();
        } else {
            cargarReservaLocal();
        }
    }

    private void mostrarDatos() {
        txtNombreHabitacion.setText(nombreHabitacion);
        String unidad = tipoReserva.equals("tour") ? "por persona" : "por noche";
        txtPrecioNoche.setText(String.format(Locale.getDefault(), "S/ %.2f %s", precioNoche, unidad));
        txtCheckIn.setText(checkIn);
        txtCheckOut.setText(checkOut);
        txtNoches.setText(noches + (noches == 1 ? (tipoReserva.equals("tour") ? " cupo" : " noche")
                : (tipoReserva.equals("tour") ? " cupos" : " noches")));
        txtTotal.setText(String.format(Locale.getDefault(), "S/ %.2f", total));
        txtMetodoPago.setText(metodoPago);
    }

    private void guardarReservaEnBD() {
        SQLiteHelper helper = new SQLiteHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("usuario", SessionManager.obtenerCorreo(this));
        valores.put("tipo", tipoReserva == null ? "habitacion" : tipoReserva);
        valores.put("titulo", nombreHabitacion == null ? "Reserva" : nombreHabitacion);
        valores.put("precioUnitario", precioNoche);
        valores.put("checkIn", checkIn);
        valores.put("checkOut", checkOut);
        valores.put("cantidad", noches);
        valores.put("total", total);
        valores.put("metodoPago", metodoPago == null ? "No especificado" : metodoPago);
        valores.put("pagoCompletado", pagoCompletado ? 1 : 0);
        valores.put("estado", pagoCompletado ? "Confirmada" : "Pendiente de pago");


        int filas = db.update("RESERVAS", valores, "id = ?", new String[]{bookingId});
        if (filas == 0) {
            valores.put("id", bookingId);
            valores.put("creado", System.currentTimeMillis());
            db.insert("RESERVAS", null, valores);
        }
        db.close();
    }

    private void marcarComoPagado() {
        pagoCompletado = true;
        actualizarEstadoPago();
        guardarReservaLocal();

        if (bookingId != null) {
            SQLiteHelper helper = new SQLiteHelper(this);
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues valores = new ContentValues();
            valores.put("pagoCompletado", 1);
            valores.put("estado", "Confirmada");
            db.update("RESERVAS", valores, "id = ?", new String[]{bookingId});
            db.close();
        }
        Toast.makeText(this, "Pago registrado correctamente", Toast.LENGTH_SHORT).show();
    }

    private void irAlHistorialCorrespondiente() {
        Intent intent = new Intent(this, RecordsActivity.class);
        intent.putExtra("tipo", tipoReserva.equals("tour") ? "tours" : "reservas");
        startActivity(intent);
        finish();
    }

    private void compartirComprobante() {
        String contenido = "COMPROBANTE - HOTEL MEDIANOCHE\n\n"
                + nombreHabitacion + "\n"
                + "Fecha: " + checkIn + (checkOut.equals(checkIn) ? "" : " al " + checkOut) + "\n"
                + "Cantidad: " + noches + "\n"
                + String.format(Locale.getDefault(), "Total: S/ %.2f\n", total)
                + "Método: " + metodoPago + "\n"
                + "Estado: " + (pagoCompletado ? "Pagado" : "Pendiente") + "\n"
                + "Código: " + valor(bookingId, "Sin código");

        Intent compartir = new Intent(Intent.ACTION_SEND);
        compartir.setType("text/plain");
        compartir.putExtra(Intent.EXTRA_SUBJECT, "Comprobante de reserva");
        compartir.putExtra(Intent.EXTRA_TEXT, contenido);
        startActivity(Intent.createChooser(compartir, "Compartir comprobante"));
    }

    private void actualizarEstadoPago() {
        if (pagoCompletado) {
            txtEstadoPagoDetalle.setText("✔ Completado");
            txtEstadoPagoDetalle.setBackgroundColor(0xFFE6F9EE);
            txtEstadoPagoDetalle.setTextColor(0xFF34C759);
            btnMarcarPagado.setVisibility(View.GONE);
        } else {
            txtEstadoPagoDetalle.setText("⏳ Pendiente");
            txtEstadoPagoDetalle.setBackgroundColor(0xFFFFE5E5);
            txtEstadoPagoDetalle.setTextColor(0xFFFF4D4D);
            btnMarcarPagado.setVisibility(soloLectura ? View.GONE : View.VISIBLE);
        }
    }

    private void guardarReservaLocal() {
        SharedPreferences.Editor editor = getSharedPreferences("reservas", MODE_PRIVATE).edit();
        editor.putBoolean("res_existe", true);
        editor.putString("res_id", bookingId);
        editor.putString("res_tipo", tipoReserva);
        editor.putString("res_habitacion", nombreHabitacion);
        editor.putLong("res_precioNoche", Double.doubleToLongBits(precioNoche));
        editor.putString("res_checkIn", checkIn);
        editor.putString("res_checkOut", checkOut);
        editor.putInt("res_noches", noches);
        editor.putLong("res_total", Double.doubleToLongBits(total));
        editor.putString("res_metodoPago", metodoPago);
        editor.putBoolean("res_pagoCompletado", pagoCompletado);
        editor.apply();
    }

    private void cargarReservaLocal() {
        SharedPreferences prefs = getSharedPreferences("reservas", MODE_PRIVATE);
        bookingId = prefs.getString("res_id", UUID.randomUUID().toString());
        tipoReserva = prefs.getString("res_tipo", "habitacion");
        nombreHabitacion = prefs.getString("res_habitacion", "Reserva");
        precioNoche = Double.longBitsToDouble(prefs.getLong("res_precioNoche", 0));
        checkIn = prefs.getString("res_checkIn", "");
        checkOut = prefs.getString("res_checkOut", "");
        noches = prefs.getInt("res_noches", 0);
        total = Double.longBitsToDouble(prefs.getLong("res_total", 0));
        metodoPago = prefs.getString("res_metodoPago", "No especificado");
        pagoCompletado = prefs.getBoolean("res_pagoCompletado", false);
    }

    private String valor(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
