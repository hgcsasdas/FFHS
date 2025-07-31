# FFHS (Free File Host System)

## Descripción del Proyecto

**FFHS (Free File Host System)** es una solución robusta y flexible diseñada para la gestión y alojamiento eficiente de archivos. Este proyecto es la herramienta definitiva para cualquiera que administre un servidor y necesite una plataforma fiable y personalizable para almacenar y servir archivos de manera segura y controlada.

Construido como una aplicación full-stack, FFHS combina un potente backend desarrollado con **Spring Boot (Java)** para la lógica de negocio y la persistencia de datos, y un frontend moderno y reactivo desarrollado con **Vite y React (TypeScript)**, asegurando una experiencia de usuario fluida y una interfaz intuitiva. La arquitectura del proyecto está diseñada para ser fácilmente desplegable y escalable utilizando Docker y Docker Compose, lo que simplifica la configuración y el mantenimiento en cualquier entorno de servidor.

### ¿Por qué deberías usar FFHS?

Si tienes un servidor y necesitas una solución para alojar y gestionar archivos, FFHS es tu mejor opción por varias razones:

* **Control Total**: A diferencia de los servicios de alojamiento de terceros, FFHS te da control completo sobre tus datos y tu infraestructura. Tú decides dónde se almacenan los archivos y cómo se gestionan.
* **Privacidad y Seguridad**: Al hospedar tus propios archivos, minimizas la exposición a políticas de privacidad de terceros y puedes implementar tus propias medidas de seguridad.
* **Personalización**: Al ser un proyecto open source, puedes modificar y adaptar FFHS a tus necesidades específicas, integrándolo con otros sistemas o añadiendo funcionalidades personalizadas.
* **Rendimiento**: Optimiza la entrega de archivos ajustando tu servidor y configuración de red, sin depender de la infraestructura de otros.
* **Despliegue Sencillo**: Gracias a Docker y Docker Compose, el proceso de despliegue es sorprendentemente fácil y consistente, permitiéndote tener FFHS funcionando en minutos.

FFHS es ideal para desarrolladores, pequeñas empresas, educadores o cualquier persona con un servidor que busque una solución de alojamiento de archivos auto-gestionada, eficiente y altamente adaptable.

## Arquitectura

FFHS se compone de dos servicios principales que interactúan entre sí:

* **Backend (Java/Spring Boot)**: Se encarga de la lógica de negocio, la gestión de usuarios, la persistencia de metadatos de archivos (nombres, rutas, archivos asociados, etc.) y la gestión del almacenamiento físico de los archivos.
* **Frontend (React/Vite)**: Proporciona la interfaz de usuario para que los usuarios puedan interactuar con el sistema, subir, descargar, listar y gestionar sus archivos.
* **Nginx**: Actúa como un proxy inverso, sirviendo el frontend estático y redirigiendo las solicitudes de API al backend.

## Integración y Acceso Externo a la API

El potente backend de FFHS no solo sirve como la columna vertebral para su propio frontend, sino que también expone una **API RESTful completa que puede ser consumida por terceros**. Esto significa que puedes integrar las capacidades de gestión de archivos de FFHS en tus propias aplicaciones, servicios o scripts, convirtiéndolo en un componente de almacenamiento de archivos altamente versátil para tu infraestructura.

Puedes realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar) en tus archivos desde cualquier aplicación externa, siempre que se proporcione la autenticación adecuada. El backend de FFHS soporta dos métodos de autenticación principal:

* **Tokens JWT (JSON Web Tokens)**: Para el acceso basado en usuario (mediante login), que se usa principalmente desde el frontend o aplicaciones de usuario.
* **Combinación de API Key y Basic Authentication**: Para acceso programático robusto desde terceros. Requiere enviar un encabezado `X-API-KEY` con tu clave de API, y un encabezado `Authorization: Basic` con las credenciales de un usuario administrador definidas en el entorno (`APP_USER` y `APP_PASSWORD`).

Esta flexibilidad hace de FFHS no solo una aplicación de cara al usuario, sino también un potente **servicio de almacenamiento de archivos programable** que puedes incorporar en tu ecosistema.

## Cómo Empezar

Para poner FFHS en marcha, asegúrate de tener Docker y Docker Compose instalados en tu sistema.

1.  **Clonar el Repositorio:**
    ```bash
    git clone [https://github.com/hgcsasdas/FFHS.git](https://github.com/hgcsasdas/FFHS.git)
    cd FFHS
    ```
