# UML Model

> Modelo UML generado automáticamente a partir del análisis de un proyecto Java.

---

## 1. Summary

| Element | Quantity |
|---|---:|
| Classifiers | 61 |
| Relationships | 89 |

## 2. Classifiers

### `com::universidad::App`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

_None._

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `main(args : java.lang.String[])` | `void` |
| `PRIVATE` | `iniciarAplicacion()` | `void` |
| `PRIVATE` | `registrarModulos(motorConsola : CliEngine, config : ConfiguracionDeDependencias)` | `void` |

---

### `com::universidad::config::modulos::ConfiguracionModuloEstudiante`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `repositorio` | `EstudianteRepositorio` | `[FINAL]` |
| `PRIVATE` | `servicio` | `EstudianteServicio` | `[FINAL]` |

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `getRepositorio()` | `EstudianteRepositorio` |
| `PUBLIC` | `getServicio()` | `EstudianteServicio` |
| `PUBLIC` | `construirVista()` | `SystemModule` |
| `PUBLIC` | `cerrarRecursos()` | `void` |

---

### `com::universidad::dto::curso::CursoActualizarDto`

**Type:** `UmlRecord`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `id` | `UUID` | `[FINAL]` |
| `PRIVATE` | `nuevoNombre` | `java.lang.String` | `[FINAL]` |
| `PRIVATE` | `nuevoCupo` | `java.lang.Integer` | `[FINAL]` |
| `PRIVATE` | `nuevoEstado` | `java.lang.Integer` | `[FINAL]` |

#### Operations

_None._

---

### `com::universidad::vista::ProfesorVista`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `ANCHO_TABLA` | `int` | `[FINAL]` |
| `PRIVATE` | `controlador` | `ProfesorControlador` | `[FINAL]` |
| `PRIVATE` | `columnasTablaProfe` | `java.util.List<TableColumn>` | `[FINAL]` |

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `getModuleName()` | `java.lang.String` |
| `PUBLIC` | `execute(cnsl : Console, sf : ScreenFormatter, tr : TableRenderer)` | `void` |
| `PRIVATE` | `mostrarMenu(console : Console, formatter : ScreenFormatter, renderer : TableRenderer)` | `void` |
| `PRIVATE` | `textoEstado(dto : ProfesorDto, formatter : ScreenFormatter)` | `java.lang.String` |
| `PRIVATE` | `extraerDatosProfesor(dto : ProfesorDto, formatter : ScreenFormatter)` | `java.lang.Object[]` |
| `PRIVATE` | `listarProfes(console : Console, formatter : ScreenFormatter, renderer : TableRenderer)` | `void` |
| `PRIVATE` | `crearProfe(console : Console, formatter : ScreenFormatter, renderer : TableRenderer)` | `void` |
| `PRIVATE` | `actualizarProfesor(console : Console, formatter : ScreenFormatter, renderer : TableRenderer)` | `void` |
| `PRIVATE` | `eliminarProfesor(console : Console)` | `void` |
| `PRIVATE` | `leerEnteroOpcional(console : Console, mensaje : java.lang.String)` | `java.lang.Integer` |

---

### `com::universidad::modelo::constante::PoliticasAcademicas`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

**Modifiers:** `[FINAL]`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PUBLIC` | `MIN_SEMANAS_ASIGNATURA` | `int` | `[STATIC, FINAL]` |
| `PUBLIC` | `MAX_SEMANAS_ASIGNATURA` | `int` | `[STATIC, FINAL]` |
| `PUBLIC` | `MIN_CUPO_MATERIA` | `int` | `[STATIC, FINAL]` |
| `PUBLIC` | `MAX_CUPO_MATERIA` | `int` | `[STATIC, FINAL]` |

#### Operations

_None._

---

### `com::universidad::modelo::Profesor`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `idProfesor` | `java.lang.Long` | `[]` |
| `PRIVATE` | `nombreProfesor` | `java.lang.String` | `[]` |
| `PRIVATE` | `celularProfesor` | `java.lang.String` | `[]` |
| `PRIVATE` | `estadoProfesor` | `EstadoEntidad` | `[]` |

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `actualizarNombre(nuevoNombre : java.lang.String)` | `void` |
| `PUBLIC` | `actualizarCelular(nuevoCelular : java.lang.String)` | `void` |
| `PUBLIC` | `cambiarEstado(nuevoEstado : EstadoEntidad)` | `void` |
| `PUBLIC` | `estaActivo()` | `boolean` |
| `PUBLIC` | `getIdProfesor()` | `java.lang.Long` |
| `PUBLIC` | `getNombreProfesor()` | `java.lang.String` |
| `PUBLIC` | `getCelularProfesor()` | `java.lang.String` |
| `PUBLIC` | `getEstadoProfesor()` | `EstadoEntidad` |

---

### `com::universidad::vista::constante::ConfiguracionCli`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

**Modifiers:** `[FINAL]`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PUBLIC` | `TITULO_SISTEMA` | `java.lang.String` | `[STATIC, FINAL]` |
| `PUBLIC` | `MENSAJE_SALIDA` | `java.lang.String` | `[STATIC, FINAL]` |
| `PUBLIC` | `CARACTER_SEPARADOR` | `java.lang.String` | `[STATIC, FINAL]` |
| `PUBLIC` | `LONGITUD_SEPARADOR` | `int` | `[STATIC, FINAL]` |
| `PUBLIC` | `PREFIJO_ERROR` | `java.lang.String` | `[STATIC, FINAL]` |
| `PUBLIC` | `TEXTO_NULO` | `java.lang.String` | `[STATIC, FINAL]` |
| `PUBLIC` | `TEXTO_SI` | `java.lang.String` | `[STATIC, FINAL]` |
| `PUBLIC` | `TEXTO_NO` | `java.lang.String` | `[STATIC, FINAL]` |
| `PUBLIC` | `FORMATO_FECHA_ENTRADA` | `java.lang.String` | `[STATIC, FINAL]` |
| `PUBLIC` | `FORMATO_FECHA_VISUAL` | `java.lang.String` | `[STATIC, FINAL]` |
| `PUBLIC` | `FORMATO_FECHA_HORA_VISUAL` | `java.lang.String` | `[STATIC, FINAL]` |
| `PUBLIC` | `LOCALE` | `Locale` | `[STATIC, FINAL]` |

