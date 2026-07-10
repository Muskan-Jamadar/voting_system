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
                echo "PATH=$PATH"
                echo "JAVA_HOME=$JAVA_HOME"
                echo "MAVEN_HOME=$MAVEN_HOME"

                which java
                java -version

                which mvn
                mvn -version

                pwd
                ls -la

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

        stage('Run Spring Boot Application') {
    steps {
        sh '''
        pkill -f "demo-0.0.1-SNAPSHOT.jar" || true

        nohup java -jar target/demo-0.0.1-SNAPSHOT.jar > app.log 2>&1 < /dev/null &

        sleep 15

        ps -ef | grep demo | grep -v grep
        ss -tulnp | grep 8083
        '''
    }
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