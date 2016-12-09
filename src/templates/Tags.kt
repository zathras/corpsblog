package templates

import com.jovial.blog.model.BlogConfig
import com.jovial.blog.model.PostContent
import com.jovial.lib.html.HTML
import com.jovial.lib.html.html
import com.jovial.util.MMMMyyyyFormat
import com.jovial.util.ddFormat

/**
 * Created by billf on 11/7/16.
 */
class Tags(val blogConfig : BlogConfig,
           val pathTo : String,
           val rootPath : String,
           val tag: String,
           val posts: List<Post>)
{
    public fun generate(): HTML = html {
        head {
            CommonHead(blogConfig, blogConfig.siteTitle, rootPath).generate(this)
        }
        body {
            BodyHeader(blogConfig, rootPath).generate(this)
            div(class_="content-wrapper") {
                div(class_ = "content-wrapper__inner") {
                    h2() { +"Tag:  $tag" }
                    var i = 0;
                    while (i < posts.size) {
                        val month: String = MMMMyyyyFormat.format(posts[i].content.date)
                        h4 { +month }
                        ul {
                            while (i < posts.size) {
                                val post = posts[i]
                                if (month != MMMMyyyyFormat.format(post.content.date)) {
                                    break;
                                }
                                li {
                                    +"${ddFormat.format(post.content.date)} - "
                                    a(href = "${rootPath}posts/${post.outputFile.name}") {
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
                Footer(blogConfig, rootPath).generate(this)
            }
        }
    }
}