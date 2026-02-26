# Desafío: Testing en Android con Mockito y MockWebServer

Este proyecto implementa pruebas unitarias para la capa de repositorio de una aplicación Android, asegurando la correcta obtención y persistencia de datos de usuario.

## Herramientas utilizadas
* **JUnit 4**: Para la estructura de las pruebas.
* **Mockito**: Para simular las interfaces de la API y la Base de Datos.
* **MockWebServer**: Para simular las respuestas del servidor REST.
* **Retrofit**: Para las llamadas de red.

## Pruebas Realizadas
1. **getUsers_SuccessfulResponse**: Verifica que cuando la API responde bien, los datos se guardan en la base de datos local.
2. **getUsers_ApiError**: Verifica que cuando la API falla, no se intenta guardar nada erróneo.
