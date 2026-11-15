package com.example.hotel;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class RoomBookingActivity extends AppCompatActivity {

    private TextView txtCheckIn, txtCheckOut, txtNoches, txtNochesResumen, txtTotal;
    private RadioButton radioDeluxe, radioSuperior, radioEstandar;
    private final Calendar calendarCheckIn = Calendar.getInstance();
    private final Calendar calendarCheckOut = Calendar.getInstance();
    private final double precioDeluxe = 250.0;
    private final double precioSuperior = 180.0;
    private final double precioEstandar = 120.0;
    private String nombreHabitacionSeleccionada = "Habitación Deluxe";
    private double precioHabitacionSeleccionada = precioDeluxe;
    private final SimpleDateFormat formatoFecha = new SimpleDateFormat("dd MMM yyyy", new Locale("es", "PE"));
    private boolean promoVeranoActiva;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_booking);

        limpiarHora(calendarCheckIn);
        calendarCheckOut.setTimeInMillis(calendarCheckIn.getTimeInMillis());
        calendarCheckOut.add(Calendar.DAY_OF_MONTH, 3);
        promoVeranoActiva = getIntent().getBooleanExtra("promoVeranoActiva", false);

        txtCheckIn = findViewById(R.id.txtCheckIn);
        txtCheckOut = findViewById(R.id.txtCheckOut);
        txtNoches = findViewById(R.id.txtNoches);
        txtNochesResumen = findViewById(R.id.txtNochesResumen);
        txtTotal = findViewById(R.id.txtTotal);

        CardView cardDeluxe = findViewById(R.id.cardDeluxe);
        CardView cardSuperior = findViewById(R.id.cardSuperior);
        CardView cardEstandar = findViewById(R.id.cardEstandar);
        radioDeluxe = findViewById(R.id.radioDeluxe);
        radioSuperior = findViewById(R.id.radioSuperior);
        radioEstandar = findViewById(R.id.radioEstandar);
        MaterialButton btnContinuar = findViewById(R.id.btnContinuar);

        actualizarTextosFecha();
        seleccionarHabitacion("deluxe");

        txtCheckIn.setOnClickListener(v -> mostrarDatePicker(true));
        txtCheckOut.setOnClickListener(v -> mostrarDatePicker(false));
        cardDeluxe.setOnClickListener(v -> seleccionarHabitacion("deluxe"));
        cardSuperior.setOnClickListener(v -> seleccionarHabitacion("superior"));
        cardEstandar.setOnClickListener(v -> seleccionarHabitacion("estandar"));

        btnContinuar.setOnClickListener(v -> {
            int noches = calcularNoches();
            if (noches <= 0) {
                Toast.makeText(this, "La fecha de salida debe ser posterior a la fecha de ingreso", Toast.LENGTH_SHORT).show();
                return;
            }

            double total = calcularTotal(noches);
            Intent intent = new Intent(this, PaymentMethodActivity.class);
            intent.putExtra("tipoReserva", "habitacion");
            intent.putExtra("nombreHabitacion", nombreHabitacionSeleccionada);
            intent.putExtra("precioNoche", precioHabitacionSeleccionada);
            intent.putExtra("checkIn", formatoFecha.format(calendarCheckIn.getTime()));
            intent.putExtra("checkOut", formatoFecha.format(calendarCheckOut.getTime()));
            intent.putExtra("noches", noches);
            intent.putExtra("total", total);
            intent.putExtra("promoAplicada", promoVeranoActiva && noches >= 3);
            startActivity(intent);
        });
    }

    private void seleccionarHabitacion(String habitacion) {
        radioDeluxe.setChecked(habitacion.equals("deluxe"));
        radioSuperior.setChecked(habitacion.equals("superior"));
        radioEstandar.setChecked(habitacion.equals("estandar"));

        if (habitacion.equals("deluxe")) {
            nombreHabitacionSeleccionada = "Habitación Deluxe";
            precioHabitacionSeleccionada = precioDeluxe;
        } else if (habitacion.equals("superior")) {
            nombreHabitacionSeleccionada = "Habitación Superior";
            precioHabitacionSeleccionada = precioSuperior;
        } else {
            nombreHabitacionSeleccionada = "Habitación Estándar";
            precioHabitacionSeleccionada = precioEstandar;
        }
        actualizarTotal();
    }

    private void mostrarDatePicker(final boolean esCheckIn) {
        Calendar base = esCheckIn ? calendarCheckIn : calendarCheckOut;
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar seleccionada = esCheckIn ? calendarCheckIn : calendarCheckOut;
                        seleccionada.set(year, month, dayOfMonth);
                        limpiarHora(seleccionada);

                        if (esCheckIn && !calendarCheckOut.after(calendarCheckIn)) {
                            calendarCheckOut.setTimeInMillis(calendarCheckIn.getTimeInMillis());
                            calendarCheckOut.add(Calendar.DAY_OF_MONTH, 1);
                        }

                        actualizarTextosFecha();
                        actualizarTotal();
                    }
                },
                base.get(Calendar.YEAR),
                base.get(Calendar.MONTH),
                base.get(Calendar.DAY_OF_MONTH)
        );

        Calendar minimo = Calendar.getInstance();
        limpiarHora(minimo);
        if (!esCheckIn) {
            minimo.setTimeInMillis(calendarCheckIn.getTimeInMillis());
            minimo.add(Calendar.DAY_OF_MONTH, 1);
        }
        dialog.getDatePicker().setMinDate(minimo.getTimeInMillis());
        dialog.show();
    }

    private int calcularNoches() {
        long diffMillis = calendarCheckOut.getTimeInMillis() - calendarCheckIn.getTimeInMillis();
        return (int) (diffMillis / 86_400_000L);
    }

    private double calcularTotal(int noches) {
        double subtotal = noches * precioHabitacionSeleccionada;
        return promoVeranoActiva && noches >= 3 ? subtotal * 0.80 : subtotal;
    }

    private void actualizarTextosFecha() {
        txtCheckIn.setText(formatoFecha.format(calendarCheckIn.getTime()));
        txtCheckOut.setText(formatoFecha.format(calendarCheckOut.getTime()));
    }

    private void actualizarTotal() {
        int noches = calcularNoches();
        if (noches <= 0) {
            txtNoches.setText("🌙 Fechas inválidas");
            txtNochesResumen.setText("Noches: -");
            txtTotal.setText("Total: S/ 0.00");
            return;
        }

        double total = calcularTotal(noches);
        txtNoches.setText("🌙 " + noches + (noches == 1 ? " noche" : " noches"));
        txtNochesResumen.setText("Noches: " + noches);
        String sufijo = promoVeranoActiva && noches >= 3 ? "  (-20% promo)" : "";
        txtTotal.setText(String.format(Locale.getDefault(), "Total: S/ %.2f%s", total, sufijo));
    }

    private void limpiarHora(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }
}
