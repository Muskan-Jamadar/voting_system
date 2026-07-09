pipeline {
    agent any

    tools {
        jdk 'jdk21'
        maven 'maven'
    }

    stages {

        stage('Build') {
            steps {
                sh '''
                echo "===== JAVA ====="
                java -version
                javac -version

                echo "===== MAVEN ====="
                mvn -version

                echo "===== JAVA_HOME ====="
                echo $JAVA_HOME

                mvn clean package
                '''
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('Check Target') {
            steps {
                sh '''
                echo "Contents of target directory:"
                ls -lh target
                '''
            }
        }

    stage("Run Spring Boot Application") {
    steps {
        sh '''
            echo "Stopping existing application..."
            pkill -f "demo-0.0.1-SNAPSHOT.jar" || true

            echo "Starting Spring Boot application..."

            BUILD_ID=dontKillMe \
            nohup $JAVA_HOME/bin/java -jar target/demo-0.0.1-SNAPSHOT.jar \
            > app.log 2>&1 &

            sleep 10

            ps -ef | grep demo | grep -v grep || true
            ss -tulnp | grep 8083 || true
        '''
    }
}
    

    post {
        success {
            echo "Pipeline Success"
        }
        failure {
            echo "Pipeline Failed"
        }
    }
}