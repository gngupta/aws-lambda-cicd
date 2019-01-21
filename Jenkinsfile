#!/usr/bin/groovy

podTemplate(label: 'jenkins-pipeline', containers: [
		containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:3.27-1-alpine', args: '${computer.jnlpmac} ${computer.name}', workingDir: '/home/jenkins', resourceRequestCpu: '200m', resourceLimitCpu: '300m', resourceRequestMemory: '256Mi', resourceLimitMemory: '512Mi'),
		containerTemplate(name: 'maven', image: 'cgswong/aws:latest', command: 'cat', ttyEnabled: true),
		containerTemplate(name: 'awscli', image: 'maven:3.5.2-jdk-8-alpine', command: 'cat', ttyEnabled: true)
	]) {

	node('jenkins-pipeline') {
		
		stage('Checkout') {
			checkout scm
		}

		stage('Build') {
			container('maven') {
				sh "mvn --version"
			}
		}

		stage('Push') {
			container('awscli') {
				sh "aws --version"
			}
		}

		stage('Deploy') {
			container('awscli') {
				sh "aws --version"
			}
		}
	}
}