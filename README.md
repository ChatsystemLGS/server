# Server for our Chatservice

## Protokoll

### Designentscheidungen

#### 1. Request-Response
* Client sendet anfrage -> Server antwortet
* Client kann nicht aktiv vom Server benachrichtigt werden
* Implementierung vergleichsweise einfach
* Client kann Nachrichten nur auf Anfrage empfangen

Beispiel:
```
> SENDMESSAGE <channelID> <data> <dataType>
< +OK
> GETMESSAGES <channelID> <beginTime> <endTime>
< +OK 3
< <data> <dataType> <timestamp>
< <data> <dataType> <timestamp>
< <data> <dataType> <timestamp>
> GETMESSAGES
< +OK 1
< <channelID> <data> <dataType> <timestamp>
> GETMESSAGES
< +OK 2
< <channelID> <data> <dataType> <timestamp>
< <channelID> <data> <dataType> <timestamp>
```

#### 2. Request-Response erweitert durch Events
* grundsätzlich gleiches Prinzip
* Server kann zusätzlich unabhängig vom client nachrichten mit bestimmtem Prefix senden, können vom client ignoriert werden
* Funktionen wie "typing indicator" lassen sich so implementieren
* Nachrichten können sowohl auf Anfrage vom Server geholt werden als auch aktiv vom server unabhängig übermittelt werden

Beispiel:
```
> SENDMESSAGE <channelID> <data> <dataType>
< +OK
< EVENT:MESSAGE <channelID> <data> <dataType> <timestamp>
< EVENT:ISTYPING <channelID> <userID>
< EVENT:MESSAGE <channelID> <data> <dataType> <timestamp>
> GETMESSAGES <channelID> <beginTime> <endTime>
< +OK 3
< <data> <dataType> <timestamp>
< <data> <dataType> <timestamp>
< <data> <dataType> <timestamp>
```

#### 3. Zwei parallele Socketverbindungen
* erste Verbidung arbeitet nach Request-Response-Prinzip
* zweite Verbindung ist unidirektional; streamt neu eintreffende Nachrichten an Client
* kein vorteil gegenüber [2.](#2.-request-response-erweitert-durch-events)?

Beispiel:
```
Connection 1:
> SENDMESSAGE <channelID> <data> <dataType>
< +OK
> GETMESSAGES <channelID> <beginTime> <endTime>
< +OK 3
< <data> <dataType> <timestamp>
< <data> <dataType> <timestamp>
< <data> <dataType> <timestamp>

Connection 2
> ISTYPING <channelID> <userID>
> MESSAGE <channelID> <data> <dataType> <timestamp>
> ISTYPING <channelID> <userID>
```