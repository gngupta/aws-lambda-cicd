def pipelineUtilTest() {
    println "Loaded PipelineUtil.groovy in current pipeline"
}

def kubectlTest() {
    // Test that kubectl can correctly communication with the Kubernetes API
    println "checking kubectl connnectivity to the API"
    sh "kubectl get pods --all-namespaces"
}

def helmTest() {
    println "checking client/server version"
    sh "helm version"
}

def helmLint(String chartDir) {
    // lint helm chart
    println "Running helm lint ${chartDir}"
    sh "helm lint ${chartDir}"

}

def helmConfig() {
    //setup helm connectivity to Kubernetes API and Tiller
    println "Initiliazing helm client"
    sh "helm init"
}


def helmDeploy(Map args) {
    def String release_overrides = args.set ? getHelmReleaseOverrides(args.set) : ""
    def String namespace = args.namespace ? args.namespace : args.name

    if (args.dryRun) {
        println "Running dry-run deployment"
        sh "helm upgrade --dry-run --install ${args.name} ${args.chartDir} " + (release_overrides ? "--set ${release_overrides}" : "") + " --namespace=${namespace}"
    } else {
        println "Running deployment"
        sh "helm dependency update ${args.chartDir}"
        sh "helm upgrade --install ${args.name} ${args.chartDir} " + (release_overrides ? "--set ${release_overrides}" : "") + " --namespace=${namespace}" + " --wait"
        echo "Application ${args.name} successfully deployed. Use helm status ${args.name} to check"
    }
}

def helmDelete(Map args) {
    println "Running helm delete ${args.name}"
    sh "helm delete ${args.name}"
}

def helmTest(Map args) {
    println "Running helm test"
    sh "helm test ${args.name} --cleanup"
}

def setGitEnvVars() {
    // create git envvars
    println "Setting env vars to tag container"
    sh "git rev-parse HEAD > git_commit_id.txt"
    try {
        env.GIT_COMMIT_ID = readFile('git_commit_id.txt').trim()
        env.GIT_SHA = env.GIT_COMMIT_ID.substring(0, 7)
    } catch (e) {
        error "${e}"
    }
    println "env.GIT_COMMIT_ID ==> ${env.GIT_COMMIT_ID}"

    sh "git config --get remote.origin.url> git_remote_origin_url.txt"
    try {
        env.GIT_REMOTE_URL = readFile('git_remote_origin_url.txt').trim()
    } catch (e) {
        error "${e}"
    }
    sh "rm -f git_commit_id.txt git_remote_origin_url.txt"
    println "env.GIT_REMOTE_URL ==> ${env.GIT_REMOTE_URL}"
}

def getImageTag() {
    def imageTag = "${env.BRANCH_NAME}-${env.BUILD_NUMBER}"
    println "Common image tag :: ${imageTag}"
    return imageTag
}

def getCommonBuildArgs() {
    def commonBuildArgs = " --build-arg VCS_REF=${env.GIT_SHA}"
    commonBuildArgs += " --build-arg VCS_URL=${env.GIT_REMOTE_URL}"
    commonBuildArgs += " --build-arg VCS_BRANCH=${env.BRANCH_NAME}"
    commonBuildArgs += " --build-arg BUILD_NUMBER=${env.BUILD_NUMBER}"
    commonBuildArgs += " --build-arg BUILD_DATE=`date -u +'%Y-%m-%dT%H:%M:%S%Z'`"
    println "Common build args :: ${commonBuildArgs}"
    return commonBuildArgs
}

def buildImage(Map args) {
    println "Build docker image :: ${args.imageName}:${args.imageTag}"
    sh "docker build . -t ${args.imageName}:${args.imageTag} -f ${args.dockerfile} ${args.buildArgs}"
}

def pushImage(Map args) {
    println "Push docker image :: ${args.imageName}:${args.imageTag}"
    withDockerRegistry([credentialsId: "docker_hub_creds", url: ""]) {
        sh "docker push ${args.imageName}:${args.imageTag}"
    }
}

@NonCPS
def getHelmReleaseOverrides(Map map = [ : ]) {
    def options = ""
    map.each { key, value ->
        options += "$key=$value,"
    }
    return options
}

def String getDomainName(String url) throws URISyntaxException {
    URI uri = new URI(url);
    String domain = uri.getHost();
    return domain.startsWith("www.") ? domain.substring(4) : domain;
}

def String getSubDomainName(String domain) {
    return domain.substring(domain.indexOf('.') + 1);
}

// Used to get the subdomain Jenkins is hosted on for new ingress resources.
def String getSubDomainNameFromURL(String url) {
    return getSubDomainName(getDomainName(url));
}

return this