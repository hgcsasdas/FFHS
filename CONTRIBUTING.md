# Cómo Contribuir a FFHS (Free File Host System)

¡Gracias por tu interés en contribuir a FFHS! Tu ayuda es fundamental para el crecimiento y la mejora de este proyecto open source. A continuación, se detallan las pautas para que tu contribución sea fluida, efectiva y se alinee con el flujo de trabajo del proyecto.

---

## 🚀 Flujo de Trabajo para Contribuyentes

Adoptamos un flujo de trabajo basado en ramas de desarrollo (`develop`) y Pull Requests. Sigue estos pasos para tu contribución:

1.  **Prepara tu Entorno:**
    * Asegúrate de tener Docker, Docker Compose, Git, y un editor de código instalados.
    * Clona el repositorio original:
        ```bash
        git clone [https://github.com/hgcsasdas/FFHS.git](https://github.com/hgcsasdas/FFHS.git)
        cd FFHS
        ```

2.  **Sincroniza con la Rama Principal de Desarrollo:**
    * Nuestra rama principal de desarrollo es `develop`. Sincroniza tu repositorio local con ella antes de empezar a trabajar.
    * Crea una rama `develop` local si no la tienes y empújala:
        ```bash
        git checkout -b develop
        git push -u origin develop
        ```

3.  **Crea una Rama para tu Trabajo:**
    * Para cada bug, feature o mejora, crea una nueva rama a partir de `develop`. Elige un nombre descriptivo y sigue la convención:
        * Para nuevas funcionalidades: `feature/nombre-de-tu-funcionalidad`
        * Para correcciones de bugs: `bugfix/descripcion-del-bug`
    * **Ejemplo:**
        ```bash
        git checkout develop
        git pull origin develop
        git checkout -b feature/implement-user-crud
        ```

4.  **Realiza tus Cambios:**
    * Implementa tu código, corrige errores o añade funcionalidades.
    * Asegúrate de que tus cambios pasen las pruebas existentes y de que no introduzcan nuevos errores. Si es posible, añade nuevas pruebas para tu código.
    * (Si aplica) Actualiza la documentación relevante (`README.md`, etc.).

5.  **Haz 'Commit' de tus Cambios:**
    * Utiliza mensajes de 'commit' claros y descriptivos. Si el commit está relacionado con una 'issue', menciónalo. Se recomienda el uso de un estilo de commit convencional (ej. `feat:`, `fix:`, `docs:`) para mantener la coherencia.
    * **Ejemplo:**
        ```bash
        git add .
        git commit -m "feat(user): Añade las operaciones CRUD completas para la gestión de usuarios"
        ```

6.  **Abre un 'Pull Request' (PR):**
    * Sube tu rama a tu repositorio remoto:
        ```bash
        git push origin nombre-de-tu-rama
        ```
    * Ve a la página de GitHub del proyecto y abre un 'Pull Request' (PR) desde tu rama de trabajo (`feature/...` o `bugfix/...`) hacia la rama `develop`.
    * En la descripción del PR, explica claramente los cambios, por qué son necesarios, y si resuelve alguna `issue`, escribe `Closes #[número de la issue]` para que GitHub la cierre automáticamente.

---

## 💡 Qué Puedes Contribuir

* **Reporte de Bugs:** Si encuentras un error, por favor, abre una [Issue](https://github.com/hgcsasdas/FFHS/issues) con la mayor cantidad de detalles posible (pasos para reproducir, comportamiento esperado vs. actual, versión, etc.).
* **Sugerencia de Funcionalidades:** ¿Tienes una idea para mejorar FFHS? Abre una [Issue](https://github.com/hgcsasdas/FFHS/issues) para discutirla.
* **Código:** Escribe código para corregir errores, añadir nuevas características o mejorar el rendimiento. 
* **Documentación:** Mejora nuestra `README.md`, añade ejemplos de código o aclara cualquier parte de la documentación.
* **Testeo:** Ayúdanos a escribir o mejorar pruebas para asegurar la calidad del código.

---

## ⚠️ Licencia de las Contribuciones

Al contribuir código u otros materiales a **FFHS**, aceptas que tus contribuciones se licencian bajo los mismos términos del modelo de **licencia dual** del proyecto.

Esto significa que tus contribuciones estarán disponibles para el uso gratuito bajo la **Licencia MIT** para fines no comerciales, y también podrán ser incluidas en las versiones comerciales del software. Tu nombre será reconocido en el historial de 'commits' del proyecto.

---

## 🤝 Código de Conducta

Para mantener un ambiente de colaboración positivo y respetuoso, te pedimos que sigas nuestro [Código de Conducta](CODE_OF_CONDUCT.md).

¡Gracias por tu interés en FFHS y por considerar contribuir!