pipeline {
    agent any

    tools {
        maven 'maven'
    }

    stages {
        stage('build stage') {
            steps {
                sh 'mvn clean package'
            }
            post {
                success {
                    echo "build success"
                }
                failure {
                    echo "build failure"
                }
            }
        }
        stage('build test') {
            steps {
                sh 'mvn test'
            }
            post {
                success {
                    echo "test success"
                }
                failure {
                    echo "test failure"
                }
            }
        }
        stage("Run the spring application") {
            steps { 
                sh '''
                    echo "Stopping existing Spring Boot application if running..."
                    if pgrep -f voting_system-0.0.1-SNAPSHOT.jar > /dev/null; then
                        sudo pkill -f voting_system-0.0.1-SNAPSHOT.jar
                        echo "Application stopped."
                    else
                        echo "No existing application running."
                    fi

                    echo "Starting the Spring Boot application..."
                    sudo java -jar target/voting_sytem-0.0.1-SNAPSHOT.jar > /dev/null 2>&1 &
                '''
            }
        }
    }
    post {
        success {
            echo "pipeline success"
        }
        failure {
            echo "pipeline failure"
        }
    }
}