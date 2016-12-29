package templates

import com.jovial.blog.model.BlogConfig
import com.jovial.blog.model.PostContent
import com.jovial.lib.html.HTML
import com.jovial.lib.html.html
import com.jovial.util.ddMMMMyyyyDateFormat
import java.io.File
import java.text.SimpleDateFormat

/**
 * Created by w.foote on 11/3/2016.
 */


class Post(val blogConfig: BlogConfig, val content: PostContent, val pathTo: String, val outputFile: File) {

  val title = content.title
  val date = content.date
  val synopsis = content.synopsis

  public fun generate(olderPost: String?, newerPost: String?) : HTML = html {
    head {
      CommonHead(blogConfig, content.title, content.rootPath, content.hasGallery).generate(this)
    }
    body {
      BodyHeader(blogConfig, content.rootPath, content.hasGallery).generate(this)
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
                    +"tags:  "
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
            Disqus(blogConfig, content.rootPath).generate(this)
            if (newerPost != null || olderPost != null) {
              div(style = "clear: both") { }
              +"&nbsp;"
              div(style = "clear: both") { }
              nav(class_ = "arrow-nav") {
                if (newerPost != null) {
                  div(class_ = "newer") {
                    a(href = newerPost) {
                      +"← NEWER"
                    }
                  }
                }
                if (olderPost != null) {
                  div(class_ = "older") {
                    a(href = olderPost) {
                      +"OLDER →"
                    }
                  }
                }
              }
            }
          }
        }
        Footer(blogConfig, content.rootPath).generate(this)
      }
    }
  }
}
