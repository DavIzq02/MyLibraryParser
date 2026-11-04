# Analizador y Generador de Arquitectura UML para Proyectos Java
> 🤖 **INSTRUCCIONES PARA MODELOS DE LENGUAJE E IAs:** Este documento contiene el contexto arquitectónico, las reglas de diseño y el estado estructural exacto de este proyecto. Cualquier código que generes, refactorices o extiendas debe respetar la arquitectura Clean Architecture, los patrones de inmutabilidad y la estructura de paquetes descrita a continuación.

## 📌 Descripción General
Este aplicativo es una herramienta diseñada para analizar proyectos desarrollados en Java y generar automáticamente su arquitectura y diseño orientado a objetos en formato PlantUML y Markdown. Su propósito es ayudar a desarrolladores y arquitectos de software a visualizar, entender y documentar bases de código existentes mediante diagramas UML claros, precisos y fieles al estándar UML 2.5.1 y a las especificaciones de diseño y reglas de dominio (RD).

## 🚀 Funcionalidad Principal
El flujo de trabajo del aplicativo se divide en tres etapas fundamentales:

1. **Entrada (Input):**
   - Recibe como entrada la ruta hacia la carpeta `src` de cualquier proyecto desarrollado en Java.
2. **Análisis (Procesamiento):**
   - Escanea y procesa el código fuente Java usando **JavaParser 3.28.2**.
   - Extrae e identifica: clases, clases abstractas, interfaces, records, enumeraciones, anotaciones, paquetes, atributos (propiedades), métodos (operaciones) y constructores.
   - **Soporte de Java Moderno (Java 17 / 21):**
     - Jerarquías selladas con modificadores `sealed` y `non-sealed`.
     - *Records* con constructor canónico implícito y sus correspondientes métodos *accessor*.
     - Constantes y campos en interfaces (implícitamente `public static final`).
     - Normalización de parámetros de longitud variable (*varargs* `Tipo...`) a arreglos (`Tipo[]`).
   - **Resolución Avanzada del Sistema de Tipos:**
     - Desempaquetado recursivo de tipos genéricos profundos en colecciones.
     - Extracción inteligente de comodines acotados (*wildcards* `? extends T` y `? super T`).
     - Aislamiento de variables de tipo genéricas (`T`, `E`, etc.) frente a clasificadores reales.
     - Mitigación de ambigüedad (*Type Shadowing*) priorizando tipos estándar del JDK frente a colisiones por nombre simple.
     - Inclusión implícita del paquete estándar `java.lang.*`.
   - **Descubrimiento y Clasificación Fina de Relaciones UML:**
     - **Generalización (`--|>`):** Herencia entre clases o subinterfaces.
     - **Realización (`..|>`):** Implementación de contratos de interfaces.
     - **Composición (`*--`):** Detección de instanciación interna directa (*inline*) o atributos `final` no inyectados por constructor.
     - **Agregación (`o--`):** Agrupación externa estructurada mediante colecciones y arreglos.
     - **Asociación Simple (`-->`):** Referencias 1 a 1 inyectadas con detección automática de multiplicidades (`1` y `*`).
     - **Dependencia Efímera (`..>`):** Uso en parámetros o retornos tipificadas con el estereotipo `<<use>>`, suprimiéndose automáticamente si ya existe una relación estructural más fuerte entre las mismas clases.
     - **Anidamiento (`+--`):** Tipos internos y estáticos anidados con resolución ascendente segura de ancestros léxicos.
     - **Filtrado de Dominio (`UmlRelationshipFilter`):** Conservación estricta de la relación más fuerte por cada par de clasificadores y eliminación de dependencias redundantes entre clases e interfaces anidadas dentro de un mismo contenedor.
