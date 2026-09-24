# Analizador y Generador de Arquitectura UML para Proyectos Java
> 🤖 **INSTRUCCIONES PARA MODELOS DE LENGUAJE E IAs:** Este documento contiene el contexto arquitectónico, las reglas de diseño y el estado estructural exacto de este proyecto. Cualquier código que generes, refactorices o extiendas debe respetar la arquitectura Clean Architecture, los patrones de inmutabilidad y la estructura de paquetes descrita a continuación.
## 📌 Descripción General
Este aplicativo es una herramienta diseñada para analizar proyectos desarrollados en Java y generar automáticamente su arquitectura y diseño orientado a objetos en formato PlantUML. Su propósito es ayudar a desarrolladores y arquitectos de software a visualizar, entender y documentar bases de código existentes mediante diagramas UML claros y precisos.

## 🚀 Funcionalidad Principal
El flujo de trabajo del aplicativo se divide en tres etapas fundamentales:

1. **Entrada (Input):**
   - Recibe como entrada la ruta hacia la carpeta `src` de cualquier proyecto desarrollado en Java.
2. **Análisis (Procesamiento):**
   - Escanea y procesa el código fuente Java usando **JavaParser 3.28.2**.
   - Extrae e identifica: clases, clases abstractas, interfaces, records, enumeraciones, anotaciones, paquetes, atributos (propiedades), métodos (operaciones) y constructores.
   - Descubre relaciones complejas: asociaciones, dependencias, generalizaciones (herencia), realizaciones (implementación de interfaces) y anidamientos (nesting).
3. **Salida (Output):**
   - Genera código en formato **PlantUML** y también puede exportar el modelo como **Markdown** (`uml-model-output.md`). El PlantUML está listo para ser renderizado y diagramado, proporcionando una representación visual fidedigna de la arquitectura del proyecto analizado.

## 🏛 Arquitectura del Sistema
El aplicativo está construido como un **Monolito** que implementa los principios de **Clean Architecture** (Arquitectura Limpia). Se basa en una arquitectura de capas concéntricas donde las dependencias siempre apuntan hacia el interior, protegiendo las reglas de negocio.

### Estructura de Capas
El proyecto se organiza en 4 grandes capas (paquetes principales):

1. **`core` (Dominio):**
   - **Es el corazón de la aplicación.** Contiene las reglas de negocio puras y los modelos del dominio (`UmlClass`, `UmlInterface`, `UmlRecord`, `UmlEnumeration`, `UmlAnnotation`, `UmlRelationship`, `UmlModel`, etc.).
   - No depende de ninguna otra capa ni de frameworks externos. Es código Java puro.
2. **`application` (Casos de Uso):**
   - Orquesta el flujo de los datos desde y hacia las entidades del dominio.
   - Contiene los **Puertos de Entrada** (`port/in/`) — interfaces de casos de uso que la presentación invoca — y los **Puertos de Salida** (`port/out/`) — interfaces que la infraestructura debe implementar.
   - Contiene también los **modelos de contexto de aplicación** (`project/`): configuración de análisis, proyectos, layouts, listas blancas/negras, etc.
   - La capa `service/` está preparada para alojar las implementaciones de los casos de uso (actualmente en construcción).
3. **`infrastructure` (Infraestructura / Adaptadores):**
   - Implementa los puertos de salida definidos en la capa de aplicación.
   - Aquí residen los detalles técnicos y la integración con herramientas externas: analizadores de código fuente (**JavaParser**), exportadores (Markdown), sistemas de archivos y telemetría.
4. **`presentation` (Presentación):**
   - El punto de entrada para el usuario al interactuar con la aplicación (CLI, API REST o Interfaz Gráfica).
   - Invoca los casos de uso de la capa de aplicación a través de los puertos de entrada.
   - **Estado actual:** paquete declarado y preparado; la tecnología de presentación (CLI, desktop, web) aún no ha sido implementada.

## 💡 Justificación de la Arquitectura
La decisión de utilizar Clean Architecture sobre un enfoque tradicional o fuertemente acoplado se fundamenta en los siguientes principios:

- **Separación clara de responsabilidades y bajo acoplamiento:** Cada capa tiene un único motivo para cambiar. La presentación no sabe cómo se analiza el código, y el core no sabe cómo se exporta.
- **Alta cohesión e independencia del dominio respecto a frameworks externos:** El dominio de UML (`core`) no está contaminado con anotaciones de librerías externas. Estas tecnologías se consideran **herramientas y detalles de implementación**, no definen la lógica central.
- **Facilidad de mantenimiento y evolución:** Si mañana se desea cambiar JavaParser por otra librería (ej. ASM o Roaster), o agregar MermaidJS como formato de salida, solo se modifica la capa de `infrastructure`, dejando el `core` y la `application` completamente intactos.
- **Inversión de Dependencias (DIP):** Se aplica el principio SOLID DIP para asegurar que el centro no dependa de los detalles. La infraestructura depende del centro (a través de la implementación de interfaces/puertos).
- **Altamente Testeable:** Al tener casos de uso y modelos de dominio libres de dependencias de infraestructura, se pueden escribir pruebas unitarias rápidas y fiables utilizando *mocks* para los puertos.

