package co.freeside.betamax.compatibility

import co.freeside.betamax.Betamax
import co.freeside.betamax.Recorder
import co.freeside.betamax.httpclient.BetamaxRoutePlanner
import co.freeside.betamax.proxy.jetty.SimpleServer
import co.freeside.betamax.util.server.EchoHandler
import org.apache.http.HttpHost
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.impl.client.SystemDefaultHttpClient
import org.junit.Rule
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Timeout
import static co.freeside.betamax.util.FileUtils.newTempDir
import static java.net.HttpURLConnection.HTTP_OK
import static org.apache.http.HttpHeaders.VIA
import static org.apache.http.conn.params.ConnRoutePNames.DEFAULT_PROXY

class HttpClientSpec extends Specification {

	@AutoCleanup('deleteDir') File tapeRoot = newTempDir('tapes')
	@Rule Recorder recorder = new Recorder(tapeRoot: tapeRoot)
	@Shared @AutoCleanup('stop') SimpleServer endpoint = new SimpleServer()

	void setupSpec() {
		endpoint.start(EchoHandler)
	}

	@Timeout(10)
	@Betamax(tape = 'http client spec')
	void 'proxy intercepts HTTPClient connections when using ProxySelectorRoutePlanner'() {
		given:
		def http = new DefaultHttpClient()
		BetamaxRoutePlanner.configure(http)

		when:
		def request = new HttpGet(endpoint.url)
		def response = http.execute(request)

		then:
		response.statusLine.statusCode == HTTP_OK
		response.getFirstHeader(VIA)?.value == 'Betamax'
	}

	@Timeout(10)
	@Betamax(tape = 'http client spec')
	void 'proxy intercepts HTTPClient connections when explicitly told to'() {
		given:
		def http = new DefaultHttpClient()
		http.params.setParameter(DEFAULT_PROXY, new HttpHost('localhost', recorder.proxyPort, 'http'))

		when:
		def request = new HttpGet(endpoint.url)
		def response = http.execute(request)

		then:
		response.statusLine.statusCode == HTTP_OK
		response.getFirstHeader(VIA)?.value == 'Betamax'
	}

	@Timeout(10)
	@Betamax(tape = 'http client spec')
	void 'proxy automatically intercepts SystemDefaultHttpClient connections'() {
		given:
		def http = new SystemDefaultHttpClient()

		when:
		def request = new HttpGet(endpoint.url)
		def response = http.execute(request)

		then:
		response.statusLine.statusCode == HTTP_OK
		response.getFirstHeader(VIA)?.value == 'Betamax'
	}

}
