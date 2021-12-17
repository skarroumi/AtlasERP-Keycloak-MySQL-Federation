

Build Command: `./gradlew build`

once build is successful a jar file named 'user-storage-spi-1.0-SNAPSHOT.jar' will be generated in build/libs folder

Copy that jar file into the   ${keycloak_server_home}/standalone/deployment folder.

Start the keycloak server, then logs will show that new jar file is deployed

Then login to the application and go to the uer federation section in keycloak admin console.
