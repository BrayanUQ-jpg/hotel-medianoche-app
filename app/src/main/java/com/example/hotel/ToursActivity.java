package com.example.hotel;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

public class ToursActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private MaterialButton btnVerTour1, btnVerTour2, btnVerTour3, btnVerTour4;
    private BottomNavigationView bottomNav;

    private EditText edtBuscarTour;
    private TextView txtSinResultados;
    private View[] tarjetasTour;
    // Texto donde se busca cada tour (nombre + descripción + tipo), en el mismo orden que las tarjetas
    private final String[] textosTour = {
            "Huacas del Sol y de la Luna Descubre la historia de Moche entre impresionantes huacas Guiado",
            "Chan Chan + Huanchaco Descubre la ciudad de adobe más grande de América Guiado",
            "Catarata de Huagapo Aventura y naturaleza en un entorno espectacular Aventura",
            "Laguna de Conache Relájate en la hermosa laguna a orilla del desierto Naturaleza"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tours);


        btnBack = findViewById(R.id.btnBack);
        btnVerTour1 = findViewById(R.id.btnVerTour1);
        btnVerTour2 = findViewById(R.id.btnVerTour2);
        btnVerTour3 = findViewById(R.id.btnVerTour3);
        btnVerTour4 = findViewById(R.id.btnVerTour4);
        bottomNav = findViewById(R.id.bottomNav);

        edtBuscarTour = findViewById(R.id.edtBuscarTour);
        txtSinResultados = findViewById(R.id.txtSinResultados);
        tarjetasTour = new View[]{
                findViewById(R.id.cardTour1),
                findViewById(R.id.cardTour2),
                findViewById(R.id.cardTour3),
                findViewById(R.id.cardTour4)
        };

        // Buscador: filtra los tours mientras el usuario escribe
        edtBuscarTour.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                filtrarTours(s.toString());
            }
        });


        bottomNav.setSelectedItemId(R.id.nav_tours);


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        btnVerTour1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirDetalleTour(
                        "Huacas del Sol y de la Luna",
                        "S/ 120.00",
                        "3 horas",
                        "Guiado",
                        "Descubre la historia de la cultura Moche visitando estas impresionantes huacas de adobe, testigos de una de las civilizaciones más importantes del antiguo Perú. Un recorrido guiado por expertos que te transportará siglos atrás.",
                        R.drawable.huaca_sol
                );
            }
        });

        btnVerTour2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirDetalleTour(
                        "Chan Chan + Huanchaco",
                        "S/ 150.00",
                        "4 horas",
                        "Guiado",
                        "Recorre la ciudadela de adobe más grande de América y conoce el pintoresco balneario de Huanchaco, cuna de los caballitos de totora, una tradición pesquera milenaria.",
                        R.drawable.chanchan
                );
            }
        });

        btnVerTour3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirDetalleTour(
                        "Catarata de Huagapo",
                        "S/ 150.00",
                        "5 horas",
                        "Aventura",
                        "Vive una experiencia de aventura y naturaleza visitando una de las caídas de agua más espectaculares de la región, rodeada de vegetación exuberante y senderos naturales.",
                        R.drawable.cataratawebp
                );
            }
        });

        btnVerTour4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirDetalleTour(
                        "Laguna de Conache",
                        "S/ 130.00",
                        "2 horas",
                        "Naturaleza",
                        "Relájate en esta hermosa laguna ubicada a orillas del desierto, un contraste único de paisajes ideal para desconectar y disfrutar de la tranquilidad de la naturaleza.",
                        R.drawable.conache
                );
            }
        });


        bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(android.view.MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_inicio) {
                    startActivity(new Intent(ToursActivity.this, DashboardActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.nav_habitaciones) {
                    startActivity(new Intent(ToursActivity.this, RoomBookingActivity.class));
                    return true;
                } else if (id == R.id.nav_taxi) {
                    startActivity(new Intent(ToursActivity.this, TaxiActivity.class));
                    return true;
                } else if (id == R.id.nav_tours) {
                    // Ya estamos en Tours
                    return true;
                } else if (id == R.id.nav_perfil) {
                    startActivity(new Intent(ToursActivity.this, ProfileActivity.class));
                    return true;
                }
                return false;
            }
        });
    }

    // Muestra solo los tours que contienen lo que el usuario escribió
    private void filtrarTours(String consulta) {
        String buscar = consulta.toLowerCase().trim();
        int visibles = 0;

        for (int i = 0; i < tarjetasTour.length; i++) {
            if (textosTour[i].toLowerCase().contains(buscar)) {
                tarjetasTour[i].setVisibility(View.VISIBLE); // coincide: se muestra
                visibles++;
            } else {
                tarjetasTour[i].setVisibility(View.GONE);    // no coincide: se oculta
            }
        }

        // Si no quedó ningún tour, se muestra el mensaje
        if (visibles == 0) {
            txtSinResultados.setVisibility(View.VISIBLE);
        } else {
            txtSinResultados.setVisibility(View.GONE);
        }
    }

    private void abrirDetalleTour(String nombre, String precio, String duracion,
                                  String tipo, String descripcion, int imagen) {
        Intent intent = new Intent(ToursActivity.this, TourDetailActivity.class);
        intent.putExtra("nombreTour", nombre);
        intent.putExtra("precioTour", precio);
        intent.putExtra("duracionTour", duracion);
        intent.putExtra("tipoTour", tipo);
        intent.putExtra("descripcionTour", descripcion);
        intent.putExtra("imagenTour", imagen);
        startActivity(intent);
    }
}