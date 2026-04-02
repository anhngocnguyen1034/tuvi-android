# Hướng dẫn tích hợp Jenkins CI/CD cho Android App với thông báo Discord

## Mục lục

1. [Yêu cầu hệ thống](#1-yêu-cầu-hệ-thống)
2. [Cài đặt Jenkins](#2-cài-đặt-jenkins)
3. [Cấu hình Jenkins cho Android](#3-cấu-hình-jenkins-cho-android)
4. [Tạo Discord Webhook](#4-tạo-discord-webhook)
5. [Cấu trúc Jenkinsfile](#5-cấu-trúc-jenkinsfile)
6. [Các Stage chi tiết](#6-các-stage-chi-tiết)
7. [Thông báo Discord nâng cao](#7-thông-báo-discord-nâng-cao)
8. [Biến môi trường & Secrets](#8-biến-môi-trường--secrets)
9. [Tích hợp vào dự án mới](#9-tích-hợp-vào-dự-án-mới)
10. [Troubleshooting](#10-troubleshooting)

---

## 1. Yêu cầu hệ thống

### Máy chủ Jenkins
| Thành phần | Phiên bản tối thiểu |
|------------|---------------------|
| Jenkins | 2.440+ (LTS) |
| Java (JDK) | 17+ |
| Android SDK | Build Tools 34+ |
| Gradle | 8.0+ |
| Git | 2.30+ |
| RAM | 4 GB (khuyến nghị 8 GB) |
| Disk | 20 GB trống |

### Jenkins Plugins cần cài
Vào **Manage Jenkins → Plugins → Available** và cài:

```
- Git Plugin
- Gradle Plugin
- Discord Notifier  (hoặc dùng HTTP Request Plugin)
- Credentials Plugin
- Pipeline Plugin
- Blue Ocean (tuỳ chọn, UI đẹp hơn)
- Timestamper
- AnsiColor
- Build Timeout
- Workspace Cleanup
```

---

## 2. Cài đặt Jenkins

### 2.1 Cài trên Ubuntu/Debian

```bash
# Thêm Jenkins repo
curl -fsSL https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key \
  | sudo tee /usr/share/keyrings/jenkins-keyring.asc > /dev/null

echo "deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc] \
  https://pkg.jenkins.io/debian-stable binary/" \
  | sudo tee /etc/apt/sources.list.d/jenkins.list > /dev/null

# Cài Java + Jenkins
sudo apt update
sudo apt install -y openjdk-17-jdk jenkins

# Khởi động
sudo systemctl enable jenkins
sudo systemctl start jenkins

# Lấy mật khẩu khởi tạo
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

### 2.2 Cài bằng Docker (khuyến nghị)

```bash
# Tạo file docker-compose.yml
cat > docker-compose.yml << 'EOF'
version: '3.8'
services:
  jenkins:
    image: jenkins/jenkins:lts-jdk17
    container_name: jenkins
    privileged: true
    user: root
    ports:
      - "8080:8080"
      - "50000:50000"
    volumes:
      - jenkins_home:/var/jenkins_home
      - /var/run/docker.sock:/var/run/docker.sock
      - /usr/local/android-sdk:/usr/local/android-sdk  # mount Android SDK
    environment:
      - ANDROID_HOME=/usr/local/android-sdk
      - JAVA_OPTS=-Djenkins.install.runSetupWizard=false
    restart: unless-stopped

volumes:
  jenkins_home:
EOF

docker-compose up -d
```

---

## 3. Cấu hình Jenkins cho Android

### 3.1 Cài Android SDK trên máy Jenkins

```bash
# Tải command line tools
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip commandlinetools-linux-*.zip -d /usr/local/android-sdk/cmdline-tools
mv /usr/local/android-sdk/cmdline-tools/cmdline-tools \
   /usr/local/android-sdk/cmdline-tools/latest

# Set environment
export ANDROID_HOME=/usr/local/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

# Chấp nhận license và cài build tools
yes | sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-35" "build-tools;35.0.0"
```

### 3.2 Cấu hình Global Tools trong Jenkins

**Manage Jenkins → Tools:**

```
JDK:
  Name: JDK-17
  JAVA_HOME: /usr/lib/jvm/java-17-openjdk-amd64

Gradle:
  Name: Gradle-8
  ✅ Install automatically → Gradle 8.6
```

### 3.3 Cấu hình Environment Variables

**Manage Jenkins → System → Global properties → Environment variables:**

```
ANDROID_HOME = /usr/local/android-sdk
JAVA_HOME    = /usr/lib/jvm/java-17-openjdk-amd64
PATH+ANDROID = /usr/local/android-sdk/platform-tools:/usr/local/android-sdk/cmdline-tools/latest/bin
```

---

## 4. Tạo Discord Webhook

### 4.1 Tạo Webhook trong Discord

1. Mở Discord → vào **Server Settings → Integrations → Webhooks**
2. Nhấn **New Webhook**
3. Đặt tên: `Jenkins CI` và chọn channel nhận thông báo
4. Copy **Webhook URL** (dạng: `https://discord.com/api/webhooks/ID/TOKEN`)

### 4.2 Lưu Webhook vào Jenkins Credentials

**Manage Jenkins → Credentials → System → Global → Add Credentials:**

```
Kind:   Secret text
Scope:  Global
Secret: https://discord.com/api/webhooks/YOUR_ID/YOUR_TOKEN
ID:     DISCORD_WEBHOOK_URL
Description: Discord Webhook cho CI notifications
```

### 4.3 Test Webhook thủ công

```bash
curl -H "Content-Type: application/json" \
  -X POST \
  -d '{
    "username": "Jenkins CI",
    "embeds": [{
      "title": "✅ Test kết nối",
      "description": "Jenkins đã kết nối Discord thành công!",
      "color": 3066993
    }]
  }' \
  https://discord.com/api/webhooks/YOUR_ID/YOUR_TOKEN
```

---

## 5. Cấu trúc Jenkinsfile

Tạo file `Jenkinsfile` ở **thư mục gốc** của dự án Android:

```groovy
pipeline {
    agent any

    // ── Cấu hình chung ──────────────────────────────────────────────────────
    options {
        timeout(time: 30, unit: 'MINUTES')   // build không quá 30 phút
        timestamps()                          // hiện timestamp trong log
        ansiColor('xterm')                    // màu log
        buildDiscarder(logRotator(            // giữ tối đa 10 build
            numToKeepStr: '10',
            artifactNumToKeepStr: '5'
        ))
        skipStagesAfterUnstable()
    }

    // ── Biến môi trường ──────────────────────────────────────────────────────
    environment {
        ANDROID_HOME        = '/usr/local/android-sdk'
        APP_NAME            = 'MyApp'                       // ← đổi tên app
        GRADLE_OPTS         = '-Dorg.gradle.daemon=false'
        DISCORD_WEBHOOK_URL = credentials('DISCORD_WEBHOOK_URL')
    }

    // ── Trigger tự động ─────────────────────────────────────────────────────
    triggers {
        // Poll SCM mỗi 5 phút (hoặc dùng GitHub Webhook)
        pollSCM('H/5 * * * *')
    }

    // ── Parameters (chạy tay) ────────────────────────────────────────────────
    parameters {
        choice(
            name: 'BUILD_TYPE',
            choices: ['debug', 'release'],
            description: 'Loại build'
        )
        booleanParam(
            name: 'RUN_TESTS',
            defaultValue: true,
            description: 'Chạy Unit Tests?'
        )
        booleanParam(
            name: 'DEPLOY_TO_FIREBASE',
            defaultValue: false,
            description: 'Deploy APK lên Firebase App Distribution?'
        )
    }

    stages {
        // ── Stage 1: Checkout ─────────────────────────────────────────────
        stage('📥 Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_COMMIT_MSG = sh(
                        script: 'git log -1 --pretty=%B | head -1',
                        returnStdout: true
                    ).trim()
                    env.GIT_AUTHOR = sh(
                        script: 'git log -1 --pretty=%an',
                        returnStdout: true
                    ).trim()
                    env.GIT_BRANCH_NAME = env.GIT_BRANCH?.replace('origin/', '') ?: 'unknown'
                }
            }
        }

        // ── Stage 2: Validate ─────────────────────────────────────────────
        stage('🔍 Validate') {
            steps {
                sh 'chmod +x gradlew'
                sh './gradlew --version'
                sh './gradlew dependencies --configuration releaseRuntimeClasspath --quiet'
            }
        }

        // ── Stage 3: Lint ─────────────────────────────────────────────────
        stage('🧹 Lint') {
            steps {
                sh './gradlew lint'
            }
            post {
                always {
                    androidLint pattern: '**/lint-results-*.xml',
                                healthy: '0', unhealthy: '10', thresholdLimit: 'low'
                }
            }
        }

        // ── Stage 4: Unit Tests ───────────────────────────────────────────
        stage('🧪 Unit Tests') {
            when {
                expression { params.RUN_TESTS == true }
            }
            steps {
                sh './gradlew test'
            }
            post {
                always {
                    junit '**/test-results/**/*.xml'
                    publishHTML(target: [
                        allowMissing: true,
                        reportDir: 'app/build/reports/tests/testDebugUnitTest',
                        reportFiles: 'index.html',
                        reportName: 'Unit Test Report'
                    ])
                }
            }
        }

        // ── Stage 5: Build ────────────────────────────────────────────────
        stage('🔨 Build') {
            steps {
                script {
                    if (params.BUILD_TYPE == 'release') {
                        withCredentials([
                            file(credentialsId: 'KEYSTORE_FILE', variable: 'KEYSTORE'),
                            string(credentialsId: 'KEY_ALIAS', variable: 'KEY_ALIAS'),
                            string(credentialsId: 'KEY_PASSWORD', variable: 'KEY_PASSWORD'),
                            string(credentialsId: 'STORE_PASSWORD', variable: 'STORE_PASSWORD')
                        ]) {
                            sh """
                                ./gradlew assembleRelease \
                                  -Pandroid.injected.signing.store.file=\$KEYSTORE \
                                  -Pandroid.injected.signing.store.password=\$STORE_PASSWORD \
                                  -Pandroid.injected.signing.key.alias=\$KEY_ALIAS \
                                  -Pandroid.injected.signing.key.password=\$KEY_PASSWORD
                            """
                        }
                    } else {
                        sh './gradlew assembleDebug'
                    }
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: 'app/build/outputs/apk/**/*.apk',
                                     fingerprint: true,
                                     allowEmptyArchive: false
                }
            }
        }

        // ── Stage 6: Deploy (tuỳ chọn) ───────────────────────────────────
        stage('🚀 Deploy to Firebase') {
            when {
                allOf {
                    expression { params.DEPLOY_TO_FIREBASE == true }
                    expression { params.BUILD_TYPE == 'release' }
                }
            }
            steps {
                withCredentials([string(credentialsId: 'FIREBASE_TOKEN', variable: 'FIREBASE_TOKEN')]) {
                    sh """
                        firebase appdistribution:distribute \
                          app/build/outputs/apk/release/app-release.apk \
                          --app \${FIREBASE_APP_ID} \
                          --token "\$FIREBASE_TOKEN" \
                          --groups "testers" \
                          --release-notes "Build #\${BUILD_NUMBER} — \${GIT_COMMIT_MSG}"
                    """
                }
            }
        }
    }

    // ── Post: thông báo sau khi pipeline kết thúc ────────────────────────────
    post {
        success {
            script { discordNotify('SUCCESS') }
        }
        failure {
            script { discordNotify('FAILURE') }
        }
        unstable {
            script { discordNotify('UNSTABLE') }
        }
        aborted {
            script { discordNotify('ABORTED') }
        }
        always {
            cleanWs()   // dọn workspace sau mỗi build
        }
    }
}

// ── Hàm gửi thông báo Discord ─────────────────────────────────────────────────
def discordNotify(String status) {
    def config = [
        'SUCCESS' : [color: 3066993,  emoji: '✅', label: 'Thành công'],
        'FAILURE' : [color: 15158332, emoji: '❌', label: 'Thất bại'],
        'UNSTABLE': [color: 16776960, emoji: '⚠️', label: 'Không ổn định'],
        'ABORTED' : [color: 9807270,  emoji: '🚫', label: 'Đã huỷ'],
    ]
    def c = config[status]

    def apkField = ''
    if (status == 'SUCCESS') {
        apkField = """,
        {
          "name": "📦 Artifact",
          "value": "[Tải APK](${env.BUILD_URL}artifact/app/build/outputs/apk/)",
          "inline": true
        }"""
    }

    def payload = """
    {
      "username": "Jenkins CI",
      "avatar_url": "https://www.jenkins.io/images/logos/jenkins/jenkins.png",
      "embeds": [{
        "title": "${c.emoji}  ${env.APP_NAME} — Build #${env.BUILD_NUMBER} ${c.label}",
        "color": ${c.color},
        "fields": [
          {
            "name": "📂 Dự án",
            "value": "${env.JOB_NAME}",
            "inline": true
          },
          {
            "name": "🌿 Branch",
            "value": "${env.GIT_BRANCH_NAME}",
            "inline": true
          },
          {
            "name": "📝 Commit",
            "value": "${env.GIT_COMMIT_MSG?.take(80) ?: 'N/A'}",
            "inline": false
          },
          {
            "name": "👤 Tác giả",
            "value": "${env.GIT_AUTHOR ?: 'N/A'}",
            "inline": true
          },
          {
            "name": "⏱ Thời gian",
            "value": "${currentBuild.durationString?.replace(' and counting', '') ?: 'N/A'}",
            "inline": true
          }
          ${apkField}
        ],
        "footer": {
          "text": "Jenkins CI • Build #${env.BUILD_NUMBER}",
          "icon_url": "https://www.jenkins.io/images/logos/jenkins/jenkins.png"
        },
        "timestamp": "${new Date().format("yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone('UTC'))}"
      }]
    }
    """.stripIndent()

    sh """
        curl -s -o /dev/null -w "%{http_code}" \\
          -H "Content-Type: application/json" \\
          -X POST \\
          -d '${payload.replace("'", "\\'")}' \\
          "\${DISCORD_WEBHOOK_URL}"
    """
}
```

---

## 6. Các Stage chi tiết

### 6.1 Sơ đồ Pipeline

```
[Push code]
     │
     ▼
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│  📥 Checkout │───▶│ 🔍 Validate │───▶│  🧹 Lint    │
└─────────────┘    └─────────────┘    └─────────────┘
                                              │
                          ┌───────────────────┘
                          ▼
                   ┌─────────────┐    ┌─────────────┐
                   │ 🧪 Tests    │───▶│  🔨 Build   │
                   └─────────────┘    └─────────────┘
                                             │
                                    ┌────────┴────────┐
                                    ▼                  ▼
                             ┌───────────┐    ┌──────────────┐
                             │ Archive   │    │ 🚀 Deploy    │
                             │   APK     │    │  (optional)  │
                             └───────────┘    └──────────────┘
                                    │
                                    ▼
                          ┌──────────────────┐
                          │ 📣 Discord Notify │
                          └──────────────────┘
```

### 6.2 Bảng màu Discord theo trạng thái

| Trạng thái | Màu HEX | Decimal | Ý nghĩa |
|------------|---------|---------|---------|
| SUCCESS | `#2ECC71` | 3066993 | Build pass hoàn toàn |
| FAILURE | `#E74C3C` | 15158332 | Build lỗi, không compile |
| UNSTABLE | `#F1C40F` | 16776960 | Build pass nhưng test fail |
| ABORTED | `#95A5A6` | 9807270 | Build bị huỷ thủ công |

---

## 7. Thông báo Discord nâng cao

### 7.1 Thông báo theo từng Stage (Stage-level notification)

Thêm vào bất kỳ stage nào để nhận ping khi stage đó fail:

```groovy
stage('🔨 Build') {
    steps {
        sh './gradlew assembleDebug'
    }
    post {
        failure {
            script {
                discordNotifyStage('🔨 Build', 'FAILURE',
                    'Lỗi compile — kiểm tra log bên dưới')
            }
        }
    }
}

// Thêm hàm helper vào cuối Jenkinsfile
def discordNotifyStage(String stageName, String status, String detail) {
    def color  = status == 'FAILURE' ? 15158332 : 3066993
    def emoji  = status == 'FAILURE' ? '❌' : '✅'
    sh """
        curl -s -X POST \\
          -H "Content-Type: application/json" \\
          -d '{
            "username": "Jenkins CI",
            "embeds": [{
              "title": "${emoji} Stage ${stageName} — ${status}",
              "description": "${detail}",
              "color": ${color},
              "fields": [
                {"name": "Job", "value": "${env.JOB_NAME}", "inline": true},
                {"name": "Build", "value": "#${env.BUILD_NUMBER}", "inline": true}
              ]
            }]
          }' \\
          "\${DISCORD_WEBHOOK_URL}"
    """
}
```

### 7.2 Thông báo khi có PR mới (GitHub Webhook)

```groovy
// Thêm vào phần environment
environment {
    PR_NUMBER = env.CHANGE_ID ?: ''
    PR_TITLE  = env.CHANGE_TITLE ?: ''
}

// Thêm stage riêng cho PR
stage('🔀 PR Check') {
    when { changeRequest() }
    steps {
        script {
            discordNotifyPR()
        }
    }
}

def discordNotifyPR() {
    sh """
        curl -s -X POST \\
          -H "Content-Type: application/json" \\
          -d '{
            "username": "Jenkins CI",
            "embeds": [{
              "title": "🔀 Pull Request #${env.PR_NUMBER} đang được kiểm tra",
              "description": "${env.PR_TITLE}",
              "color": 3447003,
              "fields": [
                {"name": "Branch", "value": "${env.GIT_BRANCH_NAME}", "inline": true},
                {"name": "Tác giả", "value": "${env.GIT_AUTHOR}", "inline": true},
                {"name": "🔗 Chi tiết", "value": "[Xem build](${env.BUILD_URL})", "inline": false}
              ]
            }]
          }' \\
          "\${DISCORD_WEBHOOK_URL}"
    """
}
```

### 7.3 Thông báo thời gian build bất thường

```groovy
post {
    always {
        script {
            // Cảnh báo nếu build mất hơn 20 phút
            def durationMin = currentBuild.duration / 1000 / 60
            if (durationMin > 20) {
                sh """
                    curl -s -X POST \\
                      -H "Content-Type: application/json" \\
                      -d '{
                        "username": "Jenkins CI",
                        "embeds": [{
                          "title": "⏰ Cảnh báo: Build quá lâu",
                          "description": "Build #${env.BUILD_NUMBER} mất **${durationMin.round(1)} phút** — bình thường ~10 phút",
                          "color": 16776960
                        }]
                      }' \\
                      "\${DISCORD_WEBHOOK_URL}"
                """
            }
        }
    }
}
```

---

## 8. Biến môi trường & Secrets

### 8.1 Danh sách Credentials cần tạo

| Credential ID | Kind | Nội dung | Dùng cho |
|---------------|------|----------|----------|
| `DISCORD_WEBHOOK_URL` | Secret text | URL Discord Webhook | Gửi thông báo |
| `KEYSTORE_FILE` | Secret file | File `.jks` / `.keystore` | Ký APK release |
| `KEY_ALIAS` | Secret text | Alias của key | Ký APK release |
| `KEY_PASSWORD` | Secret text | Mật khẩu key | Ký APK release |
| `STORE_PASSWORD` | Secret text | Mật khẩu keystore | Ký APK release |
| `FIREBASE_TOKEN` | Secret text | Token Firebase CLI | Deploy Firebase |
| `FIREBASE_APP_ID` | Secret text | App ID trên Firebase | Deploy Firebase |

### 8.2 Tạo Credentials qua giao diện

```
Manage Jenkins
  └── Credentials
       └── System
            └── Global credentials
                 └── Add Credentials
                      ├── Kind: Secret text  → paste value → Save
                      └── Kind: Secret file  → upload file → Save
```

### 8.3 Ký APK Release từ file keystore

Nếu chưa có keystore, tạo mới:

```bash
keytool -genkey -v \
  -keystore my-release-key.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias my-key-alias
```

Upload `my-release-key.jks` lên Jenkins Credentials với ID `KEYSTORE_FILE`.

---

## 9. Tích hợp vào dự án mới

### Checklist từng bước

```
□ Bước 1 — Copy Jenkinsfile vào thư mục gốc dự án
□ Bước 2 — Sửa APP_NAME trong environment
□ Bước 3 — Kiểm tra đường dẫn APK output (app/build/outputs/apk/...)
□ Bước 4 — Tạo Discord Webhook và lưu vào Jenkins Credentials
□ Bước 5 — Tạo Pipeline Job trong Jenkins
□ Bước 6 — Chạy build lần đầu và kiểm tra log
□ Bước 7 — Cấu hình GitHub/GitLab Webhook (tuỳ chọn)
```

### 9.1 Tạo Pipeline Job trong Jenkins

1. **New Item → Pipeline**
2. Đặt tên: `TenApp-CI`
3. **Pipeline → Definition:** `Pipeline script from SCM`
4. **SCM:** Git
5. **Repository URL:** `https://github.com/your-org/your-app.git`
6. **Credentials:** thêm credential SSH key hoặc username/password
7. **Branch:** `*/main` hoặc `*/develop`
8. **Script Path:** `Jenkinsfile`
9. Nhấn **Save**

### 9.2 Cấu hình GitHub Webhook (trigger tự động)

Trong GitHub repo → **Settings → Webhooks → Add webhook:**

```
Payload URL:    http://YOUR_JENKINS_HOST:8080/github-webhook/
Content type:   application/json
Events:         ✅ Pushes
                ✅ Pull requests
```

### 9.3 Tuỳ chỉnh đường dẫn APK output

Nếu dự án có nhiều module hoặc flavor, cập nhật phần `archiveArtifacts`:

```groovy
// Một module, không flavor
archiveArtifacts artifacts: 'app/build/outputs/apk/**/*.apk'

// Nhiều flavor (vd: free/pro)
archiveArtifacts artifacts: 'app/build/outputs/apk/free/**/*.apk,' +
                             'app/build/outputs/apk/pro/**/*.apk'

// Build Bundle (AAB) thay vì APK
archiveArtifacts artifacts: 'app/build/outputs/bundle/**/*.aab'
```

---

## 10. Troubleshooting

### Lỗi thường gặp

#### ❌ `ANDROID_HOME not set`
```bash
# Kiểm tra trong Jenkins → Manage Jenkins → System → Environment variables
# Thêm: ANDROID_HOME = /usr/local/android-sdk
# Restart Jenkins sau khi thay đổi
```

#### ❌ `Permission denied: ./gradlew`
```groovy
// Thêm vào đầu stage Validate
sh 'chmod +x gradlew'
```

#### ❌ Discord webhook trả về 400
```
Nguyên nhân: JSON payload có ký tự đặc biệt (', ", \n) trong commit message
Xử lý:
  - Dùng writeFile để ghi payload ra file rồi dùng -d @payload.json
  - Hoặc escape ký tự đặc biệt trong GIT_COMMIT_MSG
```

```groovy
// Cách an toàn hơn: ghi ra file
def discordNotify(String status) {
    def payload = [ /* ... */ ]
    writeFile file: 'discord_payload.json',
              text: groovy.json.JsonOutput.toJson(payload)
    sh """
        curl -s -X POST \\
          -H "Content-Type: application/json" \\
          -d @discord_payload.json \\
          "\${DISCORD_WEBHOOK_URL}"
    """
}
```

#### ❌ `SDK location not found`
```bash
# Tạo file local.properties trong workspace
echo "sdk.dir=/usr/local/android-sdk" > local.properties
```

Hoặc thêm vào Jenkinsfile:
```groovy
stage('🔍 Validate') {
    steps {
        sh 'echo "sdk.dir=$ANDROID_HOME" > local.properties'
        sh 'chmod +x gradlew'
    }
}
```

#### ❌ Build chạy được cục bộ nhưng fail trên Jenkins
```
Kiểm tra:
1. Phiên bản JDK khác nhau → đảm bảo dùng cùng JDK 17
2. File local.properties bị ignore trong .gitignore → cần tạo động (xem trên)
3. Thiếu accept Android SDK licenses:
   yes | sdkmanager --licenses
4. Gradle cache bị corrupted:
   sh './gradlew clean assembleDebug'
```

#### ❌ Webhook Discord gửi được nhưng không hiện embed

```
Kiểm tra:
- color phải là số nguyên (decimal), không phải hex string
- Đúng: "color": 3066993
- Sai:  "color": "#2ECC71"
- timestamp phải đúng format ISO 8601: "2024-01-01T00:00:00Z"
```

---

## Tài liệu tham khảo

- [Jenkins Pipeline Syntax](https://www.jenkins.io/doc/book/pipeline/syntax/)
- [Discord Webhook Guide](https://discord.com/developers/docs/resources/webhook)
- [Android Gradle Build](https://developer.android.com/build)
- [Firebase App Distribution CLI](https://firebase.google.com/docs/app-distribution/android/distribute-cli)
