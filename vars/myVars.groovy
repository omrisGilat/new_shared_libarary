// src/devops/common/utils.groovy
package devops.common;

// imports
import com.cloudbees.groovy.cps.NonCPS
import groovy.json.JsonOutput
import hudson.FilePath
import jenkins.model.Jenkins

// // checks input value for default value use if not set
// def default_input(input, default_value) {
//   return input == null ? default_value : input
// }

// // removes file
// @NonCPS
// def remove_file(String file) {
//   // delete a file off of the master
//   if (env['NODE_NAME'].equals('master')) {
//     new File(file).delete()
//   }
//   // delete a file off of the build node
//   else {
//     new FilePath(Jenkins.getInstance().getComputer(env['NODE_NAME']).getChannel(), file).delete();
//   }
// }

// // downloads file
// @NonCPS
// def download_file(String url, String dest) {
//   def file = null
//   // establish the file download for the master
//   if (env['NODE_NAME'].equals('master')) {
//     file = new File(dest).newOutputStream()
//   }
//   // establish the file download for the build node
//   else {
//     file = new FilePath(Jenkins.getInstance().getComputer(env['NODE_NAME']).getChannel(), dest).newOutputStream();
//   }
//   // download the file and close the ostream
//   file << new URL(url).openStream()
//   file.close()
// }

// // converts content map to json string
// def map_to_json(Map content) {
//   return JsonOutput.toJson(content)
// }

// def buildDescription	// for setting the build name, based on the downstream jobs name
//omri
def lastSuccessfullBuild(build) {
	if (build != null) {
			return build.displayName
	} else {
		return '0.0.0.0'
	}
}

def autoIncBuildNightlyNumber(mybuild) {
	
    def last_successfull_build = lastSuccessfullBuild(mybuild);
	if (params.Debug == true) println 'last successfull build: ' + last_successfull_build
	def tokens = last_successfull_build.tokenize('.')
	if (params.Debug == true) println 'tokens: ' + tokens
	// update global variable
	if (tokens[3] == null){ //means the last successful build got a very bad number(somehow)
	    return autoIncBuildNightlyNumber(mybuild.getPreviousBuild())
	    //tokens = '0.0.0.0'.tokenize('.')
	}
	build_number = tokens[3].toInteger() + 1
	if (params.Debug == true) println 'new build number: ' + build_number

	return build_number
}


@NonCPS
def commitInfo(idx, commit) {
	// convert EOL to HTML formating
	String newMsg = commit.msg.replaceAll("(\r\n|\n)", "<br />")
	return commit != null ? "${idx}. [CS:${commit.getVersion()}@${commit.getRepoName()}] by ${commit.getAuthor()}<br />${newMsg}<br /><br />" : ""
}

@NonCPS
def getChangeString() {
	/*
	The ChangeLogSets format can be viewed in the following URL (JSON format)
	http://ci-jenkins:8080/job/<job name>/api/json?pretty=true&tree=builds[changeSets[items[*]]]
	*/
	String changeString = ""

	echo "Gathering SCM changes..."
	def changeLogSets = currentBuild.changeSets

	for (int i = 0; i < changeLogSets.size(); i++) {
		def entries = changeLogSets[i].items

		for (int j = 0; j < entries.length; j++) {
			def entry = entries[j]
			// pay attention that getVersion can run only w/o groovy sandbox
			changeString += "${commitInfo(j+1, entry)}"
		}
	}

	if (!changeString) {
		changeString = "No new changes since last build."
	}

	return changeString
}

def testMethod(){
echo "test Passed"
}

@NonCPS
def sendNotification(user) {

	emailext (
		mimeType: 'text/html',
		to: "${user}@gilat.com",
		subject: '$PROJECT_NAME - $BUILD_DISPLAY_NAME - $BUILD_STATUS!',
		body: '''
			<p>$BUILD_STATUS - $PROJECT_NAME [$BUILD_DISPLAY_NAME]:</p>
			<p>Check console output at <a href="$BUILD_URL">$PROJECT_NAME [$BUILD_DISPLAY_NAME]</a> to view the results.</p>
			''' + getChangeString()
	)
}

@NonCPS
def commitInfo(idx, commit) {
	// convert EOL to HTML formating
	String newMsg = commit.msg.replaceAll("(\r\n|\n)", "<br />")
	return commit != null ? "${idx}. [CS:${commit.getVersion()}@${commit.getRepoName()}] by ${commit.getAuthor()}<br />${newMsg}<br /><br />" : ""
}

@NonCPS
def getChangeString() {
	/*
	The ChangeLogSets format can be viewed in the following URL (JSON format)
	http://ci-jenkins:8080/job/<job name>/api/json?pretty=true&tree=builds[changeSets[items[*]]]
	*/
	String changeString = ""

	echo "Gathering SCM changes..."
	def changeLogSets = currentBuild.changeSets

	for (int i = 0; i < changeLogSets.size(); i++) {
		def entries = changeLogSets[i].items

		for (int j = 0; j < entries.length; j++) {
			def entry = entries[j]
			// pay attention that getVersion can run only w/o groovy sandbox
			changeString += "${commitInfo(j+1, entry)}"
		}
	}

	if (!changeString) {
		changeString = "No new changes since last build."
	}

	return changeString
}
