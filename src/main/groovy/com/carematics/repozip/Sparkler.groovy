package com.carematics.repozip
import static spark.Spark.*
import groovy.json.JsonBuilder
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
    externalStaticFileLocation('src/main/web/')
    addGetRoute('/newjob/') {Request request, Response response ->
      response.type 'text/html'
      def html = 'test ' + request.params('id')
      return html
    }
  }
}
