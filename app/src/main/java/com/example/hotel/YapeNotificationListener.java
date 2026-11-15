package com.example.hotel;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class YapeNotificationListener extends NotificationListenerService {

    public static final String ACCION_PAGO_DETECTADO = "com.example.hotel.PAGO_DETECTADO";


    private static final String PAQUETE_YAPE = "com.bcp.innovacxion.yapeapp";
    private static final String PAQUETE_PLIN = "pe.bbva.plin";

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        super.onNotificationPosted(sbn);

        String paquete = sbn.getPackageName();

        boolean esYape = paquete.equals(PAQUETE_YAPE);
        boolean esPlin = paquete.equals(PAQUETE_PLIN);

        if (!esYape && !esPlin) {
            return;
        }

        Notification notification = sbn.getNotification();
        Bundle extras = notification.extras;

        CharSequence titulo = extras.getCharSequence(Notification.EXTRA_TITLE);
        CharSequence texto = extras.getCharSequence(Notification.EXTRA_TEXT);

        String contenido = "";
        if (titulo != null) contenido += titulo.toString().toLowerCase();
        if (texto != null) contenido += " " + texto.toString().toLowerCase();


        boolean pareceUnPagoRecibido = contenido.contains("recibiste")
                || contenido.contains("te yapearon")
                || contenido.contains("pago recibido")
                || contenido.contains("has recibido");

        if (pareceUnPagoRecibido) {
            Intent intent = new Intent(ACCION_PAGO_DETECTADO);
            intent.setPackage(getPackageName());
            intent.putExtra("metodo", esYape ? "yape" : "plin");
            intent.putExtra("textoCompleto", contenido);
            sendBroadcast(intent);
        }
    }
}