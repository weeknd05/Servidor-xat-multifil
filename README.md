# Servidor-xat-multifil
Actividad evaluable M9 - Adrian Memecica

## Como arrancar
- Clona el repositorio
- Arranca la clase main
- Introduce los campos que pide el menu
#### Conexión: 
- ##### Para TCP
    Para conectarte al TCP, abre un terminal y haz lo siguiente `` telnet [localhost] [port]``
- ##### Para UDP
    Simplemente ejecuta la clase UDPClient
Esta actividad esta compuesta por varias clases, a continuación voy a explicar que hace cada una.

## Clases
Esta actividad esta compuesta por varias clases, a continuación voy a explicar que hace cada una.

- ### Main.java

    Esta clase esta compuesta por el metodo main, que arranca el programa principal.
    Tambien tiene dos metodos, uno para crear un hook de cierre, que se activa cuando el programa se apaga de una forma controlada
    E.j. ``System.exit()``
    Y otro para activar un disparador de tipo scheduler, que mira la inactividad y apaga el servidor.

- ### ServerConfig.java

    Esta clase se encarga de pedir los datos de configuración al usuario, generar un puerto aleatorio y guardar la configuración en un fichero
    No tiene manejo de excepciones asi que los datos que introduce el usuario tienen que ser correctos.
    Si introduce un String cuando se espera un int, el programa va a crashear.


- ### UserNameManager.java

    Esta clase se encarga de gestionar un archivo de texto con los nombres de usuarios conectados.

- ### TCPServer.java

    Es el servidor TCP, gestiona las conexiones

- ### TCPClient.java

    La clase cliente, gestiona el login y el envio y recibo de mensajes


- ### UDPServer.java

    El servidor UDP, abre el socket para datagramas y recibe mensajes del cliente

- ### UDPClient.java

    Gestiona los mensajes y el login, envia un mensaje de confirmación al principio para ver si el servidor esta activo

## Aspectos a mejorar
    Debido a la falta del tiempo y a que se ha complicado mas de lo que me esperaba, no he podido implementar algunas cosas
    Quieria intentar implementar una solución para la confirmación de la recepción de los paquetes para UDP (Keep Alive o similar) pero no he podido
    Tampoco tendria sentido hacerlo, ya que es posible que no todos los paquetes lleguen, si no es en local, y esa confirmación fallaria. 

-   Falta manejo de excepciones en el menu de configuración

-   Buscar otra forma de manejar los usuarios de UDP, ya que actualmente para recibir información se usan los ficheros generados por otras clases
    En un entorno real, el cliente no tendria acceso a ello, tendria que preguntar al servidor.
    Cabe mencionar que hacer un chat con UDP es una mala idea y el lioso, intentar hacer arreglos para copiar las caracteristicas de TCP no es eficiente.

-   En TCP podria mejorar la parte del login, que la gestione el Servidor, no el cliente, o que el Servidor confirme si esta correcto.

-   Por lo ultimo, creo que se podria haber buscado otra actividad para UDP y el chat solo en TCP, centrarnos bien en el chat con TCP y luego    hacer otra actividad para el protocolo UDP.

