package templates

import com.jovial.blog.model.BlogConfig
import com.jovial.blog.model.PostContent
import com.jovial.lib.html.HTML
import com.jovial.lib.html.html

/**
 * Created by billf on 11/7/16.
 */
class Tags(val config : BlogConfig, val titleString: String, val rootPath: String) {
    // @@ TODO
    public fun generate(): HTML = html {
        head {
            CommonHead(config, titleString, rootPath).generate(this)
        }
        body {
            BodyHeader(config, rootPath).generate(this)
        }
    }
}