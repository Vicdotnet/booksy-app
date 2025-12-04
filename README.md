# Booksy

app para buscar y comprar libros chilenos

## Integrantes

- **Vicente Quezada** - frontend, pantallas y navegacion
- **Oscar Baez** - backend, api rest y tests


## Descripcion

booksy es una aplicacion movil para android que permite ver un catalogo de libros chilenos, buscar por titulo o autor, filtrar por categorias y agregar libros al carrito.

la app se conecta a un backend en la nube (render) que maneja los libros y usuarios.


## Funcionalidades

- **registro e inicio de sesion** - crear cuenta nueva o entrar con una existente
- **catalogo de libros** - ver libros chilenos con imagenes y precios
- **busqueda** - buscar libros por titulo o autor
- **filtros** - filtrar por categoria (ficcion, clasicos, poesia)
- **detalle del libro** - ver sinopsis completa de cada libro
- **carrito de compras** - agregar libros y ver el total
- **perfil de usuario** - cambiar foto de perfil con la camara
- **cierre de sesion** - salir de la cuenta


## Tecnologias

### Android
- kotlin
- jetpack compose (ui moderna)
- retrofit (consumo de api)
- coil (cargar imagenes)
- navigation compose
- sharedpreferences (guardar sesion)
- junit + mockk (tests unitarios)

### Backend
- nestjs (nodejs)
- mongodb atlas (base de datos en la nube)
- jwt (autenticacion)
- desplegado en render


## Instalacion

### opcion 1: descargar apk (recomendado)

1. ir a releases en github
2. descargar el apk firmado
3. instalar en tu celular android
4. abrir la app y crear cuenta

### opcion 2: compilar desde android studio

1. clonar el repo
2. abrir en android studio
3. esperar que gradle sincronice
4. conectar celular o usar emulador
5. darle run

**nota:** el backend ya esta desplegado en https://booksy-api-twu9.onrender.com


## Como usar la app

1. al abrir veras la pantalla de login
2. si no tienes cuenta toca "registrate"
3. llena tus datos y crea la cuenta
4. ya dentro veras el catalogo de libros
5. puedes buscar libros o filtrar por categoria
6. toca un libro para ver su sinopsis completa
7. agrega libros al carrito con el boton verde
8. ve al carrito con el icono arriba a la derecha
9. en perfil puedes cambiar tu foto
10. cierra sesion cuando quieras


## Estructura del codigo

```
app/src/main/java/com/booksy/
├── data/
│   ├── local/SessionManager.kt        # manejo de sesion
│   ├── remote/RetrofitClient.kt       # conexion api
│   └── models/                        # modelos de datos
├── ui/screens/                        # pantallas de la app
├── viewmodel/                         # logica de negocio
├── navigation/                        # rutas de navegacion
└── MainActivity.kt                    # punto de entrada
```


## API Backend

base url: `https://booksy-api-twu9.onrender.com`

### endpoints principales

**autenticacion**
- `POST /auth/register` - crear cuenta
- `POST /auth/login` - iniciar sesion

**libros**
- `GET /books` - obtener todos los libros
- `GET /books/:id` - obtener un libro por id

**carrito**
- `POST /cart` - agregar item al carrito
- `GET /cart/user/:userId` - ver carrito del usuario
- `DELETE /cart/:id` - eliminar item del carrito

**carrito**
- `POST /cart` - agregar item al carrito
- `GET /cart/user/:userId` - ver carrito del usuario
- `DELETE /cart/:id` - eliminar item del carrito


## Tests Unitarios

tenemos 8 tests que cubren las funcionalidades principales:

**LoginViewModelTest** (3 tests)
- validacion de email vacio
- validacion de password vacio
- validacion de password valido (8+ caracteres)

**RegisterViewModelTest** (3 tests)
- validacion de nombre vacio
- validacion de password vacio
- validacion de passwords que no coinciden

**BooksViewModelTest** (2 tests)
- busqueda de libros por titulo
- filtrado por categoria

para correr los tests:
```bash
./gradlew test
```