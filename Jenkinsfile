pipeline {
    agent any

    environment {
        JAVA_HOME               = '/opt/homebrew/opt/openjdk@21'
        ANDROID_HOME            = '/Users/nguyenquocchinh/Library/Android/sdk'
        PATH                    = "/opt/homebrew/bin:${env.JAVA_HOME}/bin:${env.ANDROID_HOME}/tools:${env.ANDROID_HOME}/platform-tools:${env.PATH}"

        // Cấu hình các kênh Discord — lấy từ Jenkins Credentials (Secret text)
        // 3 credential dạng "Secret text" trên Jenkins với ID tương ứng:
        //   discord-webhook-url (kênh GitHub) / discord-webhook-jenkins / discord-webhook-success
        WEBHOOK_GITHUB          = credentials('discord-webhook-url')
        WEBHOOK_JENKINS         = credentials('discord-webhook-jenkins')
        WEBHOOK_SUCCESS         = credentials('discord-webhook-success')
    }

    stages {

        stage('Prepare') {
            steps {
                echo "Kiểm tra môi trường..."
                sh 'java -version'
                sh 'python3 --version'
                sh 'which jq || /opt/homebrew/bin/brew install jq'

                // 1. Thông báo GitHub room: Repo updates (Commit & Comment)
                sh '''
                    BRANCH="${BRANCH_NAME:-$(git rev-parse --abbrev-ref HEAD)}"
                    COMMIT_HASH=$(git log -1 --pretty=format:"%h")
                    COMMIT_MSG=$(git log -1 --pretty=format:"%s")
                    AUTHOR=$(git log -1 --pretty=format:"%an")

                    jq -n \
                        --arg username "GitHub - $AUTHOR" \
                        --arg title "📌 New Push to $BRANCH" \
                        --arg desc "[$COMMIT_HASH] $COMMIT_MSG" \
                        '{username: $username, embeds: [{title: $title, description: $desc, color: 1752220}]}' \
                    | curl -sS -H "Content-Type: application/json" -X POST -d @- "$WEBHOOK_GITHUB"
                '''

                // 2. Thông báo Jenkins room: Bắt đầu build
                sh '''
                    BRANCH="${BRANCH_NAME:-$(git rev-parse --abbrev-ref HEAD)}"
                    jq -n \
                        --arg title "🚀 Bắt đầu build - $BRANCH" \
                        --arg desc "Build: #${BUILD_NUMBER}\nLog: ${BUILD_URL}console" \
                        '{username: "Jenkins CI", embeds: [{title: $title, description: $desc, color: 3447003}]}' \
                    | curl -sS -H "Content-Type: application/json" -X POST -d @- "$WEBHOOK_JENKINS"
                '''
            }
        }

        stage('Build & Deploy') {
            steps {
                sh 'chmod +x .anhnn/build.sh'
                // Truyền webhook Success sang script build
                withEnv(["WEBHOOK_SUCCESS=${env.WEBHOOK_SUCCESS}", "WEBHOOK_JENKINS=${env.WEBHOOK_JENKINS}"]) {
                    sh '.anhnn/build.sh'
                }
            }
        }
    }

    post {
        success {
            node('') {
                sh '''
                    jq -n \
                        --arg title "✅ Build HOÀN TẤT - ${BRANCH_NAME}" \
                        --arg desc "Build: #${BUILD_NUMBER} thành công!\nKiểm tra kênh Success để lấy QR." \
                        '{username: "Jenkins CI", embeds: [{title: $title, description: $desc, color: 3066993}]}' \
                    | curl -sS -H "Content-Type: application/json" -X POST -d @- "$WEBHOOK_JENKINS"
                '''
            }
        }
        failure {
            node('') {
                sh '''
                    jq -n \
                        --arg title "❌ Build THẤT BẠI - ${BRANCH_NAME}" \
                        --arg desc "Build: #${BUILD_NUMBER} đã gặp lỗi.\nLog: ${BUILD_URL}console" \
                        '{username: "Jenkins CI", embeds: [{title: $title, description: $desc, color: 15158332}]}' \
                    | curl -sS -H "Content-Type: application/json" -X POST -d @- "$WEBHOOK_JENKINS"
                '''
            }
        }
        always {
            // Dọn workspace sau mỗi lần build (tham khảo cách build chuẩn từ big-font)
            node('') {
                cleanWs()
            }
        }
    }
}
