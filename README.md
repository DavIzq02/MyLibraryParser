# Analizador y Generador de Arquitectura UML para Proyectos Java
> 🤖 **INSTRUCCIONES PARA MODELOS DE LENGUAJE E IAs:** Este documento contiene el contexto arquitectónico, las reglas de diseño y el estado estructural exacto de este proyecto. Cualquier código que generes, refactorices o extiendas debe respetar la arquitectura Clean Architecture, los patrones de inmutabilidad y la estructura de paquetes descrita a continuación.
## 📌 Descripción General
Este aplicativo es una herramienta diseñada para analizar proyectos desarrollados en Java y generar automáticamente su arquitectura y diseño orientado a objetos en formato PlantUML. Su propósito es ayudar a desarrolladores y arquitectos de software a visualizar, entender y documentar bases de código existentes mediante diagramas UML claros y precisos.

## 🚀 Funcionalidad Principal
El flujo de trabajo del aplicativo se divide en tres etapas fundamentales:

1. **Entrada (Input):**
   - Recibe como entrada la ruta hacia la carpeta `src` de cualquier proyecto desarrollado en Java.
2. **Análisis (Procesamiento):**
   - Escanea y procesa el código fuente.
   - Extrae e identifica: clases, interfaces, paquetes, atributos (propiedades), métodos (operaciones).
   - Descubre relaciones complejas: asociaciones, dependencias, generalizaciones (herencia), implementaciones y composiciones/agregaciones.
3. **Salida (Output):**
   - Genera código en formato **PlantUML**, el cual está listo para ser renderizado y diagramado. Esto proporciona una representación visual fidedigna de la arquitectura del proyecto analizado.

## 🏛 Arquitectura del Sistema
El aplicativo está construido como un **Monolito** que implementa los principios de **Clean Architecture** (Arquitectura Limpia). Se basa en una arquitectura de capas concéntricas donde las dependencias siempre apuntan hacia el interior, protegiendo las reglas de negocio.

### Estructura de Capas
El proyecto se organiza en 4 grandes capas (paquetes principales):

1. **`core` (Dominio):**
   - **Es el corazón de la aplicación.** Contiene las reglas de negocio puras y los modelos del dominio (ej. `UmlClass`, `UmlRelationship`, `UmlElement`).
   - No depende de ninguna otra capa ni de frameworks externos. Es código Java puro.
2. **`application` (Casos de Uso):**
   - Orquesta el flujo de los datos desde y hacia las entidades del dominio.
   - Contiene los **Puertos de Entrada** (Interfaces de Casos de Uso que la presentación invoca) y **Puertos de Salida** (Interfaces que la infraestructura debe implementar, como repositorios o generadores externos).
3. **`infrastructure` (Infraestructura / Adaptadores):**
   - Implementa los puertos de salida definidos en la capa de aplicación.
   - Aquí residen los detalles técnicos y la integración con herramientas externas: analizadores de código fuente (JavaParser), exportadores (PlantUML), sistemas de archivos, bases de datos o telemetría.
4. **`presentation` (Presentación):**
   - El punto de entrada para el usuario interactuar con la aplicación (ej. Interfaz de Línea de Comandos (CLI), API REST o Interfaz Gráfica).
   - Invoca los casos de uso de la capa de aplicación.

## 💡 Justificación de la Arquitectura
La decisión de utilizar Clean Architecture sobre un enfoque tradicional o fuertemente acoplado se fundamenta en los siguientes principios:

- **Separación clara de responsabilidades y bajo acoplamiento:** Cada capa tiene un único motivo para cambiar. La presentación no sabe cómo se analiza el código, y el core no sabe cómo se exporta a PlantUML.
- **Alta cohesión e independencia del dominio respecto a frameworks externos:** El dominio de UML (`core`) no está contaminado con anotaciones de Spring Boot, JavaParser o PlantUML. Estas tecnologías se consideran **herramientas y detalles de implementación**, no definen la lógica central.
- **Facilidad de mantenimiento y evolución:** Si mañana se desea cambiar JavaParser por otra librería (ej. ASM o Roaster), o PlantUML por MermaidJS, solo se modifica la capa de `infrastructure`, dejando el `core` y la `application` completamente intactos.
- **Inversión de Dependencias (DIP):** Se aplica el principio SOLID DIP para asegurar que el centro no dependa de los detalles. La infraestructura depende del centro (a través de la implementación de interfaces/puertos).
- **Altamente Testeable:** Al tener casos de uso y modelos de dominio libres de dependencias de infraestructura, se pueden escribir pruebas unitarias rápidas y fiables utilizando *mocks* para los puertos.

## 📁 Estructura de Carpetas (Árbol del Proyecto)
A continuación, un mapeo de la estructura de paquetes dentro de `src/main/java/generador`, evidenciando la arquitectura:

