import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.contentType;

import org.junit.Test;

import play.twirl.api.Content;


public class MabxmlApplicationTest {

	@Test
	public void renderIndexTemplate() {
		Content html = views.html.index.render("Page title");
		assertThat(contentType(html)).isEqualTo("text/html");
		assertThat(contentAsString(html)).contains("Page title");
	}
}
