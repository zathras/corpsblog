package templates

import com.jovial.blog.model.BlogConfig
import com.jovial.blog.model.PostContent
import com.jovial.lib.html.HTML
import com.jovial.lib.html.html
import com.jovial.util.MMMMyyyyFormat
import com.jovial.util.ddFormat
import java.text.SimpleDateFormat

/**
 * Created by billf on 11/6/16.
 */

class Archive(val config : BlogConfig,
              val posts: List<Post>) {

  public fun generate(): HTML = html {
    val rootPath = ""
    head {
      CommonHead(config, "Archive of ${config.siteDescription}", rootPath).generate(this)
    }
    body {
      BodyHeader(config, rootPath).generate(this)
      div(class_="content-wrapper") {
        div(class_="content-wrapper__inner") {
          h2() { +"Archive" }
	  var i = 0;
	  while (i < posts.size) {
	    val month : String = MMMMyyyyFormat.format(posts[i].content.date)
	    h4 { +month }
	    ul {
	      while (i < posts.size) {
                val post = posts[i]
	        if (month != MMMMyyyyFormat.format(post.content.date)) {
		  break;
		}
                li {
                  +"${ddFormat.format(post.content.date)} - "
                  a(href="${rootPath}posts/${post.outputFile.name}") {
                    +post.content.title
                  }
                  br()
                  em {
                    +post.content.synopsis
                  }
                }
                i++
              }
            }
          }
        }
        Footer(config, rootPath).generate(this)
      }
    }
  }
}
