#!/usr/bin/groovy

def getCommitId() {
	sh 'git rev-parse --short HEAD > .git/commitId'
	def commitId = readFile('.git/commitId').trim()
		sh 'rm .git/commitId'
		return commitId
}

podTemplate(label: 'jenkins-pipeline', containers: [
		containerTemplate(name: 'jnlp', image: 'jenkins/jnlp-slave:3.27-1-alpine', args: '${computer.jnlpmac} ${computer.name}', workingDir: '/home/jenkins', resourceRequestCpu: '200m', resourceLimitCpu: '300m', resourceRequestMemory: '256Mi', resourceLimitMemory: '512Mi'),
		containerTemplate(name: 'maven', image: 'maven:3.5.2-jdk-8-alpine', command: 'cat', ttyEnabled: true),
		containerTemplate(name: 'aws', image: 'mikesir87/aws-cli:1.16.91', command: 'cat', ttyEnabled: true)
	]) {

	node('jenkins-pipeline') {

		stage('Checkout') {
			checkout scm
		}

		def rootDir = pwd()
		def commitId = getCommitId()
		println "rootDir :: ${rootDir} commitId :: ${commitId}"

		// Read required jenkins workflow configuration values
		def pipelineUtil = load "${rootDir}/PipelineUtil.groovy"
		def inputFile = readFile('Jenkinsfile.json')
		def config = new groovy.json.JsonSlurperClassic().parseText(inputFile)
		println "Pipeline config ==> ${config}"

		if(!config.pipeline.enabled){
			println "Pipeline is not enabled in Jenkinsfile.json"
			return;
		}

		def artifactName = "${config.lambdaConfigs.name}-${commitId}"

		stage('Build') {
			container('maven') {
				sh "mvn --version"
				sh "mvn clean package -DartifactName=${artifactName} --batch-mode"
			}
		}

		stage('Push') {
			container('aws') {
				withAWS(credentials:'aws-lambda-cicd') {
					s3Upload(file: artifactName + '.jar', bucket: config.lambdaConfigs.s3Bucket', path: '')
				}
			}
		}
		stage('Deploy') {
			container('aws') {
				sh "aws --version"
			}
		}
	}
}