3. **Salida (Output):**
   - **PlantUML (`.puml`):** Código de diagramado con estética profesional para Java 17/21: paleta de colores pastel diferenciada (interfaces verde pastel `#D5F5E3`, clases abstractas naranja `#FFE5CC`, records azul `#D0E1FD`, tipos externos gris claro `#EAECEE`), enrutamiento ortogonal (`skinparam linetype ortho`), tipografía Helvetica, supresión de sombras, renderizado modular por paquetes y estereotipos explícitos (`<<Record>>`, `<<Abstract>>`, `<<Interface>>`, `<<external>>`).
     - **Paquete `Java` para Lista Blanca (Whitelist):** Las clases Java autorizadas en la lista blanca (`javaPackageWhitelist` / `whiteList`, como `BigDecimal`, `UUID`, `OffsetDateTime`, etc.) se modelan encapsuladas dentro de un paquete/folder raíz denominado **`Java`**, estructuradas ordenadamente en sus subpaquetes correspondientes (`math`, `time`, `util`, etc.) con estereotipo `<<external>>`. Además, se trazan automáticamente las relaciones estructurales (`-->`, `o--`) o dependencias (`<<use>>`) con sus multiplicidades hacia las clases del proyecto que las consumen.
   - **Markdown (`uml-model-output.md`):** Reporte técnico estructurado donde las métricas de relaciones reflejan con exactitud el modelo filtrado final, complementado por tablas con literales de enumeración, atributos, métodos y una matriz de relaciones detallada con multiplicidades (`Source (Mult)`, `Target (Mult)`) y especificación de acoplamiento (`<<use>>`, `COMPOSITE`, `SHARED`, etc.).

---

## 🛡️ Cobertura de Casos Borde y Reglas de Dominio (RD)

En cumplimiento de las especificaciones y casos borde del documento de requerimientos de diseño (RD), el motor de análisis incorpora las siguientes soluciones algorítmicas:

| Caso | Denominación | Descripción de la Regla y Solución Implementada |
| :--- | :--- | :--- |
| **CASO 3** | **Diferenciación Fina de Posesión** | Distingue entre **Composición** (`*--`), **Agregación** (`o--`) y **Asociación Simple** (`-->`). Se marca composición cuando el atributo se inicializa *inline* (`new Objeto()`) o es `final` sin recibir inyección por constructor. Se clasifica como agregación si el tipo es una colección o arreglo. Las enumeraciones y los tipos externos de librerías/JDK se excluyen de composición interna. |
| **CASO 4** | **Multiplicidad en Relaciones** | Evalúa el tipo de la propiedad: si corresponde a un arreglo o colección parametrizada, asigna multiplicidad de destino `*` y origen `1` (`"1" --> "*"`). Para referencias unitarias asigna `"1" --> "1"`. |
| **CASO 7** | **Genéricos Profundos** | Desempaqueta y resuelve recursivamente los argumentos de tipo dentro de colecciones complejas (ej. `Map<String, List<MiClase>>`), garantizando que la referencia al tipo interno del dominio sea identificada. |
| **CASO 8** | **Wildcards Acotados** | Procesa comodines acotados (`? extends T` y `? super T`), extrayendo el tipo límite superior o inferior para resolver la dependencia o asociación correspondiente en lugar de fallar o marcar un tipo desconocido. |
| **CASO 9** | **Aislamiento de Genéricos** | Valida si un identificador de tipo coincide con los parámetros de plantilla del contexto (`context.templateParameters()`, ej. `T`, `E`, `K`), modelándolo como `UmlType.TemplateParameter` y previniendo colisiones erróneas con clasificadores de dominio. |
| **CASO 10** | **Normalización de Varargs** | Convierte automáticamente los parámetros de longitud variable (`Tipo...`) tanto en métodos ordinarios como en constructores a tipos de arreglo de nivel 1 (`UmlType.Array(resolvedType, 1)`). |
| **CASO 12** | **Prevención de Type Shadowing** | Evalúa los tipos estándar del JDK (`java.lang.*`, `java.util.*`, `java.math.*`, `java.time.*`) antes de ejecutar el *fallback* de búsqueda global por nombre simple, evitando que una clase local con nombre homónimo (ej. `com.proyecto.String`) eclipse tipos nativos legítimos. |
| **CASO 13** | **Import Implícito de `java.lang`** | Registra automáticamente `java.lang` como paquete comodín (*wildcard import*) para cada unidad de compilación, emulando la semántica nativa del compilador Java. |
| **CASO 16** | **Jerarquías Selladas (Java 17+)** | Detecta los modificadores `SEALED` y `NON_SEALED` en declaraciones de clases e interfaces, incorporándolos en el enum `UmlModifier`. |
| **CASO 17** | **Constantes en Interfaces** | Parsea los campos declarados dentro de interfaces tratándolos como propiedades (implícitamente `public static final`), permitiendo su visualización en diagramas UML y reportes Markdown. |
| **CASO 23** | **Ascenso Seguro de Ancestros Anidados** | Utiliza `findAncestor(TypeDeclaration.class)` para ascender jerárquicamente a través de los contenedores léxicos, omitiendo bloques de métodos, clases anónimas o clases locales intermedias. |
| **CASO 24** | **Dependencias Efímeras y Supresión** | Extrae dependencias efímeras desde tipos de retorno y parámetros de métodos asignándoles el estereotipo `<<use>>`. Aplica el método `hasStrongerRelationship`: si ya existe herencia, realización o asociación estructural entre ambas clases, se descarta la dependencia redundante. |
| **Paquete Java** | **Modelado de Clases Java Whitelist en PUML** | Las clases Java provenientes de la lista blanca se encapsulan dentro de un paquete raíz `Java`, organizadas en subpaquetes según su espacio de nombres (`math`, `time`, `util`, etc.) con estereotipo `<<external>>`. El motor de relaciones identifica atributos, colecciones, retornos y parámetros que referencian estos tipos externos y dibuja sus relaciones (`-->`, `o--`, `..>`) y multiplicidades con las clases del proyecto que las consumen. |
| **Filtro** | **Relación más Fuerte por Par (`UmlRelationshipFilter`)** | Garantiza que para cada par no ordenado `{A, B}` sólo persista la relación de mayor fuerza según la jerarquía UML (`Nesting` > `Realization` > `Generalization` > `Composite` > `Shared` > `Association` > `Dependency`). Suprime además enlaces redundantes entre clases e interfaces anidadas en el mismo clasificador contenedor. |

