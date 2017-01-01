package com.jovial.templates

import com.jovial.blog.model.BlogConfig
import com.jovial.blog.model.PostContent
import com.jovial.templates.lib.BodyTag

/**
 * Created by billf on 11/6/16.
 */

class GoogleAnalytics(config: BlogConfig) {
  private val account : String? = config.googleAnalyticsAccount

  public fun generate(tag: BodyTag): Unit = tag.include {
    if (account != null) {
      script { +"""
        var _gaq = _gaq || [];
        var pluginUrl = '//www.google-analytics.com/plugins/ga/inpage_linkid.js';
        _gaq.push(['_require', 'inpage_linkid', pluginUrl]);
        _gaq.push(['_setAccount', '${account}']);
        _gaq.push(['_trackPageview']);

        (function() {
        var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
        ga.src = ('https:' == document.location.protocol ? 'https://' : 'http://') + 'stats.g.doubleclick.net/dc.js';
        var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
        })();
"""
      }
    }
  }
}
