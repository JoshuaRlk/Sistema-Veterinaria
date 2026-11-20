# Sistema de Veterinaria

Sistema de escritorio en Java (Swing + Maven) para la gestión de una veterinaria.

## Avance 1: Módulo de citas

El módulo de citas permite:

- **Consultar la agenda** de un día (anterior, hoy, siguiente o una fecha escrita), con los horarios de 09:00 a 17:30 cada 30 minutos y su estado (Disponible, Ocupado, Ya pasó) según el encargado seleccionado.
- **Registrar una cita** con mascota, dueño, expediente (opcional), servicio, encargado, fecha y hora. Al hacer clic en un horario disponible de la agenda, la fecha y hora se llenan solas.
- **Validar** que no falten datos, que el horario no esté ocupado por el mismo encargado, que la hora sea válida y que la fecha no haya pasado.
- **Reprogramar**, **marcar la llegada de la mascota** y **cancelar** citas desde la tabla del día.
- **Guardar las citas** en el archivo `citas.csv`, para que no se pierdan al cerrar el programa.

## Avance 2 (Hito 2): conexión con MySQL y pruebas unitarias

- Las citas ya no se guardan en `citas.csv`: la clase `datos/CitaDAO` las guarda en la tabla `cita` de la base `veterinaria_lima` con JDBC (MySQL Connector/J), respetando las llaves foráneas de mascota y usuario.
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
2. Ejecutar con *Run Project* (F6). Se abre directamente la ventana del módulo de citas.

Requiere el JDK indicado en `pom.xml` (`maven.compiler.release`). Si NetBeans marca un error de versión, cambien ese número por la versión de su JDK (por ejemplo 21).

## Estructura

```
src/main/java/
├── com/mycompany/veterinaria/Veterinaria.java   Clase principal (abre el módulo de citas)
├── modelo/Cita.java                             Datos de una cita y sus estados
├── datos/RepositorioCitas.java                  Lectura y escritura del archivo citas.csv
├── datos/CitaDAO.java                           Guardado en MySQL (reemplaza al CSV)
├── datos/ConexionBD.java                        Conexión JDBC
├── servicios/GestorCitas.java                   Reglas de negocio (horarios, validaciones, estados)
└── modulos/modulocitas.java / .form             Ventana del módulo de citas
```

## Integrantes

| Integrante | Usuario de GitHub | Aportación |
|---|---|---|
| (nombre) | (usuario) | |
| (nombre) | (usuario) | |
| (nombre) | (usuario) | |
| (nombre) | (usuario) | |
| (nombre) | (usuario) | |
