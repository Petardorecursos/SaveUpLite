# Documentación de Cambios: Funcionalidad "Set & Forget" (Planificación Financiera)

Este documento detalla todas las modificaciones, creaciones y soluciones implementadas para la funcionalidad de Presupuesto Automático y Mapeo de Metas.

## 1. Backend (Java Spring Boot)

### Entidades Nuevas
*   **`ConfiguracionPresupuesto.java`**: Almacena los porcentajes globales (50/30/20) y el estado activo del presupuesto por usuario.
*   **`AsignacionMetaPresupuesto.java`**: Entidad intermedia para asignar un porcentaje específico del "Ahorro" a una `MetaAhorro` concreta.

### Entidades Modificadas
*   **`MetaAhorro.java`**:
    *   Se añadió el campo `montoActual` (columna `MONTO_ACTUAL`) para persistir el saldo de la meta en la base de datos, en lugar de calcularlo siempre en tiempo real. Esto optimiza las consultas y permite actualizaciones atómicas desde el servicio de movimientos.
*   **`Movimiento.java`**:
    *   Se descomentó/habilitó la relación `@ManyToOne` con `MetaAhorro` para permitir vincular movimientos de tipo `ABONO_META` directamente a la meta afectada.

### DTOs (Data Transfer Objects)
*   **Nuevos**:
    *   `ConfiguracionPresupuestoRequestDTO`: Para recibir la configuración completa (porcentajes + lista de asignaciones).
    *   `AsignacionPresupuestoDTO`: Para transferir el id de la meta y su porcentaje asignado.
*   **Modificados**:
    *   `MovimientoRegistroDTO`: Se añadió el campo `Boolean aplicarPresupuesto` para activar el "Smart Split".
    *   `MetaAhorroResponseDTO`: Se renombró el campo `montoAhorrado` a `montoActual` para coincidir con la entidad y evitar confusiones en el frontend.

### Repositorios Nuevos
*   `ConfiguracionPresupuestoRepository`: Acceso a datos de configuración por RUT.
*   `AsignacionMetaPresupuestoRepository`: Acceso a las asignaciones por ID de configuración.

### Controladores
*   **Nuevo**: `ConfiguracionPresupuestoController`: Endpoints `GET` y `POST` para gestionar la configuración del usuario. Maneja la estrategia de "borrar y recrear" para las asignaciones al guardar.
*   **Existente**: `MetaAhorroController`: No sufrió cambios estructurales mayores, pero indirectamente devuelve el nuevo campo en los DTOs.

### Servicios
*   **`MovimientoService`**:
    *   Implementación de lógica "Smart Split" en `registrarMovimiento`:
        *   Detecta `aplicarPresupuesto = true`.
        *   Calcula el monto destinado a ahorro.
        *   Distribuye ese monto entre las metas configuradas (`AsignacionMetaPresupuesto`).
        *   Crea movimientos automáticos `ABONO_META`.
        *   **CRÍTICO**: Actualiza directamente `MetaAhorro.montoActual` al realizar el abono.
*   **`MetaAhorroService`**:
    *   Actualizado para usar y mantener el campo `montoActual`.
    *   Al realizar abonos/retiros manuales, ahora actualiza la columna en BD.
    *   Al convertir a DTO, lee directamente de `montoActual` en lugar de sumar historiales.

---

## 2. Frontend (Kotlin Jetpack Compose)

### Pantallas (UI)
*   **Nueva**: `PlanificacionScreen.kt`:
    *    **Refactor Visual Soft UI**: Diseño moderno con tarjetas y colores semánticos (PaleAqua, PalePink, PaleTeal).
    *   **Sliders 50/30/20 & Mapeo de Metas**: Ajuste dinámico de distribución.
    *   **Sección Colapsable**: La sección de asignación de metas se contrae automáticamente si el ahorro es 0%, mejorando la claridad.
    *   **Simulador Vivo**: Feedback visual inmediato con tarjetas de colores.
