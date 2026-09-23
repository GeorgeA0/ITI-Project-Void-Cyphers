# iWish - ITI Void Cyphers
A collaborative JavaFX desktop application for managing and funding wishlists between friends.
## Demo Video #1
https://github.com/user-attachments/assets/d2a899e2-4aa4-4a9e-9b1e-57b65c99cfb2
## Demo Video #2
https://github.com/user-attachments/assets/9099b31c-3bf5-485e-8a91-67c2c021ddaf
---
## ✨ Features
- **Client/Server Architecture:** A robust `iwish-server` handling requests and an `iwish-client` offering an interactive GUI.
- **Embedded Database:** Powered by SQLite (`iwish.db`) for a hassle-free database setup without requiring external database servers.
- **Modern UI:** Built using JavaFX for a rich desktop client experience.
- **Maven Integration:** Both modules are fully managed by Maven for easy building and dependency management.
---
## 🛠️ Prerequisites
Before you start, make sure you have the following installed on your machine:
- **Java JDK (v11 or higher)**
- **Maven** (Make sure `mvn` is accessible from your command prompt)
---
## 🚦 Running the Project locally
You will need to open **two separate terminal windows** (one for the server and one for the client). Make sure you start from the root directory of the project.
### 1️⃣ Start the Server
The server must be running before the client can connect. 
1. Open your first terminal.
2. Navigate to the server directory:
   ```bash
   cd iwish-server
