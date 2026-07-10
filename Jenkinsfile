pipeline {
    agent any

    tools {
        jdk 'jdk21'
        maven 'maven'
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main',
                    credentialsId: 'github-creds',
                    url: 'https://github.com/Muskan-Jamadar/voting_system.git'
            }
        }

        stage('Build') {
            steps {
                sh '''
                echo "===== JAVA ====="
                echo "JAVA_HOME=$JAVA_HOME"
                java -version
                javac -version

                echo "===== MAVEN ====="
                echo "MAVEN_HOME=$MAVEN_HOME"
                mvn -version

                echo "===== WORKSPACE ====="
                pwd
                ls -la

                mvn clean package
                '''
            }
        }

        stage('Check Target') {
            steps {
                sh '''
                echo "===== TARGET DIRECTORY ====="
                ls -lh target
                '''
            }
        }

        stage('Run Spring Boot Application') {
            steps {
                sh '''
                echo "Stopping old application..."
                pkill -f "demo-0.0.1-SNAPSHOT.jar" || true

                echo "Starting application..."
                BUILD_ID=dontKillMe nohup java -jar target/demo-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

                sleep 20

                echo "===== Running Process ====="
                ps -ef | grep demo | grep -v grep || true

                echo "===== Port Status ====="
                ss -tulnp | grep 8083 || true

                echo "===== Application Log ====="
                tail -50 app.log || true
                '''
            }
        }
    }

    post {
        success {
            echo "Pipeline executed successfully."
        }

        failure {
            echo "Pipeline failed."
        }

        always {
            echo "Build completed."
        }
    }
}