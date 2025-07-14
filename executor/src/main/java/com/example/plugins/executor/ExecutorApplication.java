package com.example.plugins.executor;

import com.example.plugins.InputPlugin;
import com.example.plugins.OutputPlugin;
import com.example.plugins.ProcessorPlugin;
import com.example.plugins.executor.dto.RouteConfig;
import com.example.plugins.executor.dto.RouteDefinitionList;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;
import org.springframework.plugin.core.PluginRegistry;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@SpringBootApplication
public class ExecutorApplication  {

	// Punto de entrada de la aplicación Spring Boot
	public static void main(String[] args) {
		SpringApplication.run(ExecutorApplication.class, args);
	}

	// Este método se ejecuta automáticamente al iniciar la app.
	// Lee el archivo de rutas, carga los plugins necesarios desde JARs, y ejecuta cada ruta.
	@Bean
	CommandLineRunner runApp() {
		return args -> {

			// 📥 1. Leer el archivo JSON de rutas
			String jsonRoutes = Files.readString(Path.of("routes.json"), StandardCharsets.UTF_8);
			ObjectMapper mapper = new ObjectMapper();
			RouteDefinitionList routeList = mapper.readValue(jsonRoutes, RouteDefinitionList.class);

			// 📁 2. Cargar todos los JARs en la carpeta /out/plugins
			File pluginsDir = new File("out/plugins");
			File[] todosLosJars = pluginsDir.listFiles(f -> f.getName().endsWith(".jar"));

			if (todosLosJars == null || todosLosJars.length == 0) {
				throw new RuntimeException("No se encontraron JARs en la carpeta %s".formatted(pluginsDir.getAbsolutePath()));
			}

			// 🔁 3. Procesar cada ruta definida en el archivo
			for (RouteConfig config : routeList.getRoutes()) {
				System.out.printf(">>> Procesando ruta: %s%n", config.getDescription());

				// Identificar los nombres de los plugins que esta ruta necesita
				Set<String> pluginsNecesarios = Set.of(
						config.getInput().getName(),
						config.getProcessor().getName(),
						config.getOutput().getName()
				);

				// Filtrar los JARs que contienen esos plugins necesarios
				List<URL> urlsFiltrados = new ArrayList<>();
				List<File> jarsFiltrados = new ArrayList<>();
				for (File jar : todosLosJars) {
					String jarName = jar.getName().toLowerCase();
					boolean contienePlugin = pluginsNecesarios.stream()
							.anyMatch(p -> jarName.contains(p.toLowerCase()));
					if (contienePlugin) {
						urlsFiltrados.add(jar.toURI().toURL());
						jarsFiltrados.add(jar);
					}
				}

				// Si no se encontraron los plugins requeridos, lanzar error
				if (urlsFiltrados.isEmpty()) {
					throw new RuntimeException("No se encontraron JARs para los plugins usados en ruta: " + config.getDescription());
				}

				System.out.printf(">>> Cargando JARs para ruta '%s': %s%n",
						config.getDescription(), jarsFiltrados.stream().map(File::getName).toList());

				// 📦 4. Crear un classloader nuevo para aislar los plugins de esta ruta
				try (URLClassLoader pluginClassLoader = new URLClassLoader(
						urlsFiltrados.toArray(new URL[0]),
						getClass().getClassLoader());
					 AnnotationConfigApplicationContext pluginContext = new AnnotationConfigApplicationContext()) {

					// Usar ese classloader en el nuevo contexto Spring
					pluginContext.setClassLoader(pluginClassLoader);

					// Desactivar características innecesarias de administración de Spring
					pluginContext.getEnvironment().getPropertySources().addFirst(
							new MapPropertySource("disableSpringAdmin", Map.of("spring.application.admin.enabled", "false"))
					);

					// 🔧 5. Registrar las clases @Configuration de los plugins (si las hay)
					for (File jar : jarsFiltrados) {
						List<Class<?>> configClasses = findConfigurationClasses(jar, pluginClassLoader);
						for (Class<?> configClass : configClasses) {
							System.out.println(">>> Registrando configuración del plugin: " + configClass.getName());
							pluginContext.register(configClass);
						}
					}

					// Escanear automáticamente las clases anotadas con @Component (plugins)
					pluginContext.scan("com.example.plugins");
					pluginContext.refresh();

					// 🔌 6. Obtener todos los plugins del contexto (por tipo)
					ArrayList inputPlugins = new ArrayList<>(pluginContext.getBeansOfType(InputPlugin.class).values());
					ArrayList processorPlugins = new ArrayList<>(pluginContext.getBeansOfType(ProcessorPlugin.class).values());
					ArrayList outputPlugins = new ArrayList<>(pluginContext.getBeansOfType(OutputPlugin.class).values());

					// Registrar los plugins en un registry para buscarlos fácilmente por nombre
					PluginRegistry<InputPlugin<?>, String> inputRegistry = PluginRegistry.of(inputPlugins);
					PluginRegistry<ProcessorPlugin<?, ?>, String> processorRegistry = PluginRegistry.of(processorPlugins);
					PluginRegistry<OutputPlugin<?>, String> outputRegistry = PluginRegistry.of(outputPlugins);

					// ▶️ Ejecutar la ruta
					executeRoute(config, inputRegistry, processorRegistry, outputRegistry);
				}
			}
		};
	}

