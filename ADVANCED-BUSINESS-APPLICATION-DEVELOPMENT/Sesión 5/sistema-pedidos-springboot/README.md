# Sistema de Pedidos — Spring Boot

> Proyecto académico evolutivo · **Desarrollo de Aplicaciones Empresariales Avanzado** · ISIL  
> **Sesión 7:** Spring MVC, eficiencia y seguridad web con Spring Security.

Aplicación empresarial de referencia para estudiar la migración de **Jakarta EE/WildFly a Spring Boot**, la separación de responsabilidades, la integración HTTP con `inventario-service` y la protección de una aplicación web con **autenticación, autorización y CSRF**.

El proyecto conserva una interfaz **Spring MVC + JSP** y expone una **API JSON** que reutiliza la misma capa de negocio. En la sesión 7 se agregaron usuarios de laboratorio, reglas de acceso por rol, protección CSRF y pruebas automatizadas; también se amplió el resumen de pedidos con `totalUnidades`.

**Alcance:** Implementación académica para ejecución local. No constituye una solución de seguridad lista para producción.

## Contenido

1. [Objetivos y evolución académica](#1-objetivos-y-evolución-académica)
2. [Tecnologías](#2-tecnologías)
3. [Arquitectura y responsabilidades](#3-arquitectura-y-responsabilidades)
4. [Estructura del proyecto](#4-estructura-del-proyecto)
5. [Novedades de la sesión 7](#5-novedades-de-la-sesión-7)
6. [Requisitos y configuración](#6-requisitos-y-configuración)
7. [Ejecución local](#7-ejecución-local)
8. [Rutas y contratos HTTP](#8-rutas-y-contratos-http)
9. [Autenticación, autorización y CSRF](#9-autenticación-autorización-y-csrf)
10. [Pruebas automatizadas y verificación manual](#10-pruebas-automatizadas-y-verificación-manual)
11. [Persistencia, integración y transacciones](#11-persistencia-integración-y-transacciones)
12. [Limitaciones y siguientes pasos](#12-limitaciones-y-siguientes-pasos)
13. [Referencias técnicas](#13-referencias-técnicas)

---

## 1. Objetivos y evolución académica

**Propósito:** comprender el ciclo completo de una solicitud web —desde los filtros de seguridad y `DispatcherServlet` hasta los controladores, servicios, repositorios, clientes HTTP y la respuesta JSP/JSON— sin duplicar las reglas de negocio.

| Etapa | Conceptos aplicados | Evidencia en el código |
|---|---|---|
| Integración con Jakarta EE | Comunicación entre aplicaciones independientes | `inventario-service` sobre WildFly y SQL Server |
| Inyección de dependencias | IoC, DI por constructor, interfaces y beans | `PedidoController`, `PedidoServiceImpl`, `InventarioClient` |
| Spring Framework / MVC | Mapeos HTTP, `Model`, JSP, DTO y JSON | `PedidoController`, `PedidoApiController`, `PedidoResponse` |
| **Sesión 7** | **Autenticación, autorización, CSRF y pruebas de seguridad** | **`SecurityConfig`, `pedidos.jsp`, `PedidoSecurityIntegrationTest`** |

**Resultados de aprendizaje de la sesión 7:**

- Explicar el procesamiento de `GET /pedidos` y `POST /pedidos` y la diferencia entre generar HTML mediante JSP y devolver JSON desde una API.
- Ampliar el resumen de pedidos con `totalUnidades` y comprobar su contrato mediante JUnit 5, Mockito y MockMvc.
- Configurar accesos por roles, proteger formularios frente a CSRF y demostrar la diferencia entre errores de autorización y de negocio.

### Relación entre Jakarta EE y Spring Boot

| Aplicación anterior (Jakarta EE) | Aplicación actual (Spring Boot) |
|---|---|
| `PedidoServlet` / `@WebServlet` | `PedidoController` / `@Controller`, `@GetMapping`, `@PostMapping` |
| Inyección mediante `@EJB` | Inyección de dependencias por constructor |
| Componente EJB `@Stateless` | Servicio de negocio `@Service` (sin equivalencia directa de todas las semánticas EJB) |
| Acceso a datos con `EntityManager` | `PedidoRepository` y Spring Data JPA |
| Cliente REST de Jakarta | `InventarioRestClient` y Spring `RestClient` |
| `persistence.xml` y configuración del contenedor | `application.properties` y autoconfiguración de Spring Boot |
| Despliegue del Sistema de Pedidos en WildFly | Ejecución local con servidor Tomcat embebido |

> `inventario-service` **no migra** a Spring Boot: permanece como aplicación externa Jakarta EE/WildFly.

## 2. Tecnologías

| Capa / función | Tecnología del proyecto |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot **4.1.1** / Spring Framework |
| Web | Spring MVC (`spring-boot-starter-webmvc`) |
| Vista | JSP, JSTL y Tomcat Jasper; empaquetado **WAR** |
| API | `@RestController`, DTO `PedidoResponse` y serialización JSON |
| Persistencia | Spring Data JPA, Hibernate y H2 en memoria |
| Integración | Spring `RestClient`, HTTP/JSON con `inventario-service` |
| Seguridad (S07) | Spring Security, `SecurityFilterChain`, `formLogin`, `BCryptPasswordEncoder`, CSRF |
| Pruebas | JUnit 5, Mockito, Spring Test, MockMvc y Spring Security Test |
| Construcción | Maven y Maven Wrapper |

## 3. Arquitectura y responsabilidades

```text
Navegador / cliente HTTP
          |
          | HTTP (cookie de sesión en el navegador)
          v
  Spring Security Filter Chain
  - autenticación, autorización, CSRF
          |
          v
  DispatcherServlet (Spring MVC)
          |
          +-------------------------------+
          |                               |
          v                               v
  PedidoController                PedidoApiController
  @Controller                     @RestController
  Model + ViewResolver            HttpMessageConverter
  pedidos.jsp --> HTML            PedidoResponse --> JSON
          |                               |
          +---------------+---------------+
                          |
                          v
                    PedidoService
                    (interfaz)
                          ^
                          | implementada por
                    PedidoServiceImpl
                          |
          +---------------+---------------+
          |                               |
          v                               v
   PedidoRepository                 InventarioClient
   Spring Data JPA                  (interfaz)
          |                               ^
          v                               | implementada por
      H2 (local)                    InventarioRestClient
                                          |
                                    Spring RestClient
                                          |
                                     HTTP / JSON
                                          |
                                          v
                                    inventario-service
                                    Jakarta EE / WildFly
                                    SQL Server (externo)
```

**Responsabilidades:**

- **Seguridad:** filtra solicitudes y aplica controles antes del procesamiento MVC cuando corresponde.
- **Controladores:** reciben entradas HTTP, delegan al servicio y determinan la representación de salida.
- **Servicio:** valida datos y coordina operaciones de pedido, persistencia e inventario.
- **Repositorio:** encapsula el acceso a H2 con Spring Data JPA.
- **Adaptador externo:** centraliza las llamadas HTTP mediante la abstracción `InventarioClient`.

Los controladores reciben `PedidoService` por constructor; el servicio recibe `PedidoRepository` e `InventarioClient`. No es necesario crear estos colaboradores mediante `new` dentro de la lógica del proyecto.

## 4. Estructura del proyecto

```text
sistema-pedidos-springboot/
├── pom.xml
├── mvnw / mvnw.cmd
├── README.md
├── postman/
│   └── ISIL.postman_collection.json
└── src/
    ├── main/
    │   ├── java/com/isil/edu/pe/spedidos/
    │   │   ├── client/           # InventarioClient, InventarioRestClient y DTO HTTP
    │   │   ├── config/           # InventarioHttpConfig y SecurityConfig [S07]
    │   │   ├── controller/       # HomeController, PedidoController, PedidoApiController
    │   │   │   └── dto/          # PedidoResponse
    │   │   ├── entity/           # Pedido
    │   │   ├── exception/        # PedidoException
    │   │   ├── repository/       # PedidoRepository
    │   │   ├── service/          # PedidoService y PedidoServiceImpl
    │   │   └── SistemaPedidosSpringbootApplication.java
    │   ├── resources/application.properties
    │   └── webapp/
    │       ├── assets/css/app.css
    │       └── WEB-INF/views/
    │           ├── pedidos.jsp   # Formulario y logout con CSRF [S07]
    │           └── error.jsp
    └── test/java/com/isil/edu/pe/spedidos/
        ├── SistemaPedidosSpringbootApplicationTests.java
        ├── controller/
        │   ├── PedidoApiControllerTest.java        # Contrato JSON; standalone
        │   └── PedidoSecurityIntegrationTest.java # Seguridad MVC; filtros [S07]
        └── service/impl/PedidoServiceImplTest.java
```

## 5. Novedades de la sesión 7

| Archivo | Cambio realizado |
|---|---|
| `pom.xml` | Incorpora `spring-boot-starter-security` y `spring-boot-starter-security-test`. |
| `config/SecurityConfig.java` **(nuevo)** | Define `SecurityFilterChain`, usuarios de prueba, roles y `BCryptPasswordEncoder`. |
| `resources/application.properties` | Deshabilita H2 Console y permite sobreescribir contraseñas de laboratorio mediante variables de entorno. |
| `WEB-INF/views/pedidos.jsp` | Añade un campo oculto CSRF al POST y un formulario de cierre de sesión POST con CSRF. |
| `controller/PedidoApiController.java` | Amplía `GET /api/v1/orders/resumen` con `totalUnidades`, calculado a partir de una sola consulta a pedidos. |
| `controller/PedidoApiControllerTest.java` | Amplía los tests de respuesta JSON con el total de unidades. |
| `controller/PedidoSecurityIntegrationTest.java` **(nuevo)** | Comprueba redirección anónima, roles, CSRF y acceso al resumen protegido. |
| `README.md` | Consolida documentación de arquitectura, arranque, seguridad, endpoints y pruebas. |

La configuración de seguridad **no incorpora** JWT, OAuth2/OIDC, usuarios persistidos en base de datos ni autorización por propietario de cada pedido.

## 6. Requisitos y configuración

### Requisitos

- **JDK 21** instalado y configurado (`JAVA_HOME`).
- **Maven 3.9.x** o el Maven Wrapper incluido.
- IDE compatible (por ejemplo, IntelliJ IDEA).
- `inventario-service` ejecutándose en **`http://localhost:8082`** para las funciones que consultan o reservan productos.
- Puerto **8081** disponible para el Sistema de Pedidos.

Comprobar versiones:

```bash
java -version
mvn -version
```

### Configuración relevante (`application.properties`)

```properties
server.port=8081
server.servlet.context-path=/sistema-pedidos

spring.mvc.view.prefix=/WEB-INF/views/
spring.mvc.view.suffix=.jsp

spring.datasource.url=jdbc:h2:mem:pedidosdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.open-in-view=false
spring.h2.console.enabled=false

inventario.api.base-url=http://localhost:8082/inventario-service/api/inventario
inventario.api.connect-timeout-ms=2000
inventario.api.read-timeout-ms=3000

# Únicamente para laboratorio: sustituir por variables de entorno.
demo.security.operador-password=${PEDIDOS_OPERADOR_PASSWORD:OperadorDemo2026!}
demo.security.consulta-password=${PEDIDOS_CONSULTA_PASSWORD:ConsultaDemo2026!}
```

Los timeouts HTTP de **conexión (2000 ms)** y **lectura (3000 ms)** son ajustes del cliente de inventario; no garantizan un límite idéntico para el tiempo total de una operación de negocio.

### Usuarios de laboratorio

| Usuario | Rol | Contraseña predeterminada *solo para clase* |
|---|---|---|
| `operador` | `OPERADOR` | `OperadorDemo2026!` |
| `consulta` | `CONSULTA` | `ConsultaDemo2026!` |

Se administran en memoria mediante `InMemoryUserDetailsManager`; **no existe una tabla de usuarios**. Las contraseñas se codifican con BCrypt antes de registrarse en el gestor de usuarios en memoria. Los valores de respaldo del proyecto son públicos y **no son adecuados para ambientes expuestos**.

Para sustituirlos antes de iniciar la aplicación en **PowerShell**:

```powershell
$env:PEDIDOS_OPERADOR_PASSWORD = "<clave-local-segura>"
$env:PEDIDOS_CONSULTA_PASSWORD = "<otra-clave-local-segura>"
```

En despliegues reales, eliminar los valores predeterminados y utilizar un proveedor de identidad o un mecanismo seguro de administración de credenciales.

## 7. Ejecución local

### 7.1 Iniciar la dependencia externa

Iniciar `inventario-service` en WildFly y verificar que está accesible en `http://localhost:8082`, con su propia base de datos/configuración externa.

### 7.2 Compilar y ejecutar Sistema de Pedidos

Desde la raíz de este repositorio, en **PowerShell**:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd spring-boot:run
```

Alternativa con Maven instalado:

```bash
mvn clean test
mvn spring-boot:run
```

> `clean test` y `spring-boot:run` se ejecutan de forma secuencial; el segundo comando mantiene la aplicación iniciada hasta que se detiene el proceso.

### 7.3 Acceder desde el navegador

- Aplicación: <http://localhost:8081/sistema-pedidos/>
- Pantalla de pedidos: <http://localhost:8081/sistema-pedidos/pedidos>
- Login predeterminado de Spring Security: <http://localhost:8081/sistema-pedidos/login>
- API de pedidos: <http://localhost:8081/sistema-pedidos/api/v1/orders>
- Resumen: <http://localhost:8081/sistema-pedidos/api/v1/orders/resumen>

Primero se solicita autenticación. Luego, Spring Security aplica los permisos del usuario autenticado. **La consola H2 está deshabilitada** (`spring.h2.console.enabled=false`): la ruta `/h2-console` no es una interfaz utilizable en esta variante.

> La colección existente `postman/ISIL.postman_collection.json` procede de las prácticas de integración. Para utilizar sus solicitudes contra endpoints ahora protegidos, deben configurarse autenticación por sesión y, cuando corresponda, CSRF; no se incorporó HTTP Basic ni JWT a `SecurityConfig`.

## 8. Rutas y contratos HTTP

Los mappings se escriben **sin** el contexto; en el navegador o cliente HTTP se antepone `/sistema-pedidos`.

| Método | Ruta relativa | Controlador / función | Respuesta habitual |
|---|---|---|---|
| GET | `/` | `HomeController` | Redirección a `/pedidos` tras superar seguridad. |
| GET | `/pedidos` | `PedidoController` | HTML renderizado desde `pedidos.jsp`. |
| POST | `/pedidos` | `PedidoController` | Registro y redirección (patrón POST/Redirect/GET) o error. |
| GET | `/api/v1/orders` | `PedidoApiController` | Arreglo JSON de `PedidoResponse`. |
| GET | `/api/v1/orders/resumen` | `PedidoApiController` | JSON de resumen con `modulo`, `totalPedidos`, `totalUnidades`. |
| GET/POST | `/login` | Spring Security | Formulario de login y procesamiento de autenticación. |
| POST | `/logout` | Spring Security | Cierre de sesión (requiere token CSRF válido). |

### Ejemplo: respuesta del resumen

```http
GET /sistema-pedidos/api/v1/orders/resumen
Accept: application/json
```

```json
{
  "modulo": "Sistema de Pedidos",
  "totalPedidos": 2,
  "totalUnidades": 5
}
```

**Ejemplo ilustrativo:** `totalPedidos=2` y `totalUnidades=5` equivalen a dos pedidos cuyas cantidades suman cinco. Los datos reales dependen de los registros en H2.

En `PedidoApiController`, el total se calcula a partir de una sola llamada al servicio:

```java
List<Pedido> pedidos = pedidoService.listarPedidos();
int totalUnidades = pedidos.stream()
    .mapToInt(Pedido::getCantidad)
    .sum();
```

La API devuelve DTO `PedidoResponse` en vez de exponer directamente la entidad JPA `Pedido`. Así se reduce el acoplamiento entre persistencia y contrato HTTP.

### Códigos HTTP en el formulario

| Código | Situación en esta aplicación |
|---|---|
| **302** (redirección) | Login requerido o registro exitoso con redirección, según el caso. |
| **400** (`Bad Request`) | Parámetro inválido o regla de negocio incumplida, por ejemplo `cantidad=0`. |
| **403** (`Forbidden`) | Rol insuficiente o solicitud POST sin token CSRF válido. |
| **503** (`Service Unavailable`) | Indisponibilidad de inventario detectada por el controlador. |
| **500** (`Internal Server Error`) | Error inesperado procesado por el controlador. |

Un **400 con un token CSRF válido** no significa que CSRF falló: puede indicar que la solicitud pasó el filtro y fue rechazada por las validaciones del pedido.

## 9. Autenticación, autorización y CSRF

### Matriz de accesos

| Operación | Anónimo | `CONSULTA` | `OPERADOR` |
|---|---|---|---|
| GET `/pedidos` | Login requerido | Permitido | Permitido |
| GET `/api/v1/orders` | Login requerido | Permitido | Permitido |
| GET `/api/v1/orders/resumen` | Login requerido | Permitido | Permitido |
| POST `/pedidos` con CSRF válido | Login requerido | **403** | Permitido si cumple reglas de negocio |
| POST `/pedidos` sin CSRF válido | Rechazado | **403** | **403** |
| `/h2-console/**` | Sin acceso | Sin acceso | Sin acceso |

La autenticación utiliza `formLogin` con **sesión HTTP**. Las reglas se definen en `SecurityConfig`, usando `SecurityFilterChain`. No se habilitó autenticación HTTP Basic ni validación de Bearer/JWT.

### Protección CSRF de `pedidos.jsp`

El formulario de creación y el de logout incorporan:

```jsp
<input type="hidden"
       name="${_csrf.parameterName}"
       value="${_csrf.token}">
```

La protección CSRF de Spring Security permanece **habilitada por defecto**. Para operaciones POST con sesión y cookie, el token debe enviarse correctamente; no se debe desactivar CSRF como solución a un `403`.

### Comprobación manual sin modificar datos

1. Iniciar sesión en el navegador como `operador`.
2. Ejecutar un POST de prueba sin token CSRF y con `cantidad=0`: resultado esperado, **403**.
3. Repetir el POST con el token CSRF tomado del campo oculto del formulario: resultado esperado, **400**, acompañado del mensaje de negocio sobre cantidad inválida.
4. Confirmar en **DevTools → Network** el código HTTP y los parámetros enviados.

Esta comparación evidencia que **CSRF y validación de negocio son controles independientes**. La segunda solicitud no registra pedidos porque `cantidad=0` infringe la regla del servicio.

> Para registrar un pedido real, utilizar un código de producto existente y con stock; el registro puede modificar también la información de `inventario-service`.

## 10. Pruebas automatizadas y verificación manual

Ejecutar todos los tests:

```powershell
.\mvnw.cmd clean test
```

### Tipos de pruebas

| Clase | Nivel | Qué verifica |
|---|---|---|
| `PedidoServiceImplTest` | Unitaria con Mockito | Comportamiento del servicio con colaboradores simulados. |
| `PedidoApiControllerTest` | Controlador aislado; MockMvc `standaloneSetup` | Contrato JSON, lista vacía y cálculo de `totalUnidades`. |
| `PedidoSecurityIntegrationTest` **[S07]** | Integración MVC con contexto Spring | Login requerido, autorización por roles, CSRF y acceso autenticado al resumen. |
| `SistemaPedidosSpringbootApplicationTests` | Contexto de Spring Boot | Carga del contexto de aplicación. |

En las pruebas de seguridad se utiliza:

```java
MockMvcBuilders.webAppContextSetup(context)
    .apply(springSecurity())
    .build();
```

**Regla importante:** `standaloneSetup(...)` no valida por sí mismo la cadena real de filtros. Por tanto, una prueba aislada de `PedidoApiController` no equivale a una prueba de autorización.

### Matriz mínima de pruebas de seguridad

| Escenario | Condición | Resultado esperado |
|---|---|---|
| Acceso anónimo | GET `/pedidos` sin sesión | Redirección al login. |
| Rol insuficiente | POST `/pedidos` como `CONSULTA` **con CSRF válido** | **403**, por autorización. |
| Token ausente | POST `/pedidos` como `OPERADOR` **sin CSRF** | **403**, por CSRF. |
| API autorizada | GET `/api/v1/orders/resumen` como `CONSULTA` | **200** y JSON con totales numéricos. |
| Resumen vacío | Servicio devuelve lista vacía | `totalPedidos=0` y `totalUnidades=0`. |
| Dos pedidos | Cantidades `2` y `3` simuladas con Mockito | `totalPedidos=2` y `totalUnidades=5`. |

Los tests de seguridad del repositorio simulan la identidad con utilidades de Spring Security Test y no necesitan autenticar usuarios manualmente en un navegador. Los tests de contratos JSON aislados no requieren levantar `inventario-service`.

## 11. Persistencia, integración y transacciones

### H2 local

La base de datos utiliza la URL:

```properties
spring.datasource.url=jdbc:h2:mem:pedidosdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.jpa.hibernate.ddl-auto=create-drop
```

Es una base **en memoria** y su esquema se crea y elimina según el ciclo de vida de la aplicación. Los pedidos de laboratorio no se conservan de forma permanente al reiniciar.

En el `application.properties` permanecen credenciales **demostrativas de H2**. La consola web está **desactivada**; estos parámetros no equivalen a usuarios de Spring Security.

### Integración remota

```text
sistema-pedidos (Spring Boot :8081)
   |
   | InventarioRestClient / RestClient
   v
inventario-service (Jakarta EE / WildFly :8082)
   |
   v
SQL Server
```

El GET de la pantalla JSP consulta productos desde inventario y pedidos desde H2. Por ello, la pantalla completa requiere que `inventario-service` esté disponible.

### Límite transaccional

`@Transactional` coordina la persistencia **local** de pedidos en H2. Una reserva de stock confirmada por HTTP en `inventario-service` **no se revierte automáticamente** al hacer rollback en H2. El proyecto no implementa una transacción distribuida ni una compensación automática. Esta limitación debe considerarse al diseñar reintentos y recuperaciones.

## 12. Limitaciones y siguientes pasos

- **Entorno de laboratorio:** usuarios y roles en memoria; contraseñas predeterminadas con fines docentes. Nunca desplegar tal cual en Internet.
- **Seguridad:** no incluye OAuth2/OIDC, JWT/Bearer, MFA, políticas de bloqueo, gestión persistente de usuarios, autorización por propietario ni auditoría completa.
- **Persistencia:** H2 es volátil; migrar a base de datos persistente para otro entorno.
- **Operación:** retirar secretos y valores demostrativos, emplear HTTPS, configurar perfiles, observabilidad y políticas apropiadas de manejo de sesiones.
- **Consistencia:** diseñar idempotencia, compensación y tratamiento de fallos entre H2 y el sistema remoto de inventario.
- **Validación:** ejecutar `clean test` y las pruebas manuales de login, roles, CSRF y flujo completo antes de compartir o desplegar cambios.

**Evoluciones sugeridas para sesiones posteriores:** externalizar configuración por perfiles, introducir Bean Validation con DTO de formularios, centralizar excepciones donde aporte valor y proteger APIs con un mecanismo apropiado para sus consumidores.

## 13. Referencias técnicas

- [Spring Framework — Web MVC](https://docs.spring.io/spring-framework/reference/web/webmvc.html)
- [Spring Framework — HTTP Message Conversion](https://docs.spring.io/spring-framework/reference/web/webmvc/message-converters.html)
- [Spring Security — Servlet Authorization](https://docs.spring.io/spring-security/reference/servlet/authorization/authorize-http-requests.html)
- [Spring Security — CSRF Protection](https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html)
- [Spring Security — MockMvc Test Support](https://docs.spring.io/spring-security/reference/servlet/test/mockmvc/index.html)
- [Spring Boot — Reference Documentation](https://docs.spring.io/spring-boot/reference/)

---

**Uso académico:** curso *Desarrollo de Aplicaciones Empresariales Avanzado* · Continuidad del caso **Sistema de Pedidos** · Sesión 7: **Spring MVC y Seguridad con Spring**.
