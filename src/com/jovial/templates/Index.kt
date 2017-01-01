package com.jovial.templates

import com.jovial.blog.model.BlogConfig
import com.jovial.blog.model.IndexContent
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
        val firstPost = if (posts.size > 0) posts[0] else  null
        val lastPost = if (posts.size > 0) posts[posts.size-1] else null
        val rootPath = ""

        head {
            CommonHead(blogConfig, blogConfig.siteTitle, rootPath).generate(this)
        }
        body {
            BodyHeader(blogConfig, rootPath).generate(this)
            div("content-wrapper") {
                div("content-wrapper__inner") {
                    +content.body
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
                Footer(blogConfig, rootPath).generate(this)
            }
        }
    }
}