#### Operations

_None._

---

### `com::universidad::vista::AsignaturaVista`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `ANCHO_TABLA` | `int` | `[FINAL]` |
| `PRIVATE` | `controlador` | `AsignaturaControlador` | `[FINAL]` |
| `PRIVATE` | `columnasTablaMateria` | `java.util.List<TableColumn>` | `[FINAL]` |

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `getModuleName()` | `java.lang.String` |
| `PUBLIC` | `execute(console : Console, formatter : ScreenFormatter, renderer : TableRenderer)` | `void` |
| `PRIVATE` | `mostrarMenu(console : Console, formatter : ScreenFormatter, renderer : TableRenderer)` | `void` |
| `PRIVATE` | `listarAsignatura(console : Console, formatter : ScreenFormatter, renderer : TableRenderer)` | `void` |
| `PRIVATE` | `textoEstado(dto : AsignaturaCostoDto, formatter : ScreenFormatter)` | `java.lang.String` |
| `PRIVATE` | `extraerDatosAsignatura(dto : AsignaturaCostoDto, formatter : ScreenFormatter)` | `java.lang.Object[]` |
| `PRIVATE` | `crearAsignatura(console : Console, formatter : ScreenFormatter, renderer : TableRenderer)` | `void` |

---

### `com::universidad::controlador::AsignaturaControlador`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `servicio` | `AsignaturaServicio` | `[FINAL]` |

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `crearAsignatura(dto : AsignaturaCostoCrearDto)` | `AsignaturaCostoDto` |
| `PUBLIC` | `cantidadAsignatura()` | `int` |
| `PUBLIC` | `listarAsignatura()` | `java.util.List<AsignaturaCostoDto>` |

---

### `com::universidad::mapeador::AsignaturaMapeador`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

_None._

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `toDto(entidad : AsignaturaCosto)` | `AsignaturaCostoDto` |
| `PUBLIC` | `toDtoList(entidades : java.util.List<AsignaturaCosto>)` | `java.util.List<AsignaturaCostoDto>` |

---

### `com::universidad::config::modulos::ConfiguracionModuloAsignatura`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `repositorio` | `AsignaturaRepositorio` | `[FINAL]` |
| `PRIVATE` | `servicio` | `AsignaturaServicio` | `[FINAL]` |

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `getRepositorio()` | `AsignaturaRepositorio` |
| `PUBLIC` | `getServicio()` | `AsignaturaServicio` |
| `PUBLIC` | `construirVista()` | `SystemModule` |
| `PUBLIC` | `cerrarRecursos()` | `void` |

---

### `com::universidad::mapeador::ProfesorMapeador`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

_None._

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `toDto(entidad : Profesor)` | `ProfesorDto` |
| `PUBLIC` | `toDtoList(entidades : java.util.List<Profesor>)` | `java.util.List<ProfesorDto>` |

---

### `com::universidad::repositorio::cargador::ProfesorCargador`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `cache` | `java.util.Map<java.lang.Long, Profesor>` | `[FINAL]` |
| `PRIVATE` | `repositorio` | `ProfesorRepositorio` | `[FINAL]` |

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `cargarTodos()` | `void` |
| `PUBLIC` | `cargarPorIds(ids : java.util.List<java.lang.Long>)` | `void` |
| `PUBLIC` | `registrarEnCache(entidad : Profesor)` | `void` |
| `PUBLIC` | `obtener(id : java.lang.Long)` | `java.util.Optional<Profesor>` |
| `PUBLIC` | `existe(id : java.lang.Long)` | `boolean` |

---

### `com::universidad::modelo::Curso`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `idCurso` | `UUID` | `[]` |
| `PRIVATE` | `codigoCurso` | `java.lang.String` | `[]` |
| `PRIVATE` | `nombreCurso` | `java.lang.String` | `[]` |
| `PRIVATE` | `creditosCurso` | `java.lang.Integer` | `[]` |
| `PRIVATE` | `cupoMaximoCurso` | `java.lang.Integer` | `[]` |
| `PRIVATE` | `estadoCurso` | `EstadoEntidad` | `[]` |

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `actualizarNombre(nuevoNombre : java.lang.String)` | `void` |
| `PUBLIC` | `actualizarCupo(nuevoCupo : java.lang.Integer)` | `void` |
| `PUBLIC` | `cambiarEstado(nuevoEstado : EstadoEntidad)` | `void` |
| `PUBLIC` | `estaActivo()` | `boolean` |
| `PUBLIC` | `getIdCurso()` | `UUID` |
| `PUBLIC` | `getCodigoCurso()` | `java.lang.String` |
| `PUBLIC` | `getNombreCurso()` | `java.lang.String` |
| `PUBLIC` | `getCreditosCurso()` | `java.lang.Integer` |
| `PUBLIC` | `getCupoMaximoCurso()` | `java.lang.Integer` |
| `PUBLIC` | `getEstadoCurso()` | `EstadoEntidad` |

---

### `com::universidad::persistencia::RepositorioBaseAbstracto`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

**Template parameters:** `T, ID`

**Modifiers:** `[ABSTRACT]`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PROTECTED` | `tpaRepository` | `TpaRepository<T, ID>` | `[FINAL]` |

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PROTECTED` | `permiteBorradoFisico()` | `boolean` |
| `PUBLIC` | `cerrar()` | `void` |
| `PUBLIC` | `contar()` | `long` |
| `PUBLIC` | `listarTodos()` | `java.util.List<T>` |
| `PUBLIC` | `eliminar(id : ID)` | `boolean` |
| `PUBLIC` | `buscarPorId(id : ID)` | `java.util.Optional<T>` |
| `PUBLIC` | `actualizar(entidad : T)` | `T` |
| `PUBLIC` | `guardar(entidad : T)` | `T` |

---

### `com::universidad::repositorio::cargador::CargadorDatos`

**Type:** `UmlInterface`

**Visibility:** `PUBLIC`

**Template parameters:** `T, ID`

#### Properties

