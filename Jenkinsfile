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

        stage('Run Application') {
    steps {
        sh '''
        echo "Target contents:"
        ls -lh target

        JAR=$(find target -maxdepth 1 -name "*.jar" ! -name "*.original" | head -n 1)

        if [ -z "$JAR" ]; then
            echo "No executable JAR found!"
            exit 1
        fi

        echo "Found JAR: $JAR"

        pkill -f "$(basename "$JAR")" || true

        nohup java -jar "$JAR" > app.log 2>&1 &

        sleep 10

        echo "Application process:"
        ps -ef | grep "$(basename "$JAR")" | grep -v grep || true

        echo "Application log:"
        tail -20 app.log || true
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