---

## 🏛 Arquitectura del Sistema
El aplicativo está construido como un **Monolito** que implementa rigurosamente los principios de **Clean Architecture** (Arquitectura Limpia). Se basa en una arquitectura de capas concéntricas donde las dependencias siempre apuntan hacia el interior, protegiendo las reglas de negocio.

### Estructura de Capas
El proyecto se organiza en 4 grandes capas (paquetes principales):

1. **`core` (Dominio):**
   - **Es el corazón de la aplicación.** Contiene las reglas de negocio puras y los modelos del metamodelo UML (`UmlClass`, `UmlInterface`, `UmlRecord`, `UmlEnumeration`, `UmlAnnotation`, `UmlRelationship`, `UmlModel`, `UmlRelationshipFilter`, etc.).
   - No depende de ninguna otra capa ni de frameworks externos. Es código Java puro.
2. **`application` (Casos de Uso):**
   - Orquesta el flujo de los datos desde y hacia las entidades del dominio.
   - Contiene los **Puertos de Entrada** (`port/in/`) — interfaces de casos de uso que la presentación invoca — y los **Puertos de Salida** (`port/out/`) — interfaces que la infraestructura implementa.
   - Contiene también los **modelos de contexto de aplicación** (`project/`): configuración de análisis (`AnalysisConfiguration` con su *Builder* fluido), proyectos, layouts, listas blancas y negras (`WhiteList`, `BlackList`), etc.
   - La capa `service/` está preparada para alojar las implementaciones de los casos de uso.
3. **`infrastructure` (Infraestructura / Adaptadores):**
   - Implementa los puertos de salida definidos en la capa de aplicación.
   - Aquí residen los detalles técnicos y la integración con herramientas externas: analizadores de código fuente (**JavaParser 3.28.2**), exportadores (PlantUML con paleta pastel y paquete Java para lista blanca, Markdown con tablas enriquecidas), sistemas de archivos y telemetría.
4. **`presentation` (Presentación):**
   - El punto de entrada para el usuario al interactuar con la aplicación (CLI, API REST o Interfaz Gráfica).
   - Invoca los casos de uso de la capa de aplicación a través de los puertos de entrada.
   - **Estado actual:** paquete preparado; el punto de ejecución principal activo se opera vía CLI o clase `Main`.

---

## 📁 Estructura de Carpetas (Árbol del Proyecto)