_None._

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `cargarTodos()` | `void` |
| `PUBLIC` | `cargarPorIds(ids : java.util.List<ID>)` | `void` |
| `PUBLIC` | `registrarEnCache(entidad : T)` | `void` |
| `PUBLIC` | `obtener(id : ID)` | `java.util.Optional<T>` |
| `PUBLIC` | `existe(id : ID)` | `boolean` |

---

### `com::universidad::persistencia::AsignaturaRepositorioImpl`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

_None._

#### Operations

_None._

---

### `com::universidad::modelo::enumeracion::EstadoEntidad`

**Type:** `UmlEnumeration`

**Visibility:** `PUBLIC`

#### Properties

_None._

#### Operations

_None._

---

### `com::universidad::mapeador::EstudianteMapeador`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

_None._

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `toDto(entidad : Estudiante)` | `EstudianteDto` |
| `PUBLIC` | `toDtoList(entidades : java.util.List<Estudiante>)` | `java.util.List<EstudianteDto>` |

---

### `com::universidad::servicio::ProfesorServicio`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `mapeador` | `ProfesorMapeador` | `[FINAL]` |
| `PRIVATE` | `repositorio` | `ProfesorRepositorio` | `[FINAL]` |

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `registrarProfesor(dto : ProfesorCrearDto)` | `ProfesorDto` |
| `PUBLIC` | `obtenerProfesores()` | `java.util.List<ProfesorDto>` |
| `PUBLIC` | `contarProfesores()` | `int` |
| `PUBLIC` | `actualizarProfesor(dto : ProfesorActualizarDto)` | `ProfesorDto` |
| `PUBLIC` | `eliminarProfesor(id : java.lang.Long)` | `boolean` |

---

### `com::universidad::modelo::convertidor::EstadoEntidadConverter`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

_None._

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PROTECTED` | `getEnumClass()` | `Class<EstadoEntidad>` |

---

### `com::universidad::Main`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

_None._

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `main(args : java.lang.String[])` | `void` |

---

### `com::universidad::controlador::ProfesorControlador`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `servi` | `ProfesorServicio` | `[FINAL]` |

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `crearProfesor(dto : ProfesorCrearDto)` | `ProfesorDto` |
| `PUBLIC` | `cantidadProfesores()` | `int` |
| `PUBLIC` | `listarProfesores()` | `java.util.List<ProfesorDto>` |
| `PUBLIC` | `actualizarProfesor(dto : ProfesorActualizarDto)` | `ProfesorDto` |
| `PUBLIC` | `eliminarProfesor(id : java.lang.Long)` | `boolean` |

---

### `com::universidad::servicio::AsignaturaServicio`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `mapeador` | `AsignaturaMapeador` | `[FINAL]` |
| `PRIVATE` | `repositorio` | `AsignaturaRepositorio` | `[FINAL]` |

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `registrarAsignatura(dto : AsignaturaCostoCrearDto)` | `AsignaturaCostoDto` |
| `PUBLIC` | `obtenerAsignaturas()` | `java.util.List<AsignaturaCostoDto>` |
| `PUBLIC` | `contarAsignaturas()` | `int` |

---

### `com::universidad::repositorio::ProfesorRepositorio`

**Type:** `UmlInterface`

**Visibility:** `PUBLIC`

#### Properties

_None._

#### Operations

_None._

---

### `com::universidad::controlador::CursoControlador`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `servicio` | `CursoServicio` | `[FINAL]` |

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `CrearCurso(dto : CursoCrearDto)` | `CursoDto` |
| `PUBLIC` | `listarCursos()` | `java.util.List<CursoDto>` |
| `PUBLIC` | `cantidadCursos()` | `long` |
| `PUBLIC` | `actualizarCurso(dto : CursoActualizarDto)` | `CursoDto` |
| `PUBLIC` | `eliminarCurso(id : UUID)` | `boolean` |

---

### `com::universidad::dto::profesor::ProfesorDto`

**Type:** `UmlRecord`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `id` | `java.lang.Long` | `[FINAL]` |
| `PRIVATE` | `nombre` | `java.lang.String` | `[FINAL]` |
| `PRIVATE` | `celular` | `java.lang.String` | `[FINAL]` |
| `PRIVATE` | `estado` | `EstadoEntidad` | `[FINAL]` |
| `PRIVATE` | `activo` | `java.lang.Boolean` | `[FINAL]` |

#### Operations

_None._

---

### `com::universidad::mapeador::CursoMapeador`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

_None._

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `toDto(entidad : Curso)` | `CursoDto` |
| `PUBLIC` | `toDtoList(entidades : java.util.List<Curso>)` | `java.util.List<CursoDto>` |

---

### `com::universidad::servicio::EstudianteServicio`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `mapeador` | `EstudianteMapeador` | `[FINAL]` |
| `PRIVATE` | `repositorio` | `EstudianteRepositorio` | `[FINAL]` |

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `registrarEstudiante(dto : EstudianteCrearDto)` | `EstudianteDto` |
| `PUBLIC` | `obtenerEstudiantes()` | `java.util.List<EstudianteDto>` |
| `PUBLIC` | `contarEstudiantes()` | `long` |
| `PUBLIC` | `actualizarEstudiante(dto : EstudianteActualizarDto)` | `EstudianteDto` |
| `PUBLIC` | `eliminarEstudiante(id : UUID)` | `boolean` |

---

### `com::universidad::dto::validacion::ReglasValidacion`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

**Modifiers:** `[FINAL]`

#### Properties

_None._

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `limpiarRequerido(valor : java.lang.String, mensajeError : java.lang.String)` | `java.lang.String` |
| `PUBLIC` | `limpiarUuidRequerido(valor : UUID, mensajeError : java.lang.String)` | `UUID` |
| `PUBLIC` | `limpiarEnteroRequerido(valor : java.lang.Integer, mensajeError : java.lang.String)` | `java.lang.Integer` |
| `PUBLIC` | `limpiarLongRequerido(valor : java.lang.Long, mensajeError : java.lang.String)` | `java.lang.Long` |
| `PUBLIC` | `limpiarCorreo(correo : java.lang.String)` | `java.lang.String` |
| `PUBLIC` | `limpiarCelular(celular : java.lang.String)` | `java.lang.String` |
| `PUBLIC` | `limpiarEnteroEnRango(valor : java.lang.Integer, min : int, max : int, mensajeError : java.lang.String)` | `java.lang.Integer` |
| `PUBLIC` | `limpiarEnteroPositivo(valor : java.lang.Integer, mensajeError : java.lang.String)` | `java.lang.Integer` |
| `PUBLIC` | `limpiarBigDecimalPositivo(valor : BigDecimal, mensajeError : java.lang.String)` | `BigDecimal` |
| `PUBLIC` | `limpiarBigDecimalNoNegativo(valor : BigDecimal, mensajeError : java.lang.String)` | `BigDecimal` |
| `PUBLIC` | `limpiarListaRequerida(lista : java.util.List<T>, mensajeError : java.lang.String)` | `java.util.List<T>` |

---

### `com::universidad::vista::CursoVista`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `ANCHO_TABLA` | `int` | `[STATIC]` |
| `PRIVATE` | `controlador` | `CursoControlador` | `[FINAL]` |
| `PRIVATE` | `columnas` | `java.util.List<TableColumn>` | `[FINAL]` |

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `getModuleName()` | `java.lang.String` |
| `PUBLIC` | `execute(console : Console, formatter : ScreenFormatter, renderer : TableRenderer)` | `void` |
| `PRIVATE` | `mostrarMenu(console : Console, formatter : ScreenFormatter, renderer : TableRenderer)` | `void` |
| `PRIVATE` | `extraerDatos(dto : CursoDto, formatter : ScreenFormatter)` | `java.lang.Object[]` |
| `PRIVATE` | `crearCurso(console : Console, formatter : ScreenFormatter, renderer : TableRenderer)` | `void` |
| `PRIVATE` | `listarCursos(console : Console, formatter : ScreenFormatter, renderer : TableRenderer)` | `void` |
| `PRIVATE` | `actualizarCurso(console : Console, formatter : ScreenFormatter, renderer : TableRenderer)` | `void` |
| `PRIVATE` | `eliminarCurso(console : Console)` | `void` |
| `PRIVATE` | `leerEnteroOpcional(console : Console, mensaje : java.lang.String)` | `java.lang.Integer` |

---

### `com::universidad::config::modulos::ConfiguracionModuloCurso`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `repositorio` | `CursoRepositorio` | `[FINAL]` |
| `PRIVATE` | `servicio` | `CursoServicio` | `[FINAL]` |

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `getRepositorio()` | `CursoRepositorio` |
| `PUBLIC` | `getServicio()` | `CursoServicio` |
| `PUBLIC` | `construirVista()` | `SystemModule` |
| `PUBLIC` | `cerrarRecursos()` | `void` |

---

### `com::universidad::controlador::EstudianteControlador`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `servicio` | `EstudianteServicio` | `[FINAL]` |

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `crearEstudiante(dto : EstudianteCrearDto)` | `EstudianteDto` |
| `PUBLIC` | `listarEstudiantes()` | `java.util.List<EstudianteDto>` |
| `PUBLIC` | `cantidadEstudiantes()` | `long` |
| `PUBLIC` | `actualizarEstudiante(dto : EstudianteActualizarDto)` | `EstudianteDto` |
| `PUBLIC` | `eliminarEstudiante(id : UUID)` | `boolean` |

---

### `com::universidad::dto::asignaturacosto::AsignaturaCostoDto`

**Type:** `UmlRecord`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `id` | `java.lang.Integer` | `[FINAL]` |
| `PRIVATE` | `nombre` | `java.lang.String` | `[FINAL]` |
| `PRIVATE` | `semanas` | `java.lang.Short` | `[FINAL]` |
| `PRIVATE` | `costoBase` | `BigDecimal` | `[FINAL]` |
| `PRIVATE` | `estado` | `EstadoEntidad` | `[FINAL]` |

#### Operations

_None._

---

### `com::universidad::persistencia::ProfesorRepositorioImpl`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

_None._

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PROTECTED` | `permiteBorradoFisico()` | `boolean` |

