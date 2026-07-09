pipeline {
    agent any

    tools {
        jdk 'jdk21'      // Name configured in Jenkins
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
        echo "Checking target directory..."
        ls -lh target

        echo "Stopping old application..."
        pkill -f demo-0.0.1-SNAPSHOT.jar || true

        echo "Starting application..."
        nohup java -jar target/demo-0.0.1-SNAPSHOT.jar > app.log 2>&1 &

        sleep 10

        echo "Application status:"
        ps -ef | grep demo-0.0.1-SNAPSHOT.jar | grep -v grep
        '''
    }
}
        