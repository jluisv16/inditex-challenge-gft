# Inditex Prices Service

**Challenge Técnico — Inditex**

Servicio REST desarrollado con arquitectura hexagonal y enfoque API First para consultar el precio aplicable de un producto en una cadena (brand) para una fecha determinada.

---

## Problema a Resolver

La base de datos de comercio electrónico de la compañía contiene la tabla `PRICES` que refleja el precio final (PVP) y la tarifa que aplica a un producto de una cadena entre unas fechas determinadas.

### Estructura de la tabla PRICES

| BRAND_ID | START_DATE          | END_DATE            | PRICE_LIST | PRODUCT_ID | PRIORITY | PRICE | CURR |
|----------|---------------------|---------------------|------------|------------|----------|-------|------|
| 1        | 2020-06-14 00:00:00 | 2020-12-31 23:59:59 | 1          | 35455      | 0        | 35.50 | EUR  |
| 1        | 2020-06-14 15:00:00 | 2020-06-14 18:30:00 | 2          | 35455      | 1        | 25.45 | EUR  |
| 1        | 2020-06-15 00:00:00 | 2020-06-15 11:00:00 | 3          | 35455      | 1        | 30.50 | EUR  |
| 1        | 2020-06-15 16:00:00 | 2020-12-31 23:59:59 | 4          | 35455      | 1        | 38.95 | EUR  |

**Descripción de campos:**

- **BRAND_ID**: Foreign key de la cadena del grupo (1 = ZARA)
- **START_DATE, END_DATE**: Rango de fechas en el que aplica el precio tarifa indicado
- **PRICE_LIST**: Identificador de la tarifa de precios aplicable
- **PRODUCT_ID**: Identificador código de producto
- **PRIORITY**: Desambiguador de aplicación de precios. Si dos tarifas coinciden en un rango de fechas se aplica la de mayor prioridad (mayor valor numérico)
- **PRICE**: Precio final de venta
- **CURR**: ISO de la moneda

### Requisitos del test

Construir una aplicación/servicio en Spring Boot que provea un endpoint REST de consulta tal que:

- **Acepte como parámetros de entrada**: fecha de aplicación, identificador de producto, identificador de cadena
- **Devuelva como datos de salida**: identificador de producto, identificador de cadena, tarifa a aplicar, fechas de aplicación y precio final a aplicar
- Utilice una **base de datos en memoria (H2)** e inicializar con los datos del ejemplo
- Desarrolle **tests de integración** que validen las siguientes peticiones al servicio:
  - Test 1: petición a las 10:00 del día 14 del producto 35455 para la brand 1 (ZARA)
  - Test 2: petición a las 16:00 del día 14 del producto 35455 para la brand 1 (ZARA)
  - Test 3: petición a las 21:00 del día 14 del producto 35455 para la brand 1 (ZARA)
  - Test 4: petición a las 10:00 del día 15 del producto 35455 para la brand 1 (ZARA)
  - Test 5: petición a las 21:00 del día 16 del producto 35455 para la brand 1 (ZARA)

---

## 🎯 Requisitos funcionales

El servicio debe:

- Recibir:
  - `applicationDate`
  - `productId`
  - `brandId`
- Retornar:
  - `productId`, `brandId`, `priceList`, `startDate`, `endDate`, `price`
- Usar H2 en memoria
- Resolver prioridad por solapamiento de fechas
- Retornar 404 si no existe precio aplicable

---

## Por Qué API First

Este proyecto sigue el enfoque **API First**: el contrato OpenAPI se escribe **antes** de cualquier línea de código de implementación.

El archivo `prices-api.yml` es la **única fuente de verdad** para la API. El plugin `openapi-generator-maven-plugin` lee el contrato en tiempo de compilación y genera la interfaz del controlador (`PricesApi`). La clase de implementación (`PriceController`) simplemente implementa esa interfaz.

**Ventajas de API First:**

- El contrato nunca se desincroniza del código — si el YAML cambia, el compilador obliga a actualizar la implementación
- Los equipos de frontend o consumidores pueden trabajar contra el contrato desde el primer día, sin esperar a que el backend esté listo
- La documentación (Swagger UI) está siempre sincronizada con el comportamiento real, sin esfuerzo manual
- El onboarding es más rápido — un desarrollador nuevo entiende la API leyendo el YAML, no navegando el código

Esta es la **estrategia recomendada** para cualquier microservicio donde múltiples equipos o clientes dependen del mismo contrato de API.

---

## Por Qué Arquitectura Hexagonal

La **arquitectura hexagonal** (también conocida como Ports & Adapters) mantiene la lógica de negocio completamente aislada de frameworks, bases de datos y mecanismos de entrega. Las capas de dominio y aplicación tienen **cero conocimiento** de Spring, JPA o HTTP.

### Estructura del proyecto

