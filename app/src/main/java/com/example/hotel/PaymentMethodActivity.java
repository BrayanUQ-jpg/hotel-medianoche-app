package com.example.hotel;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.provider.Settings;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

public class PaymentMethodActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView txtResumenHabitacion, txtResumenFechas, txtResumenNoches, txtResumenTotal;
    private MaterialButton btnConfirmarPago;

    private CardView cardYape, cardPlin, cardTarjeta, cardEfectivo;
    private RadioButton radioYape, radioPlin, radioTarjeta, radioEfectivo;
    private BroadcastReceiver pagoDetectadoReceiver;
    private boolean pagoDetectadoAutomaticamente = false;

    private LinearLayout layoutQR, layoutPendiente;
    private TextView txtNombreMetodoQR, txtNumeroPago;
    private CheckBox chkConfirmoPago;


    private static final String PAQUETE_YAPE = "com.bcp.innovacxion.yapeapp";
    private static final String PAQUETE_PLIN_BBVA = "com.bbva.nxt_peru";

    private MaterialButton btnAbrirAppPago;
    private TextView txtEstadoPago, txtMensajePendiente;


    private String detalleTarjeta = null;
    private boolean pagoTarjetaCompletado = false;

    private String metodoSeleccionado = "yape";
    private boolean permisoNotificacionesSolicitado = false;

    private String nombreHabitacion;
    private double precioNoche;
    private String checkIn;
    private String checkOut;
    private int noches;
    private double total;
    private String tipoReserva = "habitacion";
    private String bookingId;
    private boolean promoAplicada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);

        btnBack = findViewById(R.id.btnBack);
        txtResumenHabitacion = findViewById(R.id.txtResumenHabitacion);
        txtResumenFechas = findViewById(R.id.txtResumenFechas);
        txtResumenNoches = findViewById(R.id.txtResumenNoches);
        txtResumenTotal = findViewById(R.id.txtResumenTotal);
        btnConfirmarPago = findViewById(R.id.btnConfirmarPago);

        cardYape = findViewById(R.id.cardYape);
        cardPlin = findViewById(R.id.cardPlin);
        cardTarjeta = findViewById(R.id.cardTarjeta);
        cardEfectivo = findViewById(R.id.cardEfectivo);

        radioYape = findViewById(R.id.radioYape);
        radioPlin = findViewById(R.id.radioPlin);
        radioTarjeta = findViewById(R.id.radioTarjeta);
        radioEfectivo = findViewById(R.id.radioEfectivo);

        layoutQR = findViewById(R.id.layoutQR);
        layoutPendiente = findViewById(R.id.layoutPendiente);
        txtNombreMetodoQR = findViewById(R.id.txtNombreMetodoQR);
        txtNumeroPago = findViewById(R.id.txtNumeroPago);
        chkConfirmoPago = findViewById(R.id.chkConfirmoPago);
        btnAbrirAppPago = findViewById(R.id.btnAbrirAppPago);
        txtEstadoPago = findViewById(R.id.txtEstadoPago);
        txtMensajePendiente = findViewById(R.id.txtMensajePendiente);

        tipoReserva = getIntent().getStringExtra("tipoReserva");
        if (tipoReserva == null) tipoReserva = "habitacion";
        bookingId = getIntent().getStringExtra("bookingId");
        if (bookingId == null || bookingId.trim().isEmpty()) bookingId = UUID.randomUUID().toString();
        promoAplicada = getIntent().getBooleanExtra("promoAplicada", false);

        nombreHabitacion = getIntent().getStringExtra("nombreHabitacion");
        precioNoche = getIntent().getDoubleExtra("precioNoche", 0.0);
        checkIn = getIntent().getStringExtra("checkIn");
        checkOut = getIntent().getStringExtra("checkOut");
        noches = getIntent().getIntExtra("noches", 0);
        total = getIntent().getDoubleExtra("total", 0.0);

        if (nombreHabitacion != null) txtResumenHabitacion.setText(nombreHabitacion);
        txtResumenFechas.setText(checkIn + " - " + checkOut);
        txtResumenNoches.setText(noches + (tipoReserva.equals("tour") ? (noches == 1 ? " cupo" : " cupos") : (noches == 1 ? " noche" : " noches")));
        txtResumenTotal.setText(String.format(Locale.getDefault(), promoAplicada ? "S/ %.2f  (-20%% promo)" : "S/ %.2f", total));

        cardYape.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seleccionarMetodo("yape");
            }
        });

        cardPlin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seleccionarMetodo("plin");
            }
        });

        cardTarjeta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seleccionarMetodo("tarjeta");
            }
        });

        cardEfectivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seleccionarMetodo("efectivo");
            }
        });

        btnAbrirAppPago.setOnClickListener(v -> abrirAppDePago());

        chkConfirmoPago.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(android.widget.CompoundButton buttonView, boolean isChecked) {
                actualizarEstadoBotonConfirmar();
            }
        });

        seleccionarMetodo("yape");


        registrarReceptorDePago();

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnConfirmarPago.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean esYapePlin = metodoSeleccionado.equals("yape") || metodoSeleccionado.equals("plin");

                if (esYapePlin && !chkConfirmoPago.isChecked()) {
                    Toast.makeText(PaymentMethodActivity.this,
                            "Marca la casilla confirmando que ya realizaste el pago",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (metodoSeleccionado.equals("tarjeta")) {
                    mostrarDialogoTarjeta();
                    return;
                }

                confirmarYContinuar();
            }
        });
    }


    private void confirmarYContinuar() {
        boolean requierePagoInmediato = metodoSeleccionado.equals("efectivo")
                || (metodoSeleccionado.equals("tarjeta") && !pagoTarjetaCompletado);
        String metodoPago = obtenerNombreMetodoPago();

        Intent intent = new Intent(PaymentMethodActivity.this, BookingDetailActivity.class);
        intent.putExtra("bookingId", bookingId);
        intent.putExtra("tipoReserva", tipoReserva);
        intent.putExtra("nombreHabitacion", nombreHabitacion);
        intent.putExtra("precioNoche", precioNoche);
        intent.putExtra("checkIn", checkIn);
        intent.putExtra("checkOut", checkOut);
        intent.putExtra("noches", noches);
        intent.putExtra("total", total);
        intent.putExtra("metodoPago", metodoPago);
        intent.putExtra("pagoCompletado", !requierePagoInmediato);
        startActivity(intent);
        finish();
    }

    private void seleccionarMetodo(String metodo) {
        metodoSeleccionado = metodo;

        radioYape.setChecked(metodo.equals("yape"));
        radioPlin.setChecked(metodo.equals("plin"));
        radioTarjeta.setChecked(metodo.equals("tarjeta"));
        radioEfectivo.setChecked(metodo.equals("efectivo"));

        if (metodo.equals("yape") || metodo.equals("plin")) {
            layoutQR.setVisibility(View.VISIBLE);
            layoutPendiente.setVisibility(View.GONE);
            txtNombreMetodoQR.setText(metodo.equals("yape")
                    ? "Escanea el código QR con Yape"
                    : "Escanea el código QR con Plin");
            txtNumeroPago.setText("918 103 913");
            chkConfirmoPago.setChecked(false);
            chkConfirmoPago.setEnabled(true);
            chkConfirmoPago.setText("Ya realicé el pago");
            btnAbrirAppPago.setText(metodo.equals("yape") ? "Abrir Yape" : "Abrir Plin");
            pagoDetectadoAutomaticamente = false;


            if (!permisoNotificacionesSolicitado) {
                permisoNotificacionesSolicitado = true;
                verificarPermisoNotificaciones();
            }
        } else {
            layoutQR.setVisibility(View.GONE);
            layoutPendiente.setVisibility(View.VISIBLE);
            if (metodo.equals("tarjeta")) {
                txtEstadoPago.setText("💳 Pago con tarjeta");
                txtEstadoPago.setBackgroundColor(Color.parseColor("#E8F0FF"));
                txtEstadoPago.setTextColor(Color.parseColor("#2979FF"));
                txtMensajePendiente.setText("Ingresarás los datos de tu tarjeta al confirmar la reserva.");
            } else {
                txtEstadoPago.setText("⏳ Pago pendiente");
                txtEstadoPago.setBackgroundColor(Color.parseColor("#FFE5E5"));
                txtEstadoPago.setTextColor(Color.parseColor("#FF4D4D"));
                txtMensajePendiente.setText("Podrás pagar al momento del check-in en recepción.");
            }
        }

        actualizarEstadoBotonConfirmar();
    }

    private void actualizarEstadoBotonConfirmar() {
        boolean esYapePlin = metodoSeleccionado.equals("yape") || metodoSeleccionado.equals("plin");
        if (esYapePlin) {
            btnConfirmarPago.setEnabled(chkConfirmoPago.isChecked());
            btnConfirmarPago.setAlpha(chkConfirmoPago.isChecked() ? 1f : 0.5f);
        } else {
            btnConfirmarPago.setEnabled(true);
            btnConfirmarPago.setAlpha(1f);
        }
    }
    private void verificarPermisoNotificaciones() {
        String habilitados = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        boolean tienePermiso = habilitados != null && habilitados.contains(getPackageName());

        if (!tienePermiso) {
            Toast.makeText(this,
                    "Para detectar tu pago automáticamente, activa el acceso a notificaciones para esta app",
                    Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
        }
    }

    private void registrarReceptorDePago() {
        pagoDetectadoReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String metodo = intent.getStringExtra("metodo");
                if (metodo == null) return;

                boolean coincideConSeleccion =
                        (metodo.equals("yape") && metodoSeleccionado.equals("yape")) ||
                                (metodo.equals("plin") && metodoSeleccionado.equals("plin"));

                if (coincideConSeleccion) {
                    pagoDetectadoAutomaticamente = true;
                    chkConfirmoPago.setChecked(true);
                    chkConfirmoPago.setEnabled(false);
                    chkConfirmoPago.setText("✔ Pago detectado automáticamente");
                    Toast.makeText(PaymentMethodActivity.this,
                            "¡Pago detectado! Revisa los datos y confirma la reserva",
                            Toast.LENGTH_SHORT).show();


                }
            }
        };

        IntentFilter filtro = new IntentFilter(YapeNotificationListener.ACCION_PAGO_DETECTADO);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(pagoDetectadoReceiver, filtro, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(pagoDetectadoReceiver, filtro);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (pagoDetectadoReceiver != null) {
            unregisterReceiver(pagoDetectadoReceiver);
        }
    }


    private void abrirAppDePago() {
        boolean esYape = metodoSeleccionado.equals("yape");
        String nombre = esYape ? "Yape" : "Plin";
        String paquete = esYape ? PAQUETE_YAPE : PAQUETE_PLIN_BBVA;

        SQLiteHelper helper = new SQLiteHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();

        ContentValues valores = new ContentValues();
        valores.put("usuario", SessionManager.obtenerCorreo(this));
        valores.put("tipo", tipoReserva);
        valores.put("titulo", nombreHabitacion);
        valores.put("precioUnitario", precioNoche);
        valores.put("checkIn", checkIn);
        valores.put("checkOut", checkOut);
        valores.put("cantidad", noches);
        valores.put("total", total);
        valores.put("metodoPago", obtenerNombreMetodoPago());
        valores.put("pagoCompletado", 0);
        valores.put("estado", "Pendiente de pago");

        // Si la reserva ya existe se actualiza; si no, se inserta como nueva
        int filas = db.update("RESERVAS", valores, "id = ?", new String[]{bookingId});
        if (filas == 0) {
            valores.put("id", bookingId);
            valores.put("creado", System.currentTimeMillis());
            db.insert("RESERVAS", null, valores);
        }
        db.close();

        Intent abrir = getPackageManager().getLaunchIntentForPackage(paquete);
        if (abrir != null) {
            startActivity(abrir);
            return;
        }

        Toast.makeText(this, (esYape ? "Yape" : "La app de BBVA (Plin)") + " no está instalada. Te llevamos a la Play Store", Toast.LENGTH_LONG).show();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + paquete)));
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + paquete)));
        }
    }



    private void mostrarDialogoTarjeta() {
        View vista = getLayoutInflater().inflate(R.layout.dialog_tarjeta, null);
        TextInputLayout tilNumero = vista.findViewById(R.id.tilTarjetaNumero);
        TextInputLayout tilTitular = vista.findViewById(R.id.tilTarjetaTitular);
        TextInputLayout tilVence = vista.findViewById(R.id.tilTarjetaVence);
        TextInputLayout tilCvv = vista.findViewById(R.id.tilTarjetaCvv);
        TextInputEditText edtNumero = vista.findViewById(R.id.edtTarjetaNumero);
        TextInputEditText edtTitular = vista.findViewById(R.id.edtTarjetaTitular);
        TextInputEditText edtVence = vista.findViewById(R.id.edtTarjetaVence);
        TextInputEditText edtCvv = vista.findViewById(R.id.edtTarjetaCvv);


        edtNumero.addTextChangedListener(new TextWatcher() {
            private boolean editando = false;
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override
            public void afterTextChanged(Editable e) {
                if (editando) return;
                editando = true;
                String digitos = e.toString().replaceAll("\\D", "");
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < digitos.length(); i++) {
                    if (i > 0 && i % 4 == 0) sb.append(' ');
                    sb.append(digitos.charAt(i));
                }
                e.replace(0, e.length(), sb.toString());
                editando = false;
            }
        });


        edtVence.addTextChangedListener(new TextWatcher() {
            private boolean editando = false;
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override
            public void afterTextChanged(Editable e) {
                if (editando) return;
                editando = true;
                String digitos = e.toString().replaceAll("\\D", "");
                if (digitos.length() > 4) digitos = digitos.substring(0, 4);
                String texto = digitos.length() > 2
                        ? digitos.substring(0, 2) + "/" + digitos.substring(2)
                        : digitos;
                e.replace(0, e.length(), texto);
                editando = false;
            }
        });

        AlertDialog dialogo = new AlertDialog.Builder(this)
                .setTitle("💳 Pago con tarjeta")
                .setView(vista)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton(String.format(Locale.getDefault(), "Pagar S/ %.2f", total), null)
                .create();
        dialogo.show();


        dialogo.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String numero = textoDe(edtNumero).replaceAll("\\D", "");
            String titular = textoDe(edtTitular);
            String vence = textoDe(edtVence);
            String cvv = textoDe(edtCvv);

            tilNumero.setError(null);
            tilTitular.setError(null);
            tilVence.setError(null);
            tilCvv.setError(null);
            boolean ok = true;

            boolean esAmex = numero.startsWith("34") || numero.startsWith("37");
            int largoEsperado = esAmex ? 15 : 16;
            if (numero.length() != largoEsperado || !luhnValido(numero)) {
                tilNumero.setError("Número de tarjeta no válido");
                ok = false;
            }
            if (titular.length() < 3) {
                tilTitular.setError("Ingresa el nombre del titular");
                ok = false;
            }
            if (!vencimientoValido(vence)) {
                tilVence.setError("Fecha inválida o vencida");
                ok = false;
            }
            if (cvv.length() != (esAmex ? 4 : 3)) {
                tilCvv.setError(esAmex ? "4 dígitos" : "3 dígitos");
                ok = false;
            }
            if (!ok) return;


            detalleTarjeta = "💳 " + marcaTarjeta(numero) + " •••• " + numero.substring(numero.length() - 4);
            pagoTarjetaCompletado = true;
            dialogo.dismiss();
            confirmarYContinuar();
        });
    }

    private String textoDe(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }


    private boolean luhnValido(String numero) {
        int suma = 0;
        boolean duplicar = false;
        for (int i = numero.length() - 1; i >= 0; i--) {
            int d = numero.charAt(i) - '0';
            if (duplicar) {
                d *= 2;
                if (d > 9) d -= 9;
            }
            suma += d;
            duplicar = !duplicar;
        }
        return suma % 10 == 0;
    }

    private boolean vencimientoValido(String texto) {
        if (!texto.matches("\\d{2}/\\d{2}")) return false;
        int mes = Integer.parseInt(texto.substring(0, 2));
        int anio = 2000 + Integer.parseInt(texto.substring(3, 5));
        if (mes < 1 || mes > 12) return false;
        Calendar hoy = Calendar.getInstance();
        int anioActual = hoy.get(Calendar.YEAR);
        int mesActual = hoy.get(Calendar.MONTH) + 1;
        return anio > anioActual || (anio == anioActual && mes >= mesActual);
    }

    private String marcaTarjeta(String numero) {
        if (numero.startsWith("4")) return "Visa";
        if (numero.startsWith("34") || numero.startsWith("37")) return "American Express";
        if (numero.length() >= 2) {
            int dos = Integer.parseInt(numero.substring(0, 2));
            if (dos >= 51 && dos <= 55) return "Mastercard";
        }
        if (numero.length() >= 4) {
            int cuatro = Integer.parseInt(numero.substring(0, 4));
            if (cuatro >= 2221 && cuatro <= 2720) return "Mastercard";
        }
        return "Tarjeta";
    }

    private String obtenerNombreMetodoPago() {
        if (metodoSeleccionado.equals("yape")) {
            return "📱 Yape";
        } else if (metodoSeleccionado.equals("plin")) {
            return "📱 Plin";
        } else if (metodoSeleccionado.equals("tarjeta")) {
            return detalleTarjeta != null ? detalleTarjeta : "💳 Tarjeta de Crédito/Débito";
        } else if (metodoSeleccionado.equals("efectivo")) {
            return "💵 Efectivo (Pago en recepción)";
        }
        return "No especificado";
    }
}