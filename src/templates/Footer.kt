package templates

import com.jovial.blog.model.Config
import com.jovial.blog.model.Content
import com.jovial.lib.html.BodyTag
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by billf on 11/6/16.
 */
class Footer (val config: Config, val content: Content) {
  private val year = SimpleDateFormat("yyyy").format(Date())
  public fun generate(tag: BodyTag): Unit = tag.include {
    footer(class_ = "footer") {
      span(class_ = "footer__copyright") {
        +"&copy; ${year} ${config.siteAuthor}. "
        a(href = "${config.siteBaseURL}about.html") {
          +"About this site"
        }
      }
    }
    script(type="text/javascript", src="${content.rootPath}js/jquery-1.11.3.min.js") { }
    script(type="text/javascript", src="${content.rootPath}js/main.js") { }
    GoogleAnalytics(config, content).generate(this)
  }
}
