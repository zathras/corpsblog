package templates

import com.jovial.blog.model.BlogConfig
import com.jovial.blog.model.PostContent
import com.jovial.lib.html.Body
import com.jovial.lib.html.BodyTag
import com.jovial.lib.html.Element
import com.jovial.lib.html.Tag

/**
 * Disqus integration.  Disqus is a system for allowing comments.
 *
 * Created by billf on 11/6/16.
 */
class Disqus (val config: BlogConfig, val rootPath: String) {
  public fun generate(tag: BodyTag): Unit = tag.include {
    if (config.shareDisqus != null) {
      div (id="disqus_thread") { }
      script(type="text/javascript") {
        +"""
    var disqus_shortname = '${config.shareDisqus}';

    /* * * DON'T EDIT BELOW THIS LINE * * */
    (function() {
        var dsq = document.createElement('script'); 
                dsq.type = 'text/javascript';
                dsq.async = true;
        dsq.src = '//' + disqus_shortname + '.disqus.com/embed.js';
        (document.getElementsByTagName('head')[0] || document.getElementsByTagName('body')[0]).appendChild(dsq);
    })();

    /* * * DON'T EDIT BELOW THIS LINE * * */
    (function () {
        var s = document.createElement('script'); s.async = true;
        s.type = 'text/javascript';
        s.src = '//' + disqus_shortname + '.disqus.com/count.js';
        (document.getElementsByTagName('HEAD')[0] || document.getElementsByTagName('BODY')[0]).appendChild(s);
    }());
"""
      }
      noscript {
        +"Please enable JavaScript to view the"
        a(href="http://disqus.com/?ref_noscript") {
          +"comments."
        }
      }
      a(href="http://disqus.com", class_="dsq-brlink") {
        +"&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;See comments"
      }
    }
  }
}