```
com.inditex.prices/
├── domain/
│   └── model/
│       └── Price.java              ← Modelo puro de negocio (record, sin anotaciones de framework)
├── application/
│   ├── port/
│   │   ├── in/
│   │   │   └── GetPriceQuery.java  ← Puerto de entrada (interfaz del caso de uso)
│   │   └── out/
│   │       └── PriceRepository.java← Puerto de salida (contrato de persistencia)
│   └── usecase/
│       └── GetPriceUseCase.java    ← Lógica de negocio
└── infrastructure/
    ├── adapter/
    │   ├── in.rest/
    │   │   ├── PriceController.java ← Implementa PricesApi (generada desde OpenAPI)
    │   │   └── mapper/              ← MapStruct: Price → PriceResponse
    │   └── out.persistence/
    │       ├── PriceRepositoryAdapter.java ← Implementa PriceRepository
    │       ├── entity/              ← Entidad JPA (solo en infraestructura)
    │       ├── mapper/              ← MapStruct: PriceEntity → Price
    │       └── repository/          ← Spring Data JPA repository
    └── exception/
        ├── GlobalExceptionHandler.java
        └── PriceNotFoundException.java
```

**Beneficios prácticos:**

- Cambiar la base de datos (H2 → PostgreSQL) solo requiere reemplazar el adaptador de persistencia — el dominio y el caso de uso permanecen intactos
- El caso de uso (`GetPriceUseCase`) puede probarse de forma unitaria con un repositorio mockeado, sin necesidad de contexto de Spring
- El controlador REST solo conecta la entrada con el caso de uso — no tiene lógica de negocio propia

Esta separación hace que la base de código sea **más fácil de probar, razonar y mantener** a medida que crece.

---

## Por Qué Price es un Record y No una Entidad DDD

En Domain-Driven Design, hay dos tipos fundamentalmente diferentes de objetos de dominio:

- **Entity (Entidad)**: tiene identidad única, ciclo de vida y estado mutable (ej: una `Order` que transiciona de `DRAFT` a `CONFIRMED`)
- **Value Object**: no tiene identidad propia, es inmutable y se compara por sus valores (ej: un monto `Money`, una `Address`, un snapshot de precio)

`Price` encaja **exactamente en la definición de Value Object**. Representa un snapshot de precio aplicable en un momento dado — no tiene identidad de negocio, no cambia de estado y dos objetos `Price` con los mismos valores son intercambiables.

Un **record** de Java es la forma idiomática y correcta de expresar un Value Object en Java moderno: es inmutable por construcción, proporciona `equals`/`hashCode` basados en todos los campos y no requiere código boilerplate.

**Decisión de diseño:**

- `Price` es un Value Object → `record` es la elección correcta
- La entidad JPA (`PriceEntity`) vive **solo en la capa de infraestructura**
- Si la tecnología de persistencia cambia, el modelo de dominio no se ve afectado

Una **Entidad DDD** (con una clase, métodos de ciclo de vida y enforcement de invariantes) solo sería apropiada si el dominio requiriera escritura, actualización o gestión del estado de un precio — lo cual **no es requerido aquí**.

---

## Stack Tecnológico

| Tecnología                        | Versión    | Propósito                         |
|-----------------------------------|------------|-----------------------------------|
| Java                              | 21 (LTS)   | Lenguaje                          |
| Spring Boot                       | 3.4.0      | Framework de aplicación           |
| Spring Data JPA                   | 3.4.0      | Abstracción de persistencia       |
| H2 Database                       | 2.2.x      | Base de datos en memoria          |
| openapi-generator-maven-plugin    | 7.10.0     | Generación de código API First    |
| MapStruct                         | 1.6.3      | Mappers compile-time safe         |
| Lombok                            | 1.18.36    | Reducción de boilerplate          |
| JUnit 5 + MockMvc                 | 3.4.0      | Testing de integración y unitario |
| Docker                            | Cualquiera | Contenerización                   |
| Maven                             | 3.8+       | Herramienta de build              |
| Resilience4j                      | 2.2.0      | Resiliencia                       |
| Caffeine                          | 3.1.8      | Cache                             |
| OpenAPI Generator                 | 7.10.0     | API First                         |

---

## ⚡ Resiliencia implementada (únicas mejoras añadidas)

Este proyecto **solo ha incorporado mejoras de resiliencia**, sin cambios en el core del dominio:

- ✅ Cache (Caffeine)
- ✅ Retry (Resilience4j)
- ✅ Circuit Breaker
- ✅ TimeLimiter

📄 `GetPriceUseCase.java`
```java
@Cacheable
@Retry
@CircuitBreaker
@TimeLimiter
```

---

## 🧪 Cómo ejecutar los tests añadidos

Para validar las mejoras implementadas:

```bash
mvn clean test
```