2.  **Configuración del Entorno (`.env`):**
    Antes de desplegar, es **fundamental** crear un archivo llamado `.env` en el directorio raíz de este proyecto (`FFHS/`). Este archivo contendrá las variables de entorno necesarias para la configuración de la base de datos, el backend y el frontend.

    Crea el archivo `.env` con el siguiente contenido, **ajustando los valores a tu entorno de producción**:

    ```env
    # --- PostgreSQL Database Configuration ---
    POSTGRES_USER=filehost
    POSTGRES_PASSWORD=filehost123
    POSTGRES_DB=filehost

    # --- Spring Boot Backend Configuration ---
    # URL de conexión a la base de datos PostgreSQL
    POSTGRES_URL=jdbc:postgresql://postgres:5432/${POSTGRES_DB}

    # Credenciales de usuario administrador para autenticación Basic con API Key (se generará si no existen)
    APP_USER=admin_dev
    APP_PASSWORD=secure_dev_password

    # Clave secreta para la firma de tokens JWT. ¡CAMBIA ESTA CLAVE POR UNA MUY SEGURA EN PRODUCCIÓN!
    SECRET_KEY=dev_jwt_super_secret_key_long_and_random_1234567890abcdefghijklmnopqrstuvwxyz

    # Clave para la autenticación por X-API-KEY. ¡CAMBIA ESTA CLAVE POR UNA MUY SEGURA EN PRODUCCIÓN!
    API_KEY=dev_api_key_7890

    # Directorio donde se almacenarán los archivos subidos dentro del contenedor del backend
    UPLOAD_DIR=/app/uploads

    # Secreto JWT (redundante con SECRET_KEY en algunas configuraciones, asegúrate de la que usas)
    JWT_SECRET=dev_jwt_secret_for_signing_tokens_12345

    # Tiempo de expiración de los tokens JWT en milisegundos (ej. 3600000 ms = 1 hora)
    JWT_EXPIRATION=3600000

    # Tamaño máximo de archivo permitido para subir en megabytes
    MAX_FILE_SIZE_MB=100

    # --- Vite Frontend Configuration ---
    # URL base de la API del backend a la que el frontend se conectará
    VITE_API_BASE=http://localhost:8080
    ```

3.  **Construir y Lanzar los Contenedores:**
    ```bash
    docker compose up --build -d
    ```
    Esto construirá las imágenes del backend y frontend, y levantará los servicios en segundo plano, usando las variables definidas en tu archivo `.env`.

4.  **Acceder a la Aplicación:**
    Una vez que los contenedores estén en funcionamiento, podrás acceder a la interfaz de usuario en tu navegador a través de `http://localhost:80` (o el puerto que hayas configurado en Nginx si no es el 80 por defecto).

## Documentación de la API (Backend)

El backend de FFHS expone una serie de endpoints RESTful para la gestión de archivos y usuarios, protegidos por diferentes mecanismos de autenticación.

### Autenticación

Los endpoints de la API están protegidos por:

* **JWT (JSON Web Token)**: Utilizado principalmente por el frontend y para operaciones de usuario estándar. Se envía en el encabezado `Authorization: Bearer <your_jwt_token>`.
* **API Key y Basic Authentication**: Para acceso programático desde terceros, especialmente para operaciones a nivel de bucket o de administración.
    * **`X-API-KEY`**: Un encabezado HTTP que debe contener la `API_KEY` definida en el archivo `.env`.
    * **`Authorization: Basic`**: Un encabezado HTTP que contiene las credenciales de `APP_USER` y `APP_PASSWORD` codificadas en Base64. (Ejemplo: `Authorization: Basic <base64_encoded_username:password>`).

### Endpoints de Autenticación de Usuario (JWT)

Estos endpoints permiten a los usuarios registrarse, iniciar sesión y refrescar sus tokens JWT.

* **`POST /auth/login`**
    * **Descripción:** Autentica un usuario existente y devuelve un token JWT para usar en subsiguientes solicitudes.
    * **Cuerpo de la Solicitud (JSON):**
        ```json
        {
          "username": "tu_usuario",
          "password": "tu_contraseña"
        }
        ```
    * **Respuestas:**
        * `200 OK`: `{"token": "tu_jwt_token", "username": "tu_usuario"}`
        * `401 Unauthorized`: Credenciales inválidas, usuario deshabilitado o bloqueado.

* **`POST /auth/refresh`**
    * **Descripción:** Refresca un token JWT expirado o próximo a expirar para obtener uno nuevo.
    * **Encabezados:** `Authorization: Bearer <old_jwt_token>`
    * **Respuestas:**
        * `200 OK`: `{"token": "new_jwt_token"}`
        * `400 Bad Request`: Token no proporcionado.
        * `401 Unauthorized`: Token inválido, expirado o firma incorrecta.

