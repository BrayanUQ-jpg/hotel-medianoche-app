package com.example.hotel;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class TaxiActivity extends AppCompatActivity {

    private static final int SOLICITUD_PERMISO_UBICACION = 501;

    // Paquetes de las apps reales (tambien declarados en <queries> del AndroidManifest)
    private static final String PAQUETE_YANGO = "com.yandex.yango";
    private static final String PAQUETE_INDRIVE = "sinet.startup.inDriver";

    private MaterialButton btnAbrirApp;
    private String tripIdExterno = null;

    private RadioButton radioServicioA, radioServicioB, radioPagoEfectivo, radioPagoYape;
    private TextView txtOrigen, txtDestino, txtVehiculo, txtCosto, txtDistancia, txtLabelCosto;
    private LinearLayout layoutOferta;
    private EditText edtOferta;

    private String servicioSeleccionado = "A";
    private String metodoPagoSeleccionado = "efectivo";
    private String tipoVehiculoSeleccionado = "Sedán (1-3 pasajeros)";
    private double multiplicadorVehiculo = 1.0;

    private double origenLat = -8.1116, origenLng = -79.0288;
    private double destinoLat = -8.0967, destinoLng = -79.0350;
    private double distanciaKm = 4.0;
    private double costoBase = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taxi);

        ImageButton btnBack = findViewById(R.id.btnBack);
        CardView cardServicioA = findViewById(R.id.cardServicioA);
        CardView cardServicioB = findViewById(R.id.cardServicioB);
        radioServicioA = findViewById(R.id.radioServicioA);
        radioServicioB = findViewById(R.id.radioServicioB);
        CardView cardPagoEfectivo = findViewById(R.id.cardPagoEfectivo);
        CardView cardPagoYape = findViewById(R.id.cardPagoYape);
        radioPagoEfectivo = findViewById(R.id.radioPagoEfectivo);
        radioPagoYape = findViewById(R.id.radioPagoYape);
        txtOrigen = findViewById(R.id.txtOrigen);
        txtDestino = findViewById(R.id.txtDestino);
        txtVehiculo = findViewById(R.id.txtVehiculo);
        txtCosto = findViewById(R.id.txtCosto);
        txtDistancia = findViewById(R.id.txtDistancia);
        txtLabelCosto = findViewById(R.id.txtLabelCosto);
        layoutOferta = findViewById(R.id.layoutOferta);
        edtOferta = findViewById(R.id.edtOferta);
        LinearLayout rowOrigen = findViewById(R.id.rowOrigen);
        LinearLayout rowDestino = findViewById(R.id.rowDestino);
        LinearLayout rowVehiculo = findViewById(R.id.rowVehiculo);
        MaterialButton btnConfirmar = findViewById(R.id.btnConfirmar);
        btnAbrirApp = findViewById(R.id.btnAbrirApp);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);

        bottomNav.setSelectedItemId(R.id.nav_taxi);
        seleccionarServicio("A");
        seleccionarMetodoPago("efectivo");

        btnBack.setOnClickListener(v -> finish());
        cardServicioA.setOnClickListener(v -> seleccionarServicio("A"));
        cardServicioB.setOnClickListener(v -> seleccionarServicio("B"));
        cardPagoEfectivo.setOnClickListener(v -> seleccionarMetodoPago("efectivo"));
        cardPagoYape.setOnClickListener(v -> seleccionarMetodoPago("yape"));
        rowOrigen.setOnClickListener(v -> mostrarOpcionesOrigen());
        rowDestino.setOnClickListener(v -> pedirDestinoManual());
        rowVehiculo.setOnClickListener(v -> mostrarOpcionesVehiculo());
        btnConfirmar.setOnClickListener(v -> confirmarViaje());
        btnAbrirApp.setOnClickListener(v -> abrirAppExterna());

        solicitarUbicacionActual();
        actualizarCostoEstimado();

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_inicio) {
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
                return true;
            } else if (id == R.id.nav_habitaciones) {
                startActivity(new Intent(this, RoomBookingActivity.class));
                return true;
            } else if (id == R.id.nav_tours) {
                startActivity(new Intent(this, ToursActivity.class));
                return true;
            } else if (id == R.id.nav_perfil) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return id == R.id.nav_taxi;
        });
    }



    private void mostrarOpcionesOrigen() {
        String[] opciones = {"📍 Usar mi ubicación actual (GPS)", "✏️ Escribir otra dirección"};
        new AlertDialog.Builder(this)
                .setTitle("Origen del viaje")
                .setItems(opciones, (dialog, which) -> {
                    if (which == 0) {
                        solicitarUbicacionActual();
                    } else {
                        pedirOrigenManual();
                    }
                })
                .show();
    }

    private void pedirOrigenManual() {
        EditText input = new EditText(this);
        input.setHint("Ej: Av. España 1500, Trujillo");
        input.setText(texto(txtOrigen, ""));
        input.setPadding(dp(20), dp(16), dp(20), dp(8));
        new AlertDialog.Builder(this)
                .setTitle("¿Desde dónde te recogemos?")
                .setView(input)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String direccion = input.getText().toString().trim();
                    if (!direccion.isEmpty()) {
                        txtOrigen.setText("📍 " + direccion);
                        geocodificarYActualizar(direccion, true);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void pedirDestinoManual() {
        EditText input = new EditText(this);
        input.setHint("Ej: Real Plaza, Trujillo");
        String actual = texto(txtDestino, "").replaceFirst("^🏨 ", "");
        input.setText(actual);
        input.setPadding(dp(20), dp(16), dp(20), dp(8));
        new AlertDialog.Builder(this)
                .setTitle("¿A dónde quieres ir?")
                .setView(input)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String direccion = input.getText().toString().trim();
                    if (!direccion.isEmpty()) {
                        txtDestino.setText("🏨 " + direccion);
                        geocodificarYActualizar(direccion, false);
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void mostrarOpcionesVehiculo() {
        String[] opciones = {"Moto taxi (1 pasajero)", "Sedán (1-3 pasajeros)", "SUV / Van (hasta 6 pasajeros)"};
        double[] multiplicadores = {0.65, 1.0, 1.4};
        new AlertDialog.Builder(this)
                .setTitle("Tipo de vehículo")
                .setItems(opciones, (dialog, which) -> {
                    tipoVehiculoSeleccionado = opciones[which];
                    multiplicadorVehiculo = multiplicadores[which];
                    txtVehiculo.setText(tipoVehiculoSeleccionado);
                    actualizarCostoEstimado();
                })
                .show();
    }

    private void solicitarUbicacionActual() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    SOLICITUD_PERMISO_UBICACION);
            return;
        }
        obtenerUbicacionReal();
    }

    @SuppressWarnings("MissingPermission")
    private void obtenerUbicacionReal() {
        try {
            LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (lm == null) {
                txtOrigen.setText("📍 Mi ubicación actual");
                return;
            }
            Location mejor = null;
            for (String proveedor : lm.getProviders(true)) {
                Location loc = lm.getLastKnownLocation(proveedor);
                if (loc != null && (mejor == null || loc.getAccuracy() < mejor.getAccuracy())) {
                    mejor = loc;
                }
            }
            if (mejor != null) {
                origenLat = mejor.getLatitude();
                origenLng = mejor.getLongitude();
                String direccion = direccionDesdeCoordenadas(origenLat, origenLng);
                txtOrigen.setText("📍 " + (direccion != null ? direccion : "Mi ubicación actual"));
            } else {
                txtOrigen.setText("📍 Mi ubicación actual (activa el GPS para más precisión)");
            }
        } catch (SecurityException e) {
            txtOrigen.setText("📍 Mi ubicación actual");
        }
        actualizarCostoEstimado();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SOLICITUD_PERMISO_UBICACION) {
            boolean concedido = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (concedido) {
                obtenerUbicacionReal();
            } else {
                txtOrigen.setText("📍 Mi ubicación actual (permiso denegado, toca aquí para escribirla)");
                actualizarCostoEstimado();
            }
        }
    }

    private String direccionDesdeCoordenadas(double lat, double lng) {
        try {
            Geocoder geocoder = new Geocoder(this, new Locale("es", "PE"));
            List<Address> resultados = geocoder.getFromLocation(lat, lng, 1);
            if (resultados != null && !resultados.isEmpty()) {
                Address a = resultados.get(0);
                StringBuilder sb = new StringBuilder();
                if (a.getThoroughfare() != null) sb.append(a.getThoroughfare());
                if (a.getSubThoroughfare() != null) sb.append(" ").append(a.getSubThoroughfare());
                if (a.getLocality() != null) sb.append(sb.length() > 0 ? ", " : "").append(a.getLocality());
                return sb.length() > 0 ? sb.toString() : a.getAddressLine(0);
            }
        } catch (Exception ignored) {

        }
        return null;
    }


    private void geocodificarYActualizar(String direccion, boolean esOrigen) {
        try {
            Geocoder geocoder = new Geocoder(this, new Locale("es", "PE"));
            List<Address> resultados = geocoder.getFromLocationName(direccion + ", Trujillo, Perú", 1);
            if (resultados != null && !resultados.isEmpty()) {
                Address a = resultados.get(0);
                if (esOrigen) {
                    origenLat = a.getLatitude();
                    origenLng = a.getLongitude();
                } else {
                    destinoLat = a.getLatitude();
                    destinoLng = a.getLongitude();
                }
            }
        } catch (Exception ignored) {

        }
        actualizarCostoEstimado();
    }



    private void actualizarCostoEstimado() {
        distanciaKm = calcularDistanciaKm();
        double tarifaBase = 4.50;
        double porKm = servicioSeleccionado.equals("A") ? 2.30 : 1.90;
        costoBase = (tarifaBase + distanciaKm * porKm) * multiplicadorVehiculo;
        costoBase = Math.max(costoBase, 6.0);

        txtDistancia.setText(String.format(Locale.getDefault(), "≈ %.1f km · %d min aprox.", distanciaKm, minutosEstimados()));

        if (servicioSeleccionado.equals("B")) {
            txtLabelCosto.setText("Precio sugerido (puedes ofertar el tuyo)");
            txtCosto.setText(String.format(Locale.getDefault(), "S/ %.2f", costoBase));
            layoutOferta.setVisibility(android.view.View.VISIBLE);
            if (edtOferta.getText().toString().trim().isEmpty()) {
                edtOferta.setText(String.format(Locale.getDefault(), "%.2f", costoBase));
            }
        } else {
            txtLabelCosto.setText("Costo estimado");
            txtCosto.setText(String.format(Locale.getDefault(), "S/ %.2f", costoBase));
            layoutOferta.setVisibility(android.view.View.GONE);
        }
    }

    private int minutosEstimados() {
        int min = (int) Math.round((distanciaKm / 28.0) * 60) + 2;
        return Math.max(min, 3);
    }


    private double calcularDistanciaKm() {
        double distancia = haversineKm(origenLat, origenLng, destinoLat, destinoLng);
        if (distancia <= 0.05) {

            String clave = texto(txtOrigen, "") + texto(txtDestino, "");
            int hash = Math.abs(clave.hashCode());
            distancia = 2.0 + (hash % 140) / 10.0;
        }
        return Math.min(distancia, 35.0);
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return r * c;
    }



    private void confirmarViaje() {
        String origen = texto(txtOrigen, "Ubicación actual");
        String destino = texto(txtDestino, "Hotel Medianoche");
        String vehiculo = tipoVehiculoSeleccionado;
        String metodoPago = metodoPagoSeleccionado.equals("efectivo") ? "💵 Efectivo" : "📱 Yape";
        String servicio = servicioSeleccionado.equals("A") ? "Yango" : "inDrive";
        boolean esOferta = servicioSeleccionado.equals("B");

        double montoFinal = costoBase;
        if (esOferta) {
            String textoOferta = edtOferta.getText().toString().trim();
            if (textoOferta.isEmpty()) {
                Toast.makeText(this, "Ingresa el monto de tu oferta", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                montoFinal = Double.parseDouble(textoOferta);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "El monto de la oferta no es válido", Toast.LENGTH_SHORT).show();
                return;
            }
            if (montoFinal < 3.0) {
                Toast.makeText(this, "La oferta es muy baja, ningún conductor la aceptará", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        String costo = String.format(Locale.getDefault(), "S/ %.2f", montoFinal);
        String tripId = UUID.randomUUID().toString();

        SQLiteHelper helper = new SQLiteHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("id", tripId);
        valores.put("usuario", SessionManager.obtenerCorreo(this));
        valores.put("servicio", servicio);
        valores.put("metodoPago", metodoPago);
        valores.put("origen", origen);
        valores.put("destino", destino);
        valores.put("vehiculo", vehiculo);
        valores.put("costo", costo);
        valores.put("estado", esOferta ? "Oferta enviada" : "Buscando conductor");
        valores.put("creado", System.currentTimeMillis());

        db.insert("VIAJES", null, valores);
        db.close();

        Intent intent = new Intent(this, SearchingDriverActivity.class);
        intent.putExtra("tripId", tripId);
        intent.putExtra("servicio", servicio);
        intent.putExtra("origen", origen);
        intent.putExtra("destino", destino);
        intent.putExtra("vehiculo", vehiculo);
        intent.putExtra("costo", costo);
        intent.putExtra("montoFinal", montoFinal);
        intent.putExtra("metodoPago", metodoPago);
        intent.putExtra("distanciaKm", distanciaKm);
        intent.putExtra("esOferta", esOferta);
        startActivity(intent);
    }


    private void abrirAppExterna() {
        boolean esYango = servicioSeleccionado.equals("A");
        String servicio = esYango ? "Yango" : "inDrive";
        String paquete = esYango ? PAQUETE_YANGO : PAQUETE_INDRIVE;


        if (tripIdExterno == null) {
            tripIdExterno = UUID.randomUUID().toString();
        }

        SQLiteHelper helper = new SQLiteHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("usuario", SessionManager.obtenerCorreo(this));
        valores.put("servicio", servicio);
        valores.put("metodoPago", "Pago en la app de " + servicio);
        valores.put("origen", texto(txtOrigen, "Ubicación actual"));
        valores.put("destino", texto(txtDestino, "Hotel Medianoche"));
        valores.put("vehiculo", tipoVehiculoSeleccionado);
        valores.put("costo", "Por confirmar en " + servicio);
        valores.put("estado", "Gestionado en " + servicio);

        int filas = db.update("VIAJES", valores, "id = ?", new String[]{tripIdExterno});
        if (filas == 0) {
            valores.put("id", tripIdExterno);
            valores.put("creado", System.currentTimeMillis());
            db.insert("VIAJES", null, valores);
        }
        db.close();

        Intent abrir = getPackageManager().getLaunchIntentForPackage(paquete);
        if (abrir != null) {
            startActivity(abrir);
            return;
        }

        Toast.makeText(this, servicio + " no está instalada. Te llevamos a la Play Store", Toast.LENGTH_LONG).show();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + paquete)));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + paquete)));
        }
    }

    private void seleccionarServicio(String servicio) {
        servicioSeleccionado = servicio;
        radioServicioA.setChecked(servicio.equals("A"));
        radioServicioB.setChecked(servicio.equals("B"));
        btnAbrirApp.setText(servicio.equals("A") ? "Abrir Yango" : "Abrir inDrive");
        actualizarCostoEstimado();
    }

    private void seleccionarMetodoPago(String metodo) {
        metodoPagoSeleccionado = metodo;
        radioPagoEfectivo.setChecked(metodo.equals("efectivo"));
        radioPagoYape.setChecked(metodo.equals("yape"));
    }

    private String texto(TextView view, String fallback) {
        if (view == null || view.getText() == null || view.getText().toString().trim().isEmpty()) return fallback;
        return view.getText().toString().trim();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
