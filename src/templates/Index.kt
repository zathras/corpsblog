package templates

import com.jovial.blog.model.BlogConfig
import com.jovial.blog.model.IndexContent
import com.jovial.lib.html.HTML
import com.jovial.lib.html.html

/**
 * Created by billf on 11/7/16.
 */
class Index(val blogConfig : BlogConfig, val content: IndexContent) {
    // @@ TODO

    public fun generate(): HTML = html {
        val rootPath = ""
        head {
            CommonHead(blogConfig, blogConfig.siteTitle, rootPath).generate(this)
        }
        body {
            BodyHeader(blogConfig, rootPath).generate(this)
            div("content-wrapper") {
                div("content-wrapper__inner") {
                    +content.body
                    // @@ Add the arrows here.
                }
                Footer(blogConfig, rootPath).generate(this)
            }
        }
    }
}