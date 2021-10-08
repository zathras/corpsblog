package com.jovial.templates

import com.jovial.blog.model.BlogConfig
import com.jovial.blog.model.PostContent
import com.jovial.templates.lib.Body
import com.jovial.templates.lib.BodyTag
import com.jovial.templates.lib.Element
import com.jovial.templates.lib.Tag

/**
 * Disqus integration.  Disqus is a system for allowing comments.
 *
 * Created by billf on 11/6/16.
 */
class Disqus (val config: BlogConfig, val rootPath: String) {
  public fun generate(tag: BodyTag, pageID: String): Unit = tag.include {
    val pageIDFixed = pageID.replace('-', '_');
    if (config.shareDisqus != null) {
      div (id="disqus_thread") { }
      div (id="cb_disqus_button") {
        p {
          button(onclick="cb_show_disqus()") {
            +"See Comments"
          }
          +" &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;(comments via"
          a(href="https://en.wikipedia.org/wiki/Disqus") {
            +"Disqus"
          }
          +")"
        }
      }
      script(type="text/javascript") {
        +"""
    var disqus_config = function() {
        this.page.url = '${config.siteBaseURL}posts/${pageID}.html';
        this.page.identifier = '${pageIDFixed}';
    }
    function cb_show_disqus() {
        document.getElementById('cb_disqus_button').innerHTML = '';
        var s = document.createElement('script');
        s.src = 'https://${config.shareDisqus}.disqus.com/embed.js';
        s.setAttribute('data-timestamp', +new Date());
        (document.head || document.body).appendChild(s);
    }
"""
      }
    }
  }
}
