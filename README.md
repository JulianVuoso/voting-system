# POD - TPE1 - G6

### Preparación
Para preparar el entorno, una vez posicionado en el directorio del proyecto, se puede ejecutar el script prepare.sh 
otorgandole permisos de ejecución (de ser necesario) y luego ejecutando el script 

`chmod u+x prepare.sh` 

`./prepare.sh` 

Esto es equivalente a ejecutar los siguientes comandos (partiendo desde una terminal en el directorio raíz del proyecto):

1. `mvn clean install`
2. `cd server/target`
3. `tar -xzf tpe1-g6-server-1.0-SNAPSHOT-bin.tar.gz`
4. `chmod u+x tpe1-g6-server-1.0-SNAPSHOT/run-*`
5. `cd ../..`
6. `cd client/target`
7. `tar -xzf tpe1-g6-client-1.0-SNAPSHOT-bin.tar.gz`
8. `chmod u+x tpe1-g6-client-1.0-SNAPSHOT/run-*`

### Server-Side
Luego, se debe levantar el rmi-registry, para lo cual se debe (nuevamente desde el directorio raíz del proyecto)

1. `cd server/target/tpe1-g6-server-1.0-SNAPSHOT`
2. `./run-registry`

A continuación, levantamos el servidor en otra terminal, haciendo (desde el directorio raíz del proyecto)

1. `cd server/target/tpe1-g6-server-1.0-SNAPSHOT`
2. `./run-server`

Si el servidor inicia correctamente, imprime en pantalla el mensaje _**Election Service bound**_.

### Client-Side
Con el registry y el server corriendo, ya podemos iniciar cualquiera de los 4 clientes. Para ello, debemos ubicarnos
nuevamente en el directorio raíz del proyecto y movernos al directorio donde se encuentran los ejecutables de cada cliente

`cd client/target/tpe1-g6-client-1.0-SNAPSHOT`

Desde aquí, podremos ejecutar cada uno de los cuatro clientes disponibles. En cada uno de los clientes, se debe indicar
en el parámetro -DserverAddress la dirección y el puerto donde se encuentra corriendo el servidor. Si está corriendo
con los pasos anteriores, el parámetro es -DserverAddress=127.0.0.1:1099

- Para ejecutar el cliente de administración, se ejecuta run-management, indicando la acción a realizar (open, state 
o close)

`./run-management -DserverAddress=xx.xx.xx.xx:yyyy -Daction=actionName`

- Para ejecutar el cliente de votación, se ejecuta run-vote, indicando la ubicación del archivo .csv del cual se deben
leer los votos

`./run-vote -DserverAddress=xx.xx.xx.xx:yyyy -DvotesPath=fileName`

- Para ejecutar el cliente de fiscalización, se ejecuta run-fiscal, indicando el número de mesa de votación a fiscalizar
y el nombre del partido político del fiscal

`./run-fiscal -DserverAddress=xx.xx.xx.xx:yyyy -Did=pollingPlaceNumber -Dparty=partyName`

- Para ejecutar el cliente de consulta, se ejecuta run-query, indicando la ubicación del archivo .csv de salida y, de
forma opcional, un número de mesa a consultar ó una provincia sobre la cual se quieren los resultados. Solamente puede
indicarse uno de estos dos últimos parámetros a la vez.

`./run-query -DserverAddress=xx.xx.xx.xx:yyyy [ -Dstate=stateName | -Did=pollingPlaceNumber ] -DoutPath=fileName`
