import org.vertx.groovy.core.buffer.Buffer;
import static java.util.UUID.randomUUID

def server = vertx.createHttpServer().requestHandler { req ->
  if (req.uri.startsWith("/q/")) {
  	req.response.sendFile(req.uri[3..-1])
  } else {
  	req.response.end new File("index.html").getText()
  }
}

server.websocketHandler{ ws ->
    ws.dataHandler ({ data ->
	uuid = randomUUID() as String
	def lns = []
	a = [append : { line -> lns.add(line); return a }] as Appendable
	def proc = "docker run --name ${uuid} -t goodsvn /bin/bash go.sh ${data} ${uuid}";
	println proc
	proc = proc.execute()
	proc.waitForProcessOutput(a,a)
	proc.waitForOrKill(1000 * 60 * 60);
	"docker cp ${uuid}:${uuid}.zip .".execute()
	"docker rm ${uuid}".execute()
	lns.each { it -> ws.write(new Buffer(it))}
	ws.write(new Buffer("Done ${uuid}"));
	ws.dataHandler({ _ -> ws.close();  })
    });
}.listen(9090)
