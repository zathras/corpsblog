package com.jovial.templates

import com.jovial.blog.model.BlogConfig
import com.jovial.blog.model.IndexContent
import com.jovial.templates.lib.BodyTag
import com.jovial.templates.lib.Div
import com.jovial.templates.lib.HTML
import com.jovial.templates.lib.html

/**
 * Created by billf on 11/7/16.
 */
class Index(val blogConfig : BlogConfig,
            val content: IndexContent,
            val posts: List<Post>)
{
    public fun generate(): HTML = html {
        val rootPath = ""

        head {
            CommonHead(blogConfig, blogConfig.siteTitle, rootPath).generate(this)
            //
            // Facebook metadata:
            //
            meta(property="og:url", content=blogConfig.siteBaseURL + "/index.html")
            if (blogConfig.siteTitle != "") {
                meta(property="og:title", content=blogConfig.siteTitle)
            }
            if (blogConfig.siteDescription != "") {
                meta(property="og:description", content=blogConfig.siteDescription)
            }
            meta(property="og:type",content="blog")
            val st = blogConfig.defaultPostThumbnail
            if (st != null) {
                meta(property="og:image", content=blogConfig.siteBaseURL + st)
            }
        }
        body {
            BodyHeader(blogConfig, rootPath).generate(this)
            div("content-wrapper") {
                div("content-wrapper__inner") {
                    if (posts.size > 0) {
                        includePostLinks(this)
                        hr(class_="hr-grey")
                    }
                    +content.body
                    if (posts.size > 0) {
                        hr(class_="hr-grey")
                        includePostLinks(this)
                    }
                }
                Footer(blogConfig, rootPath).generate(this)
            }
        }
    }
    private fun includePostLinks(container: BodyTag) = container.include {
        val firstPost = if (posts.size > 0) posts[0] else  null
        val lastPost = if (posts.size > 0) posts[posts.size-1] else null
        if (firstPost != null && lastPost != null) {
            div(class_ = "newer") {
                a(href = firstPost.pathTo + "/" + firstPost.outputFile.name) {
                    +"← FIRST POST"
                }
                br()
                +"&nbsp;"
                br()
                i {
                    +firstPost.title
                }
                br()
                +"&nbsp;"
                br()
                p {
                    em {
                        +firstPost.synopsis
                    }
                }
            }
            div(class_ = "older") {
                a(href = lastPost.pathTo + "/" + lastPost.outputFile.name) {
                    +"LATEST POST →"
                }
                br()
                +"&nbsp;"
                br()
                i {
                    +lastPost.title
                }
                br()
                +"&nbsp;"
                br()
                p {
                    em {
                        +lastPost.synopsis
                    }
                }
            }
            div(style="clear:both") { }
        }
    }
}