```text
src/main/java/generador/
├── application/                # Casos de uso y Puertos (In/Out)
│   ├── port/
│   │   ├── in/                 # Interfaces que la Presentación llama (16 casos de uso)
│   │   │   ├── BuildUmlModelUseCase.java
│   │   │   ├── CreateProjectUseCase.java
│   │   │   ├── DeleteProjectUseCase.java
│   │   │   ├── DetectCodeSmellsUseCase.java
│   │   │   ├── ExportDiagramUseCase.java
│   │   │   ├── LoadPlantUmlUseCase.java
│   │   │   ├── LocateElementInDiagramUseCase.java
│   │   │   ├── MoveDiagramElementUseCase.java
│   │   │   ├── OpenProjectUseCase.java
│   │   │   ├── ParsePlantUmlUseCase.java
│   │   │   ├── ReplacePlantUmlUseCase.java
│   │   │   ├── RestoreDiagramLayoutUseCase.java
│   │   │   ├── SaveDiagramLayoutUseCase.java
│   │   │   ├── SelectUmlElementUseCase.java
│   │   │   ├── ShowDiagnosticsUseCase.java
│   │   │   └── ShowTelemetryUseCase.java
│   │   └── out/                # Interfaces que la Infraestructura implementa (9 puertos)
│   │       ├── AutoLayoutPort.java
│   │       ├── CodeSmellDetectorPort.java
│   │       ├── DiagnosticsReporterPort.java
│   │       ├── DiagramExporterPort.java
│   │       ├── DiagramLayoutStoragePort.java
│   │       ├── DiagramRendererPort.java
│   │       ├── PlantUmlParserPort.java
│   │       ├── ProjectRepositoryPort.java
│   │       └── TelemetryPort.java
│   ├── project/                # Modelos de contexto de aplicación
│   │   ├── AnalysisConfiguration.java   # Configuración de análisis (whitelist, blacklist, Java version)
│   │   ├── BlackList.java
│   │   ├── DiagramLayout.java
│   │   ├── JavaLanguageVersion.java
│   │   ├── PlantUmlDocument.java
│   │   ├── Project.java
│   │   └── WhiteList.java
│   └── service/                # Implementaciones de casos de uso (en construcción)
├── core/                       # Entidades y lógica central de UML (Sin dependencias externas)
│   └── domain/
│       ├── classifier/         # Clasificadores del metamodelo UML
│       │   ├── UmlAnnotation.java
│       │   ├── UmlClass.java
│       │   ├── UmlClassifier.java          # Clase base abstracta de clasificadores
│       │   ├── UmlEnumeration.java
│       │   ├── UmlEnumerationLiteral.java
│       │   ├── UmlInterface.java           # Soporta métodos y constantes de interfaz
│       │   └── UmlRecord.java              # Soporta componentes, canonical ctor y accessors
│       ├── element/            # Elementos base (UmlElement, UmlNamespace)
│       ├── feature/            # Miembros: UmlOperation, UmlProperty, UmlParameter
│       ├── model/              # Raíz del agregado: UmlModel (métricas y agregación)
│       ├── relationship/       # UmlRelationship + implementaciones y filtros de dominio
│       │   ├── UmlAssociation.java         # Multiplicidades y AggregationKind
│       │   ├── UmlDependency.java          # Estereotipo <<use>>
│       │   ├── UmlGeneralization.java      # Herencia pura
│       │   ├── UmlNesting.java             # Tipos anidados
│       │   ├── UmlRealization.java         # Contratos de interfaz
│       │   ├── UmlRelationship.java
│       │   └── UmlRelationshipFilter.java  # Regla de dominio: filtrado de relación más fuerte
│       ├── spec/               # Modificadores y especificaciones
│       │   ├── AggregationKind.java        # NONE, SHARED (agregación), COMPOSITE (composición)
│       │   ├── UmlClassification.java      # CLASS, ABSTRACT_CLASS, INTERFACE, ENUMERATION, RECORD, ANNOTATION
│       │   ├── UmlModifier.java            # Modificadores (incluye SEALED y NON_SEALED)
│       │   └── UmlVisibility.java
│       └── type/               # Metamodelo de tipos (Primitive, Reference, Parameterized, Array, etc.)
├── infrastructure/             # Adaptadores técnicos (JavaParser, Exportadores)
│   ├── codesmell/              # Detectores de code smells (preparado)
│   ├── export/                 # Adaptadores de exportación
│   │   ├── UmlModelMarkdownExporter.java   # Exporta a .md con métricas sincronizadas y multiplicidades
│   │   └── UmlModelPlantUmlExporter.java   # Exporta a .puml con paquete Java para whitelist y paleta pastel
│   ├── layout/                 # Manejo de layouts de diagrama (preparado)
│   ├── parser/                 # Analizadores con JavaParser 3.28.2
│   │   ├── JavaClassifierParser.java       # Extracción de clases, records, interfaces y constantes
│   │   ├── JavaNestingParser.java          # Extracción segura de anidamientos con findAncestor
│   │   ├── JavaOperationParser.java        # Extracción de métodos, constructores y varargs a arreglos
│   │   ├── JavaProjectParser.java          # Orquestador del análisis de directorios
│   │   ├── JavaPropertyParser.java         # Resolución de propiedades y tipo de agregación
│   │   ├── JavaRelationshipParser.java     # Resolución de enlaces, multiplicidad y dependencias <<use>>
│   │   ├── JavaSourceParser.java           # Parseo de archivos individuales
│   │   ├── JavaTypeResolutionContext.java  # Contexto léxico de tipos, paquetes e imports
│   │   └── JavaTypeResolver.java           # Resolución robusta de tipos, wildcards, anti-shadowing y whitelist
│   ├── persistence/            # Almacenamiento y repositorio (preparado)
│   ├── rendering/              # Integración de renderizado (preparado)
│   └── telemetry/              # Telemetría y diagnóstico (preparado)
└── presentation/               # Interfaz de usuario o CLI (preparado)
```

