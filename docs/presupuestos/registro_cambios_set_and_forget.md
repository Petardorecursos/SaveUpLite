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
    *   Sliders para regla 50/30/20.
    *   Sección "Distribución de Ahorro": Sliders dinámicos por cada meta del usuario para asignar % del ahorro.
    *   Simulador en tiempo real.
*   **Modificadas**:
    *   `DashboardScreen.kt`: Añadido switch "Aplicar Presupuesto" en el diálogo de transacción.
    *   `MetasScreen.kt`: Actualizado para leer `montoActual` (antes `montoAhorrado`) y mostrar el saldo correcto.
    *   `DetalleMetaScreen.kt`: Actualizado para leer `montoActual`.

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

### C. Error "Unresolved Reference" en UI
*   **Problema**: `PlanificacionScreen` no encontraba las propiedades `needs`, `wants`, etc.
*   **Causa**: Error humano al editar `PlanificacionViewModel` donde accidentalmente se borraron estas propiedades del `data class PlanificacionUiState`.
*   **Solución**: Se restauraron las propiedades faltantes en el estado.
