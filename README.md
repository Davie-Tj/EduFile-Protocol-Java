# 📁 EduFile Protocol

A **multi-threaded client-server file management system** built in Java using raw TCP sockets.  
Designed to simulates a university document repository where students can **login**, **view**, **download**, and **upload** educational resources.

---

##  Features

-  **User Authentication** — Login with credentials stored in `Users.txt`.
-  **List Documents** — View all available files with their IDs.
-  **Download Files** — Fetch any document by ID.
-  **Upload Files** — Add new documents to the server.
-  **Display Images** — View `.png` and `.jpeg` files directly in the GUI.
-  **Multi-threaded Server** — Handles multiple clients simultaneously.
-  **JavaFX GUI** — Clean, responsive client interface.

---

## 📦 Technologies Used

| Layer | Technology |
|---|---|
| **Language** | Java 17+ |
| **GUI** | JavaFX |
| **Networking** | Raw TCP Sockets (no external libraries) |
| **Concurrency** | Java Threads |
| **Build Tool** | Manual `.bat` scripts (or your IDE) |

---

## 📁 Project Structure
```plaintext
EduFile-Protocol-Java/
├── src/
│ ├── tj/
│ │ ├── client/
│ │ │ ├── EduClient.java # Entry point (JavaFX)
│ │ │ ├── ClientPane.java # GUI layout and events
│ │ │ └── ClientHandler.java # Client-side networking
│ │ └── server/
│ │ ├── EduServer.java # Server entry point
│ │ └── EduServerHandler.java # Handles client requests
├── data/
│ ├── client/ # Downloaded files go here
│ └── server/
│ ├── Users.txt # Login credentials
│ ├── docs.txt # File ID ↔ Name mappings
│ └── [actual files] # PDFs, images, etc.
├── docs/
│ └── *.bat # Build/run scripts
└── README.md
```


---

##  How to Run

### 1. Start the Server
bash
cd docs
buildServerMain.bat

2. Start the client
cd docs
buildClientMain.bat

3. Login
Username: ***

Password: ***

(These are stored in data/server/Users.txt) you can create your own Users.txt and poulate with usernames

### What I Learned
Designing a custom application layer protocol (EduFile).

Handling binary file transfers over TCP without corruption.

Mixing text and binary streams safely using new connections.

Building a threaded server to handle multiple clients.

Using JavaFX for a clean, functional GUI.

Stream management — separating text commands from binary file data.

## License
This project is for educational purposes only.
