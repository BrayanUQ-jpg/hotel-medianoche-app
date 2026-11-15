package com.example.hotel;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class RecordsActivity extends AppCompatActivity {

    private TextView txtTitulo, txtVacio;
    private Spinner sp1;
    private ListView lv1;
    private SQLiteHelper helper;

    private String tipo;
    private String[] filtros;
    private String filtroSeleccionado = "Todos";


    private ArrayList<String> ids = new ArrayList<>();
    private ArrayList<String> tipos = new ArrayList<>();
    private ArrayList<String> titulos = new ArrayList<>();
    private ArrayList<Double> precios = new ArrayList<>();
    private ArrayList<String> checkIns = new ArrayList<>();
    private ArrayList<String> checkOuts = new ArrayList<>();
    private ArrayList<Integer> cantidades = new ArrayList<>();
    private ArrayList<Double> totales = new ArrayList<>();
    private ArrayList<String> metodos = new ArrayList<>();
    private ArrayList<Boolean> pagados = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        ImageButton btnBack = findViewById(R.id.btnBack);
        txtTitulo = findViewById(R.id.txtTitulo);
        txtVacio = findViewById(R.id.txtVacio);
        sp1 = findViewById(R.id.spFiltro);
        lv1 = findViewById(R.id.lvRegistros);
        helper = new SQLiteHelper(this);

        tipo = getIntent().getStringExtra("tipo");
        if (tipo == null) tipo = "reservas";

        if (tipo.equals("viajes")) {
            txtTitulo.setText("Mis viajes en taxi");
            filtros = new String[]{"Todos", "Cancelados", "Realizados"};
        } else if (tipo.equals("tours")) {
            txtTitulo.setText("Mis tours reservados");
            filtros = new String[]{"Todos", "Pago completado", "Pago pendiente"};
        } else {
            txtTitulo.setText("Mis reservas");
            filtros = new String[]{"Todos", "Pago completado", "Pago pendiente"};
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        ArrayAdapter<String> adapterFiltro = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filtros);
        adapterFiltro.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp1.setAdapter(adapterFiltro);

        sp1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filtroSeleccionado = filtros[position];
                cargarRegistros();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!tipo.equals("viajes")) {
                    abrirDetalleReserva(position);
                }
            }
        });
    }

    private void cargarRegistros() {
        limpiarListas();
        ArrayList<String> textos = new ArrayList<>();
        String correo = SessionManager.obtenerCorreo(this);

        if (correo.isEmpty()) {
            mostrarLista(textos);
            mostrarVacio("Inicia sesión para consultar tu historial");
            return;
        }

        SQLiteDatabase db = helper.getReadableDatabase();
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("es", "PE"));

        if (tipo.equals("viajes")) {
            String sql = "SELECT servicio, origen, destino, costo, metodoPago, estado, creado FROM VIAJES WHERE usuario = ?";
            if (filtroSeleccionado.equals("Cancelados")) {
                sql += " AND estado = 'Cancelado'";
            } else if (filtroSeleccionado.equals("Realizados")) {
                sql += " AND estado <> 'Cancelado'";
            }
            sql += " ORDER BY creado DESC";

            Cursor cursor = db.rawQuery(sql, new String[]{correo});
            while (cursor.moveToNext()) {
                String servicio = valor(cursor.getString(0), "Taxi");
                String origen = valor(cursor.getString(1), "Origen no registrado");
                String destino = valor(cursor.getString(2), "Destino no registrado");
                String costo = valor(cursor.getString(3), "Costo por confirmar");
                String metodo = valor(cursor.getString(4), "Pago no especificado");
                String estado = valor(cursor.getString(5), "Registrado");
                String fecha = formatoFecha.format(new Date(cursor.getLong(6)));

                textos.add(servicio + "\n" + origen + "\n→ " + destino + "\n"
                        + costo + " · " + metodo + "\nEstado: " + estado + "\n" + fecha);
            }
            cursor.close();
        } else {
            String sql = "SELECT id, tipo, titulo, precioUnitario, checkIn, checkOut, cantidad, total, metodoPago, pagoCompletado, creado FROM RESERVAS WHERE usuario = ?";
            if (tipo.equals("tours")) {
                sql += " AND tipo = 'tour'";
            } else {
                sql += " AND tipo <> 'tour'";
            }
            if (filtroSeleccionado.equals("Pago completado")) {
                sql += " AND pagoCompletado = 1";
            } else if (filtroSeleccionado.equals("Pago pendiente")) {
                sql += " AND pagoCompletado = 0";
            }
            sql += " ORDER BY creado DESC";

            Cursor cursor = db.rawQuery(sql, new String[]{correo});
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                String tipoReserva = valor(cursor.getString(1), "habitacion");
                String titulo = valor(cursor.getString(2), "Reserva");
                double precio = cursor.getDouble(3);
                String checkIn = valor(cursor.getString(4), "Fecha no registrada");
                String checkOut = valor(cursor.getString(5), checkIn);
                int cantidad = cursor.getInt(6);
                double total = cursor.getDouble(7);
                String metodo = valor(cursor.getString(8), "Pago no especificado");
                boolean pagado = cursor.getInt(9) == 1;
                String fecha = formatoFecha.format(new Date(cursor.getLong(10)));

                ids.add(id);
                tipos.add(tipoReserva);
                titulos.add(titulo);
                precios.add(precio);
                checkIns.add(checkIn);
                checkOuts.add(checkOut);
                cantidades.add(cantidad);
                totales.add(total);
                metodos.add(metodo);
                pagados.add(pagado);

                textos.add(titulo + "\n"
                        + checkIn + (checkOut.equals(checkIn) ? "" : " - " + checkOut) + "\n"
                        + "Total: S/ " + String.format(Locale.getDefault(), "%.2f", total) + " · " + metodo + "\n"
                        + (pagado ? "Pago completado" : "Pago pendiente") + "\n"
                        + fecha);
            }
            cursor.close();
        }

        mostrarLista(textos);

        if (textos.isEmpty()) {
            String mensaje = tipo.equals("viajes") ? "Aún no tienes viajes registrados"
                    : tipo.equals("tours") ? "Aún no tienes tours reservados"
                    : "Aún no tienes reservas";
            mostrarVacio(mensaje);
        } else {
            txtVacio.setVisibility(View.GONE);
        }
    }

    private void mostrarLista(ArrayList<String> textos) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, textos);
        lv1.setAdapter(adapter);
    }

    private void abrirDetalleReserva(int position) {
        Intent intent = new Intent(this, BookingDetailActivity.class);
        intent.putExtra("bookingId", ids.get(position));
        intent.putExtra("tipoReserva", tipos.get(position));
        intent.putExtra("nombreHabitacion", titulos.get(position));
        intent.putExtra("precioNoche", precios.get(position).doubleValue());
        intent.putExtra("checkIn", checkIns.get(position));
        intent.putExtra("checkOut", checkOuts.get(position));
        intent.putExtra("noches", cantidades.get(position).intValue());
        intent.putExtra("total", totales.get(position).doubleValue());
        intent.putExtra("metodoPago", metodos.get(position));
        intent.putExtra("pagoCompletado", pagados.get(position).booleanValue());
        intent.putExtra("soloLectura", true);
        startActivity(intent);
    }

    private void limpiarListas() {
        ids.clear();
        tipos.clear();
        titulos.clear();
        precios.clear();
        checkIns.clear();
        checkOuts.clear();
        cantidades.clear();
        totales.clear();
        metodos.clear();
        pagados.clear();
    }

    private void mostrarVacio(String mensaje) {
        txtVacio.setText(mensaje);
        txtVacio.setVisibility(View.VISIBLE);
    }

    private String valor(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
