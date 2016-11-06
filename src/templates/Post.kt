package templates

import com.jovial.blog.model.Config
import com.jovial.blog.model.Content
import com.jovial.lib.html.HTML
import com.jovial.lib.html.html

/**
 * Created by w.foote on 11/3/2016.
 */


class Post(val config : Config, val content: Content) {
  public fun generate() : HTML = html {
    head {
      CommonHead(config, content).generate(this)
    }
    body {
      BodyHeader(config, content).generate(this)
      // @@<#include "header.ftl">
      div("content-wrapper") {
        div("content-wrapper__inner") {
           article("post-container post-container--single") {
            header("post-header") {
              div("post-meta") {
                // @@ <time datetime="${content.date?string("yyyy-MM-dd")}" class="post-meta__date date">${content.date?string("dd MMMM yyyy")}</time> &#8226;

                // @@ <#if content.tags??>
                  // @@ <span class="post-meta__tags">tags
                  // @@ <#list content.tags as tag>
                  // @@ <a href="<#if (content.rootpath)??>${content.rootpath}<#else></#if>tags/${tag}.html">${tag}</a>
                  // @@ </#list>
                  // @@ </span>
                // @@ </#if>
              }
              // @@j  <h1 class="post-title"><#escape x as x?xml>${content.title}</#escape></h1>
            }
            section("post") {
              p(style="margin-right: 50px; margin-left: 30px") {
                em {
                  +content.synopsis
                }
                // @@<em>
                  // @@${content.synopsis}
                // @@</em>
              }
              literal { +content.bodyHTML }
            }
            // <#include "disqus.ftl">
          }
        }
        // @@ <#include "footer.ftl">
      }
    }
  }
}
