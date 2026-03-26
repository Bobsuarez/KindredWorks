# AI Rules & Coding Standards - Project cplaneta

Este documento define las directrices técnicas para el desarrollo del backend de **cplaneta**. La IA y los desarrolladores deben seguir estas reglas para asegurar consistencia, escalabilidad y mantenibilidad.

## 1. Stack Tecnológico & Arquitectura
- **Lenguaje:** Java 21 (Uso de Records, Pattern Matching, Scoped Values cuando aplique).
- **Framework:** Spring Boot 3.4+.
- **Arquitectura:** Clean Architecture + SOLID.
- **Persistencia:** PostgreSQL + Spring Data JPA.
- **Mapeo:** MapStruct (Prohibido el mapeo manual en servicios).
- **Boilerplate:** Lombok (@RequiredArgsConstructor, @Slf4j, @Getter, @Setter).

## 2. Convenciones de Nombres
- **Clases:** `PascalCase` (Ej: `MasterProgramService`).
- **Métodos y Variables:** `camelCase` (Ej: `findById`, `masterProgramRepository`).
- **Endpoints:** `kebab-case` y en plural (Ej: `/api/v1/master-programs`).
- **Interfaces:** Sin prefijo "I". La implementación lleva sufijo `Impl` solo si hay múltiples implementaciones.
- **DTOs:** Sufijo `Request` para datos de entrada y `Response` para datos de salida.

## 3. Estructura de Paquetes
```text
com.kinforgework.cplaneta
├── controller    # Adaptadores de entrada (REST Controllers)
├── service       # Lógica de negocio y Casos de Uso
├── repository    # Adaptadores de persistencia (Interfaces JPA)
├── entities      # Entidades de base de datos (JPA Entities)
├── dto           # Data Transfer Objects (Preferiblemente Java Records)
├── mapper        # Interfaces de MapStruct
├── exception     # Excepciones personalizadas y Global Exception Handler
├── config        # Configuraciones (Security, CORS, Beans)
└── integration   # Clientes para servicios externos (Mail, File Storage)
```

## 4. Controllers: Reglas de Oro
- **Responsabilidad:** Únicamente orquestación de la petición. **Cero lógica de negocio.**
- **Inyección:** Usar `@RequiredArgsConstructor` para inyección por constructor.
- **Validación:** Usar siempre `@Valid` en los parámetros de entrada.
- **Retorno:** Retornar siempre `ResponseEntity<T>`.
- **Endpoints:** Seguir estándares RESTful (GET para lectura, POST creación, PUT/PATCH actualización, DELETE borrado).

```java
@RestController
@RequestMapping("/api/v1/master-programs")
@RequiredArgsConstructor
public class MasterProgramController {
    private final MasterProgramService service;

    @PostMapping
    public ResponseEntity<MasterProgramResponse> create(@Valid @RequestBody MasterProgramRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(request));
    }
}
```

## 5. Services: Capa de Negocio
- Deben estar anotados con `@Service`.
- Usar `@Transactional` en métodos que realicen operaciones de escritura o múltiples consultas.
- No deben devolver entidades JPA a los controladores; siempre convertir a `Response` DTO.
- Lanzar excepciones personalizadas (Ej: `ResourceNotFoundException`) en lugar de devolver `null`.

## 6. DTOs y Mapeo
- **DTOs:** Definirlos como `public record`.
- **MapStruct:** Definir interfaces con `@Mapper(componentModel = "spring")`.

```java
@Mapper(componentModel = "spring")
public interface MasterProgramMapper {
    MasterProgramResponse toResponse(MasterProgram entity);
    MasterProgram toEntity(MasterProgramRequest request);
}
```

## 7. Manejo de Excepciones
- Usar un `@RestControllerAdvice` para capturar excepciones globalmente.
- Formato de error estándar: `{ "timestamp": "...", "message": "...", "code": "...", "details": [...] }`.

## 8. Manejo de Archivos (Multipart)
- Los archivos deben ser validados por tipo (MIME type) y tamaño.
- El almacenamiento debe estar desacoplado (usar una interfaz `FileStorageService`).
- No guardar archivos directamente en la base de datos (guardar solo la ruta/URL).

## 9. Base de Datos & Repositorios
- Evitar `FetchType.EAGER` en relaciones `@OneToMany` o `@ManyToMany`.
- Usar nombres de métodos derivados de Spring Data JPA siempre que sea posible.
- Para consultas complejas, usar `@Query` o Criteria API.

## 10. Logging & Performance
- Usar `@Slf4j`.
- Loguear errores con contexto (ej. ID del recurso que falló).
- Usar paginación (`Pageable`) para listas de datos potencialmente grandes.

## 11. Testing
- **Unitarios:** JUnit 5 + Mockito para probar la lógica de los servicios.
- **Integración:** MockMvc para probar los controladores y el flujo completo.
- Estilo de test: `should_ExpectedBehavior_When_StateUnderTest`.

---
*Estas reglas son de cumplimiento obligatorio para mantener la integridad del proyecto cplaneta.*
