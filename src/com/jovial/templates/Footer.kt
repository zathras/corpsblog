package com.jovial.templates

import com.jovial.blog.model.BlogConfig
import com.jovial.blog.model.PostContent
import com.jovial.templates.lib.BodyTag
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by billf on 11/6/16.
 */
class Footer (val config: BlogConfig, val rootPath: String) {
  private val year = SimpleDateFormat("yyyy").format(Date())
  public fun generate(tag: BodyTag): Unit = tag.include {
    footer(class_ = "footer") {
      span(class_ = "footer__copyright") {
        +"&copy; ${year} ${config.siteAuthor}. "
        a(href = "${rootPath}index.html") {
          +"About this site"
        }
      }
    }
    script(type="text/javascript", src="${rootPath}js/jquery-1.11.3.min.js") { }
    script(type="text/javascript", src="${rootPath}js/main.js") { }
    GoogleAnalytics(config).generate(this)
  }
}
