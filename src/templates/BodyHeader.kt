package templates

import com.jovial.blog.model.BlogConfig
import com.jovial.blog.model.PostContent
import com.jovial.lib.html.Body

/**
 * Created by w.foote on 11/4/2016.
 */
class BodyHeader (val config: BlogConfig,
                  val rootPath: String,
                  val hasGallery: Boolean = false) {
  public fun generate(body: Body): Unit = body.include {

    //
    // Photoswipe UI -- initially hidden
    //
    if (hasGallery) {
      // Root element of PhotoSwipe. Must have class pswp.
      div(class_="pswp",tabindex="-1",role="dialog",aria_hidden="true") {
        // Background of PhotoSwipe.
        // It's a separate element as animating opacity is faster than rgba().
        div(class_="pswp__bg") { }
        // Slides wrapper with overflow:hidden.
        div(class_="pswp__scroll-wrap") {
          // Container that holds slides.
          // PhotoSwipe keeps only 3 of them in the DOM to save memory.
          // Don't modify these 3 pswp__item elements, data is added later on.
          div(class_="pswp__container") {
            div(class_="pswp__item") { }
            div(class_="pswp__item") { }
            div(class_="pswp__item") { }
          }
          // Default (PhotoSwipeUI_Default) interface on top of sliding area.
          // Can be changed.
          div(class_="pswp__ui pswp__ui--hidden") {
            div(class_="pswp__top-bar") {
              // Controls are self-explanatory. Order can be changed.
              div(class_="pswp__counter") { }
              button(class_="pswp__button pswp__button--close",title="Close (Esc)") { }
              button(class_="pswp__button pswp__button--share",title="Share") { }
              button(class_="pswp__button pswp__button--fs",title="Toggle fullscreen") { }
              button(class_="pswp__button pswp__button--zoom", title="Zoom in/out") { }
              // Preloader demo http://codepen.io/dimsemenov/pen/yyBWoR -->
              // element will get class pswp__preloader--active when preloader is running -->
              div(class_="pswp__preloader") {
                div(class_="pswp__preloader__icn") {
                  div(class_="pswp__preloader__cut") {
                    div(class_="pswp__preloader__donut") { }
                  }
                }
              }
            }
            div(class_="pswp__share-modal pswp__share-modal--hidden pswp__single-tap") {
              div(class_="pswp__share-tooltip") { }
            }

            button(class_="pswp__button pswp__button--arrow--left", title="Previous (arrow left)") { }

            button(class_="pswp__button pswp__button--arrow--right",title="Next (arrow right)") { }

            div(class_="pswp__caption") {
              div(class_="pswp__caption__center") { }
            }
          }
        }
      }
      script(src = "${rootPath}photoswipe/photoswipe.min.js")
      script(src = "${rootPath}photoswipe/photoswipe-ui-default.min.js")
      script(type = "text/javascript") {
        +"var pswpElement = document.querySelectorAll('.pswp')[0];"
        +"function openPhotoSwipe(index, items) {"
        +"  var options = {"
        +"    index: index-1"   // Subtract one from index so user sees 1-based numbers in href hint
        +"  };"
        +"  var gallery = new PhotoSwipe(pswpElement, PhotoSwipeUI_Default, items, options);"
        +"  gallery.init();"
        +"}"
      }
    }   // end if(hasGallery)

    //
    // Uno theme
    //
    span(class_ = "mobile btn-mobile-menu") {
      i(class_ = "icon icon-list btn-mobile-menu__icon") { }
      i(class_ = "icon icon-x-circle btn-mobile-close__icon hidden") { }
    }
    header (class_="panel-cover", style="background-image: url(${rootPath}${config.coverImage}); background-position: center;") {
      div(class_="panel-main") {
        div(class_="panel-main__inner panel-inverted") {
          div(class_="panel-main__content") {
            a(href="${rootPath}index.html", title="link to home of ${config.siteTitle}") {
              img(src="${rootPath}${config.myProfilePhoto}", class_="user-image", alt="My Profile Photo")
              h1(class_="panel-cover__title panel-title") {
                val htmlText = config.siteTitleBodyHTML
                val imgSrc = config.siteTitleBodyImage
                if (htmlText == null && imgSrc == null) {
                  +config.siteTitle
                } else {
                  if (htmlText != null) {
                    +htmlText
                  }
                  if (imgSrc != null) {
                    img(src="${rootPath}$imgSrc", width="90%")
                  }
                }
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
                    a(href="${rootPath}index.html", title="link to ${config.siteBaseURL} blog", class_="blog-button") {
                      +"Main"
                    }
                  }
                  li(class_="navigation__item") {
                    a(href="${rootPath}archive.html",title="link to archive",class_="blog-button") {
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
                    a(href=config.feedURL, title="Subscribe", target="_blank") {
                      i(class_="icon icon-rss") { }
                      span(class_="label") {
                        +"RSS"
                      }
                    }
                  }

                  // contact page
                  li(class_="navigation__item") {
                    a(href="${rootPath}contact.html", title="Contact", style="border: 0px") {
                      img(src="${rootPath}images/xbiff.png", class_="icon-image")
                      span(class_="label") {
                        +"Contact"
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

                  // Mailchimp
                  if (config.mailchimpClient != null) {
                    li(class_="navigation__item") {
                      val url = config.mailchimpClient.signup_url
                      a(href=url, title="Subscribe", target="_blank", class_="blog-button") {
                        +"Subscribe"
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

