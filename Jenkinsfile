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
		println "rootDir :: ${rootDir}"

		// Read required jenkins workflow configuration values.
		def inputFile = readFile('Jenkinsfile.json')
		def config = new groovy.json.JsonSlurperClassic().parseText(inputFile)
		println "Pipeline config ==> ${config}"

		if (!config.pipeline.enabled) {
			println "Pipeline is not enabled in Jenkinsfile.json"
			return;
		}

		def artifactName = "${config.lambda.name}-${env.BUILD_NUMBER}"
		stage('Build') {
			container('maven') {
				sh "mvn clean package -DartifactName=${artifactName} --batch-mode"
			}
		}

		def fileLocation = "${rootDir}/target/${artifactName}.jar"
		stage('Push') {
			container('aws') {
				withAWS(credentials: config.lambda.credentialId) {
					s3Upload(file: fileLocation, bucket: config.lambda.s3Bucket, path: '/' + env.BRANCH_NAME +'/')
				}
			}
		}

		stage('Deploy') {
			container('aws') {
				withAWS(credentials: config.lambda.credentialId) {
					sh "aws lambda update-function-code --function-name ${config.lambda.name} --s3-bucket ${config.lambda.s3Bucket}/${env.BRANCH_NAME} --s3-key ${artifactName}.jar --region ${config.lambda.region}"
				}
			}
		}

		def lambdaAlias = config.lambda.alias.feature
		if(config.lambda.alias[env.BRANCH_NAME]) {
			lambdaAlias = config.lambda.alias[env.BRANCH_NAME]
		}

		stage('Publish') {
			container('aws') {
				withAWS(credentials: config.lambda.credentialId) {
					def lambdaVersion = sh (
						script: "aws lambda publish-version --function-name ${config.lambda.name} --region ${config.lambda.region} | jq -r '.Version'",
						returnStdout: true
					)
					sh "aws lambda update-alias --function-name ${config.lambda.name} --name ${lambdaAlias} --region ${config.lambda.region} --function-version ${lambdaVersion}"
				}
			}
		}
	}
}