```text
src/main/java/generador/
├── application/                # Casos de uso y Puertos (In/Out)
│   ├── port/
│   │   ├── in/                 # Interfaces que la Presentación llama (Ej. BuildUmlModelUseCase, ExportDiagramUseCase)
│   │   └── out/                # Interfaces que la Infraestructura implementa (Ej. PlantUmlParserPort, DiagramExporterPort)
│   ├── project/                # Modelos de aplicación (Proyectos, Layouts, Configuraciones)
│   └── service/                # Implementación de los casos de uso
├── core/                       # Entidades y lógica central de UML (Sin dependencias)
│   └── domain/
│       ├── classifier/         # Clases, interfaces, records y enums (UmlClassifier, UmlRecord)
│       ├── element/            # Elementos base (UmlElement, UmlNamespace)
│       ├── feature/            # Operaciones y propiedades (Atributos, Parámetros, Métodos)
│       ├── model/              # Modelos agregados (UmlModel)
│       ├── relationship/       # Asociaciones, Dependencias, Herencia, Realización (UmlRelationship)
│       ├── spec/               # Modificadores, Visibilidad, Tipos de Agregación
│       └── type/               # Tipos de datos (UmlType)
├── infrastructure/             # Detalles técnicos (Frameworks, Librerías externas)
│   ├── codesmell/              # Detectores de código (opcional/plugins)
│   ├── export/                 # Exportación de diagramas (Ej. UmlModelMarkdownExporter)
│   ├── layout/                 # Manejo de layouts
│   ├── parser/                 # Implementación con JavaParser y resolutores de tipos
│   ├── persistence/            # Almacenamiento de archivos/DB
│   ├── rendering/              # Integración con PlantUML
│   └── telemetry/              # Métricas y logs
└── presentation/               # Controladores, CLI o Interfaz de Usuario
```
### 🧩 Justificación de la Estructura de Paquetes

La organización interna responde a la necesidad de mantener alta cohesión temática y una estricta separación de responsabilidades en alineación con **Clean Architecture**:

  * **`core/domain/` (Aislamiento del Metamodelo UML):** Se conserva un core de Java puro,sin dependencias externas ni frameworks,  que representa el metamodelo UML.
  * **`classifier/` y `element/`:** Separan la jerarquía básica de componentes del metamodelo UML (elementos nombrados, espacios de nombres, clasificadores abstractos y concretos).
  * **`feature/`:** Agrupa los miembros internos de las estructuras (`UmlOperation`, `UmlProperty`, `UmlParameter`), evitando sobrecargar las entidades principales.
  * **`relationship/`:** Concentra la interfaz base `UmlRelationship` y las implementaciones concretas de cada tipo de relación (`UmlAssociation`, `UmlDependency`, `UmlGeneralization`, `UmlNesting`, `UmlRealization`).
  * **`spec/` y `type/`:** Aislar los modificadores, especificadores (visibilidad, tipos de agregación) y tipos de datos en paquetes dedicados garantiza alta reusabilidad y previene la dispersión de definiciones base.
  * **`model/`:** Contiene la raíz del agregado de dominio (`UmlModel`), encapsulando y representando la totalidad del sistema UML analizado.

* **`application/` (Desacoplamiento mediante Puertos y Adaptadores):**
  * **`port/in/` y `port/out/`:** Definen los contratos (interfaces) que aíslan la lógica de aplicación tanto de la capa de entrada (CLI/REST) como de las herramientas tecnológicas externas.
  * **`service/`:** Aloja la orquestación de los casos de uso sin contener lógica técnica ni dependencias de frameworks.
  * **`project/`:** Agrupa modelos o conceptos propios del contexto de ejecución de la aplicación.

* **`infrastructure/` (Aislamiento de Tecnologías y Librerías Externas):**
  * Cada subpaquete (`parser/`, `rendering/`, `export/`, `persistence/`, `layout/`, `telemetry/`, `codesmell/`) confina una responsabilidad técnica específica. Esto asegura que cualquier cambio o sustitución de librerías externas (como **JavaParser** o **PlantUML**) afecte únicamente a su paquete asignado sin impactar el `core` ni la capa de `application`.

## 🛠 Tecnologías Involucradas
Como se mencionó, estas son vistas como "detalles" en esta arquitectura:
- **Java 21+** (Lenguaje Base)
- **Spring Boot:** Utilizado únicamente en los bordes para Inyección de Dependencias, despliegue o APIs si se requiere.
- **JavaParser:** Herramienta utilizada en `infrastructure/parser` para analizar el AST de los archivos `.java`.
- **PlantUML:** Herramienta de destino en `infrastructure/rendering` para procesar el texto generado en diagramas gráficos.
---

## 🤖 Declaración de Desarrollo Asistido por IA (AI-Assisted Development)

Este proyecto ha sido concebido y desarrollado bajo un modelo y diseño de arquitectura humana y apoyado con herramientas de Inteligencia Artificial para la colaboración, acompañamiento y consultoria:

* **Arquitectura y Lógica Central:** Diseñadas, estructuradas y supervisadas íntegramente por el desarrollador principal. Las decisiones sobre la aplicación de **Clean Architecture**, el diseño del metamodelo **UML 2.5.1** y las fronteras de dominio fueron definidas mediante criterio humano.
* **Aceleración y Generación de Código:** Se utilizaron modelos de lenguaje e Inteligencia Artificial como asistentes de desarrollo (*thought partners* y agentes de código) para agilizar el proceso creacional, la escritura de código boilerplate y la estructuración de componentes.
* **Control de Calidad y Revisión:** Cada módulo, interfaz, `record` y clase generada ha sido revisada, evaluada y validada de forma manual y rigurosa por un humano para garantizar la adherencia a los principios SOLID, la corrección del tipo de datos en Java y la integridad estructural del aplicativo.
---
> ## ↔️ GIT Release
> V1.0.0


> **Nota para IAs y LLMs:** Este archivo provee el contexto estructural y de dominio necesario. Ante cualquier modificación o agregación de nuevas funcionalidades, se debe respetar estrictamente la regla de dependencia hacia adentro (Clean Architecture): la lógica que dependa de bibliotecas externas debe ubicarse en `infrastructure`, las reglas exclusivas del diagrama UML deben ir en `core/domain`, y la orquestación en `application`.
