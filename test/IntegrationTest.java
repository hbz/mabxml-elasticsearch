import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.GET;
import static play.test.Helpers.HTMLUNIT;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.contentType;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.inMemoryDatabase;
import static play.test.Helpers.route;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import java.util.concurrent.TimeUnit;

import org.junit.BeforeClass;
import org.junit.Test;

import play.libs.F.Callback;
import play.libs.ws.WS;
import play.libs.ws.WSResponse;
import play.mvc.Http.Status;
import play.mvc.Result;
import play.test.TestBrowser;

@SuppressWarnings("javadoc")
public class IntegrationTest {

	private static final String INDEX = "hbz01";
	private static final String TEST_RESOURCE = "HT017665866";

	@BeforeClass
	public static void transformTestData() {
		running(testServer(3333), () -> {
			WSResponse response = WS.url("http://localhost:3333/hbz01/transform")//
					.setQueryParameter("dir", "test/20140817_20140818.tar.bz2")//
					.setQueryParameter("suffix", "bz2")//
					.setQueryParameter("cluster", "elasticsearch")//
					.setQueryParameter("hostname", "127.0.0.1")//
					.setQueryParameter("index", "hbz01")//
					.post("").get(60, TimeUnit.SECONDS);
			System.out.println(response.getBody());
			assertThat(response.getStatus()).isEqualTo(Status.OK);
		});
	}

	@Test
	public void testMainPage() {
		running(testServer(3333, fakeApplication(inMemoryDatabase())), HTMLUNIT,
				new Callback<TestBrowser>() {
					@Override
					public void invoke(TestBrowser browser) {
						browser.goTo("http://localhost:3333/hbz01");
						assertThat(browser.pageSource()).contains(INDEX + "/public");
						assertThat(browser.pageSource())
								.contains(INDEX + "/" + TEST_RESOURCE);
					}
				});
	}

	@Test
	public void getById() {
		running(fakeApplication(), () -> {
			Result result =
					route(fakeRequest(GET, "/" + INDEX + "/" + TEST_RESOURCE));
			assertThat(result).isNotNull();
			assertThat(contentType(result)).isEqualTo("text/xml");
			assertThat(contentAsString(result)).contains("Sozialwissenschaften");
		});
	}
}
