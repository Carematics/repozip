package com.carematics.repozipimport groovy.util.logging.Log4jimport org.apache.commons.lang3.StringUtils@Log4j
class Job {
  /** keyed by job id */
  private static Map<String, Job> jobs = [:]  static Thread jobsCleanerThread = null
  String url = ''
  String id = ''
  long created = 0
  Ripper ripper = null
  Thread thread = null
  /** will assign a random id and start a {@link Ripper} */
  Job(String url) {
    url = url.trim()
    if(!url.endsWith('/')) {
      url += '/'    }
    this.url = url
    this.created = System.currentTimeMillis()
    this.id = UUID.randomUUID().toString()
    this.ripper = new Ripper(id, url, getProjectName())
    jobs.put(id, this)  }  void start() {    this.thread = Thread.start("Job-$id"){ ripper.start() }    initJobsCleanupAsNeeded()  }
  Map status() {
    return [
      downloadURL: ripper.downloadURL,
      filesFound: ripper.filesFound,
      currentFile: ripper.currentFile,
      timeLeft: ripper.timeLeft,
      percentComplete: ripper.percentComplete,
      status: thread.isAlive() ? ripper.status : 'done'
    ]  }
  String getProjectName() {
    if(url.contains('.googlecode.com/')) {
      return StringUtils.substringBetween(url, '://', '.googlecode.com/svn/')    } else if (url.contains('//svn.code.sf.net/p/')) {
      return StringUtils.substringBetween(url, '//svn.code.sf.net/p/', '/')    } else {
      return id    }  }
  static Job byId(String id) {
    return jobs[id]  }  static initJobsCleanupAsNeeded() {    if(jobsCleanerThread != null) {      return    }    def jobExpiryDelay = 5 * 60 * 1000
    jobsCleanerThread = Thread.start('JobsCleaner') {
      while(true) {
        long lastStarted = System.currentTimeMillis()
        def iter = jobs.values().iterator()
        while(iter.hasNext()) {
          def job = iter.next()
          if(lastStarted - job.created > jobExpiryDelay) {
            log.debug "expired job $job.id"
            iter.remove()          }        }
        def timeToSleep = jobExpiryDelay - (System.currentTimeMillis() - lastStarted)
        if(timeToSleep > 0) {
          sleep(timeToSleep)        }      }    }  }
  static private testGetProjectName() {    println 'testing'
    assert new Job('http://svn.code.sf.net/p/war-sim/').getProjectName() == 'war-sim'
    assert new Job('http://wpkg-gp.googlecode.com/svn/trunk/').getProjectName() == 'wpkg-gp'  }}