*   **Modificadas**:
    *   `DashboardScreen.kt`: Añadido switch "Aplicar Presupuesto".
    *   `MetasScreen.kt`:
        *   Lectura de `montoActual`.
        *   **Banner Promocional Inteligente**: Nueva tarjeta `BudgetPlanPromoCard`. Detecta si el usuario tiene presupuesto activo:
            *   *Sin Plan*: Invita a probar el planificador.
            *   *Con Plan*: Botón de acceso directo a la edición del plan.
    *   `DetalleMetaScreen.kt`: Lectura de `montoActual`.

### ViewModels
*   **Nuevo**: `PlanificacionViewModel.kt`: Maneja el estado de la configuración, validaciones (suma 100%) y comunicación con API.
*   **Modificados**:
    *   `MetaAhorroViewModel.kt`: Actualizado para usar `montoActual` al calcular totales.
    *   `DashboardViewModel.kt`: Lógica para pasar el flag `aplicarPresupuesto`.

### Modelos Frontend
*   `ConfiguracionPresupuesto`, `ConfiguracionPresupuestoDTO`: Reflejan la estructura del backend.
*   `MetaAhorro`: Campo renombrado de `montoAhorrado` a `montoActual`.

---

## 3. Problemas Detectados y Soluciones

### A. Inconsistencia de Datos en Metas (El problema de los "Ceros")
*   **Problema**: Al registrar un ingreso con presupuesto, el saldo de las metas no se actualizaba en la pantalla `MetasScreen`, manteniéndose en 0.
*   **Causa Raíz**:
    1.  El Backend calculaba el saldo en tiempo real sumando movimientos, pero los movimientos automáticos del smart-split no estaban vinculados correctamente a la entidad Meta en BD (faltaba `setMetaAhorro`).
    2.  El DTO de respuesta del backend enviaba el campo como `montoAhorrado`, pero el Frontend (tras un ajuste intermedio) y la Entidad usaban `montoActual`.
*   **Solución**:
    1.  Se estandarizó todo a `montoActual` (Entidad, DTO Backend, Modelo Frontend).
    2.  Se actualizó `MovimientoService` para vincular correctamente la meta (`setMetaAhorro`).
    3.  Se cambió la estrategia en `MetaAhorroService` para persistir el saldo en la columna `montoActual` y leer de ahí, garantizando consistencia y rendimiento.

### B. Ambigüedad en Compilación (Overload Resolution Ambiguity)
*   **Problema**: Error de compilación en Kotlin `sumOf` en `MetaAhorroViewModel`.
*   **Causa**: Al eliminar/renombrar `montoAhorrado` a `montoActual`, el compilador perdió la referencia de tipo (Double) y no supo qué sobrecarga de `sumOf` usar.
*   **Solución**: Actualizar la referencia en el lambda a `it.montoActual`, permitiendo al compilador inferir correctamente el tipo `Double`.

### D. Error "Unresolved Reference: ArrowForward / AutoGraph"
*   **Problema**: Error de compilación en `MetasScreen` al usar iconos extendidos.
*   **Causa**: Se intentó utilizar la ruta completa del paquete (`androidx.compose.material.icons.filled...`) dentro del composable `Icon`, lo cual no es la sintaxis standard para acceder a los objetos singleton de iconos en Compose.
*   **Solución**: Se cambió el acceso a `Icons.Filled.ArrowForward` y `Icons.Filled.AutoGraph`, consistente con los imports.

---

## 4. Análisis de Lógica: El Impacto de "Necesidades" y "Deseos"

Actualmente, el sistema "Set & Forget" tiene un impacto directo en el **Ahorro** (moviendo dinero a Metas). Sin embargo, los porcentajes de **Necesidades** y **Deseos** son meramente informativos en la simulación. Para que tengan un impacto real en la experiencia del usuario, se propone la siguiente evolución:

### Estrategia de Solución Propuesta

1.  **Categorización Semántica**:
    *   Modificar la entidad `Categoria` para incluir un campo `TipoPresupuesto` (valores: `NECESIDAD`, `DESEO`, `AHORRO/INVERSION`).
    *   Ejemplo: "Alquiler" -> `NECESIDAD`, "Cine" -> `DESEO`.

