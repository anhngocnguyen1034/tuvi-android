pipeline {
    agent any

    environment {
        // 1. Khai báo đường dẫn Java (Lấy bản OpenJDK 21 bạn vừa cài qua brew)
        JAVA_HOME = '/opt/homebrew/opt/openjdk@21'

        // 2. Khai báo Android SDK
        ANDROID_HOME = '/Users/nguyenquocchinh/Library/Android/sdk'

        // 3. Cập nhật PATH để Jenkins tìm thấy lệnh 'java' và 'gradle'
        PATH = "${env.JAVA_HOME}/bin:${env.ANDROID_HOME}/tools:${env.ANDROID_HOME}/platform-tools:${env.PATH}"
    }

    stages {
        // 2. Bước kiểm tra môi trường
        stage('Prepare') {
            steps {
                echo "Đang kiểm tra môi trường..."
                sh 'java -version'
                sh 'python3 --version'
            }
        }

        // 3. Bước Build Backend (Python) - chỉ chạy nếu có requirements.txt
        stage('Build & Test Python') {
            steps {
                echo "Đang kiểm tra Python backend..."
                sh '''
                    if [ -f requirements.txt ]; then
                        pip3 install -r requirements.txt
                        python3 -m compileall .
                    else
                        echo "Không tìm thấy requirements.txt, bỏ qua bước Python."
                    fi
                '''
            }
        }

        //4. Bước Build Android (Kotlin)
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