---

### `com::universidad::repositorio::CursoRepositorio`

**Type:** `UmlInterface`

**Visibility:** `PUBLIC`

#### Properties

_None._

#### Operations

_None._

---

### `com::universidad::repositorio::AsignaturaRepositorio`

**Type:** `UmlInterface`

**Visibility:** `PUBLIC`

#### Properties

_None._

#### Operations

_None._

---

### `com::universidad::config::ConfiguracionDeDependencias`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `modulos` | `java.util.List<SystemModule>` | `[FINAL]` |
| `PRIVATE` | `modulosConfigurados` | `java.util.List<ModuloConfigurable>` | `[FINAL]` |

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `getModulos()` | `java.util.List<SystemModule>` |
| `PUBLIC` | `close()` | `void` |

---

### `com::universidad::dto::curso::CursoDto`

**Type:** `UmlRecord`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `id` | `UUID` | `[FINAL]` |
| `PRIVATE` | `codigoCurso` | `java.lang.String` | `[FINAL]` |
| `PRIVATE` | `nombreCurso` | `java.lang.String` | `[FINAL]` |
| `PRIVATE` | `creditosCurso` | `java.lang.Integer` | `[FINAL]` |
| `PRIVATE` | `cupoMaximoCurso` | `java.lang.Integer` | `[FINAL]` |
| `PRIVATE` | `estado` | `EstadoEntidad` | `[FINAL]` |
| `PRIVATE` | `activo` | `boolean` | `[FINAL]` |

#### Operations

_None._

---

### `com::universidad::modelo::convertidor::FechaLocalConverter`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

_None._

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `convertToDatabaseColumn(fecha : LocalDate)` | `java.lang.String` |
| `PUBLIC` | `convertToEntityAttribute(texto : java.lang.String)` | `LocalDate` |

---

