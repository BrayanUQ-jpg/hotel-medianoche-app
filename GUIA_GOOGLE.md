# Guía: activar "Continuar con Google" (paso a paso)

Todo se hace en la MISMA PC donde vas a generar el APK, con tu cuenta principal de Google.

## Parte 1 — Sacar el SHA-1 (en Android Studio)
1. Abre la pestaña **Terminal** (abajo) del proyecto.
2. Escribe:  `.\gradlew signingReport`   (Windows)  /  `./gradlew signingReport`  (Mac/Linux)
3. Busca el bloque **Variant: debug** y copia la línea **SHA1** (letras y números separados por `:`).
   (Alternativa: `keytool -list -v -keystore "C:\Users\TU_USUARIO\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android`)

## Parte 2 — Google Cloud Console (https://console.cloud.google.com)
1. Arriba, **Seleccionar proyecto → Proyecto nuevo**. Nombre: `Hotel Medianoche` → Crear.
2. Menú **APIs y servicios → Pantalla de consentimiento de OAuth** → **Comenzar**.
   - Nombre de la app: `Hotel Medianoche`
   - Correo de asistencia: tu Gmail
   - Público: **Externo**
   - Correo de contacto: tu Gmail → acepta → **Crear**.
3. En el menú de la izquierda entra a **Público (Audience)** → **Usuarios de prueba → Add users** → agrega tu Gmail
   (y el de quien más vaya a probar). Guardar.
4. En **Clientes (Clients)** → **Crear cliente**:
   - Tipo **Android**: nombre `Hotel Android`, paquete `com.example.hotel`, pega el **SHA-1** de la Parte 1 → Crear.
   - Otra vez **Crear cliente** → tipo **Aplicación web**: nombre `Hotel Web`, deja lo demás vacío → Crear.
     **Copia el ID de cliente** (termina en `.apps.googleusercontent.com`).

## Parte 3 — En el proyecto
1. Abre `MainActivity.java`, busca `WEB_CLIENT_ID` y pega el ID de cliente web
   en lugar de `PEGA_AQUI_TU_CLIENT_ID_WEB.apps.googleusercontent.com`.
2. **File → Sync Project with Gradle Files** (necesita internet).
3. Desinstala la app del emulador/celular y vuelve a correrla.

## Parte 4 — Probar y generar el APK
1. En el emulador (con Google Play y tu cuenta iniciada) pulsa **Continuar con Google**.
2. Para el celular: **Build → Build APK(s)** (modo debug, en la MISMA PC). Aparece un aviso "locate":
   ahí está `app-debug.apk`. Pásalo al celular e instálalo.

## Si algo falla
- Mensaje "Falta pegar el Client ID": no pegaste el ID web (Parte 3).
- "No hay cuentas de Google disponibles": el emulador/celular no tiene cuenta iniciada.
- Error de Google / acceso bloqueado: tu correo no está en Usuarios de prueba, o el SHA-1/paquete de
  la credencial Android no coincide. Revisa que el paquete sea `com.example.hotel` y que el SHA-1 sea el de la variante **debug**.
- Si generas el APK en otra PC, el SHA-1 cambia y Google lo rechaza.
