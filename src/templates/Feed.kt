package templates

import com.jovial.blog.model.BlogConfig
import java.util.*
import java.text.SimpleDateFormat

/**
 * Created by billf on 11/6/16.
 */

private val dateFormat = SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z")

class Feed(val config: BlogConfig,
           val posts: List<Post>) 
{
    public fun generate() : String {
        // This is XML, not HTML.  It's pretty simple, so I didn't bother
        // to make a builder.

        val sb = StringBuffer();
        sb.append(
"""<?xml version="1.0"?>
<rss version="2.0" xmlns:atom="http://www.w3.org/2005/Atom">
  <channel>
    <title>${config.siteTitle}</title>
    <link>${config.siteBaseURL}</link>
    <atom:link href="${config.siteBaseURL}/feed.xml" rel="self" type="application/rss+xml" />
    <description>${config.siteDescription}</description>
    <language>en-us</language>
    <pubDate>${dateFormat.format(Date())}</pubDate>
    <lastBuildDate>${dateFormat.format(Date())}</lastBuildDate>""")

        for (p in posts) {
            sb.append("""
    <item>
      <title>${p.title}</title>
      <link>${config.siteBaseURL}/${p.pathTo}/${p.outputFile.name}</link>
      <pubDate>${dateFormat.format(p.date)}</pubDate>
      <guid isPermaLink="true">${p.pathTo}/${p.outputFile.name}</guid>
        <description>
          ${p.synopsis}
        </description>
    </item>""")
        }       // End of the iteration over posts.
        sb.append("""
  </channel> 
</rss>
""")

        return sb.toString()
    }
}
