pipeline {
    agent any

    tools {
        jdk 'jdk21'      // Name configured in Jenkins
        maven 'maven'
    }

    stages {
        stage('Build') {
            steps {
                sh 'java -version'
                sh 'mvn -version'
                sh 'mvn clean package'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('Run Application') {
            steps {
                sh '''
                if pgrep -f voting_system-0.0.1-SNAPSHOT.jar; then
                    sudo pkill -f voting_system-0.0.1-SNAPSHOT.jar
                fi

                sudo java -jar target/voting_system-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
                '''
            }
        }
    }
}