Esto ejecuta:

- Tests unitarios del UseCase
- Tests de integración REST
- Tests de cache
- Tests de resiliencia (retry + circuit breaker + timeout)

✔️ Todos los tests deben pasar en entorno H2 en memoria.

---

## 🚀 Ejecución del proyecto

```bash
mvn spring-boot:run
```

---

## 🌐 Endpoint

```http
GET /api/v1/prices
```

Ejemplo:

```bash
curl "http://localhost:8080/api/v1/prices?applicationDate=2020-06-14T10:00:00Z&productId=35455&brandId=1"
```

---

## 📦 Respuesta

```json
{
  "productId": 35455,
  "brandId": 1,
  "priceList": 1,
  "startDate": "2020-06-14T00:00:00Z",
  "endDate": "2020-12-31T23:59:59Z",
  "price": 35.50,
  "currency": "EUR"
}
```

---


## Cómo Ejecutar y Probar el Proyecto

### Opción A — Docker (recomendado, no requiere setup local)

Esta es la forma más rápida de poner el servicio en marcha. Docker construye y empaqueta todo automáticamente.

**Prerrequisitos**: Docker Desktop instalado y en ejecución.

**Paso 1** — Clonar o descomprimir el proyecto:

```bash
unzip inditex-prices.zip
cd inditex-prices
```

**Paso 2** — Construir e iniciar:

```bash
docker-compose up --build
```

El servicio estará disponible en `http://localhost:8080` una vez que el log de inicio muestre:

```
Started PricesApplication in 6.591 seconds (process running for 7.15)
```

**Paso 3** — Detener el servicio:

```bash
docker-compose down
```

---

### Opción B — Local con Maven (requiere Java 21 y Maven 3.8+)

```bash
unzip inditex-prices.zip
cd inditex-prices
mvn spring-boot:run
```

El servicio se inicia en `http://localhost:8080`

---

## Ejecutar los Tests

```bash
mvn test
```

Todos los 9 tests deberían pasar. La salida mostrará:

| Test   | Fecha y Hora    | Resultado Esperado       | Precio   |
|--------|-----------------|--------------------------|----------|
| Test 1 | 14 Jun · 10:00  | Price list 1             | 35.50 EUR|
| Test 2 | 14 Jun · 16:00  | Price list 2 (prioridad) | 25.45 EUR|
| Test 3 | 14 Jun · 21:00  | Price list 1             | 35.50 EUR|
| Test 4 | 15 Jun · 10:00  | Price list 3             | 30.50 EUR|
| Test 5 | 16 Jun · 21:00  | Price list 4             | 38.95 EUR|
| Test 6 | Fuera de rango  | 404 Not Found            | —        |
| Test 7 | Parámetro falta | 400 Bad Request          | —        |
| Unit 1 | Mock: encontrado| Devuelve precio          | —        |
| Unit 2 | Mock: no existe | Lanza excepción          | —        |

---

## Llamada Manual a la API

También puedes probar el endpoint directamente con `curl`:

```bash
curl "http://localhost:8080/api/v1/prices?applicationDate=2020-06-14T10:00:00&productId=35455&brandId=1"
```

**Respuesta esperada:**

```json
{
  "productId": 35455,
  "brandId": 1,
  "priceList": 1,
  "startDate": "2020-06-14T00:00:00",
  "endDate": "2020-12-31T23:59:59",
  "price": 35.50,
  "currency": "EUR"
}
```

---


---

## Decisiones de Diseño Clave

1. Resolución de precio por prioridad delegada a la query JPA (filtrado por fechas, producto, brand y orden por `priority DESC)`, evitando lógica en la capa de servicio.

2. Uso de `ProblemDetail (RFC 7807)` para estandarizar respuestas de error HTTP sin creación de DTOs personalizados.

3. Uso de `MapStruct` para mapeo entre capas (Entity ↔ Domain ↔ DTO) con generación en tiempo de compilación y validación estática.

4. Uso de `@Transactional(readOnly = true)` en operaciones de lectura para optimizar rendimiento y evitar flush innecesario en Hibernate.

5. Implementación de caching con Spring Cache + Caffeine para reducir llamadas repetidas a base de datos y mejorar latencia.

6. Implementación de resiliencia con Resilience4j (Retry, Circuit Breaker, Bulkhead, TimeLimiter) para tolerancia a fallos y control de carga

7. Configuración de Circuit Breaker para evitar saturación del sistema ante fallos persistentes de dependencias.

8. Aplicación de arquitectura hexagonal para desacoplar dominio de frameworks (Spring, JPA, HTTP).

9. Separación estricta de responsabilidades entre dominio, aplicación e infraestructura.

10. Delegación de persistencia a adaptadores (Ports & Adapters) para mantener independencia del core de negocio.
---

**Inditex Prices Service · Challenge**