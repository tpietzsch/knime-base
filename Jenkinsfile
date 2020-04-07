#!groovy
def BN = BRANCH_NAME == "master" || BRANCH_NAME.startsWith("releases/") ? BRANCH_NAME : "master"

library "knime-pipeline@$BN"

properties([
	// provide a list of upstream jobs which should trigger a rebuild of this job
	pipelineTriggers([
		upstream('knime-core/' + env.BRANCH_NAME.replaceAll('/', '%2F')),
		upstream('knime-shared/' + env.BRANCH_NAME.replaceAll('/', '%2F')),
		upstream('knime-expressions/' + env.BRANCH_NAME.replaceAll('/', '%2F')),
	]),
	buildDiscarder(logRotator(numToKeepStr: '5')),
	disableConcurrentBuilds()
])

try {
	knimetools.defaultTychoBuild('org.knime.update.base')

	runIntegratedTests()	

 	workflowTests.runTests(
        dependencies: [
            repositories: ['knime-base', 'knime-shared', 'knime-python',
            'knime-datageneration', 'knime-database', 'knime-timeseries',
            'knime-jep', 'knime-js-base', 'knime-optimization', 'knime-xml',
            'knime-ensembles', 'knime-distance'],
            // ius: ['org.knime.json.tests']
        ],
        withAssertions: true,
        // configurations: testConfigurations
    )

 	stage('Sonarqube analysis') {
 		env.lastStage = env.STAGE_NAME
 		workflowTests.runSonar()
 	}
 } catch (ex) {
	 currentBuild.result = 'FAILED'
	 throw ex
 } finally {
	 notifications.notifyBuild(currentBuild.result);
 }

def runIntegratedTests(){
    node('maven'){ 
        stage('Integrated Tests'){
            env.lastStage = env.STAGE_NAME
            checkout scm
            withMavenJarsignerCredentials(options: [artifactsPublisher(disabled: true)]) {
                withCredentials([usernamePassword(credentialsId: 'ARTIFACTORY_CREDENTIALS', passwordVariable: 'ARTIFACTORY_PASSWORD', usernameVariable: 'ARTIFACTORY_LOGIN')]) {
                    sh '''
                        export TEMP="${WORKSPACE}/tmp"
                        mkdir "${TEMP}"
                        
                        XVFB=$(which Xvfb) || true
                        if [[ -x "$XVFB" ]]; then
                            Xvfb :$$ -pixdepths 24 -screen 0 1280x1024x24 +extension RANDR &
                            XVFB_PID=$!
                            export DISPLAY=:$$
                        fi

                        mvn -e -X -Dmaven.test.failure.ignore=true -Dknime.p2.repo=${P2_REPO} clean verify -P test
                        if [[ -n "$XVFB_PID" ]]; then
                            kill $XVFB_PID
                        fi
                    '''
                }
            }
            junit allowEmptyResults: true, testResults: '**/target/surefire-reports/TEST-*.xml'
        }
    }
}
/* vim: set ts=4: */