### `com::universidad::modelo::Estudiante`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `idEstudiante` | `UUID` | `[]` |
| `PRIVATE` | `codigoEstudiante` | `java.lang.String` | `[]` |
| `PRIVATE` | `nombreEstudiante` | `java.lang.String` | `[]` |
| `PRIVATE` | `direccionEstudiante` | `java.lang.String` | `[]` |
| `PRIVATE` | `correoEstudiante` | `java.lang.String` | `[]` |
| `PRIVATE` | `celularEstudiante` | `java.lang.String` | `[]` |
| `PRIVATE` | `estadoEstudiante` | `EstadoEntidad` | `[]` |

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `actualizarCorreo(nuevoCorreo : java.lang.String)` | `void` |
| `PUBLIC` | `actualizarCelular(nuevoCelular : java.lang.String)` | `void` |
| `PUBLIC` | `actualizarDireccion(nuevaDireccion : java.lang.String)` | `void` |
| `PUBLIC` | `cambiarEstado(nuevoEstado : EstadoEntidad)` | `void` |
| `PUBLIC` | `estaActivo()` | `boolean` |
| `PUBLIC` | `getIdEstudiante()` | `UUID` |
| `PUBLIC` | `getCodigoEstudiante()` | `java.lang.String` |
| `PUBLIC` | `getNombreEstudiante()` | `java.lang.String` |
| `PUBLIC` | `getDireccionEstudiante()` | `java.lang.String` |
| `PUBLIC` | `getCorreoEstudiante()` | `java.lang.String` |
| `PUBLIC` | `getCelularEstudiante()` | `java.lang.String` |
| `PUBLIC` | `getEstadoEstudiante()` | `EstadoEntidad` |

---

### `com::universidad::config::RutaPersistencia`

**Type:** `UmlEnumeration`

**Visibility:** `PUBLIC`

#### Properties

_None._

#### Operations

_None._

---

### `com::universidad::vista::EstudianteVista`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `ANCHO_TABLA` | `int` | `[STATIC, FINAL]` |
| `PRIVATE` | `controlador` | `EstudianteControlador` | `[FINAL]` |
| `PRIVATE` | `columnas` | `java.util.List<TableColumn>` | `[FINAL]` |

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `getModuleName()` | `java.lang.String` |
| `PUBLIC` | `execute(console : Console, formatter : ScreenFormatter, renderer : TableRenderer)` | `void` |
| `PRIVATE` | `mostrarMenu(console : Console, formatter : ScreenFormatter, renderer : TableRenderer)` | `void` |
| `PRIVATE` | `extraerDatos(dto : EstudianteDto, formatter : ScreenFormatter)` | `java.lang.Object[]` |
| `PRIVATE` | `crearEstudiante(console : Console, formatter : ScreenFormatter, renderer : TableRenderer)` | `void` |
| `PRIVATE` | `listarEstudiantes(console : Console, formatter : ScreenFormatter, renderer : TableRenderer)` | `void` |
| `PRIVATE` | `actualizarEstudiante(console : Console, formatter : ScreenFormatter, renderer : TableRenderer)` | `void` |
| `PRIVATE` | `eliminarEstudiante(console : Console)` | `void` |
| `PRIVATE` | `leerEnteroOpcional(console : Console, mensaje : java.lang.String)` | `java.lang.Integer` |

---

### `com::universidad::persistencia::CursoRepositorioImpl`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

_None._

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PROTECTED` | `permiteBorradoFisico()` | `boolean` |

---

### `com::universidad::dto::estudiante::EstudianteActualizarDto`

**Type:** `UmlRecord`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `id` | `UUID` | `[FINAL]` |
| `PRIVATE` | `nuevoCorreo` | `java.lang.String` | `[FINAL]` |
| `PRIVATE` | `nuevoCelular` | `java.lang.String` | `[FINAL]` |
| `PRIVATE` | `nuevaDireccion` | `java.lang.String` | `[FINAL]` |
| `PRIVATE` | `nuevoEstado` | `java.lang.Integer` | `[FINAL]` |

#### Operations

_None._

---

### `com::universidad::config::modulos::ModuloConfigurable`

**Type:** `UmlInterface`

**Visibility:** `PUBLIC`

#### Properties

_None._

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `construirVista()` | `SystemModule` |
| `PUBLIC` | `cerrarRecursos()` | `void` |

---

### `com::universidad::modelo::AsignaturaCosto`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `idAsignaturaCosto` | `java.lang.Integer` | `[]` |
| `PRIVATE` | `nombreAsignatura` | `java.lang.String` | `[]` |
| `PRIVATE` | `semanasDuracion` | `java.lang.Short` | `[]` |
| `PRIVATE` | `costoBase` | `BigDecimal` | `[]` |
| `PRIVATE` | `estadoAsignatura` | `EstadoEntidad` | `[]` |

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `actualizarNombre(nuevoNombre : java.lang.String)` | `void` |
| `PUBLIC` | `actualizarSemanas(nuevasSemanas : java.lang.Short)` | `void` |
| `PUBLIC` | `actualizarCosto(nuevoCosto : BigDecimal)` | `void` |
| `PUBLIC` | `suspender()` | `void` |
| `PUBLIC` | `reactivar()` | `void` |
| `PUBLIC` | `estaActiva()` | `boolean` |
| `PUBLIC` | `getIdAsignaturaCosto()` | `java.lang.Integer` |
| `PUBLIC` | `getNombreAsignatura()` | `java.lang.String` |
| `PUBLIC` | `getSemanasDuracion()` | `java.lang.Short` |
| `PUBLIC` | `getCostoBase()` | `BigDecimal` |
| `PUBLIC` | `getEstadoAsignatura()` | `EstadoEntidad` |

---

### `com::universidad::repositorio::RepositorioBase`

**Type:** `UmlInterface`

**Visibility:** `PUBLIC`

**Template parameters:** `T, ID`

#### Properties

_None._

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `guardar(entidad : T)` | `T` |
| `PUBLIC` | `actualizar(entidad : T)` | `T` |
| `PUBLIC` | `buscarPorId(id : ID)` | `java.util.Optional<T>` |
| `PUBLIC` | `eliminar(id : ID)` | `boolean` |
| `PUBLIC` | `listarTodos()` | `java.util.List<T>` |
| `PUBLIC` | `contar()` | `long` |
| `PUBLIC` | `cerrar()` | `void` |

---

### `com::universidad::persistencia::EstudianteRepositorioImpl`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

_None._

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PROTECTED` | `permiteBorradoFisico()` | `boolean` |

---

### `com::universidad::dto::asignaturacosto::AsignaturaCostoActualizarDto`

