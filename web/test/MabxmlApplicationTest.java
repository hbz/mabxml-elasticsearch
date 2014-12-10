import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.GET;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.contentType;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.fakeRequest;
import static play.test.Helpers.route;
import static play.test.Helpers.running;

import org.junit.Test;

import play.mvc.Result;
import play.twirl.api.Content;


public class MabxmlApplicationTest {
	private static final String HBZ01_HT017665866 = "/hbz01/HT017665866";

	@Test
	public void renderIndexTemplate() {
		Content html = views.html.index.render("Page title");
		assertThat(contentType(html)).isEqualTo("text/html");
		assertThat(contentAsString(html)).contains("Page title");
	}

	@Test
	public void getAndContextContentType() {
		running(fakeApplication(), () -> {
			Result result = route(fakeRequest(GET, HBZ01_HT017665866));
			assertThat(result).isNotNull();
			assertThat(contentType(result)).isEqualTo("text/xml");
		});
	}
}
