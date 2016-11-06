package templates

import com.jovial.blog.model.Config
import com.jovial.blog.model.Content
import com.jovial.lib.html.HTML
import com.jovial.lib.html.Head
import com.jovial.lib.html.Title
import com.jovial.lib.html.head

/**
 * Created by w.foote on 11/3/2016.
 */

class CommonHead(val config: Config, val content: Content) {
  public fun generate(head : Head) : Unit =  head.include {
    meta(charset="utf-8")
    title {
      +content.title
    }
    meta(name="viewport", content="width=device-width, initial-scale=1.0")
    meta(name="description", content=config.siteDescription)
    meta(name="author",  content=config.siteAuthor)
    link(href=config.feedURL, rel="alternate", type="application/rss+xml", title="RSS Feed")
    meta(name="keywords", content="")
    meta(name="generator", content="CorpsBlog")
    link(rel="stylesheet", href="${content.rootPath}css/main.css")
    link(rel="stylesheet", href="${content.rootPath}photoswipe/photoswipe.css")
    link(rel="stylesheet", href="${content.rootPath}photoswipe/default-skin/default-skin.css")

    link(rel="manifest", href="${content.rootPath}images/favicons/manifest.json")
    link(rel="shortcut icon", href="${content.rootPath}images/favicons/favicon.ico")

    script(type="text/javascript") {
	  +"var baseUrl = '${config.siteBaseURL}';"
	  +"var baseUrlIndex = '${config.siteBaseURL}index.html'; "
    }

    //  HTML5 shim, for IE6-8 support of HTML5 elements
    literal {
      +"<!--[if lt IE 9]>"
    }
    script(src="${content.rootPath}/html5shim.min.js")
    literal { +"<![endif]-->" }

    /*
    The following was commented out in jbake-uno.  Maybe it'll be useful someday?

    <!-- Fav and touch icons -->
    <!--<link rel="apple-touch-icon-precomposed" sizes="144x144" href="../assets/ico/apple-touch-icon-144-precomposed.png">
    <link rel="apple-touch-icon-precomposed" sizes="114x114" href="../assets/ico/apple-touch-icon-114-precomposed.png">
    <link rel="apple-touch-icon-precomposed" sizes="72x72" href="../assets/ico/apple-touch-icon-72-precomposed.png">
    <link rel="apple-touch-icon-precomposed" href="../assets/ico/apple-touch-icon-57-precomposed.png">-->
    */
  }
}
