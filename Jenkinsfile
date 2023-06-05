library "atolcd-jenkins"
pipeline {
  options {
    disableConcurrentBuilds()
    buildDiscarder(logRotator(numToKeepStr: '10'))
  }
  agent any
  stages {
    stage('Build Android') {
      environment {
        ANDROID_USER_HOME = "${env.WORKSPACE}/.android"
      }
      steps {
        dockerBuildAndRemove(buildDir: '.jenkins') { imageId ->
          gradleInsideDocker(imageId: imageId, imageName: '') {
            sh """
              gradle --stacktrace clean build
              """
          }
        }
      }
    }
  }
}