**Type:** `UmlRecord`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `id` | `java.lang.Integer` | `[FINAL]` |
| `PRIVATE` | `nuevoNombre` | `java.lang.String` | `[FINAL]` |
| `PRIVATE` | `nuevasSemanas` | `java.lang.Short` | `[FINAL]` |
| `PRIVATE` | `nuevoCosto` | `BigDecimal` | `[FINAL]` |

#### Operations

_None._

---

### `com::universidad::config::modulos::ConfiguracionModuloProfesor`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `repositorio` | `ProfesorRepositorio` | `[FINAL]` |
| `PRIVATE` | `servicio` | `ProfesorServicio` | `[FINAL]` |

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `getRepositorio()` | `ProfesorRepositorio` |
| `PUBLIC` | `getServicio()` | `ProfesorServicio` |
| `PUBLIC` | `construirVista()` | `SystemModule` |
| `PUBLIC` | `cerrarRecursos()` | `void` |

---

### `com::universidad::mapeador::Mapeador`

**Type:** `UmlInterface`

**Visibility:** `PUBLIC`

**Template parameters:** `E, R`

#### Properties

_None._

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `toDto(entidad : E)` | `R` |
| `PUBLIC` | `toDtoList(entidades : java.util.List<E>)` | `java.util.List<R>` |

---

### `com::universidad::dto::curso::CursoCrearDto`

**Type:** `UmlRecord`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `codigoCurso` | `java.lang.String` | `[FINAL]` |
| `PRIVATE` | `nombreCurso` | `java.lang.String` | `[FINAL]` |
| `PRIVATE` | `creditosCurso` | `java.lang.Integer` | `[FINAL]` |
| `PRIVATE` | `cupoMaximoCurso` | `java.lang.Integer` | `[FINAL]` |

#### Operations

_None._

---

### `com::universidad::dto::estudiante::EstudianteDto`

**Type:** `UmlRecord`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `id` | `UUID` | `[FINAL]` |
| `PRIVATE` | `codigo` | `java.lang.String` | `[FINAL]` |
| `PRIVATE` | `nombre` | `java.lang.String` | `[FINAL]` |
| `PRIVATE` | `correo` | `java.lang.String` | `[FINAL]` |
| `PRIVATE` | `celular` | `java.lang.String` | `[FINAL]` |
| `PRIVATE` | `direccion` | `java.lang.String` | `[FINAL]` |
| `PRIVATE` | `estado` | `EstadoEntidad` | `[FINAL]` |
| `PRIVATE` | `activo` | `boolean` | `[FINAL]` |

#### Operations

_None._

---

### `com::universidad::dto::estudiante::EstudianteCrearDto`

**Type:** `UmlRecord`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `codigo` | `java.lang.String` | `[FINAL]` |
| `PRIVATE` | `nombre` | `java.lang.String` | `[FINAL]` |
| `PRIVATE` | `correo` | `java.lang.String` | `[FINAL]` |
| `PRIVATE` | `celular` | `java.lang.String` | `[FINAL]` |
| `PRIVATE` | `direccion` | `java.lang.String` | `[FINAL]` |

#### Operations

_None._

---

### `com::universidad::repositorio::EstudianteRepositorio`

**Type:** `UmlInterface`

**Visibility:** `PUBLIC`

#### Properties

_None._

#### Operations

_None._

---

### `com::universidad::dto::profesor::ProfesorCrearDto`

**Type:** `UmlRecord`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `nombre` | `java.lang.String` | `[FINAL]` |
| `PRIVATE` | `celular` | `java.lang.String` | `[FINAL]` |

#### Operations

_None._

---

### `com::universidad::AppScannerCli`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

**Modifiers:** `[FINAL]`

#### Properties

_None._

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `configuracionCompleta()` | `CliConfig` |

---

### `com::universidad::servicio::CursoServicio`

**Type:** `UmlClass`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `mapeador` | `CursoMapeador` | `[FINAL]` |
| `PRIVATE` | `repositorio` | `CursoRepositorio` | `[FINAL]` |

#### Operations

| Visibility | Operation | Return |
|---|---|---|
| `PUBLIC` | `registrarCurso(dto : CursoCrearDto)` | `CursoDto` |
| `PUBLIC` | `obtenerCursos()` | `java.util.List<CursoDto>` |
| `PUBLIC` | `contarCursos()` | `long` |
| `PUBLIC` | `actualizarCurso(dto : CursoActualizarDto)` | `CursoDto` |
| `PUBLIC` | `eliminarCurso(id : UUID)` | `boolean` |

---

### `com::universidad::dto::profesor::ProfesorActualizarDto`

**Type:** `UmlRecord`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `id` | `java.lang.Long` | `[FINAL]` |
| `PRIVATE` | `nuevoNombre` | `java.lang.String` | `[FINAL]` |
| `PRIVATE` | `nuevoCelular` | `java.lang.String` | `[FINAL]` |
| `PRIVATE` | `nuevoEstado` | `java.lang.Integer` | `[FINAL]` |

#### Operations

_None._

---

### `com::universidad::dto::asignaturacosto::AsignaturaCostoCrearDto`

**Type:** `UmlRecord`

**Visibility:** `PUBLIC`

#### Properties

| Visibility | Name | Type | Modifiers |
|---|---|---|---|
| `PRIVATE` | `nombre` | `java.lang.String` | `[FINAL]` |
| `PRIVATE` | `semanas` | `java.lang.Short` | `[FINAL]` |
| `PRIVATE` | `costoBase` | `BigDecimal` | `[FINAL]` |

#### Operations

_None._

---

## 3. Relationships

