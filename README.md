# Booksy - app de catalogo de libros

## Integrantes

- **Vicente Quezada** - diseño de interfaz, pantallas de login y home
- **Oscar Baez** - conexion con la api, base de datos local y camara


## que hace la app

es una app sencilla para ver y gestionar libros. se pueden:
- crear una cuenta
- iniciar sesion
- ver un catalogo de libros desde el backend
- buscar libros por titulo o autor
- filtrar por categorias
- buscar libros externos con google books api
- ver carrito de compras con calculo de comision
- cambiar la foto de perfil con la camara o galeria
- cerrar sesion


## tecnologias que usamos

### app movil
- **lenguaje:** kotlin
- **ui:** jetpack compose
- **base de datos:** room (local)
- **api:** retrofit
- **navegacion:** navigation compose
- **camara:** accompanist permissions + activityresultcontracts
- **tests:** junit, mockk, turbine

### backend
- **framework:** spring boot 3.2.0
- **base de datos:** postgresql
- **build:** maven
- **arquitectura:** microservicios rest


## como correr el proyecto

### requisitos
- android studio
- java 17 o superior
- postgresql instalado
- maven (incluido en el proyecto)
- un celular o emulador android

### pasos backend

1. instalar postgresql y crear base de datos:
```sql
CREATE DATABASE booksy;
```

2. ir a la carpeta del backend:
```bash
cd booksy-backend
```

3. correr el servidor:
```bash
./mvnw spring-boot:run
```

el backend estara en `http://localhost:8080`

### pasos app movil

1. clonar el repo:

2. abrirlo en android studio
3. asegurarse que el backend este corriendo
4. correr la app en emulador o dispositivo


## estructura del proyecto

### app movil

app/src/main/java/com/booksy/
├── data/
│   ├── local/          # base de datos room
│   ├── remote/         # retrofit clients
│   └── models/         # book, user, cart, etc
├── ui/
│   ├── screens/        # login, home, cart, profile
│   └── theme/          # colores y estilos
├── viewmodel/          # logica de negocio
├── navigation/         # navegacion entre pantallas
└── MainActivity.kt


### backend

booksy-backend/src/main/java/com/booksy/backend/
├── controller/         # auth, book, cart
├── model/             # entidades jpa
├── repository/        # acceso a datos
└── resources/
    ├── application.properties
    └── data.sql       # datos iniciales



## funcionalidades

### login
- valida email y contraseña (minimo 8 caracteres)
- se conecta al backend para autenticacion
- guarda usuario localmente

### registro
- nombre, email, contraseña y confirmacion
- valida que todo este correcto
- crea cuenta en el backend

### catalogo (home)
- lista de libros desde el backend
- buscador por titulo o autor
- filtros por categoria (ficcion, infantil, poesia)
- integracion con google books api para buscar mas libros
- boton para ir al carrito

### carrito
- muestra items agregados
- calcula subtotal
- agrega comision del 10%
- muestra total final
- permite eliminar items
- boton para limpiar carrito

### perfil
- muestra nombre y correo
- foto de perfil con camara o galeria
- cerrar sesion


## microservicios backend

### endpoints auth
- `POST /auth/register` - crear cuenta
- `POST /auth/login` - iniciar sesion

### endpoints libros
- `GET /books` - obtener todos los libros
- `GET /books/{id}` - obtener libro por id
- `POST /books` - crear libro
- `PUT /books/{id}` - actualizar libro
- `DELETE /books/{id}` - eliminar libro

### endpoints carrito
- `GET /cart/{userId}` - obtener carrito del usuario
- `POST /cart/{userId}/items` - agregar item
- `DELETE /cart/{userId}/items/{itemId}` - eliminar item
- `DELETE /cart/{userId}` - limpiar carrito
- `GET /cart/{userId}/total` - obtener total con comision


## api externa

### google books api
- **url base:** `https://www.googleapis.com/books/v1/`
- **endpoint:** `GET /volumes?q={query}`
- se usa para buscar libros externos
- muestra titulo y autor
- no requiere autenticacion


## base de datos

### postgresql (backend)
- **libro:** id, titulo, autor, precio, categoria, imagen
- **usuario:** id, nombre, email, password
- **carrito_item:** id, user_id, book_id, cantidad, precio_unitario

### room (app)
- **user:** id, name, email, password, profileImagePath


## tests unitarios

tenemos 10 tests en total:

### LoginViewModelTest
- validacion de password vacio
- validacion de password con 8+ caracteres

### RegisterViewModelTest
- validacion de nombre vacio
- validacion de password vacio
- validacion de confirmacion de password

### BooksViewModelTest
- busqueda por titulo
- filtrado por categoria
- manejo de errores de conexion

### ProfileViewModelTest
- carga de datos del usuario
- logout

para correr los tests:
```bash
./gradlew test
```


## apk firmado

el apk esta firmado con un keystore para poder instalarlo en dispositivos.

archivos:
- `booksy.jks` - keystore
- configuracion en `app/build.gradle.kts`

para generar el apk:
1. Build > Generate Signed Bundle / APK
2. seleccionar APK
3. usar el keystore configurado
4. release


## problemas que resolvimos

1. **cleartext traffic no permitido**
   agregamos network_security_config.xml para http en desarrollo

2. **conexion con backend local**
   usamos 10.0.2.2 en emulador para localhost

3. **validacion de contraseñas**
   cambiamos a minimo 8 caracteres en app y backend

4. **dependencias de testing**
   agregamos mockk, turbine para los tests


## lo que aprendimos

- arquitectura mvvm con viewmodel
- integracion con spring boot y postgresql
- consumo de apis con retrofit
- testing unitario con mockk
- navegacion con jetpack compose
- manejo de estados con stateflow
- implementacion de microservicios
- persistencia con room y postgresql