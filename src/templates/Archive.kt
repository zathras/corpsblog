package templates

import com.jovial.blog.model.BlogConfig
import com.jovial.blog.model.Content
import com.jovial.lib.html.HTML
import com.jovial.lib.html.html

/**
 * Created by billf on 11/6/16.
 */
class Archive(val config : BlogConfig, val content: Content) {
  // @@ TODO
  public fun generate(): HTML = html {
    head {
      CommonHead(config, content).generate(this)
    }
    body {
      BodyHeader(config, content).generate(this)
    }
  }
}