## 📁 Estructura de Carpetas (Árbol del Proyecto)
A continuación, un mapeo de la estructura de paquetes dentro de `src/main/java/generador`, evidenciando la arquitectura:

```text
src/main/java/generador/
├── application/                # Casos de uso y Puertos (In/Out)
│   ├── port/
│   │   ├── in/                 # Interfaces que la Presentación llama
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
│   │   └── out/                # Interfaces que la Infraestructura implementa
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
│   │   ├── AnalysisConfiguration.java   # Config del análisis (whitelist, blacklist, versión Java, etc.)
│   │   ├── BlackList.java
│   │   ├── DiagramLayout.java
│   │   ├── JavaLanguageVersion.java
│   │   ├── PlantUmlDocument.java
│   │   ├── Project.java
│   │   └── WhiteList.java
│   └── service/                # Implementaciones de los casos de uso (en construcción)
├── core/                       # Entidades y lógica central de UML (Sin dependencias externas)
│   └── domain/
│       ├── classifier/         # Clasificadores concretos del metamodelo UML
│       │   ├── UmlAnnotation.java
│       │   ├── UmlClass.java
│       │   ├── UmlClassifier.java          # Clase base abstracta de todos los clasificadores
│       │   ├── UmlEnumeration.java
│       │   ├── UmlEnumerationLiteral.java
│       │   ├── UmlInterface.java
│       │   └── UmlRecord.java
│       ├── element/            # Elementos base (UmlElement, UmlNamespace)
│       ├── feature/            # Miembros internos: UmlOperation, UmlProperty, UmlParameter
│       ├── model/              # Raíz del agregado: UmlModel
│       ├── relationship/       # UmlRelationship (interfaz) + implementaciones concretas
│       │   ├── UmlAssociation.java
│       │   ├── UmlDependency.java
│       │   ├── UmlGeneralization.java
│       │   ├── UmlNesting.java
│       │   └── UmlRealization.java
│       ├── spec/               # Modificadores, visibilidad, tipos de agregación y clasificación
│       │   ├── AggregationKind.java
│       │   ├── UmlClassification.java   # Enum: CLASS, ABSTRACT_CLASS, INTERFACE, ENUMERATION, RECORD, ANNOTATION
│       │   ├── UmlModifier.java
│       │   └── UmlVisibility.java
│       └── type/               # Tipos de datos del metamodelo (UmlType)
├── infrastructure/             # Detalles técnicos (Frameworks, Librerías externas)
│   ├── codesmell/              # Detectores de code smells (preparado, sin implementación aún)
│   ├── export/                 # Exportación del modelo UML
│   │   └── UmlModelMarkdownExporter.java   # Exporta el UmlModel a Markdown estructurado
│   ├── layout/                 # Manejo de layouts de diagrama (preparado)
│   ├── parser/                 # Parsers de código Java con JavaParser 3.28.2
│   │   ├── JavaClassifierParser.java       # Parser principal de clasificadores
│   │   ├── JavaNestingParser.java
│   │   ├── JavaOperationParser.java
│   │   ├── JavaProjectParser.java
│   │   ├── JavaPropertyParser.java
│   │   ├── JavaRelationshipParser.java
│   │   ├── JavaSourceParser.java
│   │   ├── JavaTypeResolutionContext.java
│   │   └── JavaTypeResolver.java           # Resolución de tipos (externos, genéricos, arrays, etc.)
│   ├── persistence/            # Almacenamiento de archivos/DB (preparado)
│   ├── rendering/              # Integración con PlantUML (preparado)
│   └── telemetry/              # Métricas y logs (preparado)
└── presentation/               # Controladores, CLI o UI (declarado, pendiente de implementar)
```

### 🧩 Justificación de la Estructura de Paquetes

La organización interna responde a la necesidad de mantener alta cohesión temática y una estricta separación de responsabilidades en alineación con **Clean Architecture**:

  * **`core/domain/` (Aislamiento del Metamodelo UML):** Se conserva un core de Java puro, sin dependencias externas ni frameworks, que representa el metamodelo UML.
  * **`classifier/` y `element/`:** Separan la jerarquía básica de componentes del metamodelo UML (elementos nombrados, espacios de nombres, clasificadores abstractos y concretos). El clasificador base `UmlClassifier` agrupa los seis tipos concretos: `UmlClass`, `UmlInterface`, `UmlRecord`, `UmlEnumeration`, `UmlAnnotation` y el literal `UmlEnumerationLiteral`.
  * **`feature/`:** Agrupa los miembros internos de las estructuras (`UmlOperation`, `UmlProperty`, `UmlParameter`), evitando sobrecargar las entidades principales.
  * **`relationship/`:** Concentra la interfaz base `UmlRelationship` y las implementaciones concretas de cada tipo de relación (`UmlAssociation`, `UmlDependency`, `UmlGeneralization`, `UmlNesting`, `UmlRealization`).
  * **`spec/` y `type/`:** Aislar los modificadores, especificadores (visibilidad, tipos de agregación, clasificación de tipo) y tipos de datos en paquetes dedicados garantiza alta reusabilidad y previene la dispersión de definiciones base. `UmlClassification` es el enum que enumera las 6 categorías de clasificador soportadas.
  * **`model/`:** Contiene la raíz del agregado de dominio (`UmlModel`), encapsulando y representando la totalidad del sistema UML analizado. Provee métodos de estadística: `totalClasses()`, `totalInterfaces()`, `totalRecords()`, `totalEnums()`, `totalAnnotations()`, `totalRelationships()`, `totalExternalTypes()`, etc.

