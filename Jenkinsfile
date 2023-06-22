pipeline {
    agent { label 'uptycs' }
    stages  {
        stage('Maven Build') {
                docker {
                    image 'maven:3.6.3-jdk-11'
                }
            steps {
                sh 'mvn clean package -DskipTests'
                sh 'cp gateway-ha/target/gateway-ha-1.9.5-jar-with-dependencies.jar docker/gateway-ha-1.9.5-jar-with-dependencies.jar'
                stash includes: 'docker/*', name: 'mavenbuild'
            }
        }

        stage('Docker Image Build') {
            steps {
                sh '$(aws ecr get-login --registry-ids 267292272963 --region us-east-1 --no-include-email)'
                unstash 'mavenbuild'
                script{
                    prestoImage = docker.build("uptycs/presto-gateway:v1.0.0", "--build-arg VERSION=v1.0.0 ./docker/")
                    docker.withRegistry('https://267292272963.dkr.ecr.us-east-1.amazonaws.com', 'ecr:us-east-1:uptycs-shared-jenkins' ) {
                        prestoImage.push()
                        prestoImage.push('v1.0.0')
                    }
                }
            }
        }
    }
}
