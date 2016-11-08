package templates

import com.jovial.blog.model.BlogConfig
import com.jovial.blog.model.Content
import com.jovial.lib.html.HTML
import com.jovial.lib.html.html
import java.text.SimpleDateFormat

/**
 * Created by w.foote on 11/3/2016.
 */


class Post(val config : BlogConfig, val content: Content) {
  private val ddMMMMyyyyDateFormat = SimpleDateFormat("dd MMMM yyyy")
  public fun generate() : HTML = html {
    head {
      CommonHead(config, content).generate(this)
    }
    body {
      BodyHeader(config, content).generate(this)
      div("content-wrapper") {
        div("content-wrapper__inner") {
           article("post-container post-container--single") {
            header("post-header") {
              div("post-meta") {
                time(datetime = content.date, class_ = "post-meta__date date") {
                  +ddMMMMyyyyDateFormat.format(content.date)
                  +"&#8226;"
                }
                if (content.tags.size > 0) {
                  span(class_ = "post-meta__tags") {
                    +"tags"
                    for (t in content.tags) {
                      a(href = "${content.rootPath}tags/$t.html") { +t }
                    }
                  }
                }
              }
              h1(class_="post-title") {
                content.title
              }
            }
            section("post") {
              p(style="margin-right: 50px; margin-left: 30px") {
                em {
                  +content.synopsis
                }
              }
              +content.body
            }
            Disqus(config, content).generate(this)
          }
        }
        Footer(config, content).generate(this)
      }
    }
  }
}
