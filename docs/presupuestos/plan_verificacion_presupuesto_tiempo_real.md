# Plan de Verificación: Seguimiento de Presupuesto en Tiempo Real

Este documento detalla los pasos para validar que la funcionalidad de "Necesidades vs Deseos" funcione correctamente de punta a punta.

## 1. Verificación Técnica (Backend)

### 1.1 Base de Datos
*   **Objetivo**: Confirmar que la migración de datos se ejecutó correctamente.
*   **Pasos**:
    1.  Conectar a la BD (H2 o Oracle).
    2.  Ejecutar query: `SELECT * FROM CATEGORIA`.
    3.  **Validación**:
        *   La columna `TIPO_PRESUPUESTO` debe existir.
        *   Categoría "Comida" debe tener `TIPO_PRESUPUESTO = 'NECESIDAD'`.
        *   Categoría "Ocio y Entretenimiento" debe tener `TIPO_PRESUPUESTO = 'DESEO'`.

### 1.2 Endpoint API
*   **Objetivo**: Confirmar que el endpoint de ejecución calcula bien los totales.
*   **Herramienta**: Postman / Curl.
*   **Request**: `GET http://localhost:8080/api/presupuestos/ejecucion/{RUT}`
*   **Validación**:
    *   Status 200 OK.
    *   JSON debe contener campos `presupuestoNecesidades`, `gastoNecesidades`, `presupuestoDeseos`, `gastoDeseos`.
    *   `presupuestoNecesidades` debe ser (Ingresos * %Necesidades).

---

## 2. Verificación Funcional (Frontend / Usuario)

### Escenario A: Estado Inicial
1.  Iniciar sesión con un usuario nuevo o existente.
2.  Ir al Dashboard.
3.  Expandir el widget "Salud Financiera".
4.  **Resultado Esperado**:
    *   Si no hay ingresos este mes, las barras de presupuesto deben estar vacías o en 0.
    *   Si hay ingresos, las barras deben mostrar el presupuesto disponible total (Gris o vacío si gasto es 0).

### Escenario B: Registro de Ingreso (Aumentar Presupuesto)
1.  Botón "Ingreso" -> Agregar $100.000 (Sueldo).
2.  Asegurarse de tener configurado el presupuesto (ej. 50/30/20).
3.  Volver al Dashboard.
4.  **Resultado Esperado**:
    *   Barra "Necesidades": Debe indicar presupuesto de $50.000 (50% de 100k).
    *   Barra "Deseos": Debe indicar presupuesto de $30.000 (30% de 100k).
    *   Gastado: $0.

### Escenario C: Registro de Gasto - Necesidad
1.  Botón "Gasto" -> $10.000.
2.  Categoría: **"Comida"** (u otra que sea NECESIDAD).
3.  Guardar.
4.  **Resultado Esperado**:
    *   Barra "Necesidades": Debe mostrar progreso (20% llenado -> 10k de 50k).
    *   Barra "Deseos": Sin cambios.

### Escenario D: Registro de Gasto - Deseo
1.  Botón "Gasto" -> $5.000.
2.  Categoría: **"Ocio y Entretenimiento"** (u otra que sea DESEO).
3.  Guardar.
4.  **Resultado Esperado**:
    *   Barra "Necesidades": Sin cambios.
    *   Barra "Deseos": Debe mostrar progreso (~16% llenado -> 5k de 30k).

### Escenario E: Exceder Presupuesto
1.  Botón "Gasto" -> $50.000.
2.  Categoría: **"Comida"**.
3.  Guardar.
4.  **Resultado Esperado**:
    *   Barra "Necesidades": Debe llenarse completamente y cambiar a color **Rojo** (indicando sobregiro, total gastado 60k de 50k).

### Escenario F: Filtro por Fecha
1.  Usar el selector de fecha del widget para ir al mes anterior.
2.  **Resultado Esperado**:
    *   Los datos deben actualizarse para mostrar solo los ingresos y gastos de ese mes específico.
    *   Las barras deben re-calcularse según los ingresos de *ese* mes histórico.

### Escenario G: Ingresos Adicionales (Escalabilidad del Presupuesto)
1.  Estando en el mes actual (donde ya ingresamos $100.000).
2.  Registrar un **nuevo Ingreso** de $50.000 (ej. "Bono"). Aplicar presupuesto on.
3.  Volver al Dashboard.
4.  **Resultado Esperado**:
    *   **Ingresos Totales**: Deben ser $150.000.
    *   **Barra "Necesidades"**: El presupuesto base debe aumentar automáticamente.
        *   Cálculo: 50% de $150.000 = **$75.000**.
        *   Visualización: `XX% ($TotalGastado / $75.000)`.
    *   **Barra "Deseos"**:
        *   Cálculo: 30% de $150.000 = **$45.000**.
        *   Visualización: `XX% ($TotalGastado / $45.000)`.
    *   *Nota*: El progeso porcentual (%) debería bajar ligeramente, ya que ahora tienes el mismo gasto pero un presupuesto mayor ("dilución del gasto").

### Escenario H: Ingreso Sin Presupuesto Automático (Switch OFF)
1.  Registrar un **nuevo Ingreso** de $50.000 (ej. "Venta Garage").
2.  **Desactivar** el switch "Aplicar Presupuesto (Set & Forget)".
3.  Guardar.
4.  **Resultado Esperado**:
    *   **Ingresos Totales**: Aumentan a $200.000 (acumulado anterior + $50k).
    *   **Barra "Necesidades" y "Deseos"**: El presupuesto disponible **DEBE aumentar** igual que en el Escenario G.
        *   El sistema considera *todos* los ingresos líquidos para calcular tu capacidad de gasto (Necesidades/Deseos), independientemente de si decidiste ahorrar automáticamente o no en esa transacción específica.
        *   Needs: $100.000 ($200k * 50%).
        *   Wants: $60.000 ($200k * 30%).
    *   *Verificación Adicional*: Revisar pantalla "Mis Metas". El saldo de las metas **NO** debería haber aumentado proporcionalmente a estos $50k (porque apagamos el switch).

---

## 3. Checklist Rápido de Solución de Problemas

*   **¿Las barras no aparecen?**
    *   Verificar que el usuario tenga configuración de presupuesto creada (ir a Planificación). Sin configuración, usa defaults (50/30), pero el endpoint debe responder.
*   **¿El gasto no sube?**
    *   Verificar que la categoría seleccionada tenga el `TipoPresupuesto` correcto en base de datos.
    *   Asegurar que el movimiento tenga fecha del mes actual.
*   **¿Error de conexión?**
    *   Revisar logs del Backend para ver si el endpoint `ejecucion` está lanzando excepción (posible nulo en cálculos).