	/**
	 * Este método ejecuta una ruta específica: toma los datos del Input,
	 * los transforma con el Processor, y los envía con el Output.
	 */
	private void executeRoute(
			RouteConfig config,
			PluginRegistry<InputPlugin<?>, String> inputPlugins,
			PluginRegistry<ProcessorPlugin<?, ?>, String> processorPlugins,
			PluginRegistry<OutputPlugin<?>, String> outputPlugins
	) {
		System.out.printf(">>> Ejecutando ruta [%s]...%n", config.getDescription());

		try {
			// Buscar e instanciar el InputPlugin correcto
			InputPlugin<?> input = inputPlugins.getPluginFor(config.getInput().getName())
					.orElseThrow(() -> new RuntimeException("InputPlugin no encontrado: " + config.getInput().getName()));
			input.configure(config.getInput().getConfig());

			// Buscar e instanciar el ProcessorPlugin correcto
			ProcessorPlugin<?, ?> processor = processorPlugins.getPluginFor(config.getProcessor().getName())
					.orElseThrow(() -> new RuntimeException("ProcessorPlugin no encontrado: " + config.getProcessor().getName()));
			processor.configure(config.getProcessor().getConfig());

			// Buscar e instanciar el OutputPlugin correcto
			OutputPlugin<?> output = outputPlugins.getPluginFor(config.getOutput().getName())
					.orElseThrow(() -> new RuntimeException("OutputPlugin no encontrado: " + config.getOutput().getName()));
			output.configure(config.getOutput().getConfig());

			// Ejecutar el flujo completo de la ruta
			Object data = input.read();
			Object processed = ((ProcessorPlugin<Object, Object>) processor).process(data);
			((OutputPlugin<Object>) output).write(processed);

			System.out.println(">>> Ruta ejecutada con éxito.\n");

		} catch (Exception e) {
			System.err.println(">>> Error ejecutando ruta: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Este método escanea las clases .class dentro del JAR.
	 * Si alguna está anotada con @Configuration, se agrega a la lista.
	 */
	private List<Class<?>> findConfigurationClasses(File jarFile, ClassLoader pluginClassLoader) {
		List<Class<?>> configClasses = new ArrayList<>();
		try (java.util.jar.JarFile jar = new java.util.jar.JarFile(jarFile)) {
			Enumeration<java.util.jar.JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				java.util.jar.JarEntry entry = entries.nextElement();
				if (entry.getName().endsWith(".class") && !entry.isDirectory()) {
					String className = entry.getName()
							.replace("/", ".")
							.replace(".class", "");

					// Filtrar sólo clases que estén dentro de com.example.plugins
					if (!className.startsWith("com.example.plugins")) {
						continue;
					}

					try {
						Class<?> clazz = Class.forName(className, false, pluginClassLoader);
						if (clazz.isAnnotationPresent(Configuration.class)) {
							configClasses.add(clazz);
						}
					} catch (Throwable ignored) {
						// Ignorar clases que no se pueden cargar (por ejemplo, por falta de dependencias)
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Error escaneando configuraciones del plugin: " + e.getMessage());
		}
		return configClasses;
	}
}
