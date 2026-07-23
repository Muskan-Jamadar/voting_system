pipeline {

    agent any

    parameters {
        choice(
            name: 'ACTION',
            choices: ['DEPLOY', 'REMOVE'],
            description: 'Choose whether to deploy or remove application'
        )
    }

    tools {
        maven 'maven'
    }

    environment {
        APP_NAME = "voting-app"
        DOCKER_IMAGE = "jamadar21/online-voting-system"
    }


    stages {


        stage('Checkout Code') {

            when {
                expression { params.ACTION == 'DEPLOY' }
            }

            steps {
                echo "Pulling latest code from GitHub..."

                git branch: 'main',
                url: 'https://github.com/Muskan-Jamadar/voting_system.git'
            }
        }


        stage('Build JAR') {

            when {
                expression { params.ACTION == 'DEPLOY' }
            }

            steps {

                echo "Building Spring Boot Application..."

                sh 'mvn clean package -DskipTests'
            }
        }


        stage('Docker Build') {

            when {
                expression { params.ACTION == 'DEPLOY' }
            }

            steps {

                echo "Building Docker Image..."

                sh '''
                docker build -t $DOCKER_IMAGE:latest .
                '''
            }
        }


        stage('Docker Login & Push') {

            when {
                expression { params.ACTION == 'DEPLOY' }
            }

            steps {

                echo "Pushing Image to Docker Hub..."

                withCredentials([
                    usernamePassword(
                    credentialsId: 'docker-credentials',
                    usernameVariable: 'DOCKER_USERNAME',
                    passwordVariable: 'DOCKER_PASSWORD'
                    )
                ]) {

                    sh '''
                    docker login -u $DOCKER_USERNAME -p $DOCKER_PASSWORD
                    docker push $DOCKER_IMAGE:latest
                    '''
                }
            }
        }


        stage('Deploy Application') {

            when {
                expression { params.ACTION == 'DEPLOY' }
            }

            steps {

                echo "Starting Application using Docker Compose..."

                sh '''
                docker-compose down
                docker-compose up --build -d
                '''
            }
        }


        stage('Remove Application') {

            when {
                expression { params.ACTION == 'REMOVE' }
            }

            steps {

                echo "Removing Application Containers..."

                sh '''
                docker-compose down
                docker image prune -af
                '''
            }
        }

    }


    post {

        success {
            echo "Pipeline executed successfully..."
        }

        failure {
            echo "Pipeline execution failed..."
        }

        always {
            echo "Pipeline completed..."
        }
    }
}