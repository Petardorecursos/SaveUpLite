# Plan de Verificación: Insights en Pantalla de Análisis

Este plan valida la correcta visualización de las tarjetas de diagnóstico inteligente en `AnalysisScreen`.

## 1. Escenario Base: Mes Actual con Pocos Gastos
*   **Condición**: Tener ingresos registrados (ej. 100k) y muy pocos gastos.
*   **Acción**: Ir a pantalla Análisis.
*   **Resultado Esperado**:
    *   **Tarjeta**: "¡Excelente Salud Financiera!".
    *   **Color**: Verde (PaleAqua).
    *   **Icono**: Pulgar Arriba (ThumbUp).
    *   **Mensaje**: "Tus gastos están bajo control... tienes margen para aumentar tu Ahorro".

## 2. Escenario Crítico: Necesidades Desbordadas
*   **Condición**: Registrar gastos en categorías tipo `NECESIDAD` (Comida, Arriendo) que superen el 50% de los ingresos.
    *   Ej: Ingreso 100k -> Presupuesto Necesidad 50k. Gastar 60k en Comida.
*   **Acción**: Ir a pantalla Análisis.
*   **Resultado Esperado**:
    *   **Tarjeta**: "Atención con tus Necesidades".
    *   **Color**: Rojo Claro (PalePink).
    *   **Icono**: Advertencia (Warning).
    *   **Mensaje**: "...superan el presupuesto asignado (50%). Revisa categorías como Arriendo...".

## 3. Escenario Crítico: Exceso de Deseos
*   **Condición**: Registrar gastos en categorías tipo `DESEO` (Cine, Ocio) que superen el 30% de los ingresos.
    *   Ej: Ingreso 100k -> Presupuesto Deseo 30k. Gastar 35k en Cine.
*   **Acción**: Ir a pantalla Análisis.
*   **Resultado Esperado**:
    *   **Tarjeta**: "Ojo con los 'Gustitos'".
    *   **Color**: Rojo/Naranja (PalePink).
    *   **Icono**: Corazón/Mano (VolunteerActivism).
    *   **Mensaje**: "...Has excedido tu límite para Deseos... Intenta reducir compras impulsivas...".

## 4. Escenario Neutro: Al límite pero bien
*   **Condición**: Gastar un 95% del presupuesto de Necesidades (48k de 50k) y un 95% de Deseos.
*   **Acción**: Ir a pantalla Análisis.
*   **Resultado Esperado**:
    *   **Tarjeta**: "Estás dentro del Presupuesto".
    *   **Color**: Azulado/Teal (PaleTeal).
    *   **Icono**: Check Circle.
    *   **Mensaje**: "Tus gastos van acordes a lo planificado...".

## 5. Escenario Histórico (Mes Anterior)
*   **Condición**: Cambiar el selector de fecha a un mes donde no hubo ingresos ni gastos.
*   **Acción**: Ver pantalla.
*   **Resultado Esperado**:
    *   **Tarjeta**: NO debe aparecer (si `ejecucion` es null o todo es 0). O si aparece, mensaje "Excelente" (0/0 se maneja como 0%).
    *   **Lista**: Texto "No hay datos para este mes".