### Endpoints de Gestión de Archivos (Protegidos por `bucketKey` y/o JWT/API Key)

Estos endpoints permiten operaciones CRUD sobre los archivos. Los parámetros `bucketKey` en `@RequestParam` o `@RequestHeader` son fundamentales para identificar el bucket de archivos al que se está accediendo.

* **`POST /api/files/upload`**
    * **Descripción:** Sube un solo archivo a un bucket específico.
    * **Autenticación:** JWT o API Key + Basic Auth.
    * **Tipo de Contenido:** `multipart/form-data`
    * **Parámetros de Solicitud:**
        * `bucketKey` (RequestParam): La clave del bucket al que subir el archivo.
        * `file` (RequestParam): El archivo a subir.
    * **Respuestas:**
        * `200 OK` (según tu código, devuelve `Response`): `{"message": "File uploaded successfully", ...}`
        * `4xx`: Errores de validación, autenticación o permisos.

* **`POST /api/files/upload-many`**
    * **Descripción:** Sube múltiples archivos a un bucket específico.
    * **Autenticación:** JWT o API Key + Basic Auth.
    * **Tipo de Contenido:** `multipart/form-data`
    * **Parámetros de Solicitud:**
        * `bucketKey` (RequestParam): La clave del bucket al que subir los archivos.
        * `files` (RequestParam): Un array de archivos a subir.
    * **Respuestas:**
        * `200 OK` (según tu código, devuelve `Response`): `{"message": "Files uploaded successfully", ...}`
        * `4xx`: Errores de validación, autenticación o permisos.

* **`DELETE /api/files/{id}`**
    * **Descripción:** Elimina un archivo específico por su ID.
    * **Autenticación:** JWT o API Key + Basic Auth.
    * **Parámetros de Ruta:** `{id}` (ID numérico del archivo).
    * **Encabezados:** `bucketKey`: La clave del bucket al que pertenece el archivo.
    * **Respuestas:**
        * `200 OK` (según tu código, devuelve `Response`): `{"message": "File deleted successfully", ...}`
        * `4xx`: Errores de validación, autenticación, permisos o archivo no encontrado.

* **`DELETE /api/files/delete-many`**
    * **Descripción:** Elimina múltiples archivos por sus IDs.
    * **Autenticación:** JWT o API Key + Basic Auth.
    * **Cuerpo de la Solicitud (JSON):**
        ```json
        {
          "ids": [1, 2, 3],
          "bucketKey": "tu_bucket_key"
        }
        ```
    * **Respuestas:**
        * `200 OK` (según tu código, devuelve `Response`): `{"message": "Files deleted successfully", ...}`
        * `4xx`: Errores de validación, autenticación o permisos.

* **`GET /api/files`**
    * **Descripción:** Lista todos los archivos dentro de un bucket específico.
    * **Autenticación:** JWT o API Key + Basic Auth.
    * **Parámetros de Solicitud:**
        * `bucketKey` (RequestParam): La clave del bucket cuyos archivos se quieren listar.
    * **Respuestas:**
        * `200 OK`: `[{"fileId": "...", "fileName": "...", "size": "...", "uploadDate": "...", "downloadUrl": "...", "owner": {...}}, ...]`
        * `4xx`: Errores de validación, autenticación o permisos.

* **`GET /api/files/view/{id}`**
    * **Descripción:** Permite la visualización directa de un archivo por su ID (devuelve el contenido binario con el `Content-Type` adecuado).
    * **Autenticación:** API Key (mediante encabezado `X-API-KEY`).
    * **Parámetros de Ruta:** `{id}` (ID numérico del archivo).
    * **Encabezados:** `X-API-KEY`: La clave de API configurada en el `.env`.
    * **Respuestas:**
        * `200 OK`: El contenido binario del archivo.
        * `401 Unauthorized`: API Key ausente o incorrecta.
        * `404 Not Found`: Archivo no encontrado.
        * `500 Internal Server Error`: Errores internos.

* **`GET /api/files/download/{id}`**
    * **Descripción:** Permite la descarga de un archivo por su ID (fuerza la descarga mediante `Content-Disposition`).
    * **Autenticación:** API Key (mediante encabezado `X-API-KEY`).
    * **Parámetros de Ruta:** `{id}` (ID numérico del archivo).
    * **Encabezados:** `X-API-KEY`: La clave de API configurada en el `.env`.
    * **Respuestas:**
        * `200 OK`: El contenido binario del archivo con encabezado de descarga.
        * `401 Unauthorized`: API Key ausente o incorrecta.
        * `404 Not Found`: Archivo no encontrado.
        * `500 Internal Server Error`: Errores internos.

### Endpoints de Gestión de Buckets (Protegidos por API Key + Basic Auth, Rol `ADMIN`)

