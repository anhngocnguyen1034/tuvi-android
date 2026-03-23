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
        success {
            echo 'Build hoàn tất! Kiểm tra Discord để xem APK và QR code.'
        }
        failure {
            echo 'Build thất bại! Kiểm tra log phía trên.'
        }
    }
}
