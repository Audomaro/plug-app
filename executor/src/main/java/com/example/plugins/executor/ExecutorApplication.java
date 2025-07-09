package com.example.plugins.executor;

import com.example.plugins.*;
import com.example.plugins.executor.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeansException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.MapPropertySource;
import org.springframework.plugin.core.PluginRegistry;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

@SpringBootApplication
public class ExecutorApplication implements ApplicationContextAware {

    public static void main(String[] args) {
		SpringApplication.run(ExecutorApplication.class, args);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    }

	@Bean
	CommandLineRunner runApp() {
		return args -> {
			// Leer JSON con rutas
			String jsonRoutes = Files.readString(Path.of("routes.json"), StandardCharsets.UTF_8);
			ObjectMapper mapper = new ObjectMapper();
			RouteDefinitionList routeList = mapper.readValue(jsonRoutes, RouteDefinitionList.class);

			// Carpeta donde están los JARs plugins
			File pluginsDir = new File("C:\\_audomaro_\\repos_3rd_party\\plug-app\\out\\plugins");
			File[] todosLosJars = pluginsDir.listFiles(f -> f.getName().endsWith(".jar"));

			if (todosLosJars == null || todosLosJars.length == 0) {
				throw new RuntimeException("No se encontraron JARs en la carpeta plugins");
			}

			for (RouteConfig config : routeList.getRoutes()) {
				System.out.printf(">>> Procesando ruta: %s%n", config.getDescription());

				Set<String> pluginsNecesarios = Set.of(
						config.getInput().getName(),
						config.getProcessor().getName(),
						config.getOutput().getName()
				);

				// Filtrar solo los JARs que contienen los plugins necesarios
				List<URL> urlsFiltrados = new ArrayList<>();
				List<String> jarsFiltradosNombres = new ArrayList<>();
				for (File jar : todosLosJars) {
					String jarName = jar.getName().toLowerCase();
					boolean contienePlugin = pluginsNecesarios.stream()
							.anyMatch(p -> jarName.contains(p.toLowerCase()));
					if (contienePlugin) {
						urlsFiltrados.add(jar.toURI().toURL());
						jarsFiltradosNombres.add(jar.getName());
					}
				}

				if (urlsFiltrados.isEmpty()) {
					throw new RuntimeException("No se encontraron JARs para los plugins usados en ruta: " + config.getDescription());
				}

				// Log de recursos cargados
				System.out.printf(">>> Cargando recursos (JARs) para ruta '%s': %s%n",
						config.getDescription(), jarsFiltradosNombres);

				// Crear classloader y contexto para esta ruta y plugins
				try (URLClassLoader pluginClassLoader = new URLClassLoader(urlsFiltrados.toArray(new URL[0]), getClass().getClassLoader());
					 AnnotationConfigApplicationContext pluginContext = new AnnotationConfigApplicationContext()) {

					pluginContext.setClassLoader(pluginClassLoader);

					// Inyectar propiedad para evitar error JMX de Spring Admin
					pluginContext.getEnvironment().getPropertySources().addFirst(
							new MapPropertySource("disableSpringAdmin", Map.of("spring.application.admin.enabled", "false"))
					);

					pluginContext.scan("com.example.plugins");
					pluginContext.refresh();

					@SuppressWarnings("unchecked")
					List<InputPlugin<?>> inputPlugins = new ArrayList<>((Collection<InputPlugin<?>>)(Collection<?>)pluginContext.getBeansOfType(InputPlugin.class).values());
					@SuppressWarnings("unchecked")
					List<ProcessorPlugin<?, ?>> processorPlugins = new ArrayList<>((Collection<ProcessorPlugin<?, ?>>)(Collection<?>)pluginContext.getBeansOfType(ProcessorPlugin.class).values());
					@SuppressWarnings("unchecked")
					List<OutputPlugin<?>> outputPlugins = new ArrayList<>((Collection<OutputPlugin<?>>)(Collection<?>)pluginContext.getBeansOfType(OutputPlugin.class).values());

					PluginRegistry<InputPlugin<?>, String> inputRegistry = PluginRegistry.of(inputPlugins);
					PluginRegistry<ProcessorPlugin<?, ?>, String> processorRegistry = PluginRegistry.of(processorPlugins);
					PluginRegistry<OutputPlugin<?>, String> outputRegistry = PluginRegistry.of(outputPlugins);

					// Ejecutar la ruta con plugins cargados
					executeRoute(config, inputRegistry, processorRegistry, outputRegistry);

					// pluginContext.close() y pluginClassLoader.close() se ejecutan automáticamente aquí
				}
			}
		};
	}

	private void executeRoute(
			RouteConfig config,
			PluginRegistry<InputPlugin<?>, String> inputPlugins,
			PluginRegistry<ProcessorPlugin<?, ?>, String> processorPlugins,
			PluginRegistry<OutputPlugin<?>, String> outputPlugins
	) {
		System.out.printf(">>> Ejecutando ruta [%s]...%n", config.getDescription());

		try {
			InputPlugin<?> input = inputPlugins.getPluginFor(config.getInput().getName())
					.orElseThrow(() -> new RuntimeException("InputPlugin no encontrado: " + config.getInput().getName()));
			input.configure(config.getInput().getConfig());

			ProcessorPlugin<?, ?> processor = processorPlugins.getPluginFor(config.getProcessor().getName())
					.orElseThrow(() -> new RuntimeException("ProcessorPlugin no encontrado: " + config.getProcessor().getName()));
			processor.configure(config.getProcessor().getConfig());

			OutputPlugin<?> output = outputPlugins.getPluginFor(config.getOutput().getName())
					.orElseThrow(() -> new RuntimeException("OutputPlugin no encontrado: " + config.getOutput().getName()));
			output.configure(config.getOutput().getConfig());

			// Casts para tipos genéricos
			Object data = input.read();
			Object processed = ((ProcessorPlugin<Object, Object>) processor).process(data);
			((OutputPlugin<Object>) output).write(processed);

			System.out.println(">>> Ruta ejecutada con éxito.\n");

		} catch (Exception e) {
			System.err.println(">>> Error ejecutando ruta: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
