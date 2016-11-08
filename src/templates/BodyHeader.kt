package templates

import com.jovial.blog.model.BlogConfig
import com.jovial.blog.model.Content
import com.jovial.lib.html.Body

/**
 * Created by w.foote on 11/4/2016.
 */
class BodyHeader (val config: BlogConfig, val content: Content) {
  public fun generate(body: Body): Unit = body.include {
    span(class_ = "mobile btn-mobile-menu") {
      i(class_ = "icon icon-list btn-mobile-menu__icon") { }
      i(class_ = "icon icon-x-circle btn-mobile-close__icon hidden") { }
    }
    header (class_="panel-cover", style="background-image: url(${config.siteBaseURL}images/cover.jpg)") {
      div(class_="panel-main") {
        div(class_="panel-main__inner panel-inverted") {
          div(class_="panel-main__content") {
            a(href="${config.siteBaseURL}", title="link to home of ${config.siteTitle}") {
              img(src="${config.siteBaseURL}images/profile.jpg", class_="user-image", alt="My Profile Photo")
              h1(class_="panel-cover__title panel-title") {
                +config.siteTitle
              }
            }
            hr(class_="panel-cover__divider")
            p(class_="panel-cover__description") {
              +config.siteDescription
            }
            hr(class_="panel-cover__divider panel-cover__divider--secondary")
            div(class_="navigation-wrapper") {
              nav(class_="cover-navigation cover-navigation--primary") {
                ul(class_="navigation") {
                  li(class_="navigation__item") {
                    a(href="${config.siteBaseURL}#blog", title="link to ${config.siteBaseURL} blog", class_="blog-button") {
                      +"Blog"
                    }
                  }
                  li(class_="navigation__item") {
                    a(href="${config.siteBaseURL}archive.html",title="link to ${config.siteBaseURL} archive",class_="blog-button") {
                      +"Archive"
                    }
                  }
                }
              }
              nav(class_="cover-navigation navigation--social") {
                ul(class_="navigation") {

                  // twitter
                  if (config.shareTwitter != null) {
                    li(class_="navigation__item") {
                      a(href="http://twitter.com/${config.shareTwitter}", title="${config.shareTwitter} on Twitter", target="_blank") {
                        i(class_="icon icon-social-twitter") {}
                        span(class_="label") {
                          +"Twitter"
                        }
                      }
                    }
                  }

                  // LinkedIn
                  if (config.shareLinkedIn != null) {
                    li(class_="navigation__item") {
                      a(href="http://www.linkedin.com/${config.shareLinkedIn}", title="${config.shareLinkedIn} on LinkedIn", target="_blank") {
                        i(class_="icon icon-social-linkedin") {}
                        span(class_="label") {
                          +"LinkedIn"
                        }
                      }
                    }
                  }

                  // GitHub
                  if (config.shareGitHub != null) {
                    li(class_="navigation__item") {
                      a(href="http://github.com/${config.shareGitHub}", title="${config.shareGitHub} on GitHub", target="_blank") {
                        i(class_="icon icon-social-github") {}
                        span(class_="label") {
                          +"GitHub"
                        }
                      }
                    }
                  }

                  // Flickr
                  if (config.shareFlickr != null) {
                    li(class_="navigation__item") {
                      a(href="http://flickr.com/${config.shareFlickr}", title="${config.shareFlickr} on Flickr", target="_blank") {
                        i(class_="icon icon-social-flickr") {}
                        span(class_="label") {
                          +"Flickr"
                        }
                      }
                    }
                  }

                  // Garmin
                  if (config.shareGarmin != null) {
                    li(class_="navigation__item") {
                      a(href="http://connect.garmin.com/${config.shareGarmin}", title="${config.shareGarmin} on Garmin", target="_blank") {
                        i(class_="icon icon-torso") {}
                        span(class_="label") {
                          +"Garmin"
                        }
                      }
                    }
                  }

                  // RSS
                  li(class_="navigation__item") {
                    a(href="${content.rootPath}", title="Subscribe", target="_blank") {
                      i(class_="icon icon-rss") { }
                      span(class_="label") {
                        +"RSS"
                      }
                    }
                  }

                  // email
                  if (config.shareEmail != null) {
                    li(class_="navigation__item") {
                      a(href="mailto:${config.shareEmail}", title="E-mail Address", target="_blank") {
                        i(class_="icon icon-mail") {}
                        span(class_="label") {
                          +"E-mail"
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
        div(class_="panel-cover--overlay") { }
      }
    }
  }
}

