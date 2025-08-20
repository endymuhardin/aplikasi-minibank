## Setup Development Environment ##

### Prerequisites untuk Semua Operating System ###

- **Java 21** (menggunakan SDKMAN)
- **Maven 3.6+** (atau gunakan Maven wrapper `./mvnw`)
- **Node.js 18+** (menggunakan NVM)
- **Docker Desktop** (untuk database dan Selenium tests)
- **Git** (untuk version control)

### Windows Setup ###

#### 1. Install Git Bash ####
```powershell
# Download dan install Git for Windows dari https://git-scm.com/download/win
# Pastikan pilih "Git Bash" saat instalasi
```

#### 2. Install SDKMAN (melalui Git Bash) ####
```bash
# Buka Git Bash dan jalankan:
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Verify installation
sdk version
```

#### 3. Install Java 21 menggunakan SDKMAN ####
```bash
# List available Java versions
sdk list java

# Install Java 21 (Temurin/Eclipse Adoptium)
sdk install java 21.0.5-tem

# Set as default
sdk default java 21.0.5-tem

# Verify installation
java -version
```

#### 4. Install Maven menggunakan SDKMAN ####
```bash
sdk install maven
```

#### 5. Install NVM ####
```bash
# Download nvm-windows dari https://github.com/coreybutler/nvm-windows
# Atau install via chocolatey:
choco install nvm
```

#### 6. Install Node.js menggunakan NVM ####
```bash
# Install Node.js 18 LTS
nvm install 18.19.0
nvm use 18.19.0

# Verify installation
node --version
npm --version
```

#### 7. Install Docker Desktop ####
```powershell
# Download dari https://docs.docker.com/desktop/install/windows-install/
# Atau menggunakan winget
winget install Docker.DockerDesktop
```

### Ubuntu/Debian Setup ###

#### 1. Update sistem dan install dependencies ####
```bash
sudo apt update && sudo apt upgrade -y
sudo apt install curl wget gnupg2 software-properties-common apt-transport-https ca-certificates git -y
```

#### 2. Install SDKMAN ####
```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Verify installation
sdk version
```

#### 3. Install Java 21 menggunakan SDKMAN ####
```bash
# List available Java versions
sdk list java

# Install Java 21 (Temurin/Eclipse Adoptium)
sdk install java 21.0.5-tem

# Set as default
sdk default java 21.0.5-tem

# Verify installation
java -version
javac -version
```

#### 4. Install Maven menggunakan SDKMAN ####
```bash
sdk install maven

# Verify installation
mvn -version
```

#### 5. Install NVM ####
```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
source ~/.bashrc

# Verify installation
nvm --version
```

#### 6. Install Node.js menggunakan NVM ####
```bash
# Install Node.js 18 LTS
nvm install 18.19.0
nvm use 18.19.0
nvm alias default 18.19.0

# Verify installation
node --version
npm --version
```

#### 7. Install Docker Desktop ####
```bash
# Install Docker Engine
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg
echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt update
sudo apt install docker-ce docker-ce-cli containerd.io docker-compose-plugin -y

# Add user to docker group
sudo usermod -aG docker $USER
newgrp docker

# Install Docker Desktop (optional GUI)
wget https://desktop.docker.com/linux/main/amd64/docker-desktop-4.25.0-amd64.deb
sudo dpkg -i docker-desktop-4.25.0-amd64.deb
```

### macOS Setup ###

#### 1. Install Homebrew (jika belum ada) ####
```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```

#### 2. Install SDKMAN ####
```bash
curl -s "https://get.sdkman.io" | bash
source "$HOME/.sdkman/bin/sdkman-init.sh"

# Verify installation
sdk version
```

#### 3. Install Java 21 menggunakan SDKMAN ####
```bash
# List available Java versions
sdk list java

# Install Java 21 (Temurin/Eclipse Adoptium)
sdk install java 21.0.5-tem

# Set as default
sdk default java 21.0.5-tem

# Verify installation
java -version
```

