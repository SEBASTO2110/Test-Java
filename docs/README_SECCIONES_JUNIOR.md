# Secciones del README – Extracurriculares, Financiero y Recursos

> Texto listo para integrar en el `README.md` final. Cada bloque indica la sección a la que pertenece.

---

## 4. Tecnologías utilizadas (librerías agregadas por estos módulos)

| Librería | Archivo en `lib/` | Uso |
|---|---|---|
| OpenPDF 1.4.2 | `openpdf-1.4.2.jar` | Generación de la factura electrónica en PDF |
| ZXing Core 3.5.3 | `zxing-core-3.5.3.jar` | Matriz del código QR de la factura |

Ambas están registradas en `nbproject/project.properties` (NetBeans) y IntelliJ las toma de la carpeta `lib/` (`little-horizons-erp.iml`).
Las facturas se guardan en la carpeta `invoices/` del proyecto (ignorada por git).

---

## 8. Estructura del proyecto (paquetes de estos módulos)

```
com.littlehorizons.erp
├── model/
│   ├── extracurricular/   Club, ClubEnrollment, Event, EventParticipation
│   ├── finance/           PaymentConcept, StudentConceptPrice, Payment, Invoice, InvoiceDocument
│   ├── resources/         Resource, ResourceCategory, Loan (abstracta), StudentLoan, TeacherLoan
│   ├── enums/             MembershipStatus, EventType, ParticipationRole, CompetitionPosition,
│   │                      PaymentMethod, PaymentPeriod, ResourceStatus, LoanStatus, BorrowerType
│   └── report/            ClubOccupancy, StudentPodiumRanking, RecurringPaymentStatus, DelinquentStudent
├── dao/
│   ├── extracurricular/   ClubDao, ClubEnrollmentDao, EventDao, EventParticipationDao (+ impl/)
│   ├── finance/           PaymentConceptDao, StudentConceptPriceDao, PaymentDao, InvoiceDao (+ impl/)
│   ├── resources/         ResourceDao, ResourceCategoryDao, LoanDao<T> (+ impl/AbstractLoanDaoImpl...)
│   └── jdbc/              TransactionWork + JdbcHelper.inTransaction(...) (agregados)
├── controller/
│   ├── PersonRules        validación compartida: solo estudiantes/profesores activos
│   ├── extracurricular/   ClubController, EventController
│   ├── finance/           PaymentConceptController, PaymentController, InvoiceController
│   └── resources/         ResourceController, LoanController
├── view/
│   ├── extracurricular/   ClubView, EventView, ExtracurricularPickers
│   ├── finance/           PaymentConceptView, PaymentView, FinancePickers
│   └── resources/         ResourceView, LoanView, ResourcePickers
└── util/                  SchoolInfo, CufeGenerator, InvoicePdfGenerator (+ Formats.money)
```

Los tres módulos siguen la misma arquitectura del módulo académico: interfaces DAO con su implementación sobre
`JdbcHelper`, controladores que reciben sus dependencias por constructor (armados en `Main`) y vistas que extienden
`BaseMenuView` y reutilizan `AcademicPickers` para elegir estudiantes y profesores.

---

## 9. Principios y patrones aplicados en estos módulos

### 9.1 SOLID

- **S – Responsabilidad única**: `InvoiceController` arma los datos y calcula el IVA; `CufeGenerator` solo calcula el hash;
  `InvoicePdfGenerator` solo dibuja el PDF a partir de un `InvoiceDocument`, sin conocer DAOs ni controladores.
- **O – Abierto/cerrado**: `AbstractLoanDaoImpl<T>` contiene toda la lógica de préstamos; un nuevo tipo de prestatario solo
  necesita una subclase con los nombres de sus tablas (como `StudentLoanDaoImpl` y `TeacherLoanDaoImpl`).
- **L – Sustitución de Liskov**: `StudentLoan` y `TeacherLoan` se usan como `Loan` en los reportes de préstamos activos y
  vencidos (`LoanController.overdueLoans` los mezcla con `Stream.concat`).
- **I – Segregación de interfaces**: los DAO de relaciones (membresías, participaciones, precios) tienen interfaces propias con
  solo los métodos que necesitan, en lugar de implementar todo `GenericDao`.
- **D – Inversión de dependencias**: los controladores dependen de interfaces (`PaymentDao`, `LoanDao<StudentLoan>`...) que
  `Main` les inyecta por constructor.

### 9.2 Patrones de diseño

| Patrón | Dónde | Por qué |
|---|---|---|
| **DAO** | `dao/extracurricular`, `dao/finance`, `dao/resources` | Aísla el SQL del resto de la aplicación. |
| **Template Method** | `AbstractLoanDaoImpl` | El algoritmo de préstamo/devolución está en la base; las subclases solo dan las tablas. |
| **Unit of Work (transacción)** | `JdbcHelper.inTransaction` | Préstamo + estado del recurso (y devolución + estado) se guardan juntos o no se guarda nada. |
| **DTO / Record** | `InvoiceDocument`, `model/report/*` | Transportan datos ya calculados hacia la vista y el PDF. |
| **Composite** | `CompositeMenuView` (de la base) | Cada módulo es un menú compuesto por sus submenús. |

### Lambdas y Stream API

Ocupación de clubes (`sorted` por porcentaje), participación por tipo (`groupingBy` + `counting`), ranking de ganadores
(`groupingBy` por estudiante y puntaje ponderado), morosos (`filter` + `map` + `sorted`), recaudo por concepto y método
(`groupingBy` + `reducing` con `BigDecimal`), préstamos vencidos (`Stream.concat` + `sorted`) e inventario por categoría y estado
(`groupingBy` anidado).

