pipeline {
    agent any

    environment {
        JAVA_HOME    = '/opt/homebrew/opt/openjdk@21'
        ANDROID_HOME = '/Users/nguyenquocchinh/Library/Android/sdk'
        PATH         = "${env.JAVA_HOME}/bin:${env.ANDROID_HOME}/tools:${env.ANDROID_HOME}/platform-tools:${env.PATH}"
    }

    stages {

        stage('Prepare') {
            steps {
                echo "Kiểm tra môi trường..."
                sh 'java -version'
                sh 'python3 --version'
                sh 'which jq || brew install jq'
            }
        }

        stage('Build & Deploy') {
            steps {
                sh 'chmod +x .taymay/build.sh'
                sh '.taymay/build.sh'
            }
        }
    }

    post {
        failure {
            sh '''
                current_branch="${BRANCH_NAME:-$(git rev-parse --abbrev-ref HEAD)}"
                commit=$(git log -1 --pretty=format:"%h - %s")
                DISCORD_TESTING="https://discord.com/api/webhooks/1485480399210549328/9OkimO32EOZqOW7W290QZwzT807KtixzxjksOsoFot4-QUCl_Kzc2YLvQEqSzZEU2_oa"
                DISCORD_PRODUCTION="https://discord.com/api/webhooks/1485480399210549328/9OkimO32EOZqOW7W290QZwzT807KtixzxjksOsoFot4-QUCl_Kzc2YLvQEqSzZEU2_oa"

                WEBHOOK="$DISCORD_TESTING"
                if [ "$current_branch" == "main" ] || [ "$current_branch" == "release" ]; then
                    WEBHOOK="$DISCORD_PRODUCTION"
                fi

                jq -n \
                    --arg username "Jenkins" \
                    --arg title "❌ Build THẤT BẠI - $current_branch" \
                    --arg desc "Branch: \`$current_branch\`\\nCommit: \`$commit\`\\nBuild: [#${BUILD_NUMBER}](${BUILD_URL})" \
                    '{username: $username, avatar_url: "https://mirrors.tuna.tsinghua.edu.cn/jenkins/art/jenkins-logo/256x256/headshot.png", embeds: [{title: $title, description: $desc, color: 15158332}]}' \
                | curl -sS -H 'Content-Type: application/json' -X POST -d @- "$WEBHOOK"
            '''
        }
    }
}
