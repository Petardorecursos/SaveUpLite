# SaveUp Lite

**Autores:** Ricardo Henríquez, Hans Gómez  
**Trello:** [Enlace al Trello del Proyecto](https://trello.com/invite/b/68f6ed701025ea0dc520174f/ATTI84ea356b881b93bd5746b75b1c2c2a3e3E37BAA7/saveuplite)  
**Repositorio Frontend (Android):** [Petardorecursos/SaveUpLite-Android](https://github.com/Petardorecursos/SaveUpLite)  
**Repositorio Backend (Spring Boot):** * https://github.com/ItsMilkey/LITE *

---

## 1. ¿Qué es SaveUp Lite?

**SaveUp Lite** es una aplicación de finanzas personales, nativa de Android, diseñada para ofrecer una gestión financiera ágil y centrada en el usuario. A diferencia de aplicaciones más complejas, SaveUp Lite se enfoca en la simplicidad y eficiencia para el registro de transacciones diarias y el seguimiento de obligaciones financieras.

Construida con tecnologías modernas, la aplicación se conecta a un robusto backend desarrollado en Spring Boot, siguiendo una arquitectura orientada a servicios que garantiza la integridad y seguridad de los datos.

---

## 2. Características Implementadas

*    **Autenticación de Usuarios:** Registro y login seguros contra el backend.
*    **Dashboard Principal:** Visualización inmediata del saldo actual y un historial de los últimos movimientos registrados.
*    **Gestión de Transacciones:** Registro rápido de ingresos y gastos generales a través de un diálogo intuitivo.
*    **Historial Paginado:** Una pantalla dedicada para ver todo el historial de movimientos, con carga paginada para un rendimiento óptimo.
*    **Módulo de Gestión de Deudas (Completo):
    *   **Creación y visualización** de deudas con monto total y número de cuotas.
    *   **Seguimiento de progreso** con una barra visual y un indicador "X de Y cuotas pagadas" que se calcula en tiempo real basado en el monto pagado.
    *   **Registro de pagos** con un sistema flexible que permite al usuario pagar el monto de la cuota calculado o un monto personalizado.
*    **Conversor de Moneda:** Herramienta útil para conversiones rápidas.
*    **Navegación Intuitiva:** Interfaz limpia y moderna construida con Jetpack Compose y Material 3, con una barra de navegación inferior y un menú lateral.

---

## 3. Arquitectura y Stack Tecnológico

El proyecto sigue una arquitectura cliente-servidor desacoplada.

### Frontend (Android)
*   **Lenguaje:** Kotlin
*   **UI:** Jetpack Compose (declarativa y moderna)
*   **Arquitectura:** MVVM (Model-View-ViewModel)
*   **Asincronía:** Kotlin Coroutines y Flow para gestionar operaciones en segundo plano y flujos de datos reactivos.
*   **Navegación:** Jetpack Navigation Compose.
*   **Networking:** Retrofit y Gson para la comunicación con la API REST.
*   **Diseño:** Material 3.

### Backend (Spring Boot)
*   **Framework:** Spring Boot
*   **Lenguaje:** Java
*   **Base de Datos:** PostgreSQL (Producción) / Oracle (Desarrollo).
*   **Arquitectura:** API REST siguiendo un modelo orientado a transacciones, donde el saldo y otros valores son cálculos derivados del historial de movimientos, garantizando la integridad de los datos.
*   **Despliegue:** Contenerizado con Docker y desplegado en la plataforma Render.

---

## 4. Pasos para Ejecutar el Proyecto

Para ejecutar el proyecto completo, necesitas tener tanto el backend como el frontend funcionando.

### 4.1. Ejecutar el Backend

1.  Clona el repositorio del backend: `git clone <URL_DEL_REPO_BACKEND>`.
2.  Abre el proyecto en tu IDE preferido (ej. IntelliJ IDEA).
3.  Configura el archivo `application.properties` o `application-dev.properties` con los datos de conexión a tu base de datos (Oracle o PostgreSQL).
4.  Ejecuta la aplicación Spring Boot.

Por defecto, el servidor se iniciará en `http://localhost:8080`.

### 4.2. Ejecutar la App Android (Frontend)

1.  **Clonar el Repositorio:**
    
    git clone https://github.com/Petardorecursos/SaveUpLite.git

2.  **Abrir en Android Studio:** Abre el proyecto clonado con la última versión estable de Android Studio.

3.  **Configurar la Conexión al Backend (¡Paso Clave!):**
    *   Abre el archivo `app/src/main/java/com/example/saveuplite/api/RetrofitClient.kt`.
    *   Modifica la constante `BASE_URL` para que apunte a la dirección de tu backend.
        *   **Si usas un Emulador de Android:** La dirección para `localhost` es `http://10.0.2.2:8080/`.
        *   **Si usas un Dispositivo Físico:** Debes usar la dirección IP de tu máquina en la red local. Asegúrate de que tu dispositivo y tu computadora estén en la misma red Wi-Fi. La URL se verá así: `http://192.168.1.105:8080/` (reemplaza con tu IP).

4.  **Ejecutar la App:**
    *   Selecciona un emulador o un dispositivo físico conectado.
    *   Presiona "Run 'app'" (Shift + F10).

Android Studio compilará el código, instalará la aplicación y la ejecutará en el dispositivo seleccionado.

---

## 5. Estructura del Proyecto (Frontend)

El código fuente de la aplicación Android está organizado en los siguientes paquetes principales:

-   `com.example.saveuplite`
    -   `api/`: Contiene la configuración de Retrofit (`RetrofitClient`) y la interfaz `ApiService` que define los endpoints de la API.
    -   `model/`: Contiene las clases de datos (`data class`) que representan las entidades del negocio (Usuario, Deuda, Movimiento, etc.).
    -   `ui/`: Contiene todos los componentes de la interfaz de usuario construidos con Jetpack Compose.
        -   `screens/`: Cada subpaquete corresponde a una pantalla o funcionalidad principal (auth, dashboard, deudas, etc.).
        -   `navigation/`: Define el grafo de navegación (`NavGraph`) y las rutas de la aplicación.
        -   `theme/` y `utils/`: Temas de la aplicación y clases de utilidad (ej. `NumberVisualTransformation`).
    -   `viewmodel/`: Contiene las clases `ViewModel`, que gestionan el estado y la lógica de la UI, siguiendo el patrón MVVM.
