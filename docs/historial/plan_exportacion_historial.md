# Plan de Implementación: Exportación de Historial (CSV y Extensible)

## Objetivo
Permitir al usuario descargar un reporte de sus movimientos financieros desde la pantalla de Historial, con opciones para exportar todo el historial o solo un mes específico. El diseño debe ser extensible para soportar formatos futuros como PDF.

## Estrategia Técnica
Optaremos por un patrón de diseño **Strategy** en el backend para la generación de reportes. Implementaremos inicialmente la estrategia CSV, dejando la arquitectura lista para inyectar una estrategia PDF en el futuro sin modificar ni el controlador ni la lógica de negocio principal.

### 1. Backend (`saveup`)

#### Abstracción: Servicio de Reportes
*   **Interfaz**: `ReporteGenerator`
    *   Método: `byte[] generarReporte(List<Movimiento> movimientos)`
    *   Método: `String getContentType()`
    *   Método: `String getFileExtension()`

*   **Implementación 1**: `CsvReporteGenerator` (Fase 1)
    *   Usa `StringBuilder` para crear CSV con separador `;` y BOM UTF-8.
*   **Implementación 2**: `PdfReporteGenerator` (Fase Futura)
    *   Usaría iText o OpenPDF.

#### Nuevo Controlador: `ReporteController`
Endpoint único que delega en una fábrica o selecciona el generador según un parámetro `formato`.

*   **Endpoint**: `GET /api/reportes/movimientos/exportar`
*   **Parámetros**:
    *   `rut` (String, requerido)
    *   `alcance` (String): "COMPLETO" | "MENSUAL"
    *   `formato` (String): "CSV" (Default) | "PDF" (Futuro)
    *   `mes`, `anio` (Int, opcional)
*   **Lógica**:
    1.  Recuperar datos (Movimientos).
    2.  Seleccionar `ReporteGenerator` basado en `formato`.
    3.  Generar bytes.
    4.  Retornar `ResponseEntity` con el Content-Type correcto dinámicamente.

### 2. Frontend (`LITEfront`)

#### Actualización en `TransactionHistoryScreen`
*   **UI**: Botón "Exportar".
*   **Diálogo de Selección**:
    *   **Periodo**: [ Mes Actual ] [ Todo el Historial ]
    *   **Formato**: [ CSV (Excel) ] (PDF deshabilitado o "Próximamente")
*   **Lógica de Descarga**:
    *   Genérica: Recibe `ResponseBody`, lee el `Content-Type` o usa una extensión por defecto según lo que pidió, y guarda en Downloads.

## Estado: COMPLETADO (30/12/2025)

## Implementación Final

### 1. Backend (`saveup`)
Se implementó el patrón **Strategy** con éxito.

*   **Interfaz**: `ReporteGenerator` creada.
*   **Estrategias**:
    *   `CsvReporteGenerator`: Genera CSV compatible con Excel (BOM UTF-8, separador `;`).
    *   `PdfReporteGenerator`: Genera PDF estilizado usando **OpenPDF**. Incluye colores condicionales (Verde/Rojo) y tabla estructurada.
*   **Servicio**: `ReporteService` selecciona la estrategia dinámicamente según el parámetro `formato`.
*   **Controlador**: `ReporteController` expone el endpoint único y maneja la descarga de archivos (Streaming).

**Mejoras Adicionales de Integridad de Datos:**
*   **Categoría 'Ahorro'**: Se modificó `DataLoader` para asegurar la existencia de esta categoría (Color Índigo).
*   **Lógica de Negocio**:
    *   Los **Abonos Automáticos** (`MovimientoService`) ahora se asignan automáticamente a la categoría 'Ahorro'.
    *   Los movimientos manuales de **Abono/Retiro de Metas** (`MetaAhorroService`) también reciben esta categoría.

### 2. Frontend (`LITEfront`)

*   **TransactionHistoryScreen**:
    *   Botón "Exportar" funcional.
    *   Diálogo con opciones: **Alcance** (Mes Seleccionado/Actual vs. Historial Completo) y **Formato** (CSV vs. PDF).
    *   Manejo de descarga robusto usando `MediaStore` para guardar en la carpeta de Descargas del usuario.
*   **DashboardScreen**:
    *   Se excluyó la categoría 'Ahorro' de los selectores manuales de ingreso/gasto para mantener la consistencia (solo el sistema de metas debe usarla).
*   **AnalysisScreen**:
    *   Se incluyeron los movimientos de tipo `ABONO_META` en los gráficos y listas.
    *   Se asignó un color distintivo (Índigo) para visualizarlos como 'Ahorro'.
