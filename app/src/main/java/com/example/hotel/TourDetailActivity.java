package com.example.hotel;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.Calendar;
import java.util.Locale;

public class TourDetailActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageView imgTour;
    private TextView txtNombreTour, txtPrecioTour, txtDuracion, txtTipo;
    private TextView txtDescripcionCompleta, txtPrecioBarra;
    private MaterialButton btnReservarTour;
    private Spinner sp1;

    private String nombreTour;
    private double precioTour;
    private int personasSeleccionadas = 1;
    private final Calendar fechaTour = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tour_detail);

        btnBack = findViewById(R.id.btnBack);
        imgTour = findViewById(R.id.imgTour);
        txtNombreTour = findViewById(R.id.txtNombreTour);
        txtPrecioTour = findViewById(R.id.txtPrecioTour);
        txtDuracion = findViewById(R.id.txtDuracion);
        txtTipo = findViewById(R.id.txtTipo);
        txtDescripcionCompleta = findViewById(R.id.txtDescripcionCompleta);
        txtPrecioBarra = findViewById(R.id.txtPrecioBarra);
        btnReservarTour = findViewById(R.id.btnReservarTour);
        sp1 = findViewById(R.id.spPersonas);

        nombreTour = getIntent().getStringExtra("nombreTour");
        String precio = getIntent().getStringExtra("precioTour");
        String duracion = getIntent().getStringExtra("duracionTour");
        String tipo = getIntent().getStringExtra("tipoTour");
        String descripcion = getIntent().getStringExtra("descripcionTour");
        int imagenTour = getIntent().getIntExtra("imagenTour", R.drawable.hotelm);

        // Fecha por defecto: mañana
        fechaTour.add(Calendar.DAY_OF_MONTH, 1);

        precioTour = parsearPrecio(precio);

        if (nombreTour != null) txtNombreTour.setText(nombreTour);
        if (precio != null) {
            txtPrecioTour.setText(precio);
            txtPrecioBarra.setText(precio);
        }
        if (duracion != null) txtDuracion.setText("⏱ " + duracion);
        if (tipo != null) txtTipo.setText("👥 " + tipo);
        if (descripcion != null) txtDescripcionCompleta.setText(descripcion);
        imgTour.setImageResource(imagenTour);


        final String[] personas = {"1", "2", "3", "4", "5", "6"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, personas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp1.setAdapter(adapter);

        sp1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                personasSeleccionadas = position + 1;
                double totalTour = precioTour * personasSeleccionadas;
                txtPrecioBarra.setText(String.format(Locale.getDefault(), "S/ %.2f", totalTour));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnReservarTour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mostrarSelectorFecha();
            }
        });
    }

    private double parsearPrecio(String precio) {
        if (precio == null) return 0.0;
        String soloNumeros = precio.replaceAll("[^0-9.]", "");
        try {
            return Double.parseDouble(soloNumeros);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private void mostrarSelectorFecha() {
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        fechaTour.set(year, month, dayOfMonth);
                        irAPago();
                    }
                },
                fechaTour.get(Calendar.YEAR),
                fechaTour.get(Calendar.MONTH),
                fechaTour.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMinDate(System.currentTimeMillis());
        dialog.show();
    }

    private void irAPago() {
        java.text.SimpleDateFormat formato = new java.text.SimpleDateFormat("dd MMM yyyy", new Locale("es", "ES"));
        String fechaFormateada = formato.format(fechaTour.getTime());

        // Reutilizamos la misma pantalla de método de pago que usan las habitaciones
        Intent intent = new Intent(TourDetailActivity.this, PaymentMethodActivity.class);
        intent.putExtra("tipoReserva", "tour");
        intent.putExtra("nombreHabitacion", nombreTour);
        intent.putExtra("precioNoche", precioTour);
        intent.putExtra("checkIn", fechaFormateada);
        intent.putExtra("checkOut", fechaFormateada);
        intent.putExtra("noches", personasSeleccionadas);
        intent.putExtra("total", precioTour * personasSeleccionadas);
        startActivity(intent);
    }
}