---

## 🧩 Justificación de la Estructura de Paquetes y Reglas de Diseño

La arquitectura garantiza cohesión estricta y protección del dominio:

* **`core/domain/` (Aislamiento del Metamodelo UML):**
  * Metamodelo puro en Java estándar sin dependencias de JavaParser ni de frameworks de terceros.
  * `UmlClassifier` y sus especializaciones (`UmlClass`, `UmlInterface`, `UmlRecord`, `UmlEnumeration`, `UmlAnnotation`) representan fielmente los conceptos de POO y UML 2.5.1.
  * `UmlRelationshipFilter` concentra la regla de negocio que previene el ruido visual en diagramas de arquitectura, seleccionando determinísticamente la relación estructural de mayor jerarquía.
  * `UmlModifier` incluye modificadores modernos de Java (`SEALED`, `NON_SEALED`, `FINAL`, `ABSTRACT`, `STATIC`, etc.).

* **`application/` (Desacoplamiento mediante Puertos y Adaptadores):**
  * Separa estrictamente contratos de casos de uso (`port/in/`) y contratos de infraestructura (`port/out/`).
  * `AnalysisConfiguration` administra las políticas de exclusión/inclusión (`WhiteList` de paquetes Java a analizar, `BlackList` de paquetes y clases excluidas) y versión objetivo de Java.

* **`infrastructure/` (Adaptadores Tecnológicos):**
  * **`parser/`:** Implementa los algoritmos de extracción con JavaParser 3.28.2 cubriendo la totalidad de casos borde del RD (genéricos profundos, *varargs*, comodines acotados, *type shadowing*, jerarquías selladas, constantes de interfaz y resolución de relaciones hacia tipos externos de la lista blanca).
  * **`export/`:** Contiene los generadores de salida:
    * `UmlModelPlantUmlExporter`: genera especificaciones PlantUML con renderizado ortogonal, agrupamiento por paquetes del proyecto, modelado de tipos Java de la lista blanca dentro del paquete `Java` (subpaquetes `math`, `time`, `util`, etc.) con relaciones y multiplicidades hacia las clases que los consumen, conectores normalizados (`--|>`, `..|>`, `..>`, `-->`, `+--`), y paleta de colores pastel diferenciada para Java 17/21.
    * `UmlModelMarkdownExporter`: produce un informe estructurado en Markdown donde el contador de relaciones coincide con el modelo filtrado final y se detallan las multiplicidades y estereotipos.

---

## 🛠 Tecnologías Involucradas

- **Java 21 / Java 17** (Lenguaje Base — compatibilidad con características modernas del lenguaje).
- **JavaParser 3.28.2:** Dependencia de compilación para el análisis del AST de código fuente Java.
- **PlantUML:** Formato de salida estandarizado para generación de diagramas visuales de arquitectura.
- **Apache Ant + NetBeans:** Entorno de compilación y empaquetado estructurado bajo `build.xml`.

