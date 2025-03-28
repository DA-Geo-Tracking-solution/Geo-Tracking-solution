# Geo Tracking Solution

## 📊 Projektbeschreibung
Geo Tracking Solution ist eine Anwendung zur Erfassung und Verwaltung von geografischen Daten. Sie besteht aus einem **Angular-Frontend**, einem **Spring Boot-Backend** und nutzt **CassandraDB** als Datenbank. Die Authentifizierung wird mit **Keycloak** verwaltet. Zudem ist eine **Docker-Compose**-Datei vorhanden, um die Datenbank- und Authentifizierungsdienste bereitzustellen.

## 🚀 Technologien
- **Frontend**: Angular
- **Backend**: Spring Boot
- **Datenbank**: CassandraDB
- **Authentifizierung**: Keycloak
- **Container-Orchestrierung**: Docker-Compose

## 📚 Installation und Setup
### 1. Frontend starten
```sh
cd Frontend/geo-tracking-solution
npm install
ng serve
```
Das Frontend ist dann unter `http://localhost:4200/` erreichbar.

### 2. Backend starten
```sh
cd Backend
./mvnw spring-boot:run
```
Das Backend ist dann unter `http://localhost:8080/` erreichbar.

### 3. Datenbank und Keycloak mit Docker starten
```sh
cd Backend/Spring/docker
docker-compose up -d
```
Dies startet die Cassandra-Datenbank und den Keycloak-Server als Container. Die `docker-compose.yml`-Datei befindet sich unter `Backend/Spring/docker/docker-compose.yml`.

## 🔎 Verwendung
- Nach dem Start können Nutzer sich über das Frontend authentifizieren (via Keycloak) und Geo-Daten verwalten.
- Das Backend stellt REST-APIs bereit, die mit dem Frontend kommunizieren.

## 🔧 Entwicklung
- **Frontend**: Änderungen im Angular-Code erfordern einen Neustart des Dev-Servers mit `ng serve`.
- **Backend**: Änderungen am Spring Boot-Backend können per `mvn spring-boot:run` direkt getestet werden.
- **Docker**: Falls sich Konfigurationen der Datenbank oder Keycloak ändern, muss `docker-compose down && docker-compose up -d` ausgeführt werden.

## 📚 Lizenz
Dieses Projekt steht unter der MIT-Lizenz. Weitere Details findest du in der Datei `LICENSE`.

---
Falls du noch zusätzliche Details möchtest (z. B. API-Dokumentation oder spezifische Umgebungsvariablen), lass es mich wissen! 🚀

