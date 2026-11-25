# isil-2026-ii · Sistema de Pedidos — PA4: revisión y trazabilidad de pedidos

> README ubicado en la raíz del repositorio. El mismo contenido está en `ADVANCED-BUSINESS-APPLICATION-DEVELOPMENT/Sesión 5/sistema-pedidos-springboot/README.md`.

Proceso de Aprendizaje 4 (individual) · **Desarrollo de Aplicaciones Empresariales Avanzado (SRM)** · NRC 4622 · 2026-20
Estudiante: Angeles Rivera, Jeremy Andre
Repositorio: <https://github.com/orbitalconsul34-ctrl/isil-2026-ii> · Rama evaluada: `feature/pa4-angeles`

Un supervisor puede **marcar un pedido como revisado** y dejar una observación. El `UPDATE` de `pedido.revisado` y el `INSERT` en `pedido_revision` se ejecutan en **una sola transacción local** (Spring JDBC + `@Transactional`): o se guardan los dos o no se guarda ninguno. La pantalla de supervisión lee el estado desde `pedidos-reportes-service`, que expone dos recursos REST nuevos.

> Este proyecto es de uso académico y de ejecución local. No es una configuración lista para producción.

## Contenido

1. [Arquitectura](#1-arquitectura)
2. [Requisitos](#2-requisitos)
3. [Arranque rápido](#3-arranque-rápido)
4. [Variables de entorno](#4-variables-de-entorno)
5. [Cambios en la base de datos](#5-cambios-en-la-base-de-datos)
6. [Rutas nuevas y JSON de ejemplo](#6-rutas-nuevas-y-json-de-ejemplo)
7. [Usuarios y roles de laboratorio](#7-usuarios-y-roles-de-laboratorio)
8. [Comandos Maven y pruebas](#8-comandos-maven-y-pruebas)
9. [Los cinco casos prácticos](#9-los-cinco-casos-prácticos)
10. [Limitaciones](#10-limitaciones)
11. [Evidencia de `mvn clean test`](#11-evidencia-de-mvn-clean-test)
12. [Anexo: funcionalidad previa (sesiones 7 y 9)](#12-anexo-funcionalidad-previa-sesiones-7-y-9)

---

## 1. Arquitectura

![Arquitectura PA4](ADVANCED-BUSINESS-APPLICATION-DEVELOPMENT/Sesión%205/sistema-pedidos-springboot/docs/diagrama_arquitectura_pa4.png)

- **Escritura (revisar):** Navegador → Spring Security (rol, CSRF) → `DispatcherServlet` → `SupervisionPedidoController` → `PedidoRevisionService` (`@Transactional`) → `PedidoRevisionJdbcRepository` → MySQL.
- **Lectura (supervisar):** `SupervisionPedidoController` → `PedidoReporteClient` (`RestClient`) → HTTP/JSON → `pedidos-reportes-service` → `PedidoReporteService` → `PedidoReporteJdbcRepository` (usuario de solo lectura) → MySQL.
- **`inventario-service`** (Jakarta EE / WildFly, puerto 8082) es una dependencia externa que solo se usa para registrar pedidos desde `/pedidos`.

Decisiones de diseño:

- El controller **no ejecuta SQL**: valida parámetros, delega al service y aplica Post/Redirect/Get.
- **No hay llamadas HTTP dentro de la transacción.** La transacción es local a MySQL.
- Los duplicados se bloquean en dos niveles: `UPDATE ... WHERE id = ? AND revisado = false` y la restricción `UNIQUE (pedido_id)`.
- La entidad `PedidoRevision` solo sirve para que Hibernate genere la tabla, la FK y el `UNIQUE`; las escrituras se hacen con Spring JDBC (SQL parametrizado).
- CSRF sigue activo. No se desactivó la seguridad.

| Proyecto | Carpeta | Puerto | Context path |
|---|---|---|---|
| sistema-pedidos-springboot | `ADVANCED-BUSINESS-APPLICATION-DEVELOPMENT/Sesión 5/sistema-pedidos-springboot` | 8081 | `/sistema-pedidos` |
| pedidos-reportes-service | `ADVANCED-BUSINESS-APPLICATION-DEVELOPMENT/Sesión 12/pedidos-reportes-service` | 8083 | `/pedidos-reportes-service` |
| inventario-service (externo) | `ADVANCED-BUSINESS-APPLICATION-DEVELOPMENT/Sesión 4/inventario-service` | 8082 | `/inventario-service` |

## 2. Requisitos

- JDK 21 (`JAVA_HOME` configurado)
- MySQL 8.x
- Maven Wrapper incluido (`mvnw` / `mvnw.cmd`)
- Puertos **8081** y **8083** libres. El **8082** solo hace falta si vas a registrar pedidos desde `/pedidos`.

## 3. Arranque rápido

**Orden:** MySQL → (opcional) `inventario-service` → `pedidos-reportes-service` → `sistema-pedidos-springboot`.

**3.1 Crear la base y los usuarios de MySQL** (cambia las claves por las tuyas):

```sql
CREATE DATABASE IF NOT EXISTS pedidosdb;
CREATE USER IF NOT EXISTS 'spedidos_app'@'localhost' IDENTIFIED BY '<clave-app>';
CREATE USER IF NOT EXISTS 'spedidos_reportes'@'localhost' IDENTIFIED BY '<clave-reportes>';
GRANT ALL PRIVILEGES ON pedidosdb.* TO 'spedidos_app'@'localhost';
GRANT SELECT ON pedidosdb.* TO 'spedidos_reportes'@'localhost';
FLUSH PRIVILEGES;
```

**3.2 Definir variables de entorno** (ver sección 4), por ejemplo en PowerShell:

```powershell
$env:DB_USERNAME = "spedidos_app"
$env:DB_PASSWORD = "<clave-app>"
```

**3.3 Levantar los dos proyectos** desde la raíz del repositorio, cada uno en su terminal:

```powershell
# Terminal 1: reportes (8083)
cd "ADVANCED-BUSINESS-APPLICATION-DEVELOPMENT\Sesión 12\pedidos-reportes-service"
.\mvnw.cmd spring-boot:run

# Terminal 2: pedidos (8081)
cd "ADVANCED-BUSINESS-APPLICATION-DEVELOPMENT\Sesión 5\sistema-pedidos-springboot"
.\mvnw.cmd spring-boot:run
```

La aplicación de pedidos **crea las tablas nuevas al arrancar** (`ddl-auto=update`). El script equivalente está en el archivo `database/pa4_revision.sql` dentro de la carpeta `Sesión 5/sistema-pedidos-springboot`.

**3.4 Dar permiso de lectura sobre la tabla nueva.** `pedido_revision` la crea la app de pedidos al iniciar. Si el `GRANT` general no la cubrió porque aún no existía, arranca pedidos una vez y ejecuta:

```sql
GRANT SELECT ON pedidosdb.pedido_revision TO 'spedidos_reportes'@'localhost';
```

Sin este permiso, el servicio de reportes no puede leer las revisiones.

**3.5 Abrir en el navegador:**

- Supervisión: <http://localhost:8081/sistema-pedidos/supervision/pedidos> (pide login; ver sección 7)
- Reportes (JSON): <http://localhost:8083/pedidos-reportes-service/api/v1/pedidos/revisiones>

## 4. Variables de entorno

No se suben credenciales al repositorio. Si no defines una variable, se usa el valor de laboratorio definido en el `application.properties` de cada proyecto.

| Variable | Proyecto | Para qué |
|---|---|---|
| `DB_URL` | ambos | URL JDBC, p. ej. `jdbc:mysql://localhost:3306/pedidosdb?serverTimezone=America/Lima` |
| `DB_USERNAME` | ambos | `spedidos_app` en pedidos; `spedidos_reportes` (solo lectura) en reportes |
| `DB_PASSWORD` | ambos | Clave del usuario de BD |
| `PEDIDOS_OPERADOR_PASSWORD` | pedidos | Clave del usuario `operador` |
| `PEDIDOS_CONSULTA_PASSWORD` | pedidos | Clave del usuario `consulta` |

> Como pedidos y reportes usan **usuarios de BD distintos**, si defines `DB_USERNAME` y `DB_PASSWORD` hazlo en la terminal de cada proyecto con los valores que correspondan.

## 5. Cambios en la base de datos

Base: `pedidosdb`.

- `pedido.revisado`: columna `BIT NOT NULL`, valor inicial `false`.
- `pedido_revision`: tabla nueva con `id`, `pedido_id`, `observacion` (255) y `fecha_revision`.
- Clave foránea `fk_pedido_revision_pedido` hacia `pedido(id)`.
- Restricción `UNIQUE uk_pedido_revision_pedido (pedido_id)`: impide una segunda revisión del mismo pedido.

## 6. Rutas nuevas y JSON de ejemplo

Antepón el contexto: `/sistema-pedidos` (8081) o `/pedidos-reportes-service` (8083).

| Método | Ruta | Proyecto | Qué hace |
|---|---|---|---|
| GET | `/supervision/pedidos` | pedidos | Pantalla de supervisión; obtiene los datos de reportes por HTTP. |
| POST | `/supervision/pedidos/{id}/revisar` | pedidos | Revisa un pedido (parámetro `observacion`). Requiere CSRF y rol `OPERADOR`; redirige con PRG. |
| GET | `/api/v1/pedidos/revisiones?estado=PENDIENTE\|REVISADO` | reportes | Lista de pedidos con su estado de revisión. `400` si el estado no es válido. |
| GET | `/api/v1/pedidos/estadisticas/revisiones` | reportes | Totales y porcentaje revisado. |

`GET /api/v1/pedidos/revisiones?estado=REVISADO`:

```json
[
  {
    "id": 1,
    "cliente": "Ana Torres",
    "productoNombre": "Laptop",
    "total": 200,
    "revisado": true,
    "estado": "REVISADO",
    "observacion": "Datos verificados",
    "fechaRevision": "2026-11-24T23:45:35.217408"
  }
]
```

`GET /api/v1/pedidos/estadisticas/revisiones`:

```json
{ "totalPedidos": 3, "totalRevisados": 2, "totalPendientes": 1, "porcentajeRevisado": 66.67 }
```

`GET /api/v1/pedidos/revisiones?estado=XYZ` → HTTP 400:

```json
{
  "title": "Parámetro inválido",
  "status": 400,
  "detail": "El estado 'XYZ' no es válido. Valores permitidos: PENDIENTE, REVISADO.",
  "instance": "/pedidos-reportes-service/api/v1/pedidos/revisiones"
}
```

## 7. Usuarios y roles de laboratorio

| Usuario | Rol | Puede |
|---|---|---|
| `operador` | `OPERADOR` | Ver la supervisión y revisar pedidos. |
| `consulta` | `CONSULTA` | Solo ver; si intenta revisar recibe **403**. |

Las claves salen de `PEDIDOS_OPERADOR_PASSWORD` y `PEDIDOS_CONSULTA_PASSWORD` (sección 4). Los usuarios están **en memoria** (`InMemoryUserDetailsManager`) y no existe tabla de usuarios. Login: <http://localhost:8081/sistema-pedidos/login>.

Reglas: `GET /supervision/**` permite `OPERADOR` y `CONSULTA`; `POST /supervision/pedidos/**` solo `OPERADOR`. CSRF está activo y el formulario JSP envía su token.

## 8. Comandos Maven y pruebas

Se ejecutan **dentro de cada proyecto** (`sistema-pedidos-springboot` y `pedidos-reportes-service`):

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd test -Dtest=PedidoRevisionRollbackIntegrationTest   # caso 4: rollback (solo pedidos)
.\mvnw.cmd spring-boot:run
```

`mvn test` no requiere MySQL: las pruebas de integración usan H2 en memoria (perfil `test`).

### Pruebas nuevas de PA4 (21)

| Proyecto | Clase (n.º) | Qué comprueba |
|---|---|---|
| pedidos | `PedidoRevisionServiceTest` (4) | Commit correcto, revisión duplicada, pedido inexistente y validaciones sin escrituras (H2). |
| pedidos | `PedidoRevisionRollbackIntegrationTest` (1) | Rollback real: el `INSERT` falla después del `UPDATE` y `revisado` sigue en `false`. |
| pedidos | `PedidoReporteRestClientRevisionesTest` (5) | Mapeo JSON a DTO, error 5xx, conexión rechazada y estado inválido (`MockRestServiceServer`). |
| pedidos | `SupervisionPedidoControllerTest` (4) | Datos de la vista, 503 controlado, PRG y mensajes de error (Mockito + MockMvc). |
| reportes | `PedidoReporteServiceRevisionesTest` (4) | Pendientes, porcentaje, división por cero y traducción del filtro. |
| reportes | `PedidoRevisionReporteControllerTest` (3) | JSON, 400 por estado inválido y DTO de estadísticas. |

Resultado de `mvn clean test`: **26 pruebas en pedidos y 8 en reportes, 0 fallos.**

Se retiraron del proyecto de pedidos `PedidoApiControllerTest`, `PedidoReporteControllerIntegrationTest`, `PedidoReporteJdbcRepositoryTest`, `PedidoReporteWebControllerTest` y un método de `PedidoSecurityIntegrationTest`: apuntaban a código ya movido a `pedidos-reportes-service` o necesitaban el puerto 8083 encendido, por lo que no compilaban o no se podían repetir en otra máquina.

## 9. Los cinco casos prácticos

1. **Revisión correcta.** Un pedido pendiente con observación válida queda con `revisado=1`, hay una fila en `pedido_revision` y la pantalla muestra REVISADO.
2. **Revisión duplicada.** Con dos pestañas abiertas, la segunda revisión se rechaza con «ya fue revisado» y no se crea otra fila.
3. **Validaciones.** Observación vacía, pedido `9999` inexistente e id `0`: cada caso da un mensaje claro y no se escribe nada.
4. **Rollback.** La prueba de integración fuerza el fallo del `INSERT` y comprueba que el `UPDATE` se deshace.
5. **Servicio disponible y caído.** Con el 8083 activo, los dos endpoints y la pantalla responden bien. Con el servicio apagado, la pantalla muestra un mensaje controlado (503) y la base queda intacta.

Las capturas de cada caso están en el informe PA4 (Anexo A).

## 10. Limitaciones

- La transacción es local a MySQL. No hay llamadas HTTP dentro de ella ni compensación con `inventario-service`.
- Si `pedidos-reportes-service` está caído, la pantalla no muestra el listado (503 con mensaje controlado), pero las revisiones guardadas siguen consistentes.
- `fecha_revision` salió con **5 horas de diferencia** respecto a la hora local por la zona horaria del driver de MySQL. No afecta la consistencia de los datos.
- Los pedidos de las pruebas manuales se cargaron con SQL. Registrarlos desde `/pedidos` requiere `inventario-service` encendido.
- Usuarios en memoria y claves de laboratorio; el servicio de reportes no tiene autenticación propia. No apto para producción.

## 11. Evidencia de `mvn clean test`

`sistema-pedidos-springboot`: 26 pruebas, 0 fallos.

![mvn clean test, sistema-pedidos-springboot](ADVANCED-BUSINESS-APPLICATION-DEVELOPMENT/Sesión%205/sistema-pedidos-springboot/docs/evidencias/mvn_test_pedidos.png)

`pedidos-reportes-service`: 8 pruebas, 0 fallos.

![mvn clean test, pedidos-reportes-service](ADVANCED-BUSINESS-APPLICATION-DEVELOPMENT/Sesión%205/sistema-pedidos-springboot/docs/evidencias/mvn_test_reportes.png)

El aviso de Mockito sobre *self-attaching* es una advertencia del JDK y no afecta el resultado.

## 12. Anexo: funcionalidad previa (sesiones 7 y 9)

Resumen de lo que ya existía antes de PA4 (no cuenta como desarrollo de PA4):

- **Stack:** Java 21, Spring Boot, Spring MVC + JSP (WAR), Spring Data JPA, Spring `RestClient`, Spring Security, Maven.
- **Sesión 7:** autenticación con `formLogin`, roles `OPERADOR` y `CONSULTA`, CSRF y pruebas con MockMvc.
- **Sesión 9:** reportes de solo lectura con Spring JDBC (migrados luego a `pedidos-reportes-service`).

| Método | Ruta (pedidos) | Descripción |
|---|---|---|
| GET | `/pedidos` | Pantalla JSP de pedidos. |
| POST | `/pedidos` | Registra un pedido (reserva stock en `inventario-service`). Solo `OPERADOR`, con CSRF. |
| GET | `/api/v1/orders` | Lista de pedidos en JSON. |
| GET | `/api/v1/orders/resumen` | Resumen con `totalPedidos` y `totalUnidades`. |

**CSRF y validación de negocio son controles independientes:** un POST a `/pedidos` sin token da **403**; con token válido y `cantidad=0` da **400** por la regla de negocio.

`inventario-service` no migra a Spring Boot: sigue como aplicación externa Jakarta EE/WildFly. La colección `postman/ISIL.postman_collection.json` viene de las prácticas de integración; para usarla contra endpoints protegidos hace falta autenticación por sesión y, cuando corresponda, CSRF.