---

## 💻 Ejemplo de Uso Rápido

Para analizar un proyecto Java y generar tanto el reporte Markdown como el archivo PlantUML (`.puml`):

```java
import generador.application.project.AnalysisConfiguration;
import generador.application.project.JavaLanguageVersion;
import generador.core.domain.model.UmlModel;
import generador.infrastructure.export.UmlModelMarkdownExporter;
import generador.infrastructure.export.UmlModelPlantUmlExporter;
import generador.infrastructure.parser.JavaProjectParser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        Path rutaSrc = Path.of("C:/ruta/a/tu/proyecto/src");

        // Configuración del análisis con listas blancas y negras
        AnalysisConfiguration config = AnalysisConfiguration.builder(JavaLanguageVersion.JAVA_21)
                .includeJavaPackages(List.of(
                        "java.math.BigDecimal",
                        "java.util.UUID",
                        "java.time.OffsetDateTime"
                ))
                .excludePackages(List.of(
                        "com.ejemplo.vista",
                        "com.ejemplo.config"
                ))
                .excludeClasses(List.of(
                        "App",
                        "ConfiguracionGlobal"
                ))
                .build();

        // 1. Escaneo y análisis del código fuente
        JavaProjectParser parser = new JavaProjectParser(config);
        UmlModel model = parser.parse(rutaSrc);

        // 2. Exportación a Markdown (.md) con métricas y tablas completas
        UmlModelMarkdownExporter mdExporter = new UmlModelMarkdownExporter();
        Path salidaMd = Path.of("uml-model-output.md");
        mdExporter.export(model, config, salidaMd);

        // 3. Exportación a PlantUML (.puml) con paquete Java para la lista blanca y paleta pastel
        UmlModelPlantUmlExporter pumlExporter = new UmlModelPlantUmlExporter();
        Path salidaPuml = Path.of("uml-model-output.puml");
        pumlExporter.export(model, config, salidaPuml);

        System.out.println("Análisis completado exitosamente.");
        System.out.println("Reporte Markdown: " + salidaMd.toAbsolutePath());
        System.out.println("Diagrama PlantUML: " + salidaPuml.toAbsolutePath());
    }
}
```

---

## 🤖 Declaración de Desarrollo Asistido por IA (AI-Assisted Development)

Este proyecto ha sido concebido y desarrollado bajo un modelo y diseño de arquitectura humana y apoyado con herramientas de Inteligencia Artificial para la colaboración, acompañamiento y consultoría:

* **Arquitectura y Lógica Central:** Diseñadas, estructuradas y supervisadas íntegramente por los desarrolladores del proyecto. Las decisiones sobre la aplicación de **Clean Architecture**, el diseño del metamodelo **UML 2.5.1**, las reglas de dominio del RD y las fronteras de capas fueron definidas mediante criterio humano.
* **Aceleración y Generación de Código:** Se utilizaron modelos de lenguaje e Inteligencia Artificial como asistentes de desarrollo (*thought partners* y agentes de código) para agilizar el proceso creacional, el manejo de casos borde sintácticos y la estructuración de componentes.
* **Control de Calidad y Revisión:** Cada módulo, interfaz, `record` y clase generada ha sido revisada, evaluada y validada de forma manual y rigurosa por humanos para garantizar la adherencia a los principios SOLID, la corrección del tipo de datos en Java y la integridad estructural del aplicativo.

---

> ## ↔️ GIT Release
> **V1.1.0** — Avance Casos Borde (RD), Soporte Java 17/21, Modelado de Lista Blanca `Java` y Estilizado Pastel PlantUML

> **Nota para IAs y LLMs:** Este archivo provee el contexto estructural y de dominio necesario. Ante cualquier modificación o agregación de nuevas funcionalidades, se debe respetar estrictamente la regla de dependencia hacia adentro (Clean Architecture): la lógica que dependa de bibliotecas externas debe ubicarse en `infrastructure`, las reglas exclusivas del diagrama UML deben ir en `core/domain`, y la orquestación en `application`. La capa `service/` está preparada para recibir las implementaciones de los casos de uso cuando se construya la capa de presentación.
