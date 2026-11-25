# Sistema de Veterinaria

Sistema de escritorio en Java (Swing + Maven) para la gestión de una veterinaria.

## Avance 1: Módulo de citas

El módulo de citas permite:

- **Consultar la agenda** de un día (anterior, hoy, siguiente o una fecha escrita), con los horarios de 09:00 a 17:30 cada 30 minutos y su estado (Disponible, Ocupado, Ya pasó) según el encargado seleccionado.
- **Registrar una cita** con mascota, dueño, expediente (opcional), servicio, encargado, fecha y hora. Al hacer clic en un horario disponible de la agenda, la fecha y hora se llenan solas.
- **Validar** que no falten datos, que el horario no esté ocupado por el mismo encargado, que la hora sea válida y que la fecha no haya pasado.
- **Reprogramar**, **marcar la llegada de la mascota** y **cancelar** citas desde la tabla del día.
- **Guardar las citas** en el archivo `citas.csv`, para que no se pierdan al cerrar el programa.

## Avance 3: inicio de sesión y menú principal

- El sistema abre en **Inicio de sesión** (contraseñas cifradas con PBKDF2) y después en el **menú principal** con la barra del diseño del PRY04: Inicio | Dueños y mascotas | Citas | Clínica | Inventario | Cobros | Cerrar sesión.
- Cada rol ve solo sus módulos (`seguridad.Rol`).
- **Dueños y mascotas** ya funciona: buscar, registrar (ID y expediente automáticos), editar y dar de baja (solo administrador y veterinario).
- **Citas** abre la ventana `modulocitas` sin cambios.
- **Clínica:** busca la mascota, muestra vacunas, alergias e historial, y registra diagnóstico con receta. La cita queda en ATENDIDA.
- **Inventario:** alta y edición de productos, entradas de mercancía, baja lógica y avisos de stock bajo y caducidad.
- **Cobros:** cuenta del dueño con productos (por código QR) y servicios de citas atendidas, descuento, método de pago y ticket. Al cobrar se descuenta el stock y la cita queda en COBRADA, todo en una transacción.

### Preparar la base de datos
Ejecutar en Workbench, en orden: `db/veterinaria_schema.sql`, `db/datos_prueba.sql`, `db/03_login_menu.sql` y `db/04_datos_inventario.sql`.

Usuarios de prueba: `admin / Admin2026`, `veterinario / Vet2026`, `recepcion / Recepcion2026`, `almacen / Almacen2026`.

### Cómo agregar o cambiar un módulo
1. Crea tu rama desde `main` (por ejemplo `feature/reportes`).
2. Sigue el mismo patrón que Dueños y mascotas: `modelo` → `datos` (interfaz + DAO) → `servicios` (reglas y validaciones) → `ui` (panel), todo dentro de `com.mycompany.veterinaria`.
3. Conecta tu panel en `ui.MenuPrincipal.crearPanel()` y agrega el módulo y sus roles en `seguridad.Modulo` y `seguridad.Rol`.
4. Agrega pruebas en `src/test/java` y abre un Pull Request para que otro integrante lo revise.

## Avance 2 (Hito 2): conexión con MySQL y pruebas unitarias

- Las citas ya no se guardan en `citas.csv`: la clase `datos.CitaDAO` las guarda en la tabla `cita` de la base `veterinaria_lima` con JDBC (MySQL Connector/J), respetando las llaves foráneas de mascota y usuario.
- `GestorCitas` y la ventana no cambiaron: `CitaDAO` extiende a `RepositorioCitas`.
- Pruebas unitarias con JUnit 5 en `src/test/java` (22 ejecuciones).

### Preparar la base de datos
1. En MySQL Workbench ejecutar `db/veterinaria_schema.sql` y luego `db/datos_prueba.sql`.
2. El script de datos crea el usuario de MySQL `vet_app`, que es el que usa la aplicación (solo tiene permisos sobre `veterinaria_lima`). Si quieren usar otro: NetBeans → Properties → Run → VM Options: `-Ddb.user=... -Ddb.password=...`.
3. Para probar, registrar la cita con `Firulais / Pedro Hernandez` (o `Michi / Maria Lopez`).

### Correr las pruebas
Clic derecho al proyecto → **Test** (o `mvn test`). No necesitan MySQL.

## Cómo ejecutarlo

1. Abrir la carpeta del proyecto en NetBeans (*File → Open Project*).
2. Ejecutar con *Run Project* (F6). Se abre el inicio de sesión.

Requiere el JDK indicado en `pom.xml` (`maven.compiler.release`). Si NetBeans marca un error de versión, cambien ese número por la versión de su JDK (por ejemplo 21).

## Estructura

```
src/main/java/com/mycompany/veterinaria/
├── Veterinaria.java     Clase principal (abre el inicio de sesión)
├── modelo/              Datos: Cita, Dueno, Mascota, Producto, Concepto, CitaClinica,
│                        Antecedentes, RegistroClinico, Tratamiento
├── datos/               Acceso a MySQL (un Repositorio/interfaz + su DAO por módulo)
│                        ConexionBD, CitaDAO, MascotaDAO, ClinicaDAO, InventarioDAO,
│                        CobroDAO, UsuarioDAO, RepositorioCitas
├── servicios/           Reglas de negocio: GestorCitas, MascotaService, ClinicaService,
│                        InventarioService, CobroService, Cuenta
├── seguridad/           Contraseñas cifradas, roles, módulos y sesión
├── ui/                  Login, menú principal y un panel por módulo
└── modulos/             Ventana del módulo de citas (modulocitas.java / .form)

src/test/java/com/mycompany/veterinaria/   Pruebas JUnit (mismas carpetas)
db/                                        Scripts SQL (ejecutar en orden 01→04)
```

## Integrantes

| Integrante | Usuario de GitHub | Aportación |
|---|---|---|
| (nombre) | (usuario) | |
| (nombre) | (usuario) | |
| (nombre) | (usuario) | |
| (nombre) | (usuario) | |
| (nombre) | (usuario) | |
