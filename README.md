# java-project-boardgame-adrenaline

## Politecnico di Milano
## Jul 2019

- ###   Riccardo Bassani ([@BassaniRiccardo](https://github.com/BassaniRiccardo))
- ###   Marco Bagatella ([@marcobaga](https://github.com/marcobaga))
- ###   Davide Aldè ([@davidealde](https://github.com/davidealde))

## Java implementation of the game "Adrenaline", a board game by Filip Neduk (more information about the game at https://czechgames.com/en/adrenaline/)
Team project with a strong focus on the design phase. The project comprehends, in the Deliverables folder, a detailed hand-written class diagram (Hand_Written_Class_Diagram.pdf) as well as an overview of the project (Generated_Class_Diagrams.pdf) showing diagrams auto-generated by IntelliJ. An explaination of how communication was handled is also available in the Deliverables folder (Communication.pdf).

| Functionality | State |
|:-----------------------|:------------------------------------:|
| Basic rules | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| Complete rules | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| Socket | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| RMI | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| GUI | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| CLI | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |
| Multiple games | [![GREEN](https://placehold.it/15/44bb44/44bb44)](#) |


**Setup:**
- The server uses port 3994 for RMI and 4198 for TCP.
- Client parameters can be set at launch:

    java -jar client.jar [serverIP] [serverPort] [clientIP]
     
- If no args are provided, the client will try to read a client.properties file in the same folder as the jar.
- If this file is not available, default parameters will be loaded from those contained in resources/client.properties before building the jar.

- Server properties (IPs and ports) must be set in a server.property file, which can be either in the same folder as the jar file, or in /resources before building the jar.
The server will first check for properties in the same folder.
