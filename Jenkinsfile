pipeline {
    agent any

    environment {
        JAVA_HOME    = '/opt/homebrew/opt/openjdk@21'
        ANDROID_HOME = '/Users/nguyenquocchinh/Library/Android/sdk'
        PATH         = "${env.JAVA_HOME}/bin:${env.ANDROID_HOME}/tools:${env.ANDROID_HOME}/platform-tools:${env.PATH}"

        // ⚠️ Thay bằng Webhook URL Discord của bạn (Jenkins Credentials khuyên dùng hơn)
        DISCORD_WEBHOOK = credentials('discord-webhook-url')
    }

    stages {

        // ─────────────────────────────────────────────
        // STAGE 1: Kiểm tra môi trường
        // ─────────────────────────────────────────────
        stage('Prepare') {
            steps {
                echo "Đang kiểm tra môi trường..."
                sh 'java -version'
                sh 'python3 --version'
            }
        }

        // ─────────────────────────────────────────────
        // STAGE 2: Tự động tăng version theo nhánh
        //   develop  → 0.0.0.X
        //   testing  → 0.0.X.0
        //   release  → 0.X.0.0
        //   main     → X.0.0.0
        // ─────────────────────────────────────────────
        stage('Auto Version Tag') {
            steps {
                script {
                    def branch = env.BRANCH_NAME ?: sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
                    echo "Branch hiện tại: ${branch}"

                    // Lấy tag mới nhất theo pattern của từng nhánh
                    def newVersion = ''
                    if (branch == 'develop') {
                        newVersion = bumpVersion(branch, '0\\.0\\.0\\.\\d+', '0.0.0.0', 3)
                    } else if (branch == 'testing') {
                        newVersion = bumpVersion(branch, '0\\.0\\.\\d+\\.0', '0.0.0.0', 2)
                    } else if (branch == 'release') {
                        newVersion = bumpVersion(branch, '0\\.\\d+\\.0\\.0', '0.0.0.0', 1)
                    } else if (branch == 'main') {
                        newVersion = bumpVersion(branch, '\\d+\\.0\\.0\\.0', '0.0.0.0', 0)
                    } else {
                        newVersion = '0.0.0.0'
                        echo "Nhánh '${branch}' không có quy tắc version, dùng mặc định: ${newVersion}"
                    }

                    env.APP_VERSION = newVersion
                    echo "Version mới: ${env.APP_VERSION}"

                    // Tạo git tag
                    sh """
                        git config user.email "jenkins@ci.local"
                        git config user.name "Jenkins CI"
                        git tag -a "v${env.APP_VERSION}" -m "Build v${env.APP_VERSION} from ${branch}"
                        git push origin "v${env.APP_VERSION}" || echo "Push tag thất bại (có thể đã tồn tại)"
                    """
                }
            }
        }

        // ─────────────────────────────────────────────
        // STAGE 3: Python (bỏ qua nếu không có requirements.txt)
        // ─────────────────────────────────────────────
        stage('Build & Test Python') {
            steps {
                sh '''
                    if [ -f requirements.txt ]; then
                        echo "Cài đặt thư viện Python..."
                        pip3 install -r requirements.txt
                        python3 -m compileall .
                    else
                        echo "Không có requirements.txt, bỏ qua."
                    fi
                '''
            }
        }

        // ─────────────────────────────────────────────
        // STAGE 4: Build Android APK
        // ─────────────────────────────────────────────
        stage('Build Android APK') {
            steps {
                echo "Building APK v${env.APP_VERSION}..."
                sh 'chmod +x gradlew'
                sh './gradlew assembleDebug'
            }
        }

        // ─────────────────────────────────────────────
        // STAGE 5: Đổi tên APK theo version
        // ─────────────────────────────────────────────
        stage('Rename APK') {
            steps {
                script {
                    def apkSrc = 'app/build/outputs/apk/debug/app-debug.apk'
                    def apkDst = "app/build/outputs/apk/debug/TuVi-v${env.APP_VERSION}.apk"
                    sh "cp ${apkSrc} ${apkDst}"
                    env.APK_PATH = apkDst
                    echo "APK: ${env.APK_PATH}"
                }
            }
        }
    }

    // ─────────────────────────────────────────────
    // POST: Thông báo Discord + upload APK
    // ─────────────────────────────────────────────
    post {
        success {
            script {
                def branch  = env.BRANCH_NAME ?: sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
                def version = env.APP_VERSION ?: 'unknown'
                def apkPath = env.APK_PATH ?: 'app/build/outputs/apk/debug/app-debug.apk'
                def commit  = sh(script: 'git log -1 --pretty=format:"%h - %s"', returnStdout: true).trim()

                // Ghi JSON ra file tạm để tránh lỗi escape
                writeFile file: '/tmp/discord_success.json', text: groovy.json.JsonOutput.toJson([
                    content: "✅ **Build thành công!**\n🌿 Branch  : `${branch}`\n🏷️ Version : `v${version}`\n📝 Commit  : `${commit}`\n🔢 Build   : #${env.BUILD_NUMBER}"
                ])
                sh 'curl -s -X POST "$DISCORD_WEBHOOK" -H "Content-Type: application/json" --data-binary @/tmp/discord_success.json'

                // Upload APK
                sh "curl -s -X POST \"\$DISCORD_WEBHOOK\" -F 'content=📦 APK v${version}' -F 'file=@${apkPath}'"
            }
        }

        failure {
            script {
                def branch = env.BRANCH_NAME ?: sh(script: 'git rev-parse --abbrev-ref HEAD', returnStdout: true).trim()
                def commit = sh(script: 'git log -1 --pretty=format:"%h - %s"', returnStdout: true).trim()

                writeFile file: '/tmp/discord_failure.json', text: groovy.json.JsonOutput.toJson([
                    content: "❌ **Build THẤT BẠI!**\n🌿 Branch  : `${branch}`\n📝 Commit  : `${commit}`\n🔢 Build   : #${env.BUILD_NUMBER}\n🔗 Log     : ${env.BUILD_URL}"
                ])
                sh 'curl -s -X POST "$DISCORD_WEBHOOK" -H "Content-Type: application/json" --data-binary @/tmp/discord_failure.json'
            }
        }
    }
}

// ─────────────────────────────────────────────
// HÀM HELPER: Tìm tag mới nhất và tăng số
// pos: vị trí cần tăng (0=major, 1=minor, 2=patch, 3=build)
// ─────────────────────────────────────────────
def bumpVersion(branch, pattern, defaultVer, int pos) {
    def latestTag = sh(
        script: "git tag --list | grep -E '^v?${pattern}\$' | sort -V | tail -1 || true",
        returnStdout: true
    ).trim().replaceAll('^v', '')

    if (!latestTag) {
        // Chưa có tag nào, tạo tag đầu tiên
        def parts = defaultVer.split('\\.')
        parts[pos] = '1'
        return parts.join('.')
    }

    def parts = latestTag.split('\\.').collect { it.toInteger() }
    // Reset các vị trí sau pos về 0, tăng pos lên 1
    for (int i = pos + 1; i < parts.size(); i++) { parts[i] = 0 }
    parts[pos] = parts[pos] + 1
    return parts.join('.')
}