2.  **Seguimiento de Presupuesto en Tiempo Real**:
    *   En lugar de solo *mover* dinero, el sistema debe *monitorear* el gasto.
    *   **Cálculo**:
        *   `Presupuesto Necesidades` = Total Ingresos del Mes * % Configurado (ej. 50%).
        *   `Gasto Real Necesidades` = Suma de movimientos categorizados como `NECESIDAD`.
    *   **Visualización**: Mostrar barras de progreso en el Dashboard o Análisis: "Has gastado el 80% de tu presupuesto para Necesidades".

3.  **Alertas Inteligentes**:
    *   Notificar al usuario cuando esté cerca de exceder su asignación para "Deseos", fomentando el control de gastos innecesarios.

Esta implementación cerraría el ciclo del presupuesto 50/30/20, pasando de una calculadora pasiva a una herramienta de control activo.

---

## 5. Implementación: Seguimiento en Tiempo Real (Necesidades y Deseos)

### Backend (Java/Spring Boot)
*   **Modelo de Datos**:
    *   **Nuevo Enum `TipoPresupuesto`**: Define los tipos de presupuesto: `NECESIDAD`, `DESEO`, `AHORRO`, `OTROS`.
    *   **Actualización `Categoria`**: Se añadió la columna `tipoPresupuesto`.
    *   **`DataLoader`**: Actualizado para asignar automáticamente el tipo de presupuesto correcto a las categorías predeterminadas durante la inicialización (ej: Comida -> NECESIDAD, Ocio -> DESEO).
*   **Lógica de Negocio y API**:
    *   **Nuevo DTO `EjecucionPresupuestoDTO`**: Estructura para transportar los datos de ejecución presupuestaria (presupuesto vs gasto real).
    *   **Actualización `MovimientoRepository`**: Nuevo método `findByUsuarioRutAndFechaBetween` para consultas eficientes por rango de fechas.
    *   **Nuevo Endpoint**: `GET /api/presupuestos/ejecucion/{rut}` en `ConfiguracionPresupuestoController`. Este endpoint calcula dinámicamente:
        *   Ingresos totales del mes seleccionado.
        *   Presupuesto asignado para Necesidades y Deseos (basado en los % configurados por el usuario).
        *   Gasto Real actual agrupado por el `TipoPresupuesto` de las categorías de los gastos.

### Frontend (Kotlin/Compose)
*   **Integración**:
    *   Actualización de `ApiService` y modelos (`CategoriaDTO`, `TipoPresupuesto`) para soportar los nuevos campos y endpoints.
    *   **`DashboardViewModel`**: Ahora carga los datos de ejecución presupuestaria (`cargarEjecucionPresupuesto`) al iniciar y al cambiar el filtro de fecha.
*   **Interfaz de Usuario (Financial Health Widget)**:
    *   **Refactorización**: El widget `FinancialHealthWidget` en `DashboardScreen` ahora es expandible.
    *   **Visualización de Presupuesto**: Al expandir el widget, se muestra una nueva sección "Ejecución Presupuestaria".
        *   Incluye barras de progreso visuales para **Necesidades** y **Deseos**.
        *   **Feedback Detallado**: Las barras ahora muestran el formato `% Gastado ($Gastado / $PresupuestoTotal)`, permitiendo al usuario ver claramente su límite incluso si no ha gastado nada (0%).
        *   Utiliza colores semánticos (PaleAqua para Necesidades, PaleSalmon para Deseos) para facilitar la lectura rápida.
    *   **Ayuda Contextual**:
        *   Se integró un **botón de Información (i)** que despliega el `BudgetExplanationDialog`.
        *   Este diálogo explica didácticamente cómo funciona el presupuesto dinámico (basado en ingresos) y da ejemplos de la regla 50/30/20, aclarando que es un ejemplo para no confundir a usuarios con configuraciones personalizadas.
