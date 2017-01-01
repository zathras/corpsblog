package com.jovial.templates

import com.jovial.blog.model.BlogConfig
import com.jovial.blog.model.ContactContent
import com.jovial.blog.model.IndexContent
import com.jovial.templates.lib.HTML
import com.jovial.templates.lib.html

/**
 * Created by billf on 11/7/16.
 */
class Contact(val blogConfig : BlogConfig,
            val content: ContactContent)
{
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
                }
                Footer(blogConfig, rootPath).generate(this)
            }
        }
    }
}