Estos endpoints permiten la creación, listado y eliminación de buckets, que son contenedores lógicos para organizar archivos. Requieren rol `ADMIN`.

* **`POST /api/buckets/create`**
    * **Descripción:** Crea un nuevo bucket.
    * **Autenticación:** API Key (`X-API-KEY`) + Basic Auth (`APP_USER`, `APP_PASSWORD`) y Rol `ADMIN`.
    * **Cuerpo de la Solicitud (JSON):**
        ```json
        {
          "name": "nombre_del_bucket"
        }
        ```
    * **Respuestas:**
        * `201 Created`: `{"message": "Bucket creado con éxito", "bucketKey": "nueva_clave_de_bucket"}`
        * `401 Unauthorized`: Autenticación fallida.
        * `403 Forbidden`: No tiene rol de `ADMIN`.
        * `400 Bad Request`: Nombre ya en uso o inválido.

* **`GET /api/buckets`**
    * **Descripción:** Obtiene los detalles de un bucket específico usando su clave de API.
    * **Autenticación:** API Key (`X-API-KEY`) + Basic Auth (`APP_USER`, `APP_PASSWORD`) y Rol `ADMIN`.
    * **Cuerpo de la Solicitud (JSON):**
        ```json
        {
          "apiKey": "clave_de_api_del_bucket"
        }
        ```
    * **Respuestas:**
        * `200 OK`: `{ "id": ..., "name": "...", "apiKey": "..." }`
        * `401 Unauthorized`: Autenticación fallida.
        * `403 Forbidden`: No tiene rol de `ADMIN`.
        * `404 Not Found`: Bucket no encontrado.

* **`GET /api/buckets/all`**
    * **Descripción:** Lista todos los buckets existentes en el sistema.
    * **Autenticación:** API Key (`X-API-KEY`) + Basic Auth (`APP_USER`, `APP_PASSWORD`) y Rol `ADMIN`.
    * **Respuestas:**
        * `200 OK`: `[{"id": "...", "name": "...", "apiKey": "..."}, ...]`
        * `401 Unauthorized`: Autenticación fallida.
        * `403 Forbidden`: No tiene rol de `ADMIN`.

* **`DELETE /api/buckets`**
    * **Descripción:** Elimina un bucket por su clave de API.
    * **Autenticación:** API Key (`X-API-KEY`) + Basic Auth (`APP_USER`, `APP_PASSWORD`) y Rol `ADMIN`.
    * **Cuerpo de la Solicitud (JSON):**
        ```json
        {
          "apiKey": "clave_de_api_del_bucket_a_eliminar"
        }
        ```
    * **Respuestas:**
        * `200 OK` (según tu código, devuelve `Response`): `{"message": "Bucket deleted successfully", ...}`
        * `401 Unauthorized`: Autenticación fallida.
        * `403 Forbidden`: No tiene rol de `ADMIN`.
        * `404 Not Found`: Bucket no encontrado.

## Contribución

¡Las contribuciones son bienvenidas! Si deseas contribuir a FFHS, por favor, revisa `CONTRIBUTING.md` para obtener más detalles sobre cómo puedes ayudar.

## Licencia

Este proyecto, **FFHS (Free File Host System)**, opera bajo un modelo de **licencia dual** para ofrecer flexibilidad y sostenibilidad.

### Uso Open Source (Gratuito)

Puedes usar, modificar y distribuir **FFHS** gratuitamente bajo los términos de la [**Licencia MIT**](LICENSE) para:

* **Proyectos personales y hobby.**
* **Fines educativos y de investigación.**
* **Desarrollo y contribución open source.**
* **Cualquier uso que no implique una actividad comercial directa o indirecta.**

La Licencia MIT permite una gran libertad, siempre y cuando se mantenga el aviso de copyright y los términos de la licencia.

### Uso Comercial

Si tu organización (empresa, negocio, entidad sin fines de lucro, entidad gubernamental) utiliza **FFHS** de una manera que **contribuye directa o indirectamente a la generación de ingresos**, o si **integras o incorporas FFHS en un producto o servicio que vendes, ofreces o proporcionas a terceros a cambio de una compensación**, entonces se requiere una **licencia comercial**.

Para discutir opciones de licencia comercial, que pueden incluir un pago único por licencia o un porcentaje de los ingresos generados por el uso del software, por favor, rellena el formulario de contacto en nuestra web: [hgccarlos.es](https://hgccarlos.es).

Al adoptar este modelo dual, buscamos fomentar la innovación abierta y la colaboración de la comunidad, al mismo tiempo que garantizamos la sostenibilidad del proyecto a largo plazo.