package com.example.plugins.core_app;

import com.example.plugins.InputPlugin;
import com.example.plugins.OutputPlugin;
import com.example.plugins.ProcessorPlugin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public class CoreAppApplication {

	public static void main(String[] args) throws Exception {
	    // 1. Configuración embebida JSON (podrías cargar de archivo también)
		String jsonConfig = """
        {
          "inputPlugin": "com.example.plugins.inputwebapi",
          "processorPlugin": "com.example.plugins.processorlog",
          "outputPlugin": "com.example.plugins.outputfile",
          "inputConfig": {
            "url": "https://jsonplaceholder.typicode.com/todos",
            "method": "GET",
            "headers": {}
          },
          "processorConfig": {},
          "outputConfig": {
            "filePath": "salida.txt"
          }
        }
        """;

		ObjectMapper mapper = new ObjectMapper();
		RouteConfig config = mapper.readValue(jsonConfig, RouteConfig.class);

		// 2. Carga dinámica de JARs
		File pluginsDir = new File("C:\\_audomaro_\\repos_3rd_party\\plug-app\\out\\plugins");
		File[] jars = pluginsDir.listFiles(f -> f.getName().endsWith(".jar"));
		if (jars == null || jars.length == 0) {
			throw new RuntimeException("No se encontraron JARs en la carpeta plugins");
		}
		URL[] urls = Arrays.stream(jars)
				.map(f -> {
					try {
						return f.toURI().toURL();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}).toArray(URL[]::new);
		URLClassLoader pluginClassLoader = new URLClassLoader(urls, CoreAppApplication.class.getClassLoader());

		// 3. Crear contextos y cargar beans dinámicamente por plugin
		try (
				AnnotationConfigApplicationContext inputContext = new AnnotationConfigApplicationContext();
				AnnotationConfigApplicationContext processorContext = new AnnotationConfigApplicationContext();
				AnnotationConfigApplicationContext outputContext = new AnnotationConfigApplicationContext()
		) {
			// Input plugin
			inputContext.setClassLoader(pluginClassLoader);
			inputContext.scan(config.inputPlugin);
			inputContext.refresh();
			InputPlugin inputPlugin = inputContext.getBean(InputPlugin.class);
			inputPlugin.configure(config.inputConfig);

			// Processor plugin
			processorContext.setClassLoader(pluginClassLoader);
			processorContext.scan(config.processorPlugin);
			processorContext.refresh();
			ProcessorPlugin processorPlugin = processorContext.getBean(ProcessorPlugin.class);
			processorPlugin.configure(config.processorConfig);

			// Output plugin
			outputContext.setClassLoader(pluginClassLoader);
			outputContext.scan(config.outputPlugin);
			outputContext.refresh();
			OutputPlugin outputPlugin = outputContext.getBean(OutputPlugin.class);
			outputPlugin.configure(config.outputConfig);

			// 4. Ejecutar la ruta
			String data = inputPlugin.read();
			System.out.println(">>> Entrada recibida:\n" + data);

			String processed = processorPlugin.process(data);
			System.out.println(">>> Procesado:\n" + processed);

			outputPlugin.write(processed);
			System.out.println(">>> Salida escrita. Ruta completada.");
		}
	}
}
