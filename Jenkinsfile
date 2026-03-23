pipeline {
    agent any

    environment {
        JAVA_HOME    = '/opt/homebrew/opt/openjdk@21'
        ANDROID_HOME = '/Users/nguyenquocchinh/Library/Android/sdk'
        PATH         = "/opt/homebrew/bin:${env.JAVA_HOME}/bin:${env.ANDROID_HOME}/tools:${env.ANDROID_HOME}/platform-tools:${env.PATH}"
    }

    stages {

        stage('Prepare') {
            steps {
                echo "Kiểm tra môi trường..."
                sh 'java -version'
                sh 'python3 --version'
                sh 'which jq || /opt/homebrew/bin/brew install jq'

                // Thông báo Discord: bắt đầu build
                sh '''
                    WEBHOOK="https://discord.com/api/webhooks/1485480399210549328/9OkimO32EOZqOW7W290QZwzT807KtixzxjksOsoFot4-QUCl_Kzc2YLvQEqSzZEU2_oa"
                    BRANCH="${BRANCH_NAME:-$(git rev-parse --abbrev-ref HEAD)}"
                    COMMIT=$(git log -1 --pretty=format:"%h - %s")
                    AUTHOR=$(git log -1 --pretty=format:"%an")
                    jq -n \
                        --arg username "$AUTHOR" \
                        --arg avatar "https://mirrors.tuna.tsinghua.edu.cn/jenkins/art/jenkins-logo/256x256/headshot.png" \
                        --arg title "🚀 Bắt đầu build - $BRANCH" \
                        --arg desc "Commit: $COMMIT\nBuild: #${BUILD_NUMBER}\nLog: ${BUILD_URL}console" \
                        '{username: $username, avatar_url: $avatar, embeds: [{title: $title, description: $desc, color: 3447003}]}' \
                    | curl -sS -H "Content-Type: application/json" -X POST -d @- "$WEBHOOK"
                '''
            }
        }

        stage('Build & Deploy') {
            steps {
                sh 'chmod +x .anhnn/build.sh'
                sh '.anhnn/build.sh'
            }
        }
    }

    post {
        success {
            echo 'Build hoàn tất! Kiểm tra Discord để xem APK và QR code.'
        }
        failure {
            echo 'Build thất bại! Kiểm tra log phía trên.'
        }
    }
}