#### 4. Install Maven menggunakan SDKMAN ####
```bash
sdk install maven

# Verify installation
mvn -version
```

#### 5. Install NVM ####
```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
source ~/.zshrc

# Verify installation
nvm --version
```

#### 6. Install Node.js menggunakan NVM ####
```bash
# Install Node.js 18 LTS
nvm install 18.19.0
nvm use 18.19.0
nvm alias default 18.19.0

# Verify installation
node --version
npm --version
```

#### 7. Install Docker Desktop ####
```bash
brew install --cask docker

# Atau download manual dari https://docs.docker.com/desktop/install/mac-install/
```

#### 8. Install Git (jika belum ada) ####
```bash
brew install git
```

### Verification dan First Setup ###

#### 1. Clone repository ####
```bash
git clone <repository-url>
cd aplikasi-minibank
```

#### 2. Verify semua tools terinstall ####
```bash
# Check Java
java -version
# Expected: openjdk version "21.0.5"

# Check Maven
mvn -version
# Expected: Apache Maven 3.x.x

# Check Node.js
node --version
# Expected: v18.19.0

# Check npm
npm --version

# Check Docker
docker --version
docker-compose --version

# Check SDKMAN
sdk version

# Check NVM
nvm --version
```

#### 3. Setup IDE (Opsional) ####

**Visual Studio Code:**
- Install extension: "Extension Pack for Java"
- Install extension: "Spring Boot Extension Pack"
- Install extension: "Tailwind CSS IntelliSense"

**IntelliJ IDEA:**
- Install Spring Boot plugin
- Import project sebagai Maven project
- Enable annotation processing untuk Lombok

#### 4. First Run Test ####
```bash
# Test Maven build
./mvnw clean compile

# Test frontend build
cd src/main/frontend
npm install
cd ../../..

# Test Docker
docker --version
docker-compose --version
```

### Version Management dengan SDKMAN dan NVM ###

#### SDKMAN Commands ####
```bash
# List installed Java versions
sdk list java

# Switch Java version
sdk use java 21.0.5-tem

# Install additional Java version
sdk install java 17.0.9-tem

# Set default Java version
sdk default java 21.0.5-tem

# Update SDKMAN
sdk update
```

#### NVM Commands ####
```bash
# List installed Node versions
nvm list

# Switch Node version
nvm use 18.19.0

# Install additional Node version
nvm install 20.10.0

# Set default Node version
nvm alias default 18.19.0

# Update NVM
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.39.0/install.sh | bash
```

### Troubleshooting Common Issues ###

#### Windows Issues ####
- **SDKMAN tidak dikenali**: Pastikan menjalankan di Git Bash, bukan Command Prompt
- **Docker permission**: Jalankan Docker Desktop sebagai administrator
- **Port conflicts**: Pastikan port 8080 dan 2345 tidak digunakan aplikasi lain

#### Ubuntu Issues ####
- **Permission denied (Docker)**: Pastikan user sudah di group docker: `sudo usermod -aG docker $USER`
- **SDKMAN not found**: Restart terminal atau jalankan `source ~/.bashrc`
- **NVM command not found**: Restart terminal atau jalankan `source ~/.bashrc`

#### macOS Issues ####
- **SDKMAN not found**: Restart terminal atau jalankan `source ~/.zshrc`
- **NVM command not found**: Restart terminal atau jalankan `source ~/.zshrc`
- **Docker memory**: Tingkatkan Docker Desktop memory allocation di preferences

#### General SDKMAN/NVM Issues ####
- **Version conflicts**: Gunakan `sdk current` dan `nvm current` untuk check active versions
- **Path issues**: SDKMAN dan NVM otomatis manage PATH, hindari manual PATH modification
- **Shell conflicts**: Pastikan menggunakan shell yang benar (bash/zsh)
