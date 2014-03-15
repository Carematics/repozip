package com.carematics.repozip
import static spark.Spark.*
import groovy.json.JsonOutput
import groovy.util.logging.Log4j
import spark.*
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
    externalStaticFileLocation('src/main/web/')
    addGetRoute('/newjob') {Request request, Response response ->
      response.type 'application/json'
      Job job = new Job(request.queryParams('url'))
      job.start()
      return JsonOutput.toJson([ id: job.id ])
    }
    addGetRoute('/jobstatus/:id'){Request request, Response response ->
      response.type 'application/json'
      def toReturn = []
      def id = request.params('id')
      Job job = Job.byId(id)
      if(job != null) {
        toReturn = job.status()
      } else {
        log.debug "query for expired job $id"
      }
      return JsonOutput.toJson(toReturn)
    }
  }
}