---

## 10. Funcionalidades principales

### Actividades Extracurriculares (menú 4)
- **Clubes**: CRUD (nombre único, descripción, cupo máximo 1-200, profesor encargado activo, horario) y activar/desactivar.
  No se puede eliminar un club con miembros activos ni bajar el cupo por debajo de los miembros actuales.
- **Inscripción**: valida estudiante activo, club activo, que no esté ya inscrito y **que no se exceda el cupo máximo**.
  Registra fecha de ingreso y estado (Activa / Retirada); el retiro guarda fecha y motivo.
- **Eventos**: CRUD con tipo (Académico, Cultural, Deportivo, Social, Científico), fecha, lugar, organizador y competencia.
- **Participantes**: rol (Participante, Competidor, Organizador, Asistente) sin duplicados.
- **Posiciones**: 1er, 2do, 3er lugar o Participante, solo en eventos con competencia; cada podio es de un solo estudiante.

### Gestión Financiera (menú 5)
- **Conceptos de pago**: CRUD con código único, valor base y si es recurrente; con pagos no se elimina, se desactiva.
- **Becas y descuentos**: valor personalizado por estudiante (tabla `precios_concepto_estudiante`), entre $ 0 (beca completa)
  y el valor base. Al registrar un pago se sugiere el valor que le corresponde al estudiante.
- **Pagos**: estudiante, acudiente que paga (debe estar vinculado al estudiante), concepto, monto, fecha, método
  (Efectivo, Transferencia, Tarjeta Débito, Tarjeta Crédito, Cheque), período y **recibo automático** (`REC-2026-000523`).
- **Historiales**: por estudiante y por acudiente (todos sus hijos, con total por estudiante y total familiar).
- **Morosos**: estudiantes activos con la pensión (o un servicio recurrente que usan) sin pagar hace más de un mes, con meses
  pendientes, deuda estimada y contacto del acudiente principal. Los becados al 100 % no aparecen.

### Factura electrónica (estilo DIAN, simulada)
Al confirmar un pago se genera `invoices/factura_000001.pdf` con datos del colegio (razón social, NIT, dirección, teléfono, correo),
datos del acudiente, número consecutivo (`LH-000001`), fecha, detalle (descripción, cantidad, valor unitario, subtotal),
subtotal, IVA, total, **CUFE** (SHA-384 de número + NIT + fecha), **código QR** con los datos de la factura y resolución simulada.
El IVA del 19 % solo aplica al uniforme: los servicios educativos están excluidos (Art. 476 del Estatuto Tributario) y el valor
pagado se toma con IVA incluido. Si el PDF falla, el pago queda guardado y la factura se puede generar de nuevo.
Ejemplo (uniforme con IVA): `docs/ejemplo_factura_LH-000002.pdf`.

### Gestión de Recursos (menú 6)
- **Inventario**: CRUD con código único, nombre, categoría, ubicación y estado; filtros, búsqueda y resumen por categoría.
- **Estados**: "Prestado" solo lo asigna un préstamo y solo la devolución lo libera; En Mantenimiento / Dado de Baja / Disponible
  se cambian desde el inventario. Un recurso con historial no se borra, se da de baja.
- **Préstamos** a estudiantes (máx. 30 días) y profesores (máx. 90 días), **solo si el recurso está Disponible**, en una transacción.
- **Devolución**: fecha real, préstamo *Devuelto* y recurso *Disponible* (también en una transacción).
- **Vencidos**: los préstamos con fecha pasada se marcan *Vencido*; el reporte los ordena por días de retraso.

### Reportes (menú 7, agregados con `ReportView.addReport`)
Ocupación de clubes, participación por tipo de evento, ranking de ganadores, morosos, recaudo por concepto y método,
préstamos vencidos y resumen del inventario.

---

## 11. Manejo de errores (en estos módulos)

- El SQL pasa por `JdbcHelper`, que registra el error en `error_log.txt` y lanza `DataAccessException` con un mensaje claro.
- Las reglas de negocio lanzan `ValidationException` ("El club alcanzó su cupo máximo", "El recurso no está disponible"...),
  que `BaseMenuView` muestra como `[ERROR]` antes de volver al menú.
- Si falla el PDF de una factura, se registra con `AppLogger` y el pago se conserva.

---

## Datos de prueba (sección 2.5 de `docs/database.sql`)

30 clubes, 175 membresías, 16 eventos, 90 participaciones, 10 conceptos de pago, 12 becas/descuentos, 521 pagos,
42 recursos, 18 préstamos a estudiantes y 12 a profesores. Casos para probar:

- **Club de Astronomía** lleno (8/8) → validación de cupo.
- **Morosos**: estudiantes 28 (sin pensión en el año), 23, 8, 15, 19, 10 y 33; el 16 debe transporte y el 13 alimentación.
- **Hermanos** 31 a 36 con descuento del 10 %; estudiante 17 con beca del 100 % (no aparece en morosos).
- **Préstamos vencidos**: 4 de estudiantes y 2 de profesores.

---

## 13. Limitaciones (de estos módulos)

- La factura es una representación gráfica simulada: no se envía a la DIAN y el CUFE no sigue el algoritmo oficial completo.
- Cada factura corresponde a un solo pago (así lo define la tabla `facturas`).
- El consecutivo de factura y de recibo se calcula con `MAX + 1`, suficiente para una aplicación de consola de un solo usuario.
- La deuda del reporte de morosos es estimada (meses pendientes × valor mensual del estudiante).
