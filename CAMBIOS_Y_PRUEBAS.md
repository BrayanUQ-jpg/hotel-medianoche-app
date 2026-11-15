# HOTEL — versión con SQLite (sin Firebase)

## Qué cambió

- Se quitó Firebase por completo (Authentication y Firestore): ya no hay `google-services.json`,
  ni plugins, ni dependencias, ni imports de Firebase.
- `SQLiteHelper` tiene la misma estructura que el de la clase: constructor con el nombre de la BD (`HotelMedianoche.db`),
  `onCreate` con los `CREATE TABLE` y `onUpgrade` con `DROP TABLE` + `onCreate`.
  Tablas: `USUARIOS`, `RESERVAS` (habitaciones y tours) y `VIAJES` (taxi).
- Los `insert`, `update` y `rawQuery` se escriben dentro de cada Activity (con `getWritableDatabase()`, `ContentValues` y `db.close()`).
- En taxi, "Abrir Yango / inDrive" crea **un solo viaje por pantalla**: los toques siguientes actualizan la misma fila.
- La sesión se sigue guardando en `SessionManager` (SharedPreferences) con el correo del usuario.

## Elementos nuevos aplicados

| Elemento | Dónde |
|---|---|
| SQLite | Login, registro, perfil, reservas, tours y viajes de taxi |
| ListView | `RecordsActivity`: historial de reservas, tours y viajes |
| Spinner | `RecordsActivity`: filtro del historial; `TourDetailActivity`: cantidad de personas |
| ImageButton | Flechas de regresar (`btnBack`) de todas las pantallas |
| Intent + putExtra | Del historial al detalle de la reserva (`BookingDetailActivity`) |

## Pruebas recomendadas en Android Studio

1. Desinstala la app del emulador (para empezar con la base de datos vacía).
2. **File > Sync Project with Gradle Files** y luego **Build > Rebuild Project**.
3. Regístrate con un correo nuevo; cierra sesión desde Perfil e inicia sesión de nuevo.
4. Intenta registrar el mismo correo otra vez: debe decir que ya está registrado.
5. Reserva una habitación, confirma el pago y revisa **Perfil > Mis reservas**.
6. Reserva un tour eligiendo 3 personas en el Spinner y revisa **Perfil > Mis tours reservados**.
7. En el historial prueba el Spinner (Todos, Pago completado, Pago pendiente).
8. Solicita un taxi, cancela uno y revisa **Perfil > Mis viajes en taxi** con el filtro Cancelados.

## Pruebas extra

9. En Taxi toca varias veces "Abrir Yango" y revisa en **Mis viajes en taxi** que salga un solo viaje.
10. En Pagos, con Yape o Plin toca "Abrir ..." y luego confirma: la reserva debe quedar una sola vez en el historial.

## Nota

Las contraseñas se guardan tal cual en SQLite, lo cual es válido para el curso. En una app real
se guardarían cifradas (hash).
