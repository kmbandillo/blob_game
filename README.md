# 🎮 Blob.io

Blob.io is a 2D arcade survival game built with JavaFX to demonstrate core Object-Oriented Programming (OOP) principles. Players strive to survive and grow by eating food and smaller blobs while avoiding larger enemies. 

---

## Table of Contents
- [Game Overview](#-game-overview)
- [Features & Gameplay](#-features--gameplay)
- [Controls](#-controls)
- [Prerequisites & Required Installations](#-prerequisites--required-installations)
- [How to Clone the Repository](#-how-to-clone-the-repository)
- [Step-by-Step Setup in Eclipse IDE](#-step-by-step-setup-in-eclipse-ide)
- [Running from Command Line](#-running-from-command-line)
- [Developer & Credits](#-developer--credits)

---

## Game Overview

In **Blob.io**, you start as a single blob navigating a massive map. Your objective is simple: **grow, survive, and dominate**.
- **Feed & Grow**: Devour cookie pellets scattered across the playground (+10 size each).
- **Hunt & Evade**: Absorb enemy blobs smaller than you to gain their mass. Steer clear of larger enemies!
- **Power-Up Snacks**: Pick up special treats for game-changing temporary buffs.
- **Split Mechanics**: Divide into an organic circular cluster to increase agility and coverage. Colliding larger sub-blobs re-absorb smaller ones automatically.
- **Full Playground Accessibility**: Move smoothly all the way to the red boundary walls without artificial borders or dead zones.

---

## Features & Gameplay

### Food & Power-Ups
| Item | Type | Effect |
| :--- | :--- | :--- |
| 🍪 **Cookie** | Regular Food | Increases blob size by **+10** |
| ☕ **Hot Choco** | Power-Up | **Speed Boost**: Doubles movement speed for **5 seconds** |
| 🥜 **Peanut** | Power-Up | **Immunity**: Total invulnerability against larger blobs for **5 seconds** |

### ⏸️ Stateful Menu & Pause Navigation
- **In-Game Menu Button**: Pause your game at any moment and return to the main menu.
- **Continue Button**: Seamlessly resume your paused session right where you left off with blob size, split state, score, and remaining power-up durations preserved.
- **Game Over & Instructions Screens**: Beautiful unified card layout with custom graphics and easy navigation back to the main menu.

---

## Controls

| Action | Key / Control |
| :--- | :--- |
| **Move Up** | `W` or `Up Arrow` |
| **Move Down** | `S` or `Down Arrow` |
| **Move Left** | `A` or `Left Arrow` |
| **Move Right** | `D` or `Right Arrow` |
| **Split Blob** | `Spacebar` (requires size $\ge$ 80) |
| **Pause / Menu** | Click the **Menu** button at the top-right |

---

## Prerequisites & Required Installations

Before running the project, make sure the following software is installed on your computer:

1. **Java Development Kit (JDK 17 or higher)**
   - Download and install [Oracle JDK 17+](https://www.oracle.com/java/technologies/downloads/) or [Eclipse Temurin OpenJDK 17+](https://adoptium.net/).
   - Verify installation in your terminal:
     ```bash
     java -version
     javac -version
     ```

2. **JavaFX SDK (version 17 or higher)**
   - Download the JavaFX SDK matching your operating system and architecture from [Gluon JavaFX](https://gluonhq.com/products/javafx/).
   - Extract the downloaded `.zip` file to a known directory (e.g., `C:\javafx-sdk-17` on Windows or `/opt/javafx-sdk-17` on macOS/Linux).
   - Note down the path to the `lib` folder inside the extracted SDK (e.g., `C:\javafx-sdk-17\lib`).

3. **Eclipse IDE**
   - Download and install **[Eclipse IDE for Java Developers](https://www.eclipse.org/downloads/packages/)**.

4. **Git** (optional, for cloning)
   - Download from [git-scm.com](https://git-scm.com/).

---

## How to Clone the Repository

Clone this repository to your local machine using Git:

```bash
git clone https://github.com/your-username/blob-game.git
cd blob-game
```

*(Alternatively, click the green **Code** button on the GitHub repository page and select **Download ZIP**, then extract the contents to a folder.)*

---

## Step-by-Step Setup in Eclipse IDE

Follow these steps to import and configure the project in Eclipse:

### Step 1: Import the Project into Eclipse
1. Open **Eclipse IDE**.
2. Go to **File** > **Import...**
3. Select **General** > **Existing Projects into Workspace** and click **Next**.
4. Choose **Select root directory** and browse to the folder where you cloned/extracted the project.
5. Make sure the project is checked in the list, then click **Finish**.

---

### Step 2: Configure the JavaFX User Library in Eclipse
1. In Eclipse, go to **Window** > **Preferences** (on macOS: **Eclipse** > **Preferences**).
2. Expand **Java** > **Build Path** > **User Libraries**.
3. Click **New...**, enter `JavaFX` as the library name, and click **OK**.
4. Select the newly created `JavaFX` library and click **Add External JARs...**.
5. Navigate to the `lib` folder of your downloaded JavaFX SDK (e.g., `C:\javafx-sdk-17\lib`).
6. Select all `.jar` files in that folder:
   - `javafx.base.jar`
   - `javafx.controls.jar`
   - `javafx.fxml.jar`
   - `javafx.graphics.jar`
   - `javafx.media.jar`
   - `javafx.swing.jar`
   - `javafx.web.jar`
   - *(and `javafx-swt.jar` if present)*
7. Click **Open**, then click **Apply and Close**.

---

### Step 3: Attach the JavaFX Library to the Project Build Path
1. In the **Package Explorer**, right-click on the project (`Mini Project Draft` or `Blob Game`).
2. Select **Build Path** > **Configure Build Path...**.
3. Go to the **Libraries** tab.
4. Select **Classpath** (or **Modulepath**), then click **Add Library...**.
5. Select **User Library**, click **Next**, check `JavaFX`, and click **Finish**.
6. If there are any missing or broken library paths shown with a red cross, select them and click **Remove**.
7. Click **Apply and Close**.

---

### Step 4: Configure Run Configurations (VM Arguments)
Because JavaFX 11+ is modularized, VM arguments are required to link the JavaFX modules at runtime:

1. In the **Package Explorer**, locate `src/blobgame/Main.java`.
2. Right-click `Main.java` and select **Run As** > **Run Configurations...**.
3. In the left panel, select your application under **Java Application** (or double-click **Java Application** to create one).
4. Click on the **Arguments** tab.
5. In the **VM arguments** box, paste the following line (replace the path with your actual JavaFX `lib` folder path):

   **Windows**:
   ```text
   --module-path "C:\path\to\javafx-sdk\lib" --add-modules javafx.controls,javafx.media
   ```

   **macOS / Linux**:
   ```text
   --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.media
   ```

6. *(Optional)* If you see a modularity checkbox labeled **"Use the -XstartOnFirstThread argument when launching with SWT"** (on macOS), ensure it is unchecked.
7. Click **Apply**, then click **Run**!

---

## Running from Command Line

If you prefer compiling and running directly from a terminal or command prompt:

### Windows (PowerShell / Command Prompt)
```powershell
# 1. Compile Java source files
javac --module-path "C:\path\to\javafx-sdk\lib" --add-modules javafx.controls,javafx.media -d bin src/blobgame/*.java

# 2. Run the game
java --module-path "C:\path\to\javafx-sdk\lib" --add-modules javafx.controls,javafx.media -cp bin blobgame.Main
```

### macOS / Linux (Bash / Zsh)
```bash
# 1. Compile Java source files
javac --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.media -d bin src/blobgame/*.java

# 2. Run the game
java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.media -cp bin blobgame.Main
```

---

## Developer & Credits

- **Developer**: Kimberly Bandillo
- **Github**: kmbandillo
- **Course Reference**: CMSC 22 Object-Oriented Programming

Enjoy playing **Blob.io**!
