pipeline {
    agent any

    // 1. Khai báo biến môi trường (Cực kỳ quan trọng trên Mac)
    environment {
        ANDROID_HOME = '/Users/nguyenquocchinh/Library/Android/sdk'
        PATH = "${env.ANDROID_HOME}/tools:${env.ANDROID_HOME}/platform-tools:${env.PATH}"
    }

    stages {
        // 2. Bước kiểm tra môi trường
        stage('Prepare') {
            steps {
                echo "Đang kiểm tra môi trường build..."
                sh 'python3 --version'
                sh 'java -version'
            }
        }

        // 3. Bước Build Backend (Python)
        stage('Build & Test Python') {
            steps {
                echo "Đang cài đặt thư viện Python..."
                // Giả sử bạn có file requirements.txt
                sh 'pip3 install -r requirements.txt'
                sh 'python3 -m compileall .'
            }
        }

        // 4. Bước Build Android (Kotlin)
        stage('Build Android APK') {
            steps {
                echo "Đang khởi tạo Gradle..."
                sh 'chmod +x gradlew' // Cấp quyền chạy cho file gradlew trên Mac
                sh './gradlew assembleDebug'
            }
        }
    }

    // 5. Tổng kết sau khi build xong
    post {
        success {
            echo 'Chúc mừng Chinh! Build thành công rực rỡ.'
        }
        failure {
            echo 'Build lỗi rồi, kiểm tra lại code trong Cursor nhé!'
        }
    }
}
