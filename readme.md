
# Proyecto CoreApp Plugins

## Descripción general

Este proyecto está basado en una arquitectura plugable con plugins de entrada (Input), procesamiento (Processor) y salida (Output).  
Las rutas de procesamiento se configuran vía archivo JSON (`routes.json`) donde se definen los plugins a usar y sus configuraciones.

Cada plugin es un módulo independiente que se empaqueta en un JAR y se carga dinámicamente en la ejecución.  
El executor lee las rutas, carga los plugins correspondientes, y ejecuta el flujo Input → Processor → Output.

## Módulos principales

- **plugin-api**  
  Define las interfaces comunes para plugins:
  - `InputPlugin<T>`  
  - `ProcessorPlugin<I, O>`  
  - `OutputPlugin<T>`

- **Plugins implementados** (ejemplos):  
  - `plugin-input-webapi`: Lee datos desde una API REST  
  - `plugin-input-jsonfile`: Lee datos JSON desde archivo  
  - `plugin-output-txtfile`: Escribe datos tipo String en archivo TXT  
  - `plugin-output-jsonfile`: Escribe datos JSON en archivo  
  - `plugin-processor-consolelog`: Muestra datos en consola (logging)

- **executor**  
  Aplicación Spring Boot que lee el archivo JSON con las rutas, carga los JARs de plugins y ejecuta las rutas.

## Estructura del JSON de configuración (`routes.json`)

```json
{
  "routes": [
    {
      "description": "Descarga de todos",
      "input": {
        "name": "plugin-input-webapi",
        "config": {
          "url": "https://jsonplaceholder.typicode.com/todos",
          "method": "GET",
          "headers": {}
        }
      },
      "processor": {
        "name": "plugin-processor-consolelog",
        "config": {}
      },
      "output": {
        "name": "plugin-output-txtfile",
        "config": {
          "filePath": "typicode_todos.txt"
        }
      }
    }
  ]
}
```

Cada ruta define tres objetos: `input`, `processor` y `output` con el nombre del plugin y su configuración específica.

## Cómo crear nuevos plugins

1. Implementa la interfaz adecuada en tu módulo, por ejemplo:

```java
public class MiInputPlugin implements InputPlugin<TipoDato> {
    @Override
    public void configure(Map<String, Object> config) {
        // Configurar parámetros específicos
    }
    @Override
    public TipoDato read() {
        // Lógica de lectura de datos
    }
    @Override
    public boolean supports(String name) {
        return "plugin-input-miinput".equalsIgnoreCase(name);
    }
}
```

1. Anota la clase con `@Component` para que Spring la descubra.
1. Empaqueta el plugin como JAR independiente.
1. Copia el JAR generado a la carpeta `plugins/`.
1. Referencia el plugin en el JSON de rutas usando el nombre que devuelve `supports()`.

## Compilación y empaquetado usando IntelliJ IDEA

### Configuración del artefacto JAR

- Abre **File > Project Structure > Artifacts**.
- Agrega un nuevo artefacto JAR “From modules with dependencies” para el módulo plugin o executor.
- Marca **Include in project build**.
- Configura para incluir dependencias necesarias dentro del JAR.
- Guarda.

### Construir el JAR

- Usa **Build > Build Artifacts...**.
- Selecciona el artefacto y haz clic en **Build**.
- El JAR queda en la carpeta configurada (`out/artifacts/`).

### Ejecutar

- Para el executor, asegúrate que:
  - El archivo `routes.json` está en la raíz.
  - La carpeta `plugins/` contiene los JAR de plugins.
- Ejecuta:

```bash
java -jar executor.jar
```

## Notas importantes

- Cada plugin debe implementar `supports(String name)` para identificarse.
- Las configuraciones de plugins se pasan en el JSON y se reciben como `Map<String, Object>` en `configure()`.
- Los tipos genéricos `<T>`, `<I, O>` permiten flexibilidad en tipos de datos entre plugins.
- El executor crea un `URLClassLoader` por ruta para cargar dinámicamente los plugins.
- El procesamiento sigue la cadena: Input → Processor → Output.
