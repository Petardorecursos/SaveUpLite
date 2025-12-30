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

## Estructura de Tareas
1.  **Backend Core**: interfaz `ReporteGenerator` y `CsvReporteGenerator`.
2.  **Backend API**: `ReporteController` con soporte de parámetros.
3.  **Frontend**: Integración en `ApiService` y UI de descarga.

## Beneficios Modularidad
*   Si mañana queremos PDF, solo creamos `PdfReporteGenerator` y lo registramos. El Controller no cambia. El Frontend solo habilita un nuevo botón.