| Type | Source | Target |
|---|---|---|
| `UmlRealization` | `com::universidad::mapeador::CursoMapeador` | `com::universidad::mapeador::Mapeador` |
| `UmlDependency` | `com::universidad::servicio::CursoServicio` | `com::universidad::dto::curso::CursoCrearDto` |
| `UmlDependency` | `com::universidad::mapeador::CursoMapeador` | `com::universidad::modelo::Curso` |
| `UmlDependency` | `com::universidad::servicio::CursoServicio` | `com::universidad::dto::curso::CursoDto` |
| `UmlRealization` | `com::universidad::config::modulos::ConfiguracionModuloAsignatura` | `com::universidad::config::modulos::ModuloConfigurable` |
| `UmlDependency` | `com::universidad::controlador::ProfesorControlador` | `com::universidad::dto::profesor::ProfesorDto` |
| `UmlRealization` | `com::universidad::persistencia::ProfesorRepositorioImpl` | `com::universidad::repositorio::ProfesorRepositorio` |
| `UmlGeneralization` | `com::universidad::repositorio::AsignaturaRepositorio` | `com::universidad::repositorio::RepositorioBase` |
| `UmlDependency` | `com::universidad::servicio::EstudianteServicio` | `com::universidad::dto::estudiante::EstudianteActualizarDto` |
| `UmlDependency` | `com::universidad::modelo::AsignaturaCosto` | `com::universidad::modelo::enumeracion::EstadoEntidad` |
| `UmlRealization` | `com::universidad::mapeador::ProfesorMapeador` | `com::universidad::mapeador::Mapeador` |
| `UmlDependency` | `com::universidad::vista::EstudianteVista` | `com::universidad::dto::estudiante::EstudianteDto` |
| `UmlDependency` | `com::universidad::servicio::ProfesorServicio` | `com::universidad::dto::profesor::ProfesorDto` |
| `UmlAssociation` | `com::universidad::config::modulos::ConfiguracionModuloEstudiante` | `com::universidad::repositorio::EstudianteRepositorio` |
| `UmlAssociation` | `com::universidad::config::modulos::ConfiguracionModuloAsignatura` | `com::universidad::repositorio::AsignaturaRepositorio` |
| `UmlRealization` | `com::universidad::persistencia::CursoRepositorioImpl` | `com::universidad::repositorio::CursoRepositorio` |
| `UmlDependency` | `com::universidad::controlador::EstudianteControlador` | `com::universidad::dto::estudiante::EstudianteActualizarDto` |
| `UmlAssociation` | `com::universidad::repositorio::cargador::ProfesorCargador` | `com::universidad::modelo::Profesor` |
| `UmlAssociation` | `com::universidad::servicio::AsignaturaServicio` | `com::universidad::repositorio::AsignaturaRepositorio` |
| `UmlAssociation` | `com::universidad::controlador::EstudianteControlador` | `com::universidad::servicio::EstudianteServicio` |
| `UmlGeneralization` | `com::universidad::persistencia::CursoRepositorioImpl` | `com::universidad::persistencia::RepositorioBaseAbstracto` |
| `UmlGeneralization` | `com::universidad::persistencia::EstudianteRepositorioImpl` | `com::universidad::persistencia::RepositorioBaseAbstracto` |
| `UmlDependency` | `com::universidad::modelo::Estudiante` | `com::universidad::modelo::enumeracion::EstadoEntidad` |
| `UmlAssociation` | `com::universidad::servicio::ProfesorServicio` | `com::universidad::mapeador::ProfesorMapeador` |
| `UmlAssociation` | `com::universidad::servicio::ProfesorServicio` | `com::universidad::repositorio::ProfesorRepositorio` |
| `UmlDependency` | `com::universidad::modelo::convertidor::EstadoEntidadConverter` | `com::universidad::modelo::enumeracion::EstadoEntidad` |
| `UmlAssociation` | `com::universidad::config::ConfiguracionDeDependencias` | `com::universidad::config::modulos::ModuloConfigurable` |
| `UmlDependency` | `com::universidad::servicio::AsignaturaServicio` | `com::universidad::dto::asignaturacosto::AsignaturaCostoCrearDto` |
| `UmlDependency` | `com::universidad::servicio::AsignaturaServicio` | `com::universidad::dto::asignaturacosto::AsignaturaCostoDto` |
| `UmlDependency` | `com::universidad::servicio::ProfesorServicio` | `com::universidad::dto::profesor::ProfesorActualizarDto` |
| `UmlRealization` | `com::universidad::persistencia::RepositorioBaseAbstracto` | `com::universidad::repositorio::RepositorioBase` |
| `UmlDependency` | `com::universidad::mapeador::ProfesorMapeador` | `com::universidad::modelo::Profesor` |
| `UmlAssociation` | `com::universidad::repositorio::cargador::ProfesorCargador` | `com::universidad::repositorio::ProfesorRepositorio` |
| `UmlRealization` | `com::universidad::repositorio::cargador::ProfesorCargador` | `com::universidad::repositorio::cargador::CargadorDatos` |
| `UmlRealization` | `com::universidad::mapeador::AsignaturaMapeador` | `com::universidad::mapeador::Mapeador` |
| `UmlDependency` | `com::universidad::mapeador::AsignaturaMapeador` | `com::universidad::dto::asignaturacosto::AsignaturaCostoDto` |
| `UmlDependency` | `com::universidad::servicio::CursoServicio` | `com::universidad::dto::curso::CursoActualizarDto` |
| `UmlGeneralization` | `com::universidad::persistencia::AsignaturaRepositorioImpl` | `com::universidad::persistencia::RepositorioBaseAbstracto` |
| `UmlRealization` | `com::universidad::config::modulos::ConfiguracionModuloEstudiante` | `com::universidad::config::modulos::ModuloConfigurable` |
| `UmlDependency` | `com::universidad::vista::ProfesorVista` | `com::universidad::dto::profesor::ProfesorDto` |
| `UmlDependency` | `com::universidad::servicio::EstudianteServicio` | `com::universidad::dto::estudiante::EstudianteDto` |
| `UmlAssociation` | `com::universidad::servicio::CursoServicio` | `com::universidad::mapeador::CursoMapeador` |
| `UmlDependency` | `com::universidad::controlador::EstudianteControlador` | `com::universidad::dto::estudiante::EstudianteCrearDto` |
| `UmlDependency` | `com::universidad::modelo::Profesor` | `com::universidad::modelo::enumeracion::EstadoEntidad` |
| `UmlDependency` | `com::universidad::modelo::Curso` | `com::universidad::modelo::enumeracion::EstadoEntidad` |
| `UmlDependency` | `com::universidad::servicio::EstudianteServicio` | `com::universidad::dto::estudiante::EstudianteCrearDto` |
| `UmlRealization` | `com::universidad::config::modulos::ConfiguracionModuloProfesor` | `com::universidad::config::modulos::ModuloConfigurable` |
| `UmlDependency` | `com::universidad::controlador::CursoControlador` | `com::universidad::dto::curso::CursoDto` |
| `UmlAssociation` | `com::universidad::servicio::CursoServicio` | `com::universidad::repositorio::CursoRepositorio` |
| `UmlDependency` | `com::universidad::controlador::AsignaturaControlador` | `com::universidad::dto::asignaturacosto::AsignaturaCostoCrearDto` |
| `UmlAssociation` | `com::universidad::controlador::CursoControlador` | `com::universidad::servicio::CursoServicio` |
| `UmlDependency` | `com::universidad::controlador::AsignaturaControlador` | `com::universidad::dto::asignaturacosto::AsignaturaCostoDto` |
| `UmlAssociation` | `com::universidad::config::modulos::ConfiguracionModuloCurso` | `com::universidad::repositorio::CursoRepositorio` |
| `UmlGeneralization` | `com::universidad::repositorio::CursoRepositorio` | `com::universidad::repositorio::RepositorioBase` |
| `UmlDependency` | `com::universidad::App` | `com::universidad::config::ConfiguracionDeDependencias` |
| `UmlDependency` | `com::universidad::vista::AsignaturaVista` | `com::universidad::dto::asignaturacosto::AsignaturaCostoDto` |
| `UmlAssociation` | `com::universidad::servicio::EstudianteServicio` | `com::universidad::repositorio::EstudianteRepositorio` |
| `UmlDependency` | `com::universidad::vista::CursoVista` | `com::universidad::dto::curso::CursoDto` |
| `UmlGeneralization` | `com::universidad::repositorio::EstudianteRepositorio` | `com::universidad::repositorio::RepositorioBase` |
| `UmlAssociation` | `com::universidad::vista::AsignaturaVista` | `com::universidad::controlador::AsignaturaControlador` |
| `UmlDependency` | `com::universidad::mapeador::CursoMapeador` | `com::universidad::dto::curso::CursoDto` |
| `UmlDependency` | `com::universidad::controlador::ProfesorControlador` | `com::universidad::dto::profesor::ProfesorActualizarDto` |
| `UmlDependency` | `com::universidad::controlador::ProfesorControlador` | `com::universidad::dto::profesor::ProfesorCrearDto` |
| `UmlAssociation` | `com::universidad::servicio::AsignaturaServicio` | `com::universidad::mapeador::AsignaturaMapeador` |
| `UmlDependency` | `com::universidad::mapeador::EstudianteMapeador` | `com::universidad::modelo::Estudiante` |
| `UmlGeneralization` | `com::universidad::repositorio::ProfesorRepositorio` | `com::universidad::repositorio::RepositorioBase` |
| `UmlAssociation` | `com::universidad::controlador::AsignaturaControlador` | `com::universidad::servicio::AsignaturaServicio` |
| `UmlAssociation` | `com::universidad::vista::EstudianteVista` | `com::universidad::controlador::EstudianteControlador` |
| `UmlAssociation` | `com::universidad::servicio::EstudianteServicio` | `com::universidad::mapeador::EstudianteMapeador` |
| `UmlRealization` | `com::universidad::mapeador::EstudianteMapeador` | `com::universidad::mapeador::Mapeador` |
| `UmlAssociation` | `com::universidad::config::modulos::ConfiguracionModuloProfesor` | `com::universidad::servicio::ProfesorServicio` |
| `UmlAssociation` | `com::universidad::vista::CursoVista` | `com::universidad::controlador::CursoControlador` |
| `UmlGeneralization` | `com::universidad::persistencia::ProfesorRepositorioImpl` | `com::universidad::persistencia::RepositorioBaseAbstracto` |
| `UmlAssociation` | `com::universidad::config::modulos::ConfiguracionModuloProfesor` | `com::universidad::repositorio::ProfesorRepositorio` |
| `UmlDependency` | `com::universidad::servicio::ProfesorServicio` | `com::universidad::dto::profesor::ProfesorCrearDto` |
| `UmlDependency` | `com::universidad::mapeador::AsignaturaMapeador` | `com::universidad::modelo::AsignaturaCosto` |
| `UmlDependency` | `com::universidad::controlador::EstudianteControlador` | `com::universidad::dto::estudiante::EstudianteDto` |
| `UmlRealization` | `com::universidad::config::modulos::ConfiguracionModuloCurso` | `com::universidad::config::modulos::ModuloConfigurable` |
| `UmlAssociation` | `com::universidad::config::modulos::ConfiguracionModuloAsignatura` | `com::universidad::servicio::AsignaturaServicio` |
| `UmlRealization` | `com::universidad::persistencia::EstudianteRepositorioImpl` | `com::universidad::repositorio::EstudianteRepositorio` |
| `UmlAssociation` | `com::universidad::config::modulos::ConfiguracionModuloCurso` | `com::universidad::servicio::CursoServicio` |
| `UmlRealization` | `com::universidad::persistencia::AsignaturaRepositorioImpl` | `com::universidad::repositorio::AsignaturaRepositorio` |
| `UmlAssociation` | `com::universidad::vista::ProfesorVista` | `com::universidad::controlador::ProfesorControlador` |
| `UmlDependency` | `com::universidad::controlador::CursoControlador` | `com::universidad::dto::curso::CursoActualizarDto` |
| `UmlAssociation` | `com::universidad::config::modulos::ConfiguracionModuloEstudiante` | `com::universidad::servicio::EstudianteServicio` |
| `UmlAssociation` | `com::universidad::controlador::ProfesorControlador` | `com::universidad::servicio::ProfesorServicio` |
| `UmlDependency` | `com::universidad::controlador::CursoControlador` | `com::universidad::dto::curso::CursoCrearDto` |
| `UmlDependency` | `com::universidad::mapeador::EstudianteMapeador` | `com::universidad::dto::estudiante::EstudianteDto` |
| `UmlDependency` | `com::universidad::mapeador::ProfesorMapeador` | `com::universidad::dto::profesor::ProfesorDto` |

