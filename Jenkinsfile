pipeline {
    agent any

    environment {
        GRADLE_HOME = 'C:/workspace/gradle-8.12-bin/gradle-8.12'
        PATH = "$GRADLE_HOME/bin:$PATH"
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                script {
                    bat './gradlew clean test'
                }
            }
        }

        stage('Generate Allure Report') {
            steps {
                script {
                    // Genera el reporte de Allure
                    bat './gradlew allureReport'
                }
            }
        }

        stage('Publish Allure Report') {
            steps {
                allure includeProperties: false, jdk: '', results: [[path: 'resultsTests']]
            }
        }
    }

    post {
        always {
            // Publica los resultados de prueba aunque haya fallos
            junit 'build/test-results/test/*.xml'
        }
    }
}