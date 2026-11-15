package com.example.hotel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class PromoDetailActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private MaterialButton btnReservarConPromo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_promo_detail);

        btnBack = findViewById(R.id.btnBack);
        btnReservarConPromo = findViewById(R.id.btnReservarConPromo);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnReservarConPromo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(PromoDetailActivity.this, RoomBookingActivity.class);
                intent.putExtra("promoVeranoActiva", true);
                startActivity(intent);
                finish();
            }
        });
    }
}