* **`application/` (Desacoplamiento mediante Puertos y Adaptadores):**
  * **`port/in/`:** Define **16 contratos de casos de uso** que cubren todo el ciclo de vida del modelo: creación y apertura de proyectos, construcción del modelo UML, exportación, detección de code smells, navegación/selección de elementos, manejo de layout, parsing de PlantUML, telemetría y diagnósticos.
  * **`port/out/`:** Define **9 interfaces de salida** que la infraestructura debe implementar: `AutoLayoutPort`, `CodeSmellDetectorPort`, `DiagnosticsReporterPort`, `DiagramExporterPort`, `DiagramLayoutStoragePort`, `DiagramRendererPort`, `PlantUmlParserPort`, `ProjectRepositoryPort`, `TelemetryPort`.
  * **`service/`:** Preparado para alojar la orquestación de los casos de uso sin contener lógica técnica ni dependencias de frameworks (actualmente en construcción).
  * **`project/`:** Agrupa modelos propios del contexto de ejecución: `Project`, `AnalysisConfiguration` (con su Builder fluido), `DiagramLayout`, `PlantUmlDocument`, `JavaLanguageVersion`, `WhiteList` y `BlackList`.

* **`infrastructure/` (Aislamiento de Tecnologías y Librerías Externas):**
  * **`parser/`:** Implementación activa con **JavaParser 3.28.2**. Consta de 9 clases especializadas que resuelven clasificadores, relaciones, propiedades, operaciones, anidamientos y tipos (incluidos genéricos, arrays y referencias externas).
  * **`export/`:** `UmlModelMarkdownExporter` exporta el `UmlModel` completo a un documento Markdown estructurado (véase `uml-model-output.md` en la raíz del proyecto como ejemplo de salida real).
  * Los subpaquetes `codesmell/`, `layout/`, `persistence/`, `rendering/` y `telemetry/` están declarados y preparados para recibir implementaciones futuras sin afectar el `core` ni la capa de `application`.

## 🛠 Tecnologías Involucradas
Como se mencionó, estas son vistas como "detalles" en esta arquitectura:
- **Java 21** (Lenguaje Base — `javac.source=21`, `javac.target=21`)
- **JavaParser 3.28.2:** Única dependencia de compilación. Utilizada en `infrastructure/parser` para analizar el AST de los archivos `.java`.
- **PlantUML:** Herramienta de destino en `infrastructure/rendering` para procesar el texto generado en diagramas gráficos (integración preparada, no activa aún).
- **Apache Ant + NetBeans:** Sistema de build y entorno de desarrollo. El proyecto se construye con `build.xml` y la configuración de `nbproject/`. No usa Maven, Gradle ni Spring Boot.
---

## 🤖 Declaración de Desarrollo Asistido por IA (AI-Assisted Development)

Este proyecto ha sido concebido y desarrollado bajo un modelo y diseño de arquitectura humana y apoyado con herramientas de Inteligencia Artificial para la colaboración, acompañamiento y consultoría:

* **Arquitectura y Lógica Central:** Diseñadas, estructuradas y supervisadas íntegramente por el desarrollador principal. Las decisiones sobre la aplicación de **Clean Architecture**, el diseño del metamodelo **UML 2.5.1** y las fronteras de dominio fueron definidas mediante criterio humano.
* **Aceleración y Generación de Código:** Se utilizaron modelos de lenguaje e Inteligencia Artificial como asistentes de desarrollo (*thought partners* y agentes de código) para agilizar el proceso creacional, la escritura de código boilerplate y la estructuración de componentes.
* **Control de Calidad y Revisión:** Cada módulo, interfaz, `record` y clase generada ha sido revisada, evaluada y validada de forma manual y rigurosa por un humano para garantizar la adherencia a los principios SOLID, la corrección del tipo de datos en Java y la integridad estructural del aplicativo.
---
> ## ↔️ GIT Release
> V1.0.0


> **Nota para IAs y LLMs:** Este archivo provee el contexto estructural y de dominio necesario. Ante cualquier modificación o agregación de nuevas funcionalidades, se debe respetar estrictamente la regla de dependencia hacia adentro (Clean Architecture): la lógica que dependa de bibliotecas externas debe ubicarse en `infrastructure`, las reglas exclusivas del diagrama UML deben ir en `core/domain`, y la orquestación en `application`. La capa `service/` está preparada para recibir las implementaciones de los casos de uso cuando se construya la capa de presentación.
