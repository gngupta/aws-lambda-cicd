#!/usr/bin/groovy

podTemplate(label: 'jenkins-pipeline', containers: [
		containerTemplate(name: 'awscli', image: 'brentley/awscli:latest', command: 'cat', ttyEnabled: true)
	]) {

	node('jenkins-pipeline') {
		stage('Checkout') {
			checkout scm
		}
	}
}