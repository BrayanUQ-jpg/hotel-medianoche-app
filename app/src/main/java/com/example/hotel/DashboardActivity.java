package com.example.hotel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {

    private CardView cardHabitacion;
    private CardView cardTaxi;
    private CardView cardTours;
    private CardView cardPerfil;
    private CardView cardPromoVerano;
    private ImageView imgNotification;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        cardHabitacion = findViewById(R.id.cardHabitacion);
        cardTaxi = findViewById(R.id.cardTaxi);
        cardTours = findViewById(R.id.cardTours);
        cardPerfil = findViewById(R.id.cardPerfil);
        cardPromoVerano = findViewById(R.id.cardPromoVerano);
        imgNotification = findViewById(R.id.imgNotification);
        bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setSelectedItemId(R.id.nav_inicio);


        cardHabitacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, RoomBookingActivity.class);
                startActivity(intent);
            }
        });


        cardTaxi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, TaxiActivity.class);
                startActivity(intent);
            }
        });


        cardTours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, ToursActivity.class);
                startActivity(intent);
            }
        });


        cardPerfil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });


        imgNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DashboardActivity.this, "No tienes notificaciones nuevas", Toast.LENGTH_SHORT).show();
            }
        });


        cardPromoVerano.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, PromoDetailActivity.class);
                startActivity(intent);
            }
        });


        bottomNav.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(android.view.MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_inicio) {
                    // Ya estamos en el Dashboard, no hacemos nada
                    return true;
                } else if (id == R.id.nav_habitaciones) {
                    startActivity(new Intent(DashboardActivity.this, RoomBookingActivity.class));
                    return true;
                } else if (id == R.id.nav_taxi) {
                    startActivity(new Intent(DashboardActivity.this, TaxiActivity.class));
                    return true;
                } else if (id == R.id.nav_tours) {
                    startActivity(new Intent(DashboardActivity.this, ToursActivity.class));
                    return true;
                } else if (id == R.id.nav_perfil) {
                    startActivity(new Intent(DashboardActivity.this, ProfileActivity.class));
                    return true;
                }
                return false;
            }
        });
    }
}