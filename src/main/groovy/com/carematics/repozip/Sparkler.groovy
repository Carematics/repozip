package com.carematics.repozip
import static spark.Spark.*
import groovy.json.JsonOutput
import groovy.util.logging.Log4j
import spark.*
import twitter4j.*
@Log4j
class Sparkler {
  static void addGetRoute(route, closure) {
    get(new Route(route) {
          public Object handle(Request request, Response response) {
            try {
              return closure(request, response)
            } catch (e) {
              Sparkler.log.error "$route error", e
            }
          }
        })
  }
  static void main(args) {
    log.debug 'main()'
    def jobs = [:]
    externalStaticFileLocation('src/main/web/')
    addGetRoute('/newjob') {Request request, Response response ->
      response.type 'application/json'
      String url = request.queryParams('url').trim()
      if(!url.endsWith('/')) {
        url += '/'
      }
      String id = UUID.randomUUID().toString()
      Ripper ripper = new Ripper(id, url)
      def job = [
        url: url,
        ripper: ripper,
        thread: Thread.start {
          ripper.start()
        }
      ]
      jobs[id] = job;
      return JsonOutput.toJson([
        id: id
      ])
    }
    addGetRoute('/jobstatus/:id'){Request request, Response response ->
      response.type 'application/json'
      def id = request.params('id')
      def job = jobs[id];
      Ripper ripper = ((Ripper)job.ripper)
      return JsonOutput.toJson([
        downloadURL: ripper.downloadURL,
        filesFound: ripper.filesFound,
        currentFile: ripper.currentFile,
        timeLeft: ripper.timeLeft,
        percentComplete: ripper.percentComplete,
        status: job.thread.isAlive() ? ripper.status : 'done'
      ])
    }